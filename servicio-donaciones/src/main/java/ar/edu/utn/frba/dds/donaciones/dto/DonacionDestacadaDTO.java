package ar.edu.utn.frba.dds.donaciones.dto;
import ar.edu.utn.frba.dds.donaciones.domain.categorias.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
/** Contrato público: deliberadamente sin persona donante, dirección, contactos ni descripción libre. */
public record DonacionDestacadaDTO(Categoria categoria, Subcategoria subcategoria, BigDecimal cantidad,
                                    UnidadMedida unidadMedida, String entidad, LocalDateTime fechaEntrega) { }
