package ar.edu.utn.frba.dds.donaciones.scheduler;

import ar.edu.utn.frba.dds.donaciones.domain.personas.Donante;
import ar.edu.utn.frba.dds.donaciones.service.DonanteService;
import ar.edu.utn.frba.dds.donaciones.service.NotificacionClienteService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Job diario que implementa el caso de notificación pendiente:
 * "A una persona donante, cuando no registre interacción con la plataforma
 * durante más de 20 días, con el objetivo de incentivar a realizar una
 * nueva donación" (Entrega 2 - Servicio de Donaciones).
 *
 * Antes de este cambio no existía ningún mecanismo que revisara la
 * inactividad de los donantes.
 *
 * Se ejecuta una vez por día, en un horario de baja carga (igual criterio
 * que el matchmaking, que corre a las 2am). Cada donante se notifica una
 * sola vez mientras siga inactivo (Donante.notificadoInactividad); el flag
 * se resetea automáticamente en cuanto vuelve a donar
 * (ver Donante.agregarDonacion).
 */
@Component
public class InactividadScheduler {

    private static final int DIAS_INACTIVIDAD = 20;

    private final DonanteService donanteService;
    private final NotificacionClienteService notificacionClienteService;

    public InactividadScheduler(
            DonanteService donanteService,
            NotificacionClienteService notificacionClienteService) {
        this.donanteService = donanteService;
        this.notificacionClienteService = notificacionClienteService;
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void notificarDonantesInactivos() {
        LocalDate hoy = LocalDate.now();

        for (Donante donante : donanteService.obtenerTodosDominio()) {
            if (donante.isNotificadoInactividad()) {
                continue;
            }
            if (donante.getUltimaActividad() == null) {
                continue;
            }

            long diasSinActividad = ChronoUnit.DAYS.between(donante.getUltimaActividad(), hoy);
            if (diasSinActividad > DIAS_INACTIVIDAD) {
                notificacionClienteService.notificarPersona(
                        donante.getPersona(),
                        "Hace más de " + DIAS_INACTIVIDAD + " días que no donás. "
                                + "¡Te extrañamos! Animate a hacer una nueva donación."
                );
                donante.setNotificadoInactividad(true);
            }
        }
    }
}
