package ar.edu.utn.frba.dds.donaciones.service;

import ar.edu.utn.frba.dds.donaciones.client.NotificacionesClient;
import domain.donaciones.Donacion;
import domain.necesidades.Necesidad;
import domain.personas.Donante;
import domain.personas.Persona;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class DonacionesService {

    private final NotificacionesClient notificacionesClient;
    private final ContactoPreferidoResolver contactoPreferidoResolver;

    public DonacionesService(NotificacionesClient notificacionesClient,
                             ContactoPreferidoResolver contactoPreferidoResolver) {
        this.notificacionesClient = notificacionesClient;
        this.contactoPreferidoResolver = contactoPreferidoResolver;
    }

    public void asignarDonacion(Donante donante, Persona entidadBeneficiaria, Donacion donacion, Necesidad necesidad) {
        donacion.asignarA(necesidad);
        notificarEntidadPorDonacionAsignada(entidadBeneficiaria);
        notificarDonantePorDonacionAsignada(donante);
    }

    public void notificarDonantesInactivos(Iterable<Donante> donantes) {
        LocalDate limite = LocalDate.now().minusDays(20);
        for (Donante donante : donantes) {
            if (donante.getFechaUltimaInteraccion() != null && donante.getFechaUltimaInteraccion().isBefore(limite)) {
                notificarDonanteInactivo(donante);
            }
        }
    }

    private void notificarDonanteInactivo(Donante donante) {
        enviarADonante(donante, "Hace mas de 20 dias que no interactuas con DonaTrack. Te invitamos a realizar una nueva donacion.");
    }

    private void notificarEntidadPorDonacionAsignada(Persona entidadBeneficiaria) {
        contactoPreferidoResolver.resolver(entidadBeneficiaria)
                .ifPresent(contacto -> notificacionesClient.enviar(
                        "Una donacion fue asignada a tu entidad beneficiaria.",
                        contacto.medio(),
                        contacto.contacto()
                ));
    }

    private void notificarDonantePorDonacionAsignada(Donante donante) {
        enviarADonante(donante, "Tu donacion fue asignada a una entidad beneficiaria.");
    }

    private void enviarADonante(Donante donante, String mensaje) {
        if (donante == null) {
            return;
        }
        contactoPreferidoResolver.resolver(donante.getPersona())
                .ifPresent(contacto -> notificacionesClient.enviar(mensaje, contacto.medio(), contacto.contacto()));
    }
}
