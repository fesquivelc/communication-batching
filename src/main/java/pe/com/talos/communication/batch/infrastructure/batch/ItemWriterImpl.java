package pe.com.talos.communication.batch.infrastructure.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.*;
import org.springframework.stereotype.Component;
import pe.com.talos.communication.batch.domain.Communication;

import java.util.Objects;

@Component
@Slf4j
public class ItemWriterImpl implements ItemWriter<Communication> {

    @Override
    public void write(Chunk<? extends Communication> chunk) throws Exception {
        chunk.getItems()
                .stream()
                .filter(Objects::nonNull)
                .forEach(this::uploadToOdoo);

    }

    private void uploadToOdoo(Communication communication) {
        // Aquí se implementaría la lógica para subir la comunicación a Odoo
        // Por ejemplo, utilizando un cliente HTTP o un cliente específico de Odoo
        // que se haya configurado en el contexto de la aplicación.
        log.info("Subiendo comunicación a Odoo: {}", communication);
    }
}
