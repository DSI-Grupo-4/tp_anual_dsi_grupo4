package ar.edu.utn.frba.dds.donaciones.service;

import ar.edu.utn.frba.dds.donaciones.domain.algoritmos.AlgoritmoAsignacion;
import ar.edu.utn.frba.dds.donaciones.domain.algoritmos.CompatibilidadSemantica;
import ar.edu.utn.frba.dds.donaciones.domain.algoritmos.PrioridadSubatendidos;
import ar.edu.utn.frba.dds.donaciones.domain.categorias.Subcategoria;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.Donacion;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.ItemDonado;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import ar.edu.utn.frba.dds.donaciones.dto.EntidadCandidataDTO;
import ar.edu.utn.frba.dds.donaciones.dto.SolicitudAsignacionDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AsignacionService {

    private final EntidadBeneficiariaService entidadBeneficiariaService;

    public AsignacionService(EntidadBeneficiariaService entidadBeneficiariaService) {
        this.entidadBeneficiariaService = entidadBeneficiariaService;
    }

    public List<EntidadCandidataDTO> obtenerCandidatas(SolicitudAsignacionDTO dto) {
        ItemDonado item = new ItemDonado(
                null,
                dto.getDescripcionItem(),
                new Subcategoria(dto.getSubcategoria()),
                dto.getCantidad(),
                null,
                null
        );

        Donacion donacionProxy = new Donacion(null, item, dto.getCantidad());

        AlgoritmoAsignacion algoritmo = seleccionarAlgoritmo(dto.getAlgoritmo());

        return algoritmo.ejecutarAlgoritmo(
                        donacionProxy,
                        entidadBeneficiariaService.obtenerEntidadesDominio()
                )
                .stream()
                .map(entidad -> convertirADTO(entidad, item))
                .toList();
    }

    private AlgoritmoAsignacion seleccionarAlgoritmo(String algoritmo) {
        if ("PRIORIDAD_SUBATENDIDOS".equalsIgnoreCase(algoritmo)) {
            return new PrioridadSubatendidos();
        }
        return new CompatibilidadSemantica();
    }

    private EntidadCandidataDTO convertirADTO(
            EntidadBeneficiaria entidad,
            ItemDonado item) {

        EntidadCandidataDTO dto = new EntidadCandidataDTO();

        dto.setEntidadBeneficiariaId(entidad.getId());
        dto.setDescripcion(entidad.getDescripcion());

        if (entidad.getEntidad() != null) {
            dto.setRazonSocial(entidad.getEntidad().getRazonSocial());
        }

        dto.setPuntaje((int) entidad.getNecesidades().stream()
                .filter(n -> !n.satisfecha())
                .filter(n -> n.getSubcategoria() != null
                        && item.getSubcategoria() != null
                        && n.getSubcategoria().getNombre()
                        .equalsIgnoreCase(item.getSubcategoria().getNombre()))
                .count());

        return dto;
    }
}