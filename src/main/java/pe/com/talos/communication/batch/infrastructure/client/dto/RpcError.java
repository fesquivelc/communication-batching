package pe.com.talos.communication.batch.infrastructure.client.dto;

public record RpcError(
        int code,
        String message,
        RpcErrorData data
) {
}
