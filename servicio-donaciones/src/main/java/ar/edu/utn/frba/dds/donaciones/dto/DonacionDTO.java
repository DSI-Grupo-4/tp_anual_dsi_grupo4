package ar.edu.utn.frba.dds.donaciones.dto;

import ar.edu.utn.frba.dds.donaciones.domain.donaciones.EstadoTrack;
import lombok.Getter;
import java.math.BigDecimal;
import java.util.List;
import ar.edu.utn.frba.dds.donaciones.domain.categorias.*;
import lombok.Setter;

@Getter
@Setter
public class DonacionDTO {
    private Long id;
    private Long donanteId;
    private String descripcionItem;
    private BigDecimal cantidadAsignada;
    private Long solicitudOrigenId;
    private Categoria categoria;
    private Subcategoria subcategoria;
    private UnidadMedida unidadMedida;
    private List<ItemDonadoDTO> items;
    private EstadoTrack estadoActual;
    private Long entidadBeneficiariaId;
    private Long necesidadId;
    private Integer pesoKg;
    private Integer volumenM3;
    private Integer alturaM;
}
