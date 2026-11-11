package ar.edu.utn.frba.dds.web.error;

/** Servicios de DonaTrack que consume el cliente web. El nombre visible se usa en los mensajes de error. */
public enum ServicioBackend {
    DONACIONES("Donaciones"),
    INCENTIVOS("Incentivos"),
    LOGISTICA("Logística");

    private final String nombreVisible;

    ServicioBackend(String nombreVisible) {
        this.nombreVisible = nombreVisible;
    }

    public String getNombreVisible() {
        return nombreVisible;
    }
}
