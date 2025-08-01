package pe.com.talos.communication.batch.domain;

public record Communication(
    Long originRefId,
    Long partnerRefId,
    String stageName,
    String name
) { }