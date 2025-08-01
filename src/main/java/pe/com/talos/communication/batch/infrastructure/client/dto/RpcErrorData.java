package pe.com.talos.communication.batch.infrastructure.client.dto;

public record RpcErrorData(
        String name,
        String message,
        String debug
) {
}
