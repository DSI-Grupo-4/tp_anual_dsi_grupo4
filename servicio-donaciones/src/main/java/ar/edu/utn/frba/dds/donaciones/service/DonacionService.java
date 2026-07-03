package ar.edu.utn.frba.dds.donaciones.service;

import ar.edu.utn.frba.dds.donaciones.domain.donaciones.CambioEstado;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.Donacion;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.EstadoTrack;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.ItemDonado;
import ar.edu.utn.frba.dds.donaciones.domain.lugares.Ciudad;
import ar.edu.utn.frba.dds.donaciones.domain.lugares.Direccion;
import ar.edu.utn.frba.dds.donaciones.domain.lugares.Provincia;
import ar.edu.utn.frba.dds.donaciones.domain.necesidades.Necesidad;
import ar.edu.utn.frba.dds.donaciones.domain.personas.Donante;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import ar.edu.utn.frba.dds.donaciones.dto.CambioEstadoDTO;
import ar.edu.utn.frba.dds.donaciones.dto.CiudadDTO;
import ar.edu.utn.frba.dds.donaciones.dto.DireccionDTO;
import ar.edu.utn.frba.dds.donaciones.dto.DonacionDTO;
import ar.edu.utn.frba.dds.donaciones.dto.DonacionPendienteDTO;
import ar.edu.utn.frba.dds.donaciones.dto.ProvinciaDTO;
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
    private final DonanteService donanteService;

    public DonacionService(
            EntidadBeneficiariaService entidadBeneficiariaService,
            NecesidadService necesidadService,
            DonanteService donanteService) {
        this.entidadBeneficiariaService = entidadBeneficiariaService;
        this.necesidadService = necesidadService;
        this.donanteService = donanteService;
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
        item.setPesoKg(dto.getPesoKg());
        item.setVolumenM3(dto.getVolumenM3());
        item.setAlturaM(dto.getAlturaM());

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

        if (dto.getDonanteId() != null) {
            Donante donante = donanteService.obtenerDominioPorId(dto.getDonanteId());
            donacion.setDonante(donante);
            // Registra la actividad del donante y resetea el flag de
            // inactividad (ver InactividadScheduler).
            donante.agregarDonacion(donacion);
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

    public List<DonacionPendienteDTO> obtenerPendientes(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("page debe ser >= 0");
        }
        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("size debe estar entre 1 y 100");
        }

        int skip = page * size;

        return donaciones.stream()
                .filter(this::estaPendienteDePlanificacion)
                .skip(skip)
                .limit(size)
                .map(this::convertirAPendienteDTO)
                .toList();
    }

    private boolean estaPendienteDePlanificacion(Donacion donacion) {
        return donacion.getEstadoActual() == EstadoTrack.ASIGNACION_REALIZADA
                && donacion.getEntidadBeneficiaria() != null;
    }

    private DonacionPendienteDTO convertirAPendienteDTO(Donacion donacion) {
        DonacionPendienteDTO dto = new DonacionPendienteDTO();
        dto.setIdDonacion(donacion.getId().intValue());
        dto.setEntidadBeneficiariaAsociadaID(
                donacion.getEntidadBeneficiaria().getId().intValue());
        dto.setDireccionDestino(
                convertirDireccionADTO(donacion.getEntidadBeneficiaria().getDireccion()));

        ItemDonado item = donacion.getItemDonado();
        if (item != null) {
            dto.setPesoKG(item.getPesoKg());
            dto.setVolumenM3(item.getVolumenM3());
            dto.setAlturaM(item.getAlturaM());
        }
        return dto;
    }

    private DireccionDTO convertirDireccionADTO(Direccion direccion) {
        if (direccion == null) {
            return null;
        }

        DireccionDTO dto = new DireccionDTO();
        dto.setCalle(direccion.getCalle());
        dto.setNumero(direccion.getNumero());

        if (direccion.getCiudad() != null) {
            CiudadDTO ciudadDTO = new CiudadDTO();
            ciudadDTO.setNombre(direccion.getCiudad().getNombre());

            if (direccion.getCiudad().getProvincia() != null) {
                ProvinciaDTO provinciaDTO = new ProvinciaDTO();
                provinciaDTO.setNombre(direccion.getCiudad().getProvincia().getNombre());
                ciudadDTO.setProvincia(provinciaDTO);
            }

            dto.setCiudad(ciudadDTO);
        }

        return dto;
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
        if (donacion.getDonante() != null) {
            dto.setDonanteId(donacion.getDonante().getId());
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
