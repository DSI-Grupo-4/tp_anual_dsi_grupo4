package ar.edu.utn.frba.dds.web.error;

/** Servicios de DonaTrack que consume el cliente web. El nombre visible se usa en los mensajes de error. */
public enum ServicioBackend {
    DONACIONES("Donaciones"),
    INCENTIVOS("Incentivos"),
    LOGISTICA("Logística"),
    /** Keycloak, cuando hay que pedir o renovar un token antes de llamar a una API. */
    AUTENTICACION("Autenticación");

    private final String nombreVisible;

    ServicioBackend(String nombreVisible) {
        this.nombreVisible = nombreVisible;
    }

    public String getNombreVisible() {
        return nombreVisible;
    }
}
