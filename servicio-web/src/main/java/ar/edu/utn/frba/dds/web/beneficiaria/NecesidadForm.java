package ar.edu.utn.frba.dds.web.beneficiaria;

import ar.edu.utn.frba.dds.web.client.donaciones.NecesidadApi;
import ar.edu.utn.frba.dds.web.client.donaciones.NecesidadRequest;
import java.math.BigDecimal;

/**
 * Datos del formulario de una necesidad. Se valida en NecesidadesController (no con anotaciones)
 * porque los campos obligatorios dependen del tipo: la periodicidad es de las recurrentes y el tipo
 * de situación, de las extraordinarias.
 */
public class NecesidadForm {

    public static final String RECURRENTE = "RECURRENTE";
    public static final String EXTRAORDINARIA = "EXTRAORDINARIA";

    private String tipo = RECURRENTE;
    private String descripcion;
    private String subcategoria;
    private String unidadMedida;
    private BigDecimal cantidadRequerida;
    private String periodicidad;
    private String tipoExtraordinario;

    public static NecesidadForm desde(NecesidadApi necesidad) {
        NecesidadForm form = new NecesidadForm();
        form.tipo = necesidad.recurrente() ? RECURRENTE : EXTRAORDINARIA;
        form.descripcion = necesidad.descripcion();
        form.subcategoria = necesidad.subcategoria();
        form.unidadMedida = necesidad.unidadMedida();
        form.cantidadRequerida = necesidad.cantidadRequerida();
        form.periodicidad = necesidad.periodicidad();
        form.tipoExtraordinario = necesidad.tipoExtraordinario();
        return form;
    }

    public boolean esRecurrente() {
        return !EXTRAORDINARIA.equals(tipo);
    }

    /** Arma el pedido para Donaciones; solo manda los campos propios del tipo. */
    public NecesidadRequest aPedido() {
        boolean recurrente = esRecurrente();
        return new NecesidadRequest(
                recurrente ? RECURRENTE : EXTRAORDINARIA,
                descripcion == null ? null : descripcion.trim(),
                subcategoria,
                unidadMedida,
                cantidadRequerida,
                recurrente ? periodicidad : null,
                recurrente ? null : tipoExtraordinario);
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = EXTRAORDINARIA.equals(tipo) ? EXTRAORDINARIA : RECURRENTE;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getSubcategoria() {
        return subcategoria;
    }

    public void setSubcategoria(String subcategoria) {
        this.subcategoria = subcategoria;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public BigDecimal getCantidadRequerida() {
        return cantidadRequerida;
    }

    public void setCantidadRequerida(BigDecimal cantidadRequerida) {
        this.cantidadRequerida = cantidadRequerida;
    }

    public String getPeriodicidad() {
        return periodicidad;
    }

    public void setPeriodicidad(String periodicidad) {
        this.periodicidad = periodicidad;
    }

    public String getTipoExtraordinario() {
        return tipoExtraordinario;
    }

    public void setTipoExtraordinario(String tipoExtraordinario) {
        this.tipoExtraordinario = tipoExtraordinario;
    }
}
