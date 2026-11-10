package ar.edu.utn.frba.dds.web.mapa;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Configuración del componente de mapa (donatrack.web.mapa.*). En las vistas: ${@mapa.tilesUrl}. */
@Component("mapa")
public class MapaConfig {

    private final String tilesUrl;
    private final String atribucion;
    private final double centroLat;
    private final double centroLng;
    private final int zoom;

    public MapaConfig(@Value("${donatrack.web.mapa.tiles-url}") String tilesUrl,
                      @Value("${donatrack.web.mapa.atribucion}") String atribucion,
                      @Value("${donatrack.web.mapa.centro-lat}") double centroLat,
                      @Value("${donatrack.web.mapa.centro-lng}") double centroLng,
                      @Value("${donatrack.web.mapa.zoom}") int zoom) {
        this.tilesUrl = tilesUrl;
        this.atribucion = atribucion;
        this.centroLat = centroLat;
        this.centroLng = centroLng;
        this.zoom = zoom;
    }

    public String getTilesUrl() { return tilesUrl; }
    public String getAtribucion() { return atribucion; }
    public double getCentroLat() { return centroLat; }
    public double getCentroLng() { return centroLng; }
    public int getZoom() { return zoom; }
}
