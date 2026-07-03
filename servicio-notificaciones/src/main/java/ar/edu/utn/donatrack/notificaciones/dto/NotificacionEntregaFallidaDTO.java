package ar.edu.utn.donatrack.notificaciones.dto;

import ar.edu.utn.donatrack.notificaciones.enums.MedioComunicacion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * DTO para notificar que una entrega no pudo concretarse.
 *
 * Según el enunciado (Entrega 3): se notifica a la entidad beneficiaria,
 * al donante y a las personas administradoras del sistema.
 * Incluye el motivo del fallo y si la donación puede replanificarse.
 */
public class NotificacionEntregaFallidaDTO {

    @NotNull
    private Long idEntrega;

    @NotNull
    private Long idDonacion;

    @NotBlank(message = "El motivo es obligatorio")
    private String motivo;

    private boolean replanificable;

    // Donante
    @NotBlank
    private String contactoDonante;
    @NotNull
    private MedioComunicacion medioDonante;

    // Entidad beneficiaria
    @NotBlank
    private String contactoEntidadBeneficiaria;
    @NotNull
    private MedioComunicacion medioEntidadBeneficiaria;

    // Administradoras (puede haber varias)
    private List<AdministradoraDTO> administradoras;

    public NotificacionEntregaFallidaDTO() {}

    public Long getIdEntrega() { return idEntrega; }
    public void setIdEntrega(Long idEntrega) { this.idEntrega = idEntrega; }

    public Long getIdDonacion() { return idDonacion; }
    public void setIdDonacion(Long idDonacion) { this.idDonacion = idDonacion; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public boolean isReplanificable() { return replanificable; }
    public void setReplanificable(boolean replanificable) { this.replanificable = replanificable; }

    public String getContactoDonante() { return contactoDonante; }
    public void setContactoDonante(String contactoDonante) { this.contactoDonante = contactoDonante; }

    public MedioComunicacion getMedioDonante() { return medioDonante; }
    public void setMedioDonante(MedioComunicacion medioDonante) { this.medioDonante = medioDonante; }

    public String getContactoEntidadBeneficiaria() { return contactoEntidadBeneficiaria; }
    public void setContactoEntidadBeneficiaria(String c) { this.contactoEntidadBeneficiaria = c; }

    public MedioComunicacion getMedioEntidadBeneficiaria() { return medioEntidadBeneficiaria; }
    public void setMedioEntidadBeneficiaria(MedioComunicacion m) { this.medioEntidadBeneficiaria = m; }

    public List<AdministradoraDTO> getAdministradoras() { return administradoras; }
    public void setAdministradoras(List<AdministradoraDTO> administradoras) { this.administradoras = administradoras; }

    public static class AdministradoraDTO {
        private String contacto;
        private MedioComunicacion medio;

        public AdministradoraDTO() {}

        public String getContacto() { return contacto; }
        public void setContacto(String contacto) { this.contacto = contacto; }

        public MedioComunicacion getMedio() { return medio; }
        public void setMedio(MedioComunicacion medio) { this.medio = medio; }
    }
}
