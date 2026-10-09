package ar.edu.utn.frba.dds.incentivos;
import ar.edu.utn.frba.dds.incentivos.donante.Donante;
import ar.edu.utn.frba.dds.incentivos.donacion.DatosDonacion;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;
class ActividadIdentificadaTest {
 @Test void altaYEntregaNoDuplicanBienesYActualizanContacto() {
    var donante=new Donante(9L);
    var alta=new DatosDonacion(LocalDate.now(),"ALIMENTOS",new BigDecimal("2.5"),false,null);alta.setDonacionId(1L);
    assertThat(donante.registrarActividadDonacion(alta)).isNotNull();
    assertThat(donante.registrarActividadDonacion(alta)).isNull();
    var entrega=new DatosDonacion(LocalDate.now(),"ALIMENTOS",new BigDecimal("2.5"),true,null);entrega.setDonacionId(1L);
    donante.registrarActividadDonacion(entrega);donante.registrarActividadDonacion(entrega);
    assertThat(donante.getHistorialDonaciones()).hasSize(1);assertThat(donante.getSolicitudesDonacionHechas()).isEqualTo(1);
    assertThat(donante.getHistorialDonaciones().get(0).isDonacionExitosa()).isTrue();
    assertThat(donante.getHistorialDonaciones().get(0).getCantidadBienes()).isEqualByComparingTo("2.5");
    donante.actualizarContactoSiFalta("EMAIL","viejo@example.org");donante.actualizarContactoSiFalta("WHATSAPP","3515551234");
    assertThat(donante.getMedioContactoPreferido()).isEqualTo("WHATSAPP");assertThat(donante.getContactoPreferido()).isEqualTo("3515551234");
 }
}
