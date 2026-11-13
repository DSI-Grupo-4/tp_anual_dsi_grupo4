package ar.edu.utn.frba.dds.web.publico;

import java.util.List;

/** Página de la galería. {@code pagina} empieza en 1. */
public record PaginaFotos(List<DonacionPublica> elementos, int pagina, int totalPaginas, int total) {

    public boolean tieneAnterior() {
        return pagina > 1;
    }

    public boolean tieneSiguiente() {
        return pagina < totalPaginas;
    }
}
