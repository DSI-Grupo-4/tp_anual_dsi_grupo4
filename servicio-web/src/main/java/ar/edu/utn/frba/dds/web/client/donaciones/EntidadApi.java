package ar.edu.utn.frba.dds.web.client.donaciones;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Entidad beneficiaria (EntidadBeneficiariaDTO). Sin dirección ni medios de contacto: no los usa ninguna vista pública. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EntidadApi(Long id, Boolean activa, String descripcion, PersonaJuridica personaJuridica) {

    public String nombre() {
        return personaJuridica != null && personaJuridica.razonSocial() != null ? personaJuridica.razonSocial() : "Entidad " + id;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PersonaJuridica(String razonSocial) {
    }
}
