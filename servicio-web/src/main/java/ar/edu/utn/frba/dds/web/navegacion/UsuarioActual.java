package ar.edu.utn.frba.dds.web.navegacion;

/** Nombre visible en el encabezado privado. Lo arma NavegacionModelAdvice desde el usuario OIDC. */
public final class UsuarioActual {

    private final String nombre;

    public UsuarioActual(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }
}
