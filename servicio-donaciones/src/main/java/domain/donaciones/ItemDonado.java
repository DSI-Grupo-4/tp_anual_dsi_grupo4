package domain.donaciones;

import domain.categorias.Categoria;

public class ItemDonado {
    private String descripcion;
    private Categoria categoria;
    private Integer cantidad;
    private UnidadMedida unidadMedida;
    private String foto;

    public ItemDonado(String descripcion, Categoria categoria, Integer cantidad, UnidadMedida unidadMedida, String foto) {
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.cantidad = cantidad;
        this.unidadMedida = unidadMedida;
        this.foto = foto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public UnidadMedida getUnidadMedida() {
        return unidadMedida;
    }

    public String getFoto() {
        return foto;
    }
}
