package pe.com.talos.communication.batch.domain.ports.out;

import pe.com.talos.communication.batch.domain.Communication;

public interface CommunicationPort {
    boolean save(Communication communication);
}
