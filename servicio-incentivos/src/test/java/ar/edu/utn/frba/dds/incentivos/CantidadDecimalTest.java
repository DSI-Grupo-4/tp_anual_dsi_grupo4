package ar.edu.utn.frba.dds.incentivos;
import ar.edu.utn.frba.dds.incentivos.donacion.DatosDonacion;
import ar.edu.utn.frba.dds.incentivos.misiones.*;
import ar.edu.utn.frba.dds.incentivos.progreso.ProgresoMision;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.*;
class CantidadDecimalTest {
    @Test void noTruncaElProgresoNiCompletaAntesDelUmbral() {
        Mision.HabilDonador m = new Mision.HabilDonador("Aporte", new Insignia("Insignia","foto"),3);
        ProgresoMision.ProgresoHabilDonador p = new ProgresoMision.ProgresoHabilDonador(m);
        p.actualizarProgresoMision(new DatosDonacion(LocalDate.now(),"ALIMENTOS",new BigDecimal("2.5"),true,null));
        assertThat(p.obtenerProgresoActual()).isEqualByComparingTo("2.5");
        assertThat(p.distanciaRestante()).isEqualByComparingTo("0.5");
        assertThat(m.validarCumplimiento(p)).isFalse();
        p.actualizarProgresoMision(new DatosDonacion(LocalDate.now(),"ALIMENTOS",new BigDecimal("3.1"),true,null));
        assertThat(m.validarCumplimiento(p)).isTrue();
    }
}
