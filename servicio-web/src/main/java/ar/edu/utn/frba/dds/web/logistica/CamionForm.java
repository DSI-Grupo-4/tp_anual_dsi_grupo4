package ar.edu.utn.frba.dds.web.logistica;

import ar.edu.utn.frba.dds.web.client.logistica.CamionApi;
import ar.edu.utn.frba.dds.web.client.logistica.CamionRequest;

/** Datos del formulario de un camión. Se valida en CamionesAdminController. */
public class CamionForm {

    private String patente;
    private Integer capacidadCargaKg;
    private Integer capacidadVolumenM3;
    private Integer alturaM;
    private String estadoCamion;

    public static CamionForm desde(CamionApi camion) {
        CamionForm form = new CamionForm();
        form.patente = camion.patente();
        form.capacidadCargaKg = camion.capacidadCargaKg();
        form.capacidadVolumenM3 = camion.capacidadVolumenM3();
        form.alturaM = camion.alturaM();
        form.estadoCamion = camion.estadoCamion();
        return form;
    }

    /** La patente se guarda en mayúsculas y sin espacios, para que AB 123 CD y ab123cd sean la misma. */
    public CamionRequest aPedido(boolean estadoEditable) {
        String normalizada = patente == null ? null : patente.replaceAll("\\s+", "").toUpperCase();
        return new CamionRequest(normalizada, capacidadVolumenM3, alturaM, capacidadCargaKg,
                estadoEditable && estadoCamion != null && !estadoCamion.isBlank() ? estadoCamion : null);
    }

    public String getPatente() {
        return patente;
    }

    public void setPatente(String patente) {
        this.patente = patente;
    }

    public Integer getCapacidadCargaKg() {
        return capacidadCargaKg;
    }

    public void setCapacidadCargaKg(Integer capacidadCargaKg) {
        this.capacidadCargaKg = capacidadCargaKg;
    }

    public Integer getCapacidadVolumenM3() {
        return capacidadVolumenM3;
    }

    public void setCapacidadVolumenM3(Integer capacidadVolumenM3) {
        this.capacidadVolumenM3 = capacidadVolumenM3;
    }

    public Integer getAlturaM() {
        return alturaM;
    }

    public void setAlturaM(Integer alturaM) {
        this.alturaM = alturaM;
    }

    public String getEstadoCamion() {
        return estadoCamion;
    }

    public void setEstadoCamion(String estadoCamion) {
        this.estadoCamion = estadoCamion;
    }
}
