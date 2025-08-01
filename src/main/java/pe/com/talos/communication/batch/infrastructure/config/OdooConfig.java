package pe.com.talos.communication.batch.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "odoo")
@Data
public class OdooConfig {
    private String url;
    private String username;
    private String password;
    private String database;
}
