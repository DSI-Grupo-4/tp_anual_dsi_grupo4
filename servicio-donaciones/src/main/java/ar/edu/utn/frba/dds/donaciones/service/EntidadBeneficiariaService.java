package ar.edu.utn.frba.dds.donaciones.service;

import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import ar.edu.utn.frba.dds.donaciones.domain.personas.PersonaHumana;
import ar.edu.utn.frba.dds.donaciones.domain.personas.PersonaJuridica;
import ar.edu.utn.frba.dds.donaciones.dto.EntidadBeneficiariaDTO;
import ar.edu.utn.frba.dds.donaciones.dto.PersonaJuridicaDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EntidadBeneficiariaService {
    private List<EntidadBeneficiaria> entidades =
            new ArrayList<>();

    private Long siguienteId = 1L;

    public EntidadBeneficiariaDTO crear(EntidadBeneficiariaDTO dto) {
        PersonaJuridicaDTO personaJuridicaDTO = dto.getPersonaJuridica();

        PersonaJuridica personaJuridica =
                new PersonaJuridica(
                        personaJuridicaDTO.getRazonSocial(),
                        personaJuridicaDTO.getTipo(),
                        personaJuridicaDTO.getRubro(),
                        null
                );

        EntidadBeneficiaria entidad =
                new EntidadBeneficiaria(
                        siguienteId++,
                        personaJuridica,
                        dto.getDescripcion()
                );
        entidades.add(entidad);

        return convertirADTO(entidad);
    }

    public List<EntidadBeneficiariaDTO> obtenerTodas() {
        return entidades.stream().map(this::convertirADTO).toList();
    }

    public EntidadBeneficiariaDTO obtenerPorId(Long id) {

        EntidadBeneficiaria entidad = entidades.stream()
                .filter(e -> e.getId().equals(id))
                .findFirst()
                .orElseThrow();

        return convertirADTO(entidad);
    }

    public EntidadBeneficiaria buscarEntidad(Long id) {

        return entidades.stream()
                .filter(e -> e.getId().equals(id))
                .findFirst()
                .orElseThrow();
    }

    public void eliminar(Long id) {

        entidades.removeIf(
                e -> e.getId().equals(id)
        );
    }

    public EntidadBeneficiariaDTO convertirADTO(
            EntidadBeneficiaria entidad) {

        EntidadBeneficiariaDTO dto =
                new EntidadBeneficiariaDTO();

        PersonaJuridicaDTO personaDTO =
                new PersonaJuridicaDTO();

        personaDTO.setRazonSocial(
                entidad.getEntidad().getRazonSocial()
        );

        personaDTO.setTipo(
                entidad.getEntidad().getTipo()
        );

        personaDTO.setRubro(
                entidad.getEntidad().getRubro()
        );

        dto.setPersonaJuridica(personaDTO);
        dto.setDescripcion(entidad.getDescripcion());
        dto.setId(entidad.getId());

        return dto;
    }

    public EntidadBeneficiariaDTO actualizar(
            Long id,
            EntidadBeneficiariaDTO dto) {

        EntidadBeneficiaria entidad =
                buscarEntidad(id);

        entidad.setDescripcion(
                dto.getDescripcion()
        );

        PersonaJuridica persona =
                entidad.getEntidad();

        persona.setRazonSocial(
                dto.getPersonaJuridica().getRazonSocial()
        );

        persona.setTipo(
                dto.getPersonaJuridica().getTipo()
        );

        persona.setRubro(
                dto.getPersonaJuridica().getRubro()
        );

        return convertirADTO(entidad);
    }
}
