package ar.edu.utn.frba.dds.donaciones.service;

import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import ar.edu.utn.frba.dds.donaciones.dto.EntidadBeneficiariaDTO;
import ar.edu.utn.frba.dds.donaciones.dto.PersonaJuridicaDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class EntidadBeneficiariaService {

    private final Map<Long, EntidadBeneficiaria> entidades = new ConcurrentHashMap<>();
    private final AtomicLong contador = new AtomicLong(1);

    public EntidadBeneficiariaDTO crear(EntidadBeneficiariaDTO dto) {
        EntidadBeneficiaria entidad = new EntidadBeneficiaria();
        entidad.setNombre(dto.getPersonaJuridica() != null ? dto.getPersonaJuridica().getRazonSocial() : dto.getDescripcion());
        long id = contador.getAndIncrement();
        entidades.put(id, entidad);
        return convertirADTO(id, entidad, dto.getDescripcion(), dto.getPersonaJuridica());
    }

    public List<EntidadBeneficiariaDTO> obtenerTodas() {
        List<EntidadBeneficiariaDTO> result = new ArrayList<>();
        entidades.forEach((id, e) -> result.add(convertirADTO(id, e, null, null)));
        return result;
    }

    public EntidadBeneficiariaDTO obtenerPorId(Long id) {
        return convertirADTO(id, buscarEntidad(id), null, null);
    }

    public EntidadBeneficiaria buscarEntidad(Long id) {
        EntidadBeneficiaria entidad = entidades.get(id);
        if (entidad == null) {
            throw new RuntimeException("EntidadBeneficiaria no encontrada: " + id);
        }
        return entidad;
    }

    public void eliminar(Long id) {
        entidades.remove(id);
    }

    public EntidadBeneficiariaDTO convertirADTO(EntidadBeneficiaria entidad) {
        long id = entidades.entrySet().stream()
                .filter(e -> e.getValue() == entidad)
                .map(Map.Entry::getKey)
                .findFirst().orElse(-1L);
        return convertirADTO(id, entidad, null, null);
    }

    public EntidadBeneficiariaDTO actualizar(Long id, EntidadBeneficiariaDTO dto) {
        EntidadBeneficiaria entidad = buscarEntidad(id);
        if (dto.getPersonaJuridica() != null) {
            entidad.setNombre(dto.getPersonaJuridica().getRazonSocial());
        }
        return convertirADTO(id, entidad, dto.getDescripcion(), dto.getPersonaJuridica());
    }

    public List<EntidadBeneficiaria> obtenerEntidadesDominio() {
        return new ArrayList<>(entidades.values());
    }

    private EntidadBeneficiariaDTO convertirADTO(Long id, EntidadBeneficiaria entidad, String descripcion, PersonaJuridicaDTO personaJuridicaDTO) {
        EntidadBeneficiariaDTO dto = new EntidadBeneficiariaDTO();
        dto.setId(id);
        dto.setDescripcion(descripcion != null ? descripcion : entidad.getNombre());
        PersonaJuridicaDTO pjDTO = personaJuridicaDTO != null ? personaJuridicaDTO : new PersonaJuridicaDTO();
        if (personaJuridicaDTO == null) {
            pjDTO.setRazonSocial(entidad.getNombre());
        }
        dto.setPersonaJuridica(pjDTO);
        return dto;
    }
}
