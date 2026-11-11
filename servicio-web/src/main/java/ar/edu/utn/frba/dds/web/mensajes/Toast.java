package ar.edu.utn.frba.dds.web.mensajes;

/** Mensaje no intrusivo que muestra el layout después de una acción. */
public record Toast(Tipo tipo, String texto) {

    public enum Tipo { EXITO, ERROR, ADVERTENCIA, INFO;

        public String getClase() {
            return name().toLowerCase();
        }
    }
}
