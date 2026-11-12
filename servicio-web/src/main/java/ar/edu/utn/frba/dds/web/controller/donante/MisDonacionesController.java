package ar.edu.utn.frba.dds.web.controller.donante;

import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteApi.CambioEstado;
import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteApi.DetalleDonacion;
import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteApi.DonacionDelDonante;
import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteApi.Entidad;
import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteApi.Pagina;
import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteClient;
import ar.edu.utn.frba.dds.web.donante.DonanteActual;
import ar.edu.utn.frba.dds.web.donante.DonanteNoVinculadoException;
import ar.edu.utn.frba.dds.web.donante.DonanteVista;
import ar.edu.utn.frba.dds.web.error.BackendException;
import ar.edu.utn.frba.dds.web.error.RecursoNoEncontradoException;
import ar.edu.utn.frba.dds.web.navegacion.Miga;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

/** "Mis donaciones": lista con filtros y detalle con historial de estados (Persona 4). */
@Controller
@RequestMapping("/donante/donaciones")
public class MisDonacionesController {

    private static final Logger log = LoggerFactory.getLogger(MisDonacionesController.class);
    static final int POR_PAGINA = 10;
    private static final int LARGO_MAXIMO_BUSQUEDA = 100;
    private static final String RUTA = "/donante/donaciones";

    private final DonanteActual donanteActual;
    private final PortalDonanteClient donaciones;

    public MisDonacionesController(DonanteActual donanteActual, PortalDonanteClient donaciones) {
        this.donanteActual = donanteActual;
        this.donaciones = donaciones;
    }

    /**
     * Filtros ya validados: un valor desconocido se descarta en lugar de mandarlo a la API,
     * así un enlace viejo o editado a mano no termina en una página de error.
     */
    public record Filtros(String q, String estado, String categoria, String subcategoria, int page) {

        static Filtros validar(String q, String estado, String categoria, String subcategoria, String page) {
            // List.of/Map.of no aceptan null en contains: se normaliza antes de comparar.
            estado = estado == null ? "" : estado;
            categoria = categoria == null ? "" : categoria;
            subcategoria = subcategoria == null ? "" : subcategoria;
            String texto = q == null ? "" : q.strip();
            if (texto.length() > LARGO_MAXIMO_BUSQUEDA) {
                texto = texto.substring(0, LARGO_MAXIMO_BUSQUEDA);
            }
            String est = DonanteVista.ESTADOS.containsKey(estado) ? estado : "";
            String cat = DonanteVista.CATEGORIAS.contains(categoria) ? categoria : "";
            String sub = DonanteVista.categoriaDe(subcategoria) != null ? subcategoria : "";
            if (!cat.isEmpty() && !sub.isEmpty() && !cat.equals(DonanteVista.categoriaDe(sub))) {
                sub = ""; // Combinación imposible (p. ej. Alimentos + Sillas): se queda la categoría.
            }
            int pagina;
            try {
                pagina = Math.max(0, Integer.parseInt(page == null ? "0" : page.strip()));
            } catch (NumberFormatException e) {
                pagina = 0;
            }
            return new Filtros(texto, est, cat, sub, pagina);
        }

        public boolean hayFiltros() {
            return !q.isEmpty() || !estado.isEmpty() || !categoria.isEmpty() || !subcategoria.isEmpty();
        }

        /** Parámetros para la API (sin vacíos), con la página indicada. */
        Map<String, String> parametros(int pagina) {
            Map<String, String> p = new LinkedHashMap<>();
            if (!q.isEmpty()) p.put("q", q);
            if (!estado.isEmpty()) p.put("estado", estado);
            if (!categoria.isEmpty()) p.put("categoria", categoria);
            if (!subcategoria.isEmpty()) p.put("subcategoria", subcategoria);
            p.put("page", String.valueOf(pagina));
            return p;
        }

        /** Enlace a otra página de la lista conservando los filtros. */
        public String enlace(int pagina) {
            UriComponentsBuilder uri = UriComponentsBuilder.fromPath(RUTA);
            parametros(pagina).forEach((clave, valor) -> {
                if (!"page".equals(clave) || pagina > 0) {
                    uri.queryParam(clave, valor);
                }
            });
            return uri.build().encode().toUriString();
        }
    }

    @GetMapping
    public String lista(@RequestParam(required = false) String q,
                        @RequestParam(required = false) String estado,
                        @RequestParam(required = false) String categoria,
                        @RequestParam(required = false) String subcategoria,
                        @RequestParam(required = false) String page,
                        Model model) {
        long id = donanteActual.id();
        Filtros filtros = Filtros.validar(q, estado, categoria, subcategoria, page);
        Map<String, String> parametros = filtros.parametros(filtros.page());
        parametros.put("size", String.valueOf(POR_PAGINA));

        Pagina<DonacionDelDonante> pagina;
        try {
            pagina = donaciones.donaciones(id, parametros);
        } catch (RecursoNoEncontradoException e) {
            // Este endpoint solo responde 404 si el donante no existe.
            throw new DonanteNoVinculadoException(id, e);
        }

        long desde = pagina.content().isEmpty() ? 0 : (long) pagina.number() * POR_PAGINA + 1;
        model.addAttribute("filtros", filtros);
        model.addAttribute("pagina", pagina);
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", desde == 0 ? 0 : desde + pagina.content().size() - 1);
        model.addAttribute("estados", DonanteVista.ESTADOS.keySet());
        model.addAttribute("categorias", DonanteVista.CATEGORIAS);
        model.addAttribute("subcategorias", DonanteVista.SUBCATEGORIAS);
        return "donante/donaciones";
    }

    @GetMapping("/{donacionId:[0-9]+}")
    public String detalle(@PathVariable long donacionId, Model model) {
        long id = donanteActual.id();
        // Si la donación no existe o es de otro donante, la API responde 404 y se muestra la página de error.
        DetalleDonacion donacion = donaciones.donacion(id, donacionId);
        List<CambioEstado> historial = new ArrayList<>(donaciones.historial(id, donacionId));
        Collections.reverse(historial); // El más reciente primero.

        Entidad entidad = null;
        if (donacion.entidadBeneficiariaId() != null) {
            try {
                entidad = donaciones.entidad(donacion.entidadBeneficiariaId());
            } catch (BackendException e) {
                log.warn("Detalle de la donación {} sin datos de la entidad {}: {}",
                        donacionId, donacion.entidadBeneficiariaId(), e.getMessage());
            }
        }

        String titulo = "Donación #" + donacionId;
        model.addAttribute("titulo", titulo);
        model.addAttribute("donacion", donacion);
        model.addAttribute("historial", historial);
        model.addAttribute("entidad", entidad);
        model.addAttribute("enCamino", donacion.estadoActual() != null
                && DonanteVista.EN_CAMINO.contains(donacion.estadoActual()));
        model.addAttribute("migas", List.of(
                Miga.clave("nav.donante.panel", "/donante"),
                Miga.clave("nav.donante.donaciones", RUTA),
                Miga.texto(titulo, null)));
        return "donante/donacion-detalle";
    }
}
