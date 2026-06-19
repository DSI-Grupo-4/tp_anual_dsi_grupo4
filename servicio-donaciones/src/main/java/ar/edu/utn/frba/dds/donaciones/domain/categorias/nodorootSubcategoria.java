package ar.edu.utn.frba.dds.donaciones.domain.categorias;

public class nodorootSubcategoria extends SubCategoria {
    public static final nodorootSubcategoria FIDEOS = new nodorootSubcategoria("FIDEOS");
    public static final nodorootSubcategoria ZAPATOS = new nodorootSubcategoria("ZAPATOS");
    public static final nodorootSubcategoria SILLA = new nodorootSubcategoria("SILLA");
    public static final nodorootSubcategoria MESA = new nodorootSubcategoria("MESA");

    private nodorootSubcategoria(String nombre) {
        this.nombreSubcategoria = nombre;
    }
}
