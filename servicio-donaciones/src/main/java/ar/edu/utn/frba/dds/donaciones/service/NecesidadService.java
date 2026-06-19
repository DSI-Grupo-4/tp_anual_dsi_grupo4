package ar.edu.utn.frba.dds.donaciones.service;

import ar.edu.utn.frba.dds.donaciones.domain.necesidades.Necesidad;
import ar.edu.utn.frba.dds.donaciones.domain.necesidades.NecesidadExtraordinaria;
import ar.edu.utn.frba.dds.donaciones.domain.necesidades.NecesidadRecurrente;
import ar.edu.utn.frba.dds.donaciones.dto.NecesidadDTO;
import ar.edu.utn.frba.dds.donaciones.dto.NecesidadExtraordinariaDTO;
import ar.edu.utn.frba.dds.donaciones.dto.NecesidadRecurrenteDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class NecesidadService {

    private final Map<Long, Necesidad> necesidades = new ConcurrentHashMap<>();
    private final AtomicLong contador = new AtomicLong(1);

    private final EntidadBeneficiariaService entidadBeneficiariaService;

    public NecesidadService(EntidadBeneficiariaService entidadBeneficiariaService) {
        this.entidadBeneficiariaService = entidadBeneficiariaService;
    }

    public NecesidadDTO crearRecurrente(NecesidadRecurrenteDTO dto) {
        NecesidadRecurrente necesidad = new NecesidadRecurrente();
        necesidad.setDescripcion(dto.getDescripcion());
        necesidad.setCantidadRequerida(dto.getCantidadRequerida() != null ? dto.getCantidadRequerida() : 0);
        necesidad.setPeriodicidad(dto.getPeriodicidad());
        long id = contador.getAndIncrement();
        necesidades.put(id, necesidad);
        return convertirADTO(id, necesidad);
    }

    public NecesidadDTO crearExtraordinaria(NecesidadExtraordinariaDTO dto) {
        NecesidadExtraordinaria necesidad = new NecesidadExtraordinaria();
        necesidad.setDescripcion(dto.getDescripcion());
        necesidad.setCantidadRequerida(dto.getCantidadRequerida() != null ? dto.getCantidadRequerida() : 0);
        necesidad.setTipoExtraordinario(dto.getTipoExtraordinario());
        long id = contador.getAndIncrement();
        necesidades.put(id, necesidad);
        return convertirADTO(id, necesidad);
    }

    public List<NecesidadDTO> obtenerTodas() {
        List<NecesidadDTO> result = new ArrayList<>();
        necesidades.forEach((id, n) -> result.add(convertirADTO(id, n)));
        return result;
    }

    public NecesidadDTO obtenerPorId(Long id) {
        return convertirADTO(id, buscarDominioPorId(id));
    }

    public NecesidadDTO actualizarRecurrente(Long id, NecesidadRecurrenteDTO dto) {
        Necesidad necesidad = buscarDominioPorId(id);
        necesidad.setDescripcion(dto.getDescripcion());
        necesidad.setCantidadRequerida(dto.getCantidadRequerida() != null ? dto.getCantidadRequerida() : 0);
        if (necesidad instanceof NecesidadRecurrente recurrente) {
            recurrente.setPeriodicidad(dto.getPeriodicidad());
        }
        return convertirADTO(id, necesidad);
    }

    public NecesidadDTO actualizarExtraordinaria(Long id, NecesidadExtraordinariaDTO dto) {
        Necesidad necesidad = buscarDominioPorId(id);
        necesidad.setDescripcion(dto.getDescripcion());
        necesidad.setCantidadRequerida(dto.getCantidadRequerida() != null ? dto.getCantidadRequerida() : 0);
        if (necesidad instanceof NecesidadExtraordinaria extraordinaria) {
            extraordinaria.setTipoExtraordinario(dto.getTipoExtraordinario());
        }
        return convertirADTO(id, necesidad);
    }

    public List<NecesidadDTO> obtenerPorEntidad(Long entidadId) {
        return List.of();
    }

    public void eliminar(Long id) {
        necesidades.remove(id);
    }

    public Necesidad buscarDominioPorId(Long id) {
        Necesidad necesidad = necesidades.get(id);
        if (necesidad == null) {
            throw new RuntimeException("Necesidad no encontrada: " + id);
        }
        return necesidad;
    }

    private NecesidadDTO convertirADTO(Long id, Necesidad necesidad) {
        NecesidadDTO dto = new NecesidadDTO();
        dto.setId(id);
        dto.setDescripcion(necesidad.getDescripcion());
        dto.setCantidadRequerida(necesidad.getCantidadRequerida());
        dto.setCantidadRecibida(0);
        dto.setSatisfecha(false);

        if (necesidad instanceof NecesidadRecurrente recurrente) {
            dto.setTipo("RECURRENTE");
            dto.setPeriodicidad(recurrente.getPeriodicidad());
        }
        if (necesidad instanceof NecesidadExtraordinaria extraordinaria) {
            dto.setTipo("EXTRAORDINARIA");
            dto.setTipoExtraordinario(extraordinaria.getTipoExtraordinario());
        }
        return dto;
    }
}
