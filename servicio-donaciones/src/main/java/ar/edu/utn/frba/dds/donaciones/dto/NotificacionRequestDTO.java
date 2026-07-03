package ar.edu.utn.frba.dds.donaciones.dto;

/**
 * Espejo del NotificacionRequestDTO expuesto por el Servicio de Notificaciones
 * (POST /api/notificaciones). Como cada servicio es un módulo/deploy
 * independiente, no se comparte la clase entre ambos: este DTO define el
 * "contrato" del lado del cliente (Servicio de Donaciones).
 *
 * medio se modela como String (y no como un enum propio) para no acoplar
 * este servicio al enum MedioComunicacion de Notificaciones; el valor debe
 * ser uno de "EMAIL", "SMS" o "WHATSAPP".
 */
public class NotificacionRequestDTO {

    private String mensaje;
    private String medio;
    private String contacto;
    private String servicioOrigen;

    public NotificacionRequestDTO() {
    }

    public NotificacionRequestDTO(String mensaje, String medio, String contacto, String servicioOrigen) {
        this.mensaje = mensaje;
        this.medio = medio;
        this.contacto = contacto;
        this.servicioOrigen = servicioOrigen;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getMedio() {
        return medio;
    }

    public void setMedio(String medio) {
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
}
