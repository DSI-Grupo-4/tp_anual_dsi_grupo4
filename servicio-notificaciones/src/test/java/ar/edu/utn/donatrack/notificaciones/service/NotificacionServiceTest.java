package ar.edu.utn.donatrack.notificaciones.service;

import ar.edu.utn.donatrack.notificaciones.dto.NotificacionRequestDTO;
import ar.edu.utn.donatrack.notificaciones.enums.EstadoNotificacion;
import ar.edu.utn.donatrack.notificaciones.enums.MedioComunicacion;
import ar.edu.utn.donatrack.notificaciones.exception.EnvioNotificacionException;
import ar.edu.utn.donatrack.notificaciones.model.Notificacion;
import ar.edu.utn.donatrack.notificaciones.repository.NotificacionRepository;
import ar.edu.utn.donatrack.notificaciones.strategy.EmailNotificador;
import ar.edu.utn.donatrack.notificaciones.strategy.NotificadorFactory;
import ar.edu.utn.donatrack.notificaciones.strategy.SmsNotificador;
import ar.edu.utn.donatrack.notificaciones.strategy.WhatsAppNotificador;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificacionServiceTest {

    // Simula la tabla con un mapa, para no necesitar MySQL en el test
    private final Map<String, Notificacion> tabla = new LinkedHashMap<>();
    private EmailNotificador email;
    private NotificacionService servicio;

    @BeforeEach
    void preparar() {
        NotificacionRepository repo = mock(NotificacionRepository.class);
        when(repo.save(any(Notificacion.class))).thenAnswer(inv -> {
            Notificacion n = inv.getArgument(0);
            tabla.put(n.getId(), n);
            return n;
        });
        when(repo.findByServicioOrigenAndEventoId(anyString(), anyString())).thenAnswer(inv ->
                tabla.values().stream()
                        .filter(n -> n.getServicioOrigen().equals(inv.getArgument(0))
                                && n.getEventoId().equals(inv.getArgument(1)))
                        .findFirst());
        when(repo.findAll(any(Sort.class))).thenAnswer(inv -> new ArrayList<>(tabla.values()));

        email = spy(new EmailNotificador(mock(JavaMailSender.class), "test@donatrack.local"));
        var factory = new NotificadorFactory(email, new SmsNotificador(), new WhatsAppNotificador());
        servicio = new NotificacionService(factory, repo);
    }

    @Test
    void noReenviaUnEventoYaCompletadoYCompletaLosTresMedios() {
        var solicitud = new NotificacionRequestDTO("Prueba", MedioComunicacion.EMAIL, "ana@example.org",
                "donaciones", "BIENVENIDA", "evento-1");

        var original = servicio.enviarNotificacion(solicitud);
        var reintento = servicio.enviarNotificacion(solicitud);

        assertThat(reintento.getId()).isEqualTo(original.getId());
        verify(email, times(1)).enviarNotificacion(any());

        for (var medio : new MedioComunicacion[]{MedioComunicacion.SMS, MedioComunicacion.WHATSAPP}) {
            var n = servicio.enviarNotificacion(new NotificacionRequestDTO("Prueba", medio, "+5491155551234",
                    "donaciones", "PRUEBA", medio.name()));
            assertThat(n.getEstado()).isEqualTo(EstadoNotificacion.COMPLETADA);
        }
        assertThat(servicio.listarTodas()).hasSize(3);
    }

    @Test
    void registraElFalloYReintentaSobreLaMismaFila() {
        doThrow(new RuntimeException("servidor de correo caido"))
                .doCallRealMethod()
                .when(email).enviarNotificacion(any());
        var solicitud = new NotificacionRequestDTO("Prueba", MedioComunicacion.EMAIL, "ana@example.org",
                "donaciones", "BIENVENIDA", "evento-2");

        assertThatThrownBy(() -> servicio.enviarNotificacion(solicitud))
                .isInstanceOf(EnvioNotificacionException.class);
        Notificacion fallida = tabla.values().iterator().next();
        assertThat(fallida.getEstado()).isEqualTo(EstadoNotificacion.FALLIDA);
        assertThat(fallida.getDetalleError()).contains("caido");

        var reintento = servicio.enviarNotificacion(solicitud);

        assertThat(reintento.getId()).isEqualTo(fallida.getId());
        assertThat(reintento.getEstado()).isEqualTo(EstadoNotificacion.COMPLETADA);
        assertThat(reintento.getIntentos()).isEqualTo(2);
        assertThat(tabla).hasSize(1);
    }
}