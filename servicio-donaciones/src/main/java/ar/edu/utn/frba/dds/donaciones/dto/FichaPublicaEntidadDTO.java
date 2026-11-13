package ar.edu.utn.frba.dds.donaciones.dto;

import ar.edu.utn.frba.dds.donaciones.domain.lugares.Ciudad;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;

/**
 * Lo que un donante puede ver de una entidad: nombre, tipo, rubro, descripción
 * y ciudad. Sin calle, contactos ni representantes.
 */
public record FichaPublicaEntidadDTO(
        Long id, Boolean activa, String descripcion, Persona personaJuridica, Direccion direccion) {

    public record Persona(String razonSocial, String tipo, String rubro) {
    }

    public record Direccion(CiudadPublica ciudad) {
    }

    public record CiudadPublica(String nombre, Provincia provincia) {
    }

    public record Provincia(String nombre) {
    }

    public static FichaPublicaEntidadDTO desde(EntidadBeneficiaria entidad) {
        var persona = entidad.getEntidad();
        return new FichaPublicaEntidadDTO(
                entidad.getId(),
                entidad.isActiva(),
                entidad.getDescripcion(),
                new Persona(persona.getRazonSocial(),
                        persona.getTipo() == null ? null : persona.getTipo().name(),
                        persona.getRubro()),
                direccion(entidad));
    }

    private static Direccion direccion(EntidadBeneficiaria entidad) {
        if (entidad.getDireccion() == null) {
            return null;
        }
        Ciudad ciudad = entidad.getDireccion().getCiudad();
        if (ciudad == null) {
            return null;
        }
        Provincia provincia = ciudad.getProvincia() == null ? null : new Provincia(ciudad.getProvincia().getNombre());
        return new Direccion(new CiudadPublica(ciudad.getNombre(), provincia));
    }
}
