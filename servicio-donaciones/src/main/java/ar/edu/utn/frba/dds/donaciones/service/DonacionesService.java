package ar.edu.utn.frba.dds.donaciones.service;

import ar.edu.utn.frba.dds.donaciones.client.NotificacionesClient;
import ar.edu.utn.frba.dds.donaciones.domain.personas.Donante;
import ar.edu.utn.frba.dds.donaciones.domain.personas.Persona;
import org.springframework.stereotype.Service;

@Service
public class DonacionesService {

    private final NotificacionesClient notificacionesClient;
    private final ContactoPreferidoResolver contactoPreferidoResolver;

    public DonacionesService(NotificacionesClient notificacionesClient,
                             ContactoPreferidoResolver contactoPreferidoResolver) {
        this.notificacionesClient = notificacionesClient;
        this.contactoPreferidoResolver = contactoPreferidoResolver;
    }

    public void notificarDonantesInactivos(Iterable<Donante> donantes) {
        for (Donante donante : donantes) {
            if (donante.getUltimaActividad() == null) {
                notificarDonanteInactivo(donante);
            }
        }
    }

    private void notificarDonanteInactivo(Donante donante) {
        Persona persona = donante.getPersonaAsociada();
        enviarAPersona(persona, "Hace mas de 20 dias que no interactuas con DonaTrack. Te invitamos a realizar una nueva donacion.");
    }

    private void enviarAPersona(Persona persona, String mensaje) {
        contactoPreferidoResolver.resolver(persona)
                .ifPresent(contacto -> notificacionesClient.enviar(mensaje, contacto.medio(), contacto.contacto()));
    }
}
