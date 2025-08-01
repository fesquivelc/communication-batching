package pe.com.talos.communication.batch.infrastructure.client.dto;

import java.util.List;

public record OdooParams(
        String service,
        String method,
        List<Object> args
) {
}
