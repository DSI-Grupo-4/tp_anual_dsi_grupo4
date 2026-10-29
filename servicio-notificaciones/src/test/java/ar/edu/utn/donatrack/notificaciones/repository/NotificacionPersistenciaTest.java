package ar.edu.utn.donatrack.notificaciones.repository;

import ar.edu.utn.donatrack.notificaciones.enums.EstadoNotificacion;
import ar.edu.utn.donatrack.notificaciones.enums.MedioComunicacion;
import ar.edu.utn.donatrack.notificaciones.model.Notificacion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Prueba el mapeo JPA contra una base real (H2 en memoria): que la entidad
 * se guarde y se lea completa, y que la restricción única impida duplicados.
 */
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class NotificacionPersistenciaTest {

    @Autowired
    private NotificacionRepository repository;

    private Notificacion nueva(String eventoId) {
        return new Notificacion("Tu donación fue asignada", MedioComunicacion.EMAIL,
                "ana@example.org", "donaciones", "DONACION_ASIGNADA", eventoId);
    }

    @Test
    void guardaYRecuperaLaNotificacionCompleta() {
        Notificacion original = nueva("evento-1");
        original.registrarIntento();
        original.marcarCompletada();
        repository.saveAndFlush(original);

        Notificacion leida = repository.findById(original.getId()).orElseThrow();

        assertThat(leida.getMedio()).isEqualTo(MedioComunicacion.EMAIL);
        assertThat(leida.getEstado()).isEqualTo(EstadoNotificacion.COMPLETADA);
        assertThat(leida.getIntentos()).isEqualTo(1);
        assertThat(leida.getFechaEnvio()).isNotNull();
    }

    @Test
    void encuentraPorServicioOrigenYEventoId() {
        repository.saveAndFlush(nueva("evento-2"));

        assertThat(repository.findByServicioOrigenAndEventoId("donaciones", "evento-2")).isPresent();
        assertThat(repository.findByServicioOrigenAndEventoId("incentivos", "evento-2")).isEmpty();
    }

    @Test
    void laBaseNoPermiteDosNotificacionesDelMismoEvento() {
        repository.saveAndFlush(nueva("evento-3"));

        assertThatThrownBy(() -> repository.saveAndFlush(nueva("evento-3")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}