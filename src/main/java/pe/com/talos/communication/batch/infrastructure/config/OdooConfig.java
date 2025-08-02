package pe.com.talos.communication.batch.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "odoo")
@Data
public class OdooConfig {
    private String url;
    private String username;
    private String password;
    private String database;
}
