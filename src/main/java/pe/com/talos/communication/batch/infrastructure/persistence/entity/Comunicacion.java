package pe.com.talos.communication.batch.infrastructure.persistence.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Comunicacion {
    private Long idcomunicacion;
    private Long cliente;
    private String tipocomunicacion;
    private String comentario;
}
