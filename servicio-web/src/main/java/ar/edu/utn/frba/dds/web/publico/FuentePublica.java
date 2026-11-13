package ar.edu.utn.frba.dds.web.publico;

/**
 * Datos de las páginas públicas (landing y galería).
 *
 * La implementación actual ({@link FuentePublicaApis}) arma los datos a partir de los
 * listados completos de Logística y Donaciones. Es provisoria (D-041): cuando las
 * Personas 3 y 5 publiquen endpoints públicos de solo lectura, se reemplaza la
 * implementación y las vistas no cambian.
 */
public interface FuentePublica {

    ResumenPublico resumen();

    /** Fotos de entregas, de la más reciente a la más antigua. {@code pagina} empieza en 1. */
    PaginaFotos fotos(int pagina, int tamanio);
}
