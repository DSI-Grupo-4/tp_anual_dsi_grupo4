package ar.edu.utn.frba.dds.donaciones.service;

import ar.edu.utn.frba.dds.donaciones.domain.categorias.Subcategoria;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.Donacion;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.GestorDonaciones;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.ItemDonado;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.ResultadoMatchmaking;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import ar.edu.utn.frba.dds.donaciones.dto.EntidadCandidataDTO;
import ar.edu.utn.frba.dds.donaciones.dto.ResultadoMatchmakingDTO;
import ar.edu.utn.frba.dds.donaciones.dto.SolicitudAsignacionDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AsignacionService {

    private final EntidadBeneficiariaService entidadBeneficiariaService;
    private final GestorDonaciones gestorDonaciones = new GestorDonaciones();

    public AsignacionService(EntidadBeneficiariaService entidadBeneficiariaService) {
        this.entidadBeneficiariaService = entidadBeneficiariaService;
    }

    public ResultadoMatchmakingDTO obtenerCandidatas(SolicitudAsignacionDTO dto) {
        ItemDonado item = new ItemDonado(
                null,
                dto.getDescripcionItem(),
                new Subcategoria(dto.getSubcategoria()),
                dto.getCantidad(),
                null,
                null
        );

        Donacion donacionProxy = new Donacion(null, item, dto.getCantidad());

        ResultadoMatchmaking resultado = gestorDonaciones.ejecutarMatchmaking(
                donacionProxy,
                entidadBeneficiariaService.obtenerEntidadesDominio()
        );

        return convertirADTO(resultado, item);
    }

    private ResultadoMatchmakingDTO convertirADTO(
            ResultadoMatchmaking resultado,
            ItemDonado item) {

        ResultadoMatchmakingDTO dto = new ResultadoMatchmakingDTO();
        dto.setPorCompatibilidad(convertirEntidades(resultado.getPorCompatibilidad(), item));
        dto.setPorSubatencion(convertirEntidades(resultado.getPorSubatencion(), item));
        dto.setInterseccion(convertirEntidades(resultado.getInterseccion(), item));
        return dto;
    }

    private List<EntidadCandidataDTO> convertirEntidades(
            List<EntidadBeneficiaria> entidades,
            ItemDonado item) {

        return entidades.stream()
                .map(entidad -> convertirADTO(entidad, item))
                .toList();
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
