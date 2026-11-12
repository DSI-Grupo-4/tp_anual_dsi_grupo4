package ar.edu.utn.frba.dds.web.logistica;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class VistaLogisticaTest {

    private final VistaLogistica vista = new VistaLogistica();

    @Test
    void soloSeEnlazanLasFotosConUrlHttpOHttps() {
        assertThat(vista.urlSegura("https://ejemplo.org/f.jpg")).isEqualTo("https://ejemplo.org/f.jpg");
        assertThat(vista.urlSegura("  HTTP://ejemplo.org/f.jpg ")).isEqualTo("HTTP://ejemplo.org/f.jpg");
        assertThat(vista.urlSegura("javascript:alert(1)")).isNull();
        assertThat(vista.urlSegura("data:text/html;base64,AAAA")).isNull();
        assertThat(vista.urlSegura("/ruta/relativa")).isNull();
        assertThat(vista.urlSegura(null)).isNull();
    }

    @Test
    void cadaEstadoTieneSuClaseYUnoDesconocidoNoRompe() {
        assertThat(vista.claseRuta("INICIADA")).isEqualTo("insignia-estado--advertencia");
        assertThat(vista.claseRuta("FINALIZADA")).isEqualTo("insignia-estado--exito");
        assertThat(vista.claseEntrega("NO_RECIBIDA")).isEqualTo("insignia-estado--error");
        assertThat(vista.claseEntrega("REPLANIFICABLE")).isEqualTo("insignia-estado--info");
        assertThat(vista.claseCamion("FUERA_DE_SERVICIO")).isEqualTo("insignia-estado--error");
        assertThat(vista.claseCamion("???")).isEmpty();
        assertThat(vista.claseRuta(null)).isEmpty();
    }
}
