package ar.edu.utn.frba.dds.donaciones.service;

import ar.edu.utn.frba.dds.donaciones.domain.categorias.*;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.Donacion;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.GestorDonaciones;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.ItemDonado;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.ResultadoMatchmaking;
import ar.edu.utn.frba.dds.donaciones.dto.ResultadoMatchmakingDTO;
import ar.edu.utn.frba.dds.donaciones.dto.SolicitudAsignacionDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AsignacionService {

    private final EntidadBeneficiariaService entidadBeneficiariaService;
    private final GestorDonaciones gestorDonaciones;

    public AsignacionService(
            EntidadBeneficiariaService entidadBeneficiariaService,
            GestorDonaciones gestorDonaciones) {
        this.entidadBeneficiariaService = entidadBeneficiariaService;
        this.gestorDonaciones = gestorDonaciones;
    }

    public ResultadoMatchmakingDTO obtenerCandidatas(SolicitudAsignacionDTO dto) {
        ItemDonado item = new ItemDonado(
                null,
                dto.getDescripcion(), dto.getCategoria(), dto.getSubcategoria(),
                dto.getUnidadMedida(), dto.getCantidad(), dto.getFoto(),
                dto.getFechaVencimiento() == null ? null : new Perecedero(dto.getFechaVencimiento()),
                dto.getCondicion() == null ? null : new ConEstado(dto.getCondicion()),
                dto.getPesoKg(), dto.getVolumenM3(), dto.getAlturaM()
        );

        Donacion donacionProxy = new Donacion(null, List.of(item), null);

        ResultadoMatchmaking resultado = gestorDonaciones.ejecutarMatchmaking(
                donacionProxy,
                entidadBeneficiariaService.obtenerEntidadesDominio()
        );

        return entidadBeneficiariaService.convertirResultadoADTO(
                resultado, item.getSubcategoria(), item.getUnidadMedida());
    }
}
