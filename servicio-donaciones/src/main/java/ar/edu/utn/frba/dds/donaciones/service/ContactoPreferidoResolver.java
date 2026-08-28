package ar.edu.utn.frba.dds.donaciones.service;

import ar.edu.utn.frba.dds.donaciones.domain.personas.Persona;
import ar.edu.utn.frba.dds.donaciones.domain.personas.TipoContacto;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ContactoPreferidoResolver {

    public Optional<ContactoNotificacion> resolver(Persona persona) {
        if (persona == null) {
            return Optional.empty();
        }
        return persona.medioPreferido()
                .map(medio -> new ContactoNotificacion(mapearMedio(medio.getTipo()), medio.getValor()));
    }

    private String mapearMedio(TipoContacto tipo) {
        if (tipo == TipoContacto.EMAIL) return "EMAIL";
        if (tipo == TipoContacto.WHATSAPP) return "WHATSAPP";
        return "SMS";
    }

    public record ContactoNotificacion(String medio, String contacto) {
    }
}
