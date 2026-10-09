package ar.edu.utn.donatrack.notificaciones.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ar.edu.utn.donatrack.notificaciones.enums.MedioComunicacion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Datos minimos que cualquier otro servicio debe enviar para solicitar el
 * envio de una notificacion.
 */
@Schema(example = """
{
  "mensaje": "Tu donacion fue asignada al Comedor Sonrisas",
  "medio": "EMAIL",
  "contacto": "pruebas@example.org",
  "servicioOrigen": "DONACIONES",
  "tipoEvento": "ASIGNACION_REALIZADA",
  "eventoId": "swagger-prueba-001"
}
""")
public class NotificacionRequestDTO {

    @NotBlank(message = "El mensaje no puede estar vacio")
    private String mensaje;

    @NotNull(message = "El medio de comunicacion es obligatorio (WHATSAPP, EMAIL o SMS)")
    private MedioComunicacion medio;

    @NotBlank(message = "El contacto del destinatario es obligatorio (email, telefono o numero de WhatsApp)")
    private String contacto;

    private String servicioOrigen;

    @NotBlank(message = "tipoEvento es obligatorio (trazabilidad y, a futuro, enrutamiento del mensaje)")
    private String tipoEvento;

    @NotBlank(message = "eventoId es obligatorio (permite deduplicar en reintentos de la cola)")
    private String eventoId;

    public NotificacionRequestDTO() {
    }

    public NotificacionRequestDTO(String mensaje, MedioComunicacion medio, String contacto, String servicioOrigen,
                                   String tipoEvento, String eventoId) {
        this.mensaje = mensaje;
        this.medio = medio;
        this.contacto = contacto;
        this.servicioOrigen = servicioOrigen;
        this.tipoEvento = tipoEvento;
        this.eventoId = eventoId;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public MedioComunicacion getMedio() {
        return medio;
    }

    public void setMedio(MedioComunicacion medio) {
        this.medio = medio;
    }

    public String getContacto() {
        return contacto;
    }

    public void setContacto(String contacto) {
        this.contacto = contacto;
    }

    public String getServicioOrigen() {
        return servicioOrigen;
    }

    public void setServicioOrigen(String servicioOrigen) {
        this.servicioOrigen = servicioOrigen;
    }

    public String getTipoEvento() {
        return tipoEvento;
    }

    public void setTipoEvento(String tipoEvento) {
        this.tipoEvento = tipoEvento;
    }

    public String getEventoId() {
        return eventoId;
    }

    public void setEventoId(String eventoId) {
        this.eventoId = eventoId;
    }
}
