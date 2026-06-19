package ar.edu.utn.frba.dds.donaciones.service;

import ar.edu.utn.frba.dds.donaciones.domain.personas.MedioContacto;
import ar.edu.utn.frba.dds.donaciones.domain.personas.Persona;
import ar.edu.utn.frba.dds.donaciones.domain.personas.TipoMedio;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ContactoPreferidoResolver {

    public Optional<ContactoNotificacion> resolver(Persona persona) {
        if (persona == null || persona.getMedios().isEmpty()) {
            return Optional.empty();
        }
        MedioContacto medio = persona.getMedios().get(0);
        return Optional.of(new ContactoNotificacion(mapearMedio(medio.getType()), medio.getValor()));
    }

    private String mapearMedio(TipoMedio tipo) {
        if (tipo == TipoMedio.EMAIL) return "EMAIL";
        if (tipo == TipoMedio.WHATSAPP) return "WHATSAPP";
        return "SMS";
    }

    public record ContactoNotificacion(String medio, String contacto) {
    }
}
