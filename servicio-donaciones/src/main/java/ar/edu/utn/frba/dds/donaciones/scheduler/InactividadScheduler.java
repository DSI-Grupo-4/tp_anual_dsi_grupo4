package ar.edu.utn.frba.dds.donaciones.scheduler;

import ar.edu.utn.frba.dds.donaciones.domain.personas.Donante;
import ar.edu.utn.frba.dds.donaciones.domain.personas.GestorDonantes;
import ar.edu.utn.frba.dds.donaciones.integracion.PublicadorEventosPort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Notifica a una persona donante cuando no registra interacción con la
 * plataforma (alta o donación) durante más de 20 días, para incentivarla a
 * donar de nuevo. Cada episodio de inactividad se notifica una sola vez
 * (ver Donante.debeNotificarsePorInactividad/marcarNotificadoPorInactividad).
 */
@Component
public class InactividadScheduler {

    private static final int UMBRAL_DIAS_INACTIVIDAD = 20;

    private final GestorDonantes gestorDonantes;
    private final PublicadorEventosPort publicadorEventos;

    public InactividadScheduler(GestorDonantes gestorDonantes, PublicadorEventosPort publicadorEventos) {
        this.gestorDonantes = gestorDonantes;
        this.publicadorEventos = publicadorEventos;
    }

    @Scheduled(cron = "${donantes.inactividad.cron:0 0 4 * * *}")
    public void verificarInactividad() {
        for (Donante donante : gestorDonantes.getDonantesRegistrados()) {
            if (donante.debeNotificarsePorInactividad(UMBRAL_DIAS_INACTIVIDAD)) {
                publicadorEventos.publicar("INACTIVIDAD_20_DIAS", donante);
                donante.marcarNotificadoPorInactividad();
            }
        }
    }
}
