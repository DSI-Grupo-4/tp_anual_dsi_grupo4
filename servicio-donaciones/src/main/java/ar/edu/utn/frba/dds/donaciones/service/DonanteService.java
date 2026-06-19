package ar.edu.utn.frba.dds.donaciones.service;

import ar.edu.utn.frba.dds.donaciones.domain.personas.Donante;
import ar.edu.utn.frba.dds.donaciones.domain.personas.PersonaHumana;
import ar.edu.utn.frba.dds.donaciones.domain.personas.PersonaJuridica;
import ar.edu.utn.frba.dds.donaciones.domain.personas.TipoDoc;
import ar.edu.utn.frba.dds.donaciones.domain.personas.TipoOrg;
import ar.edu.utn.frba.dds.donaciones.dto.DonanteDTO;
import ar.edu.utn.frba.dds.donaciones.dto.PersonaHumanaDTO;
import ar.edu.utn.frba.dds.donaciones.dto.PersonaJuridicaDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class DonanteService {

    private final Map<Long, Donante> donantes = new ConcurrentHashMap<>();
    private final AtomicLong contador = new AtomicLong(1);

    public DonanteDTO convertirADTO(Donante donante) {
        long id = donantes.entrySet().stream()
                .filter(e -> e.getValue() == donante)
                .map(Map.Entry::getKey)
                .findFirst().orElse(-1L);
        DonanteDTO dto = new DonanteDTO();
        dto.setId(id);
        if (donante.getPersonaAsociada() instanceof PersonaHumana) {
            PersonaHumana humana = (PersonaHumana) donante.getPersonaAsociada();
            dto.setTipo("HUMANA");
            dto.setNombre(humana.getNombre());
            dto.setApellido(humana.getApellido());
        } else if (donante.getPersonaAsociada() instanceof PersonaJuridica) {
            PersonaJuridica juridica = (PersonaJuridica) donante.getPersonaAsociada();
            dto.setTipo("JURIDICA");
            dto.setRazonSocial(juridica.getRazonSocial());
        }
        return dto;
    }

    public DonanteDTO crearDonanteHumano(PersonaHumanaDTO dto) {
        PersonaHumana persona = new PersonaHumana(
                dto.getNombre(),
                dto.getApellido(),
                TipoDoc.DNI,
                0,
                dto.getGenero()
        );
        Donante donante = new Donante(persona);
        long id = contador.getAndIncrement();
        donantes.put(id, donante);
        return convertirADTO(donante);
    }

    public List<DonanteDTO> obtenerTodos() {
        List<DonanteDTO> result = new ArrayList<>();
        donantes.forEach((id, d) -> {
            DonanteDTO dto = convertirADTO(d);
            dto.setId(id);
            result.add(dto);
        });
        return result;
    }

    public DonanteDTO crearDonanteJuridico(PersonaJuridicaDTO dto) {
        PersonaJuridica persona = new PersonaJuridica(
                dto.getRazonSocial(),
                TipoOrg.EMPRESA,
                dto.getRubro()
        );
        Donante donante = new Donante(persona);
        long id = contador.getAndIncrement();
        donantes.put(id, donante);
        return convertirADTO(donante);
    }

    public DonanteDTO buscarPorId(Long id) {
        Donante donante = donantes.get(id);
        if (donante == null) throw new RuntimeException("Donante no encontrado: " + id);
        DonanteDTO dto = convertirADTO(donante);
        dto.setId(id);
        return dto;
    }

    public void eliminar(Long id) {
        donantes.remove(id);
    }

    public DonanteDTO actualizarHumano(Long id, PersonaHumanaDTO dto) {
        Donante donante = donantes.get(id);
        if (donante == null) throw new RuntimeException("Donante no encontrado: " + id);
        if (donante.getPersonaAsociada() instanceof PersonaHumana) {
            PersonaHumana humana = (PersonaHumana) donante.getPersonaAsociada();
            humana.setNombre(dto.getNombre());
            humana.setApellido(dto.getApellido());
            humana.setGenero(dto.getGenero());
        }
        DonanteDTO result = convertirADTO(donante);
        result.setId(id);
        return result;
    }

    public DonanteDTO actualizarJuridico(Long id, PersonaJuridicaDTO dto) {
        Donante donante = donantes.get(id);
        if (donante == null) throw new RuntimeException("Donante no encontrado: " + id);
        if (donante.getPersonaAsociada() instanceof PersonaJuridica) {
            PersonaJuridica juridica = (PersonaJuridica) donante.getPersonaAsociada();
            juridica.setRazonSocial(dto.getRazonSocial());
            juridica.setRubro(dto.getRubro());
        }
        DonanteDTO result = convertirADTO(donante);
        result.setId(id);
        return result;
    }

    public List<Donante> obtenerTodosDominio() {
        return new ArrayList<>(donantes.values());
    }
}
