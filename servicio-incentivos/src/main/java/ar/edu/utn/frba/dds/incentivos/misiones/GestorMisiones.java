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
        Insignia rachaOro = new Insignia("Racha de Oro", "https://incentivos.local/insignias/racha-oro.png");
        Insignia coleccionista = new Insignia("Coleccionista", "https://incentivos.local/insignias/coleccionista.png");
        Insignia granDonante = new Insignia("Gran Donante", "https://incentivos.local/insignias/gran-donante.png");
        Insignia donanteConfiable = new Insignia("Donante Confiable", "https://incentivos.local/insignias/donante-confiable.png");

        Categoria bronce = new Categoria("Bronce");
        bronce.agregarMision(new Mision.Racha("Racha Bronce", rachaOro, 3, Periodicidad.MENSUAL));
        bronce.agregarMision(new Mision.Completitud("Variedad Bronce", coleccionista, 2));

        Categoria plata = new Categoria("Plata");
        plata.agregarMision(new Mision.HabilDonador("Gran Aporte Plata", granDonante, 50));
        plata.agregarMision(new Mision.DonacionesExitosas("Confiable Plata", donanteConfiable, 5));

        catalogoCategorias.add(bronce);
        catalogoCategorias.add(plata);
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
