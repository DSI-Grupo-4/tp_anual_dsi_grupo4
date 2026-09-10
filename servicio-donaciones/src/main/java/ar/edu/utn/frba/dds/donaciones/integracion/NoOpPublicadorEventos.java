package ar.edu.utn.frba.dds.donaciones.integracion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Adapter no-op de PublicadorEventosPort: solo loguea. Permite desplegar y
 * probar Donaciones sin depender de que RabbitMQ ni el Servicio de
 * Notificaciones estén levantados (Etapa 4). Se reemplaza por un adapter
 * real de RabbitMQ cuando se implemente RF-3.
 */
@Component
public class NoOpPublicadorEventos implements PublicadorEventosPort {

    private static final Logger logger = LoggerFactory.getLogger(NoOpPublicadorEventos.class);

    @Override
    public void publicar(String tipoEvento, Object payload) {
        logger.info("[NoOp] Se simuló la publicación del evento '{}' hacia Notificaciones "
                + "(cola RabbitMQ aún no implementada, ver RF-3): {}", tipoEvento, payload);
    }
}
