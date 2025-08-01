package pe.com.talos.communication.batch.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record JsonRpcRequest<T>(
        @JsonProperty("jsonrpc") String jsonrpc,
        @JsonProperty("method") String method,
        @JsonProperty("params") T params,
        @JsonProperty("id") int id
) {
}
