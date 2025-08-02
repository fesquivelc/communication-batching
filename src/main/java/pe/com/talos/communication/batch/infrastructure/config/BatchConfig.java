package pe.com.talos.communication.batch.infrastructure.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.transaction.PlatformTransactionManager;
import pe.com.talos.communication.batch.domain.Communication;
import pe.com.talos.communication.batch.infrastructure.persistence.entity.Comunicacion;

import javax.sql.DataSource;

@Slf4j
@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class BatchConfig {
    @Value("${jobParameters.chunkSize:500}")
    private Integer chunkSize;
    private final DataSource dataSource;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ItemProcessor<Comunicacion, Communication> itemProcessor;
    private final ItemWriter<Communication> itemWriter;
    private static final String JOB_NAME = "migrationJob";
    private static final String STEP_NAME = "migrationStep";


    @Bean
    public Job migrationJob() {
        var jobBuilder = new JobBuilder(JOB_NAME, jobRepository);
        return jobBuilder.start(migrationStep())
                .build();
    }

    @Bean
    public Step migrationStep() {
        return new StepBuilder(STEP_NAME, jobRepository)
                .<Comunicacion, Communication>chunk(chunkSize, transactionManager)
                .reader(pagingItemReader())
                .processor(itemProcessor)
                .writer(itemWriter)
                .listener(new StepExecutionListener() {
                    @Override
                    public void beforeStep(StepExecution stepExecution) {
                        log.info("🎬 === INICIANDO STEP: {} ===", stepExecution.getStepName());
                    }

                    @Override
                    public ExitStatus afterStep(StepExecution stepExecution) {
                        log.info("🏁 === STEP COMPLETADO: {} ===", stepExecution.getStepName());
                        log.info("📊 Métricas del Step:");
                        log.info("  📖 Leídos: {}", stepExecution.getReadCount());
                        log.info("  ⚙️ Procesados: {}", stepExecution.getWriteCount());
                        log.info("  ⏭️ Saltados: {}", stepExecution.getSkipCount());
                        log.info("  ❌ Errores de lectura: {}", stepExecution.getReadSkipCount());
                        log.info("  ❌ Errores de procesamiento: {}", stepExecution.getProcessSkipCount());
                        log.info("  ❌ Errores de escritura: {}", stepExecution.getWriteSkipCount());
                        return stepExecution.getExitStatus();
                    }
                })
                .build();
    }

    @Bean
    public JdbcPagingItemReader<Comunicacion> pagingItemReader() {
        var reader = new JdbcPagingItemReader<Comunicacion>();

        reader.setDataSource(dataSource);
        reader.setFetchSize(chunkSize);
        reader.setRowMapper(new BeanPropertyRowMapper<>(Comunicacion.class));

        var queryProvider = new SqlPagingQueryProviderFactoryBean();
        queryProvider.setDataSource(dataSource);
        queryProvider.setSelectClause("SELECT idcomunicacion, cliente, tipocomunicacion, comentario");
        queryProvider.setFromClause("FROM comunicaciones");
        queryProvider.setWhereClause("WHERE procesado = 0");
        queryProvider.setSortKey("idcomunicacion");

        try {
            reader.setQueryProvider(queryProvider.getObject());
            return reader;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
