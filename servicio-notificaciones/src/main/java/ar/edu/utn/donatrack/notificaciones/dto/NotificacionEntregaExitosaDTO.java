package ar.edu.utn.donatrack.notificaciones.dto;

import ar.edu.utn.donatrack.notificaciones.enums.MedioComunicacion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * DTO para notificar que una entrega fue realizada con éxito.
 *
 * Según el enunciado (Entrega 3): se notifica tanto a la entidad beneficiaria
 * como al donante correspondiente. La notificación incluye un comprobante
 * con fecha, hora y camión responsable.
 */
public class NotificacionEntregaExitosaDTO {

    @NotNull
    private Long idEntrega;

    @NotNull
    private Long idDonacion;

    // Datos del comprobante
    @NotBlank(message = "La patente del camión es obligatoria")
    private String patenteCamion;

    @NotNull(message = "La fecha y hora de entrega es obligatoria")
    private LocalDateTime fechaHoraEntrega;

    private String observacion;

    // Datos del donante
    @NotBlank
    private String contactoDonante;
    @NotNull
    private MedioComunicacion medioDonante;

    // Datos de la entidad beneficiaria
    @NotBlank
    private String contactoEntidadBeneficiaria;
    @NotNull
    private MedioComunicacion medioEntidadBeneficiaria;

    public NotificacionEntregaExitosaDTO() {}

    public Long getIdEntrega() { return idEntrega; }
    public void setIdEntrega(Long idEntrega) { this.idEntrega = idEntrega; }

    public Long getIdDonacion() { return idDonacion; }
    public void setIdDonacion(Long idDonacion) { this.idDonacion = idDonacion; }

    public String getPatenteCamion() { return patenteCamion; }
    public void setPatenteCamion(String patenteCamion) { this.patenteCamion = patenteCamion; }

    public LocalDateTime getFechaHoraEntrega() { return fechaHoraEntrega; }
    public void setFechaHoraEntrega(LocalDateTime fechaHoraEntrega) { this.fechaHoraEntrega = fechaHoraEntrega; }

    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }

    public String getContactoDonante() { return contactoDonante; }
    public void setContactoDonante(String contactoDonante) { this.contactoDonante = contactoDonante; }

    public MedioComunicacion getMedioDonante() { return medioDonante; }
    public void setMedioDonante(MedioComunicacion medioDonante) { this.medioDonante = medioDonante; }

    public String getContactoEntidadBeneficiaria() { return contactoEntidadBeneficiaria; }
    public void setContactoEntidadBeneficiaria(String c) { this.contactoEntidadBeneficiaria = c; }

    public MedioComunicacion getMedioEntidadBeneficiaria() { return medioEntidadBeneficiaria; }
    public void setMedioEntidadBeneficiaria(MedioComunicacion m) { this.medioEntidadBeneficiaria = m; }
}
