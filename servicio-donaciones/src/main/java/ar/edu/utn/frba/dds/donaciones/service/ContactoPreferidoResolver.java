package ar.edu.utn.frba.dds.donaciones.service;

import domain.personas.MedioContacto;
import domain.personas.Persona;
import domain.personas.TipoContacto;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ContactoPreferidoResolver {

    public Optional<ContactoNotificacion> resolver(Persona persona) {
        if (persona == null) {
            return Optional.empty();
        }
        return persona.medioPreferido().map(this::mapear);
    }

    private ContactoNotificacion mapear(MedioContacto medioContacto) {
        return new ContactoNotificacion(mapearMedio(medioContacto.getTipo()), medioContacto.getValor());
    }

    private String mapearMedio(TipoContacto tipoContacto) {
        if (tipoContacto == TipoContacto.EMAIL) {
            return "EMAIL";
        }
        if (tipoContacto == TipoContacto.WHATSAPP) {
            return "WHATSAPP";
        }
        return "SMS";
    }

    public record ContactoNotificacion(String medio, String contacto) {
    }
}
