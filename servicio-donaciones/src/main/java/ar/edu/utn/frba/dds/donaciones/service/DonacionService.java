package ar.edu.utn.frba.dds.donaciones.service;

import ar.edu.utn.frba.dds.donaciones.domain.donaciones.CambioEstado;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.Donacion;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.EstadoTrack;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.ItemDonado;
import ar.edu.utn.frba.dds.donaciones.domain.necesidades.Necesidad;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import ar.edu.utn.frba.dds.donaciones.dto.CambioEstadoDTO;
import ar.edu.utn.frba.dds.donaciones.dto.DonacionDTO;
import ar.edu.utn.frba.dds.donaciones.dto.TimeStampDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DonacionService {

    private final List<Donacion> donaciones = new ArrayList<>();
    private Long siguienteId = 1L;

    private final EntidadBeneficiariaService entidadBeneficiariaService;
    private final NecesidadService necesidadService;

    public DonacionService(
            EntidadBeneficiariaService entidadBeneficiariaService,
            NecesidadService necesidadService) {
        this.entidadBeneficiariaService = entidadBeneficiariaService;
        this.necesidadService = necesidadService;
    }

    public DonacionDTO crear(DonacionDTO dto) {
        ItemDonado item = new ItemDonado(
                null,
                dto.getDescripcionItem(),
                null,
                dto.getCantidadAsignada(),
                null,
                null
        );

        Donacion donacion;
        if (dto.getEntidadBeneficiariaId() != null && dto.getNecesidadId() != null) {
            EntidadBeneficiaria entidad =
                    entidadBeneficiariaService.buscarEntidad(dto.getEntidadBeneficiariaId());
            Necesidad necesidad =
                    necesidadService.buscarDominioPorId(dto.getNecesidadId());
            donacion = new Donacion(siguienteId++, item, dto.getCantidadAsignada(),
                    necesidad, entidad);
        } else {
            donacion = new Donacion(siguienteId++, item, dto.getCantidadAsignada());
        }

        donaciones.add(donacion);
        return convertirADTO(donacion);
    }

    public List<DonacionDTO> obtenerTodas() {
        return donaciones.stream().map(this::convertirADTO).toList();
    }

    public DonacionDTO obtenerPorId(Long id) {
        return convertirADTO(obtenerDominioPorId(id));
    }

    public void eliminar(Long id) {
        donaciones.removeIf(d -> d.getId().equals(id));
    }

    public DonacionDTO cambiarEstado(Long id, CambioEstadoDTO dto) {
        Donacion donacion = obtenerDominioPorId(id);
        donacion.cambiarEstado(dto.getNuevoEstado(), dto.getJustificacion());
        return convertirADTO(donacion);
    }

    public List<TimeStampDTO> obtenerHistorial(Long id) {
        return obtenerDominioPorId(id).getHistorialEstados().stream()
                .map(this::convertirCambioEstadoADTO)
                .toList();
    }

    public List<Donacion> obtenerDonacionesEnDeposito() {
        return donaciones.stream()
                .filter(d -> d.getEstadoActual() == EstadoTrack.EN_DEPOSITO)
                .toList();
    }

    public Donacion obtenerDominioPorId(Long id) {
        return donaciones.stream()
                .filter(d -> d.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Donación no encontrada: " + id));
    }

    private DonacionDTO convertirADTO(Donacion donacion) {
        DonacionDTO dto = new DonacionDTO();
        dto.setId(donacion.getId());
        dto.setDescripcionItem(donacion.getItemDonado().getDescripcion());
        dto.setCantidadAsignada(donacion.getCantidadAsignada());
        dto.setEstadoActual(donacion.getEstadoActual());

        if (donacion.getEntidadBeneficiaria() != null) {
            dto.setEntidadBeneficiariaId(donacion.getEntidadBeneficiaria().getId());
        }
        if (donacion.getNecesidadAsignada() != null) {
            dto.setNecesidadId(donacion.getNecesidadAsignada().getId());
        }
        return dto;
    }

    private TimeStampDTO convertirCambioEstadoADTO(CambioEstado cambioEstado) {
        TimeStampDTO dto = new TimeStampDTO();
        dto.setEstado(cambioEstado.getEstadoNuevo());
        dto.setFecha(cambioEstado.getFechaCambio());
        dto.setJustificacion(cambioEstado.getJustificacion());
        return dto;
    }

    private static class NoSuchElementException extends RuntimeException {
        NoSuchElementException(String msg) { super(msg); }
    }
}
