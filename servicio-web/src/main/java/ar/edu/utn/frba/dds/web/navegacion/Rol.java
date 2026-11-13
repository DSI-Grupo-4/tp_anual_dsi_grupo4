package ar.edu.utn.frba.dds.web.navegacion;

import java.util.Arrays;
import java.util.Optional;

/**
 * Roles de DonaTrack. Coinciden con los roles del realm de Keycloak (rama iam, D-039).
 * Cada rol tiene su zona de rutas: /admin/**, /donante/**, /beneficiaria/**.
 */
public enum Rol {
    ADMIN("admin"),
    DONANTE("donante"),
    BENEFICIARIA("beneficiaria");

    private final String zona;

    Rol(String zona) {
        this.zona = zona;
    }

    public String getZona() {
        return zona;
    }

    public String getRutaInicio() {
        return "/" + zona;
    }

    public static Optional<Rol> desdeZona(String zona) {
        return Arrays.stream(values()).filter(r -> r.zona.equals(zona)).findFirst();
    }
}
