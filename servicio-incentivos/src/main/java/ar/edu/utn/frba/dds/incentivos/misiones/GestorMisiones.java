package ar.edu.utn.frba.dds.incentivos.misiones;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GestorMisiones {

    private static GestorMisiones instancia;

    private final List<Categoria> catalogoCategorias;
    private ar.edu.utn.frba.dds.incentivos.repository.CategoriaRepository categoriaRepository;

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

    /**
     * Catálogo cerrado: 5 misiones por categoría, escalando en dificultad
     * (Colaborador &lt; Sostenedor &lt; Transformador), usando únicamente los
     * 4 tipos de misión que da el enunciado como ejemplo (Racha, Completitud,
     * HabilDonador, DonacionesExitosas) -- se repite DonacionesExitosas una
     * vez por categoría (con un umbral más alto) para llegar a 5 sin inventar
     * un 5to tipo que el enunciado no pide.
     * Los umbrales son placeholders a propósito, ajustables sin tocar
     * estructura. Excepción real: Completitud no puede pedir más de 3
     * (Donaciones solo tiene 3 categorías de bienes: Alimentos/Mobiliario/
     * Vestimenta), así que Sostenedor y Transformador quedan en el mismo
     * techo -- no hay forma de escalarlo más sin tocar otro servicio.
     */
    private void sembrarCatalogo() {
        Categoria colaborador = new Categoria("Colaborador");
        colaborador.agregarMision(new Mision.Racha("Racha Colaborador",
                new Insignia("Primer Paso", "https://incentivos.local/insignias/primer-paso.png"), 1));
        colaborador.agregarMision(new Mision.DonacionesExitosas("Confiable Colaborador",
                new Insignia("Mano Amiga", "https://incentivos.local/insignias/mano-amiga.png"), 2));
        colaborador.agregarMision(new Mision.HabilDonador("Generoso Colaborador",
                new Insignia("Aporte Destacado", "https://incentivos.local/insignias/aporte-destacado.png"), 5));
        colaborador.agregarMision(new Mision.Completitud("Variedad Colaborador",
                new Insignia("Coleccionista", "https://incentivos.local/insignias/coleccionista.png"), 2));
        colaborador.agregarMision(new Mision.DonacionesExitosas("Constante Colaborador",
                new Insignia("Donante Recurrente", "https://incentivos.local/insignias/donante-recurrente.png"), 5));

        Categoria sostenedor = new Categoria("Sostenedor");
        sostenedor.agregarMision(new Mision.Racha("Racha Sostenedor",
                new Insignia("Compromiso Sostenido", "https://incentivos.local/insignias/compromiso-sostenido.png"), 3));
        sostenedor.agregarMision(new Mision.DonacionesExitosas("Confiable Sostenedor",
                new Insignia("Donante Confiable", "https://incentivos.local/insignias/donante-confiable.png"), 8));
        sostenedor.agregarMision(new Mision.HabilDonador("Gran Aporte Sostenedor",
                new Insignia("Gran Donante", "https://incentivos.local/insignias/gran-donante.png"), 15));
        sostenedor.agregarMision(new Mision.Completitud("Variedad Sostenedor",
                new Insignia("Donante Integral", "https://incentivos.local/insignias/donante-integral.png"), 3));
        sostenedor.agregarMision(new Mision.DonacionesExitosas("Constante Sostenedor",
                new Insignia("Pilar de la Comunidad", "https://incentivos.local/insignias/pilar-comunidad.png"), 15));

        Categoria transformador = new Categoria("Transformador");
        transformador.agregarMision(new Mision.Racha("Racha Transformador",
                new Insignia("Racha Transformadora", "https://incentivos.local/insignias/racha-transformadora.png"), 6));
        transformador.agregarMision(new Mision.DonacionesExitosas("Confiable Transformador",
                new Insignia("Impacto Real", "https://incentivos.local/insignias/impacto-real.png"), 20));
        transformador.agregarMision(new Mision.HabilDonador("Gran Aporte Transformador",
                new Insignia("Mecenas", "https://incentivos.local/insignias/mecenas.png"), 30));
        transformador.agregarMision(new Mision.Completitud("Variedad Transformador",
                new Insignia("Donante Completo", "https://incentivos.local/insignias/donante-completo.png"), 3));
        transformador.agregarMision(new Mision.DonacionesExitosas("Constante Transformador",
                new Insignia("Pilar Solidario", "https://incentivos.local/insignias/pilar-solidario.png"), 40));

        agregarCategoria(colaborador);
        agregarCategoria(sostenedor);
        agregarCategoria(transformador);
    }

    public void agregarCategoria(Categoria categoria) {
        categoria.setOrden(catalogoCategorias.size());
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

    /**
     * Inyectado al boot por PersistenciaConfigurer (GestorMisiones es un
     * singleton manual, no un bean de Spring -- mismo patrón que
     * WebhookN8nConfigurer/NotificacionesConfigurer). Si la tabla está vacía
     * (primer arranque), persiste el catálogo que sembrarCatalogo() ya armó
     * en memoria. Si ya hay datos, descarta ese catálogo en memoria y carga
     * el real de la base -- no se resiembra ni se duplica en cada reinicio.
     */
    public synchronized void configurarPersistencia(
            ar.edu.utn.frba.dds.incentivos.repository.CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
        List<Categoria> existentes = categoriaRepository.findAllByOrderByOrdenAsc();
        if (existentes.isEmpty()) {
            for (Categoria categoria : catalogoCategorias) {
                categoriaRepository.save(categoria);
            }
        } else {
            catalogoCategorias.clear();
            catalogoCategorias.addAll(existentes);
        }
    }
}
