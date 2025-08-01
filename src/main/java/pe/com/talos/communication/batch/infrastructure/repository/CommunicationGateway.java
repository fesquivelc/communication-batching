package pe.com.talos.communication.batch.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.com.talos.communication.batch.domain.Communication;
import pe.com.talos.communication.batch.domain.ports.out.CommunicationPort;
import pe.com.talos.communication.batch.infrastructure.client.OdooClient;

@Component
@RequiredArgsConstructor
public class CommunicationGateway implements CommunicationPort {
    private final OdooClient odooClient;
    private final String model = "project.project";
    private final String methodName = "upload_from_batch";
    @Override
    public boolean save(Communication communication) {
        return false;
    }
}
