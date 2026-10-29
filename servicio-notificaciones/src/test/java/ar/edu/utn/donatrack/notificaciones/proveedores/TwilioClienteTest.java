package ar.edu.utn.donatrack.notificaciones.proveedores;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TwilioClienteTest {

    @Test
    void normalizaElFormatoDelCsvAE164() {
        assertThat(TwilioCliente.aE164("+54 11 5181-9600")).isEqualTo("+541151819600");
    }

    @Test
    void asumeArgentinaSiNoHayCodigoDePais() {
        assertThat(TwilioCliente.aE164("11 5181-9600")).isEqualTo("+541151819600");
    }

    @Test
    void convierteElPrefijoInternacional00() {
        assertThat(TwilioCliente.aE164("0054 9 11 5181 9600")).isEqualTo("+5491151819600");
    }

    @Test
    void sinCredencialesQuedaDeshabilitado() {
        assertThat(new TwilioCliente("", "", "", "+14155238886").estaHabilitado()).isFalse();
    }

    @Test
    void conCredencialesPeroSinNumeroSmsNoPuedeEnviarSms() {
        var twilio = new TwilioCliente("ACtest", "token-falso", "", "+14155238886");
        assertThat(twilio.estaHabilitado()).isTrue();
        assertThat(twilio.puedeEnviarSms()).isFalse();
    }

    @Test
    void lasVariablesDeLaPlantillaLlevanElMensajeEnUnaSolaLinea() {
        String json = TwilioCliente.variablesPlantilla("Tu donación\nfue asignada");
        assertThat(json).startsWith("{\"1\":\"Tu donación fue asignada\",\"2\":\"");
    }
    @Test
    void enModoPruebaElSmsUsaLaPlantillaYSinoElTexto() {
        var prueba = new TwilioCliente("ACtest", "token", "+17372508034", "+17372508034",
                "", "sms_appointment_reminders", false);
        var normal = new TwilioCliente("ACtest", "token", "+17372508034", "+17372508034");

        assertThat(prueba.cuerpoSms("Tu donación fue asignada")).isEqualTo("sms_appointment_reminders");
        assertThat(normal.cuerpoSms("Tu donación fue asignada")).isEqualTo("Tu donación fue asignada");
        assertThat(prueba.puedeEnviarWhatsApp()).isFalse();
    }
}
