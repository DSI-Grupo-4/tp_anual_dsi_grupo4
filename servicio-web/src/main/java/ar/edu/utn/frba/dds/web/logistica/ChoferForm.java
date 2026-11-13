package ar.edu.utn.frba.dds.web.logistica;

import ar.edu.utn.frba.dds.web.client.logistica.ChoferApi;
import ar.edu.utn.frba.dds.web.client.logistica.ChoferRequest;

/** Datos del formulario de un chofer. Se valida en ChoferesAdminController. */
public class ChoferForm {

    private String nombre;
    private Integer dni;
    private boolean habilitado = true;

    public static ChoferForm desde(ChoferApi chofer) {
        ChoferForm form = new ChoferForm();
        form.nombre = chofer.nombre();
        form.dni = chofer.dni();
        form.habilitado = Boolean.TRUE.equals(chofer.habilitado());
        return form;
    }

    public ChoferRequest aPedido() {
        String normalizado = nombre == null ? null : nombre.trim();
        return new ChoferRequest(normalizado, dni, habilitado);
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getDni() {
        return dni;
    }

    public void setDni(Integer dni) {
        this.dni = dni;
    }

    public boolean isHabilitado() {
        return habilitado;
    }

    public void setHabilitado(boolean habilitado) {
        this.habilitado = habilitado;
    }
}
