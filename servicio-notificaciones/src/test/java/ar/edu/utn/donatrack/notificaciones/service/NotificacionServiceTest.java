package ar.edu.utn.donatrack.notificaciones.service;
import ar.edu.utn.donatrack.notificaciones.strategy.*;
import ar.edu.utn.donatrack.notificaciones.dto.NotificacionRequestDTO;
import ar.edu.utn.donatrack.notificaciones.enums.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
class NotificacionServiceTest {
    @TempDir Path dir;
    @Test void deduplicaTrasReiniciarYCompletaLosTresMedios() {
        var email=spy(new EmailNotificador()); var sms=spy(new SmsNotificador()); var wa=spy(new WhatsAppNotificador());
        var factory=new NotificadorFactory(email,sms,wa);
        var servicio=new NotificacionService(factory,new ObjectMapper(),dir.toString());
        var solicitud=new NotificacionRequestDTO("Prueba",MedioComunicacion.EMAIL,"ana@example.org","donaciones","BIENVENIDA","evento-1");
        var original=servicio.enviarNotificacion(solicitud);
        var reiniciado=new NotificacionService(factory,new ObjectMapper(),dir.toString());
        assertThat(reiniciado.enviarNotificacion(solicitud).getId()).isEqualTo(original.getId());
        verify(email,times(1)).enviarNotificacion(any());
        for(var medio:new MedioComunicacion[]{MedioComunicacion.SMS,MedioComunicacion.WHATSAPP})
            assertThat(reiniciado.enviarNotificacion(new NotificacionRequestDTO("Prueba",medio,"3515551234","donaciones","PRUEBA",medio.name())).getEstado()).isEqualTo(EstadoNotificacion.COMPLETADA);
        assertThat(reiniciado.listarTodas()).hasSize(3);
    }
}
