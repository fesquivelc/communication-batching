package pe.com.talos.communication.batch.infrastructure.batch;

import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import pe.com.talos.communication.batch.domain.Communication;
import pe.com.talos.communication.batch.infrastructure.persistence.entity.Comunicacion;

import java.util.Optional;

@Slf4j
@Component
public class ItemProcessorImpl implements ItemProcessor<Comunicacion, Communication> {
    @Override
    public Communication process(@Nullable Comunicacion item) throws Exception {
        log.info("Procesando item: {}", item);
        return Optional.ofNullable(item)
                .map(it -> new Communication(
                        it.getIdcomunicacion(),
                        it.getCliente(),
                        it.getTipocomunicacion(),
                        it.getComentario()
                )).orElse(null);
    }
}
