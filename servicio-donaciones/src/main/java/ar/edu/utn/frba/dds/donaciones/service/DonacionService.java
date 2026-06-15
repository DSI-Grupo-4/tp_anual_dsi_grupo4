package ar.edu.utn.frba.dds.donaciones.service;

import ar.edu.utn.frba.dds.donaciones.domain.donaciones.Donacion;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.EstadoDonacion;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.ItemDonado;
import ar.edu.utn.frba.dds.donaciones.domain.necesidades.Necesidad;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import ar.edu.utn.frba.dds.donaciones.dto.DonacionDTO;
import ar.edu.utn.frba.dds.donaciones.dto.TimeStampDTO;
import org.springframework.stereotype.Service;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.GestorDonaciones;

import java.util.ArrayList;
import java.util.List;

@Service
public class DonacionService {
    private List<Donacion> donaciones = new ArrayList<>();

    private Long siguienteId = 1L;

    private final GestorDonaciones gestorDonaciones;

    public DonacionService(GestorDonaciones gestorDonaciones) {
        this.gestorDonaciones = gestorDonaciones;
    }

    public Donacion crear(DonacionDTO dto) {

        ItemDonado item = buscarItem(dto.getItemId());
        Necesidad necesidad = buscarNecesidad(dto.getNecesidadId());
        EntidadBeneficiaria entidad = buscarEntidad(dto.getEntidadId());

        Donacion donacion = gestorDonaciones.asignarDonacion(
                siguienteId++,
                item,
                necesidad,
                dto.getCantidad()
        );

        donaciones.add(donacion);
        return donacion;
    }

    private ItemDonado buscarItem(Long id) {
        return gestorDonaciones.getDeposito()
                .itemsDisponibles()
                .stream()
                .filter(i -> i.getId().equals(id))
                .findFirst()
                .orElseThrow();
    }

    private Necesidad buscarNecesidad(Long id) {
        return gestorDonaciones.getNecesidades()
                .stream()
                .filter(n -> n.getId().equals(id))
                .findFirst()
                .orElseThrow();
    }

    private EntidadBeneficiaria buscarEntidad(Long id) {
        return gestorDonaciones.getEntidades()
                .stream()
                .filter(e -> e.getId().equals(id))
                .findFirst()
                .orElseThrow();
    }


    public List<Donacion> obtenerTodas() {
        return donaciones;
    }

    public Donacion buscarPorId(Long id) {
        return donaciones.stream()
                .filter(d -> d.getId().equals(id))
                .findFirst()
                .orElseThrow();
    }

    public void eliminar(Long id) {
        donaciones.removeIf(
                d -> d.getId().equals(id)
        );
    }

    public Donacion cambiarEstado(Long id, EstadoDonacion estado, String motivo) {

        Donacion donacion = buscarPorId(id);

        switch (estado) {
            case ASIGNADA -> donacion.asignar();
            case LISTA_PARA_ENTREGAR -> donacion.listaParaEntregar();
            case EN_TRASLADO -> donacion.iniciarTraslado();
            case ENTREGADA -> donacion.entregar();
            case ENTREGA_FALLIDA -> donacion.marcarFallida(motivo);
            case VENCIDA -> donacion.vencer(motivo);
            default -> throw new IllegalArgumentException("Estado inválido");
        }

        return donacion;
    }

    public DonacionDTO toDTO(Donacion d) {

        DonacionDTO dto = new DonacionDTO();

        dto.setItemId(d.getItemDonado().getId());
        dto.setCantidad(d.getCantidadAsignada());
        dto.setNecesidadId(d.getNecesidadAsignada().getId());
        dto.setEntidadId(d.getEntidadBeneficiaria().getId());

        dto.setEstadoActual(d.getEstadoActual().name());
        dto.setFechaCreacion(d.getFechaCreacion());

        dto.setHistorial(
                d.getHistorialEstados().stream().map(ts -> {
                    TimeStampDTO t = new TimeStampDTO();
                    t.setEstado(ts.getEstado());
                    t.setFecha(ts.getFecha());
                    t.setJustificacion(ts.getJustificacion());
                    return t;
                }).toList()
        );

        return dto;
    }
}
