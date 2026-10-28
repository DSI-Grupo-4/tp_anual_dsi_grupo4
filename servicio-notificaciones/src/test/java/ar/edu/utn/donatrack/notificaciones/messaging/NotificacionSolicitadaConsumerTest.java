package ar.edu.utn.donatrack.notificaciones.messaging;

import ar.edu.utn.donatrack.notificaciones.dto.NotificacionRequestDTO;
import ar.edu.utn.donatrack.notificaciones.enums.MedioComunicacion;
import ar.edu.utn.donatrack.notificaciones.service.NotificacionService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class NotificacionSolicitadaConsumerTest {

    @Test
    void delegaLaSolicitudConsumidaAlServicio() {
        NotificacionService service = mock(NotificacionService.class);
        NotificacionSolicitadaConsumer consumer = new NotificacionSolicitadaConsumer(service);
        NotificacionRequestDTO solicitud = new NotificacionRequestDTO(
                "Completaste una mision",
                MedioComunicacion.EMAIL,
                "donante@correo.com",
                "incentivos",
                "MISION_COMPLETADA",
                "evt-1");

        consumer.consumir(solicitud);

        verify(service).enviarNotificacion(solicitud);
    }
}
