package ar.edu.utn.frba.dds.donaciones.service;

import ar.edu.utn.frba.dds.donaciones.domain.donaciones.Donacion;
import ar.edu.utn.frba.dds.donaciones.dto.CambioEstadoDTO;
import ar.edu.utn.frba.dds.donaciones.dto.DonacionDTO;
import ar.edu.utn.frba.dds.donaciones.dto.TimeStampDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class DonacionService {

    private final Map<Long, Donacion> donaciones = new ConcurrentHashMap<>();
    private final AtomicLong contador = new AtomicLong(1);

    private final EntidadBeneficiariaService entidadBeneficiariaService;
    private final NecesidadService necesidadService;

    public DonacionService(
            EntidadBeneficiariaService entidadBeneficiariaService,
            NecesidadService necesidadService) {
        this.entidadBeneficiariaService = entidadBeneficiariaService;
        this.necesidadService = necesidadService;
    }

    public DonacionDTO crear(DonacionDTO dto) {
        Donacion donacion = new Donacion();
        donacion.setCantidadAportada(dto.getCantidadAsignada() != null ? dto.getCantidadAsignada() : 0);
        long id = contador.getAndIncrement();
        donaciones.put(id, donacion);
        return convertirADTO(id, donacion, dto);
    }

    public List<DonacionDTO> obtenerTodas() {
        List<DonacionDTO> result = new ArrayList<>();
        donaciones.forEach((id, d) -> result.add(convertirADTO(id, d, null)));
        return result;
    }

    public DonacionDTO obtenerPorId(Long id) {
        return convertirADTO(id, obtenerDominioPorId(id), null);
    }

    public void eliminar(Long id) {
        donaciones.remove(id);
    }

    public DonacionDTO cambiarEstado(Long id, CambioEstadoDTO dto) {
        Donacion donacion = obtenerDominioPorId(id);
        return convertirADTO(id, donacion, null);
    }

    public List<TimeStampDTO> obtenerHistorial(Long id) {
        return List.of();
    }

    public Donacion obtenerDominioPorId(Long id) {
        Donacion donacion = donaciones.get(id);
        if (donacion == null) {
            throw new RuntimeException("Donacion no encontrada: " + id);
        }
        return donacion;
    }

    private DonacionDTO convertirADTO(Long id, Donacion donacion, DonacionDTO original) {
        DonacionDTO dto = new DonacionDTO();
        dto.setId(id);
        dto.setCantidadAsignada(donacion.getCantidadAportada());
        if (original != null) {
            dto.setDescripcionItem(original.getDescripcionItem());
            dto.setEntidadBeneficiariaId(original.getEntidadBeneficiariaId());
            dto.setNecesidadId(original.getNecesidadId());
        }
        return dto;
    }
}
