package ar.edu.utn.frba.dds.logistica.dto;

import ar.edu.utn.frba.dds.logistica.domain.rutas.Entrega;

import java.util.List;

public class SolicitudPlanificadorDTO {

    private List<Entrega> entregas;
    private String callbackUrl;

    public SolicitudPlanificadorDTO(List<Entrega> entregas, String callbackUrl) {
        this.entregas = entregas;
        this.callbackUrl = callbackUrl;
    }

    public List<Entrega> getEntregas() {
        return entregas;
    }

    public void setEntregas(List<Entrega> entregas) {
        this.entregas = entregas;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }
}

