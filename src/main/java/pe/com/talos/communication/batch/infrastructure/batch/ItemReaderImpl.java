package pe.com.talos.communication.batch.infrastructure.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import pe.com.talos.communication.batch.infrastructure.persistence.entity.Comunicacion;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class ItemReaderImpl {
    @Value("${jobParameters.chunkSize:200}")
    private final Integer chunkSize;
    private final DataSource dataSource;

    @Bean
    public JdbcPagingItemReader<Comunicacion> pagingItemReader() {
        var reader = new JdbcPagingItemReader<Comunicacion>();

        reader.setDataSource(dataSource);
        reader.setFetchSize(chunkSize);
        reader.setRowMapper(new BeanPropertyRowMapper<>(Comunicacion.class));

        var queryProvider = new SqlPagingQueryProviderFactoryBean();
        queryProvider.setDataSource(dataSource);
        queryProvider.setSelectClause("SELECT idcomunicacion, cliente, tipocomunicacion, comentario, procesado");
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
