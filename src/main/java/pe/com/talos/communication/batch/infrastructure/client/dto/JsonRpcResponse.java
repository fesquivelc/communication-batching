package pe.com.talos.communication.batch.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record JsonRpcResponse<T>(
        @JsonProperty("jsonrpc") String jsonrpc,
        @JsonProperty("result") T result,
        @JsonProperty("error") RpcError error,
        @JsonProperty("id") int id
) {}
