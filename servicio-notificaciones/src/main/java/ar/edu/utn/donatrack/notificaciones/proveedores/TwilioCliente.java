package ar.edu.utn.donatrack.notificaciones.proveedores;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Encapsula la comunicación con Twilio. Los notificadores de SMS y WhatsApp
 * lo usan sin conocer los detalles de la API. Si no hay credenciales, queda
 * deshabilitado y los notificadores simulan el envío.
 *
 * Cuenta de prueba de Twilio: no permite texto libre. Con
 * twilio.sms-cuerpo-prueba configurado, el SMS se envía con esa plantilla de
 * Twilio en lugar del texto de DonaTrack. Con cuenta paga se deja vacío.
 */
@Component
public class TwilioCliente {

    private static final Logger logger = LoggerFactory.getLogger(TwilioCliente.class);
    private static final int MAX_VARIABLE = 200;

    private final boolean habilitado;
    private final String numeroSms;
    private final String numeroWhatsApp;
    private final String contentSidWhatsApp;
    private final String cuerpoSmsPrueba;
    private final boolean whatsAppActivo;

    @Autowired
    public TwilioCliente(@Value("${twilio.account-sid:}") String accountSid,
                         @Value("${twilio.auth-token:}") String authToken,
                         @Value("${twilio.numero-sms:}") String numeroSms,
                         @Value("${twilio.numero-whatsapp:+14155238886}") String numeroWhatsApp,
                         @Value("${twilio.whatsapp-content-sid:}") String contentSidWhatsApp,
                         @Value("${twilio.sms-cuerpo-prueba:}") String cuerpoSmsPrueba,
                         @Value("${twilio.whatsapp-activo:true}") boolean whatsAppActivo) {
        this.habilitado = !accountSid.isBlank() && !authToken.isBlank();
        this.numeroSms = numeroSms;
        this.numeroWhatsApp = numeroWhatsApp;
        this.contentSidWhatsApp = contentSidWhatsApp;
        this.cuerpoSmsPrueba = cuerpoSmsPrueba;
        this.whatsAppActivo = whatsAppActivo;
        if (habilitado) {
            Twilio.init(accountSid, authToken);
            logger.info("Twilio configurado: SMS {}; WhatsApp {}", descripcionSms(), descripcionWhatsApp());
        } else {
            logger.warn("Twilio sin credenciales: SMS y WhatsApp se simulan");
        }
    }

    // Para los tests: sin plantillas y con WhatsApp activo
    public TwilioCliente(String accountSid, String authToken, String numeroSms, String numeroWhatsApp) {
        this(accountSid, authToken, numeroSms, numeroWhatsApp, "", "", true);
    }

    public boolean estaHabilitado() {
        return habilitado;
    }

    public boolean puedeEnviarSms() {
        return habilitado && !numeroSms.isBlank();
    }

    public boolean puedeEnviarWhatsApp() {
        return habilitado && whatsAppActivo;
    }

    public String enviarSms(String destino, String texto) {
        Message mensaje = Message.creator(
                new PhoneNumber(aE164(destino)),
                new PhoneNumber(aE164(numeroSms)),
                cuerpoSms(texto)).create();
        return mensaje.getSid();
    }

    public String enviarWhatsApp(String destino, String texto) {
        PhoneNumber para = new PhoneNumber("whatsapp:" + aE164(destino));
        PhoneNumber desde = new PhoneNumber("whatsapp:" + aE164(numeroWhatsApp));

        if (contentSidWhatsApp.isBlank()) {
            return Message.creator(para, desde, texto).create().getSid();
        }
        // Con plantilla no se manda cuerpo: el texto sale de la plantilla + sus variables
        return Message.creator(para, desde, (String) null)
                .setContentSid(contentSidWhatsApp)
                .setContentVariables(variablesPlantilla(texto))
                .create()
                .getSid();
    }

    // En modo prueba se manda el nombre de la plantilla de Twilio; si no, el texto de DonaTrack
    String cuerpoSms(String texto) {
        return cuerpoSmsPrueba.isBlank() ? texto : cuerpoSmsPrueba;
    }

    /**
     * Arma las variables de una plantilla de WhatsApp en JSON: {"1": "<mensaje>", "2": "<hora>"}.
     * WhatsApp no acepta saltos de línea en las variables, por eso se aplana el texto.
     */
    static String variablesPlantilla(String texto) {
        String plano = texto.replaceAll("\\s+", " ").trim();
        if (plano.length() > MAX_VARIABLE) {
            plano = plano.substring(0, MAX_VARIABLE - 3) + "...";
        }
        Map<String, String> variables = new LinkedHashMap<>();
        variables.put("1", plano);
        variables.put("2", LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        try {
            return new ObjectMapper().writeValueAsString(variables);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudieron armar las variables de la plantilla", e);
        }
    }

    /**
     * Twilio exige formato E.164: "+" + código de país + número, sin espacios ni guiones.
     * "+54 11 5181-9600" -> "+541151819600". Sin código de país, se asume Argentina (+54).
     */
    public static String aE164(String numero) {
        String limpio = numero.trim().replaceAll("[^0-9+]", "");
        if (limpio.startsWith("00")) {
            limpio = "+" + limpio.substring(2);
        }
        if (!limpio.startsWith("+")) {
            limpio = "+54" + limpio;
        }
        return limpio;
    }

    private String descripcionSms() {
        if (numeroSms.isBlank()) return "simulado (sin número de Twilio para SMS)";
        return cuerpoSmsPrueba.isBlank() ? "real con texto libre" : "real en modo prueba (plantilla " + cuerpoSmsPrueba + ")";
    }

    private String descripcionWhatsApp() {
        if (!whatsAppActivo) return "simulado (desactivado por configuración)";
        return contentSidWhatsApp.isBlank() ? "real con texto libre" : "real con plantilla " + contentSidWhatsApp;
    }
}