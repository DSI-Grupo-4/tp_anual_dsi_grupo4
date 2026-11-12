package ar.edu.utn.frba.dds.logistica.domain.geo;

/** Cálculos de distancia y de movimiento sobre el mapa. Sin dependencias: se prueba solo. */
public final class Geo {

    private static final double RADIO_TIERRA_KM = 6371.0;
    /** Ancho en grados (≈ 20 km) del cuadrado alrededor del depósito donde caen los puntos de ejemplo. */
    private static final double ANCHO_PUNTOS_DE_EJEMPLO = 0.18;

    private Geo() {
    }

    /** Distancia en kilómetros sobre la superficie terrestre (fórmula de haversine). */
    public static double distanciaKm(Punto a, Punto b) {
        double dLat = Math.toRadians(b.latitud() - a.latitud());
        double dLng = Math.toRadians(b.longitud() - a.longitud());
        double h = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(a.latitud())) * Math.cos(Math.toRadians(b.latitud()))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return 2 * RADIO_TIERRA_KM * Math.asin(Math.min(1.0, Math.sqrt(h)));
    }

    /**
     * Punto que está a {@code fraccion} (0 a 1) del camino de origen a destino, en línea recta.
     * Para los trayectos urbanos de la demo la línea recta en grados es una aproximación suficiente.
     */
    public static Punto avanzar(Punto origen, Punto destino, double fraccion) {
        double f = Math.max(0.0, Math.min(1.0, fraccion));
        return new Punto(
                origen.latitud() + (destino.latitud() - origen.latitud()) * f,
                origen.longitud() + (destino.longitud() - origen.longitud()) * f);
    }

    /**
     * Punto de ejemplo para una entidad que todavía no tiene coordenadas: siempre el mismo para la misma
     * entidad, repartido en un cuadrado de ≈ 20 km alrededor del depósito. Es solo para la demo.
     */
    public static Punto puntoDeEjemplo(Integer idEntidad, Punto deposito) {
        int semilla = idEntidad == null ? 0 : idEntidad;
        int h = Math.floorMod(semilla * 2_654_435_761L, 1_000_003L) > 0
                ? (int) Math.floorMod(semilla * 2_654_435_761L, 1_000_003L) : 1;
        double x = (h % 1000) / 1000.0 - 0.5;
        double y = ((h / 1000) % 1000) / 1000.0 - 0.5;
        return new Punto(deposito.latitud() + x * ANCHO_PUNTOS_DE_EJEMPLO, deposito.longitud() + y * ANCHO_PUNTOS_DE_EJEMPLO);
    }
}
