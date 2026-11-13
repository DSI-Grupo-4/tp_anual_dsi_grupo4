package ar.edu.utn.frba.dds.web.beneficiaria;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class VistaBeneficiariaTest {

    private final VistaBeneficiaria vista = new VistaBeneficiaria();

    @Test
    void laCantidadNoMuestraCerosDeMas() {
        assertThat(vista.cantidad(new BigDecimal("10.00"))).isEqualTo("10");
        assertThat(vista.cantidad(new BigDecimal("10.50"))).isEqualTo("10.5");
        assertThat(vista.cantidad(new BigDecimal("1E+2"))).isEqualTo("100");
        assertThat(vista.cantidad(null)).isEqualTo("—");
    }

    @Test
    void cadaEstadoTieneSuClaseYUnEstadoDesconocidoNoRompe() {
        assertThat(vista.claseEstado("ENTREGADA")).isEqualTo("insignia-estado--exito");
        assertThat(vista.claseEstado("EN_TRASLADO")).isEqualTo("insignia-estado--advertencia");
        assertThat(vista.claseEstado("ENTREGA_FALLIDA")).isEqualTo("insignia-estado--error");
        assertThat(vista.claseEstado("VENCIDA")).isEmpty();
        assertThat(vista.claseEstado(null)).isEmpty();
    }
}
