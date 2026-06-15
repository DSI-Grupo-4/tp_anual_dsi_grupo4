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

    public EntidadBeneficiaria crear(EntidadBeneficiariaDTO dto) {
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

        return entidad;
    }

    public List<EntidadBeneficiaria> obtenerTodas() {
        return entidades;
    }

    public EntidadBeneficiaria obtenerPorId(Long id) {

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
}
