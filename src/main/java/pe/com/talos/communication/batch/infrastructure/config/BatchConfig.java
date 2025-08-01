package pe.com.talos.communication.batch.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import pe.com.talos.communication.batch.domain.Communication;
import pe.com.talos.communication.batch.infrastructure.persistence.entity.Comunicacion;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class BatchConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ItemReader<Comunicacion> itemReader;
    private final ItemProcessor<Comunicacion, Communication> itemProcessor;
    private final ItemWriter<Communication> itemWriter;
    private static final String JOB_NAME = "migrationJob";
    private static final String STEP_NAME = "migrationStep";

    //    @Bean
//    fun communicationMigrationJob(): Job {
//        return JobBuilder("communicationMigrationJob", jobRepository)
//                .start(migrationStep())
//                .build()
//    }
//
//    @Bean
//    fun migrationStep(): Step {
//        return StepBuilder("migrationStep", jobRepository)
//                .chunk<Comunicacion, Map<String, Any>>(200, transactionManager)
//            .reader(reader)
//                .processor(processor)
//                .writer(writer)
//                .build()
//    }
    @Bean
    public Job migrationJob() {
        var jobBuilder = new JobBuilder(JOB_NAME, jobRepository);
        return jobBuilder.start(migrationStep())
                .build();
    }

    @Bean
    public Step migrationStep() {
        return new StepBuilder(STEP_NAME, jobRepository)
                .<Comunicacion, Communication>chunk(200, transactionManager)
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .build();
    }

}
