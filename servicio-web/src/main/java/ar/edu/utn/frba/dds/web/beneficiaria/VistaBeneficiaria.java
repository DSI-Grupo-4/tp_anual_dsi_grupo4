package ar.edu.utn.frba.dds.web.beneficiaria;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/** Helpers de presentación del portal de la entidad. Se usan en las plantillas como ${@vistaBeneficiaria.cantidad(x)}. */
@Component("vistaBeneficiaria")
public class VistaBeneficiaria {

    /** Cantidad sin ceros de más: 10.00 se ve como 10 y 10.50 como 10.5. */
    public String cantidad(BigDecimal valor) {
        if (valor == null) {
            return "—";
        }
        BigDecimal limpio = valor.stripTrailingZeros();
        return (limpio.scale() < 0 ? limpio.setScale(0) : limpio).toPlainString();
    }

    /**
     * Sufijo de la clase insignia-estado--X de un estado de donación. El color nunca es la única señal:
     * el texto del estado siempre se muestra.
     */
    public String claseEstado(String estado) {
        if (estado == null) {
            return "";
        }
        return switch (estado) {
            case "ENTREGADA" -> "insignia-estado--exito";
            case "EN_TRASLADO" -> "insignia-estado--advertencia";
            case "ENTREGA_FALLIDA" -> "insignia-estado--error";
            case "ASIGNACION_REALIZADA", "LISTA_PARA_ENTREGAR" -> "insignia-estado--marca";
            case "EN_DEPOSITO" -> "insignia-estado--info";
            default -> "";
        };
    }
}
