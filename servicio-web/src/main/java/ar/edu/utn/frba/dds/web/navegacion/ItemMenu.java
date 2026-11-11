package ar.edu.utn.frba.dds.web.navegacion;

/**
 * Entrada del menú lateral de una zona privada.
 *
 * @param clave   clave de messages.properties con la etiqueta visible
 * @param seccion segmento de ruta dentro de la zona ("" para el panel de inicio)
 * @param icono   id del símbolo en /img/iconos.svg
 */
public record ItemMenu(String clave, String seccion, String icono, String ruta) {

    static ItemMenu de(Rol rol, String clave, String seccion, String icono) {
        String ruta = seccion.isEmpty() ? rol.getRutaInicio() : rol.getRutaInicio() + "/" + seccion;
        return new ItemMenu(clave, seccion, icono, ruta);
    }

    public boolean esInicio() {
        return seccion.isEmpty();
    }

    /** El panel solo se marca activo en su ruta exacta; el resto, también en sus subrutas. */
    public boolean contiene(String ruta) {
        return esInicio() ? this.ruta.equals(ruta) : ruta.equals(this.ruta) || ruta.startsWith(this.ruta + "/");
    }
}
