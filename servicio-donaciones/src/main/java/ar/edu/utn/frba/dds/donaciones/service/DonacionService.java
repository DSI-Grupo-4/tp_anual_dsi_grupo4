package ar.edu.utn.frba.dds.donaciones.service;

import ar.edu.utn.frba.dds.donaciones.domain.donaciones.Donacion;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.ItemDonado;
import ar.edu.utn.frba.dds.donaciones.domain.necesidades.Necesidad;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import ar.edu.utn.frba.dds.donaciones.dto.DonacionDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DonacionService {
    private List<Donacion> donaciones = new ArrayList<>();

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
        EntidadBeneficiaria entidad =
                entidadBeneficiariaService.buscarEntidad(dto.getEntidadBeneficiariaId());

        Necesidad necesidad =
                necesidadService.buscarDominioPorId(dto.getNecesidadId());

        ItemDonado item =
                new ItemDonado(
                        dto.getId(),
                        dto.getDescripcionItem(),
                        null,
                        dto.getCantidadAsignada(),
                        null,
                        null
                );
        Donacion donacion =
                new Donacion(
                        siguienteId++,
                        item,
                        dto.getCantidadAsignada(),
                        necesidad,
                        entidad
                );

        donaciones.add(donacion);

        return convertirADTO(donacion);
    }

    public List<DonacionDTO> obtenerTodas() {

        return donaciones.stream()
                .map(this::convertirADTO)
                .toList();
    }

    public DonacionDTO obtenerPorId(Long id) {

        Donacion donacion =
                donaciones.stream()
                        .filter(d -> d.getId().equals(id))
                        .findFirst()
                        .orElseThrow();

        return convertirADTO(donacion);
    }

    public void eliminar(Long id) {

        donaciones.removeIf(
                d -> d.getId().equals(id)
        );
    }

    private DonacionDTO convertirADTO(
            Donacion donacion) {

        DonacionDTO dto =
                new DonacionDTO();

        dto.setId(
                donacion.getId()
        );

        dto.setDescripcionItem(
                donacion.getItemDonado()
                        .getDescripcion()
        );

        dto.setCantidadAsignada(
                donacion.getCantidadAsignada()
        );

        dto.setEstadoActual(
                donacion.getEstadoActual()
        );

        if(donacion.getEntidadBeneficiaria() != null) {

            dto.setEntidadBeneficiariaId(
                    donacion.getEntidadBeneficiaria()
                            .getId()
            );
        }

        if(donacion.getNecesidadAsignada() != null) {

            dto.setNecesidadId(
                    donacion.getNecesidadAsignada()
                            .getId()
            );
        }

        return dto;
    }
}
