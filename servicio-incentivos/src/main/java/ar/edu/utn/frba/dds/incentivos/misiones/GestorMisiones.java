package ar.edu.utn.frba.dds.incentivos.misiones;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GestorMisiones {

    private static GestorMisiones instancia;

    private final List<Categoria> catalogoCategorias;

    private GestorMisiones() {
        this.catalogoCategorias = new ArrayList<>();
        sembrarCatalogo();
    }

    public static synchronized GestorMisiones getInstance() {
        if (instancia == null) {
            instancia = new GestorMisiones();
        }
        return instancia;
    }

    private void sembrarCatalogo() {
        Insignia rachaColaborador = new Insignia("Racha Colaboradora", "https://incentivos.local/insignias/racha-colaborador.png");
        Insignia coleccionista = new Insignia("Coleccionista", "https://incentivos.local/insignias/coleccionista.png");
        Insignia granDonante = new Insignia("Gran Donante", "https://incentivos.local/insignias/gran-donante.png");
        Insignia donanteConfiable = new Insignia("Donante Confiable", "https://incentivos.local/insignias/donante-confiable.png");
        Insignia rachaTransformadora = new Insignia("Racha Transformadora", "https://incentivos.local/insignias/racha-transformadora.png");
        Insignia pilarSolidario = new Insignia("Pilar Solidario", "https://incentivos.local/insignias/pilar-solidario.png");

        Categoria colaborador = new Categoria("Colaborador");
        colaborador.agregarMision(new Mision.Racha("Racha Colaborador", rachaColaborador, 2));
        colaborador.agregarMision(new Mision.Completitud("Variedad Colaborador", coleccionista, 2));

        Categoria sostenedor = new Categoria("Sostenedor");
        sostenedor.agregarMision(new Mision.HabilDonador("Gran Aporte Sostenedor", granDonante, 30));
        sostenedor.agregarMision(new Mision.DonacionesExitosas("Confiable Sostenedor", donanteConfiable, 3));

        Categoria transformador = new Categoria("Transformador");
        transformador.agregarMision(new Mision.Racha("Racha Transformador", rachaTransformadora, 6));
        transformador.agregarMision(new Mision.DonacionesExitosas("Pilar Transformador", pilarSolidario, 10));

        catalogoCategorias.add(colaborador);
        catalogoCategorias.add(sostenedor);
        catalogoCategorias.add(transformador);
    }

    public void agregarCategoria(Categoria categoria) {
        catalogoCategorias.add(categoria);
    }

    public Categoria siguienteCategoria(int indice) {
        return catalogoCategorias.get(indice);
    }

    public int cantCategorias() {
        return catalogoCategorias.size();
    }

    public Optional<Categoria> buscarCategoriaPorNombre(String nombre) {
        return catalogoCategorias.stream()
                .filter(categoria -> categoria.getNombre().equalsIgnoreCase(nombre))
                .findFirst();
    }
}
