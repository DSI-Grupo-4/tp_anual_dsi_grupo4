package ar.edu.utn.frba.dds.web.navegacion;

/**
 * Elemento de las migas de pan. La etiqueta es una clave de messages.properties
 * o, si {@code literal} es true, el texto tal cual (por ejemplo, el nombre de una entidad).
 * La última miga es la página actual y no lleva ruta.
 */
public record Miga(String etiqueta, String ruta, boolean literal) {

    public static Miga clave(String clave, String ruta) {
        return new Miga(clave, ruta, false);
    }

    public static Miga texto(String texto, String ruta) {
        return new Miga(texto, ruta, true);
    }
}
