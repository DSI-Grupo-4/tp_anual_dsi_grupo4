package ar.edu.utn.frba.dds.donaciones.integracion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapter no-op de PublicadorEventosPort: solo loguea. Ya no es un
 * @Component — RF-3 está implementado (ver RabbitPublicadorEventos), que es
 * el bean real registrado. Esta clase queda disponible para instanciar a
 * mano en tests o smoke tests locales sin RabbitMQ levantado, no se registra
 * en el contexto de Spring para evitar un conflicto de bean con
 * RabbitPublicadorEventos.
 */
public class NoOpPublicadorEventos implements PublicadorEventosPort {

    private static final Logger logger = LoggerFactory.getLogger(NoOpPublicadorEventos.class);

    @Override
    public void publicar(String tipoEvento, Object payload) {
        logger.info("[NoOp] Se simuló la publicación del evento '{}' hacia Notificaciones "
                + "(cola RabbitMQ aún no implementada, ver RF-3): {}", tipoEvento, payload);
    }
}
