package ar.edu.utn.donatrack.notificaciones.dto;

import ar.edu.utn.donatrack.notificaciones.enums.MedioComunicacion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * DTO que recibe el Servicio de Donaciones (o quien consuma los eventos de Logística)
 * para notificar el inicio de una ruta.
 *
 * Según el enunciado (Entrega 3): se notifica a todas las entidades beneficiarias
 * y a los donantes cuyas entregas formen parte de la ruta iniciada.
 * La notificación incluye un enlace al mapa interactivo.
 */
public class NotificacionInicioRutaDTO {

    @NotNull(message = "El id de ruta es obligatorio")
    private Long idRuta;

    @NotBlank(message = "El enlace al mapa es obligatorio")
    private String enlaceMapa;

    // Lista de destinatarios: cada uno tiene contacto y medio preferido
    @NotNull
    private List<DestinatarioDTO> destinatarios;

    public NotificacionInicioRutaDTO() {}

    public Long getIdRuta() { return idRuta; }
    public void setIdRuta(Long idRuta) { this.idRuta = idRuta; }

    public String getEnlaceMapa() { return enlaceMapa; }
    public void setEnlaceMapa(String enlaceMapa) { this.enlaceMapa = enlaceMapa; }

    public List<DestinatarioDTO> getDestinatarios() { return destinatarios; }
    public void setDestinatarios(List<DestinatarioDTO> destinatarios) { this.destinatarios = destinatarios; }

    /**
     * Un destinatario individual dentro del evento: puede ser donante o entidad beneficiaria.
     */
    public static class DestinatarioDTO {

        @NotBlank
        private String contacto;

        @NotNull
        private MedioComunicacion medio;

        // Identifica si es donante o entidad (para personalizar el mensaje)
        private String nombreDestinatario;

        public DestinatarioDTO() {}

        public String getContacto() { return contacto; }
        public void setContacto(String contacto) { this.contacto = contacto; }

        public MedioComunicacion getMedio() { return medio; }
        public void setMedio(MedioComunicacion medio) { this.medio = medio; }

        public String getNombreDestinatario() { return nombreDestinatario; }
        public void setNombreDestinatario(String nombreDestinatario) { this.nombreDestinatario = nombreDestinatario; }
    }
}
