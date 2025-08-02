package pe.com.talos.communication.batch.infrastructure.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.com.talos.communication.batch.exceptions.InternalException;
import pe.com.talos.communication.batch.exceptions.OdooException;
import pe.com.talos.communication.batch.infrastructure.client.dto.JsonRpcRequest;
import pe.com.talos.communication.batch.infrastructure.client.dto.JsonRpcResponse;
import pe.com.talos.communication.batch.infrastructure.client.dto.OdooParams;
import pe.com.talos.communication.batch.infrastructure.config.OdooConfig;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
@RequiredArgsConstructor
public class OdooClient {
    private final static String RPC_VERSION = "2.0";
    private final static String USER_AGENT = "Odoo Java Native Client 1.0";
    private final static String CONTENT_TYPE = "application/json";
    private final static int TIMEOUT_SECONDS = 30;
    private final static int STATUS_OK = 200;
    private final AtomicInteger requestId = new AtomicInteger(1);
    private final OdooConfig config;
    private ObjectMapper mapper;
    private HttpClient httpClient;
    private Integer uid;
    private URI endpoint;

    @PostConstruct
    public void init() {
        this.endpoint = URI.create(String.format("%s/jsonrpc", config.getUrl()));
        this.mapper = new ObjectMapper();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    public <P, R> R call(String method, P params, Class<R> resultClass) {
        try {
            int id = requestId.getAndIncrement();
            JsonRpcRequest<P> request = new JsonRpcRequest<>(RPC_VERSION, method, params, id);

            String json;

            json = mapper.writeValueAsString(request);

            HttpRequest httpRequest = HttpRequest.newBuilder(endpoint)
                    .header("Content-Type", CONTENT_TYPE)
                    .header("User-Agent", USER_AGENT)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != STATUS_OK) {
                throw new OdooException("Error HTTP: " + response.statusCode() + " " + response.body());
            }

            JavaType resultType = mapper.getTypeFactory().constructParametricType(JsonRpcResponse.class, resultClass);
            JsonRpcResponse<R> rpcResponse = mapper.readValue(response.body(), resultType);

            if (rpcResponse.error() != null) {
                if (rpcResponse.error().data() != null) {
                    log.error("Error RPC: {} - Data: {}", rpcResponse.error().message(), rpcResponse.error().data().message());
                } else {
                    log.error("Error RPC: {}", rpcResponse.error().message());
                }
                throw new OdooException("Odoo RPC error: " + rpcResponse.error().message());
            }

            return rpcResponse.result();
        } catch (JsonProcessingException e) {
            throw new InternalException("Error al procesar json", e);
        } catch (IOException e) {
            throw new InternalException("Error al leer json", e);
        } catch (InterruptedException e) {
            throw new InternalException("Se interrumpioo la conexion", e);
        }
    }

    public boolean authenticate() {
        var params = new OdooParams(
                "common",
                "authenticate",
                List.of(
                        config.getDatabase(),
                        config.getUsername(),
                        config.getPassword()
                )
        );

        var response = this.call("call", params, Integer.class);

        if (response == null || response <= 0) {
            log.error("Respuesta insatisfactoria del servidor Odoo, response={}", response);
            return false;
        }

        this.uid = response;
        return true;
    }

//    private JsonRpcResponse executeJsonRpc(String endpoint, Object params) throws IOException, InterruptedException {
//        var request = JsonRpcRequest.create("call", params, requestId.getAndIncrement());
//        var json = mapper.writeValueAsString(request);
//
//        log.debug("Enviando request: {}", json);
//
//        var url = String.format("%s%s", config.getUrl(), endpoint);
//
//        var httpRequest = HttpRequest.newBuilder()
//                .uri(URI.create(url))
//                .header("Content-Type", "application/json")
//                .header("User-Agent", "Odoo Java Native Client 1.0")
//                .timeout(Duration.ofSeconds(60))
//                .POST(HttpRequest.BodyPublishers.ofString(json))
//                .build();
//
//        var response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
//
//        if (response.statusCode() != 200) {
//            throw new OdooException("Error HTTP: " + response.statusCode() + " " + response.body());
//        }
//
//        var responseBody = response.body();
//        if (responseBody == null || responseBody.isEmpty()) {
//            throw new OdooException("Respuesta vacía del servidor");
//        }
//
//        log.debug("Respuesta recibida: {}", responseBody);
//
//        var jsonRpcResponse = mapper.readValue(responseBody, JsonRpcResponse.class);
//
//        if (jsonRpcResponse.error() != null) {
//            log.error("Error en respuesta JSON-RPC: {}", jsonRpcResponse.error());
//        }
//
//        return jsonRpcResponse;
//    }

    public <R> R executeMethod(String model, String method, List<Object> args, Map<String, Object> kwargs, Class<R> resultCass) {
        checkAuthentication();

        try {
            var params = new OdooParams(
                    "object",
                    "execute_kw",
                    List.of(config.getDatabase(), uid, config.getPassword(), model, method, args, kwargs)
            );

            return call("call", params, resultCass);

        } catch (Exception e) {
            log.error("Error ejecutando método {}", method, e);
            throw new OdooException("Error con executeMethod", e);
        }
    }

    private void checkAuthentication() {
        if (uid == null || uid <= 0) {
            throw new OdooException("No autenticado. Llama a authenticate() primero.");
        }
    }
}
