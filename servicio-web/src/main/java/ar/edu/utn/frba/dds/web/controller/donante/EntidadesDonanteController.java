package ar.edu.utn.frba.dds.web.controller.donante;

import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteApi.DonacionDelDonante;
import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteApi.Entidad;
import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteApi.Necesidad;
import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteApi.Pagina;
import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteClient;
import ar.edu.utn.frba.dds.web.donante.DonanteActual;
import ar.edu.utn.frba.dds.web.error.BackendException;
import ar.edu.utn.frba.dds.web.navegacion.Miga;
import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Entidades beneficiarias, vistas por el donante (Persona 4): búsqueda y ficha con sus
 * necesidades abiertas y las donaciones que les hizo. Solo entidades activas, y sin datos
 * de contacto, representantes ni calle.
 */
@Controller
@RequestMapping("/donante/entidades")
public class EntidadesDonanteController extends PortalDonanteBase {

    private static final Logger log = LoggerFactory.getLogger(EntidadesDonanteController.class);
    static final int POR_PAGINA = 12;
    private static final int DONACIONES_EN_FICHA = 5;
    private static final String RUTA = "/donante/entidades";

    public EntidadesDonanteController(DonanteActual donanteActual, PortalDonanteClient donaciones) {
        super(donanteActual, donaciones);
    }

    @GetMapping
    public String lista(@RequestParam(required = false) String q,
                        @RequestParam(required = false) String tipo,
                        @RequestParam(required = false) String page,
                        Model model) {
        List<Entidad> activas = donaciones.entidades().stream()
                .filter(Objects::nonNull)
                .filter(Entidad::estaActiva)
                .sorted(Comparator.comparing(e -> normalizar(e.nombre())))
                .toList();
        List<String> tipos = activas.stream().map(Entidad::tipo).filter(Objects::nonNull).distinct().sorted().toList();

        String busqueda = q == null ? "" : q.strip();
        if (busqueda.length() > 100) {
            busqueda = busqueda.substring(0, 100);
        }
        String tipoElegido = tipo != null && tipos.contains(tipo) ? tipo : "";
        String texto = normalizar(busqueda);
        List<Entidad> filtradas = activas.stream()
                .filter(e -> tipoElegido.isEmpty() || tipoElegido.equals(e.tipo()))
                .filter(e -> texto.isEmpty() || coincide(e, texto))
                .toList();

        int totalPaginas = Math.max(1, (filtradas.size() + POR_PAGINA - 1) / POR_PAGINA);
        int pagina = Math.min(paginaPedida(page), totalPaginas - 1);
        int desde = pagina * POR_PAGINA;
        List<Entidad> enPagina = filtradas.subList(Math.min(desde, filtradas.size()),
                Math.min(desde + POR_PAGINA, filtradas.size()));

        model.addAttribute("q", busqueda);
        model.addAttribute("tipo", tipoElegido);
        model.addAttribute("tipos", tipos);
        model.addAttribute("hayFiltros", !busqueda.isEmpty() || !tipoElegido.isEmpty());
        model.addAttribute("entidades", enPagina);
        model.addAttribute("totalActivas", activas.size());
        model.addAttribute("totalTipos", tipos.size());
        model.addAttribute("totalUbicaciones", activas.stream().map(Entidad::ubicacion).filter(Objects::nonNull).distinct().count());
        model.addAttribute("totalFiltradas", filtradas.size());
        model.addAttribute("pagina", pagina);
        model.addAttribute("totalPaginas", totalPaginas);
        model.addAttribute("anterior", pagina > 0 ? enlace(busqueda, tipoElegido, pagina - 1) : null);
        model.addAttribute("siguiente", pagina + 1 < totalPaginas ? enlace(busqueda, tipoElegido, pagina + 1) : null);
        return "donante/entidades";
    }

    @GetMapping("/{entidadId:[0-9]+}")
    public String ficha(@PathVariable long entidadId, Model model) {
        // 404 de la API → página de error. Una entidad inactiva tampoco se muestra.
        Entidad entidad = donaciones.entidad(entidadId);
        if (entidad == null || !entidad.estaActiva()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        List<Necesidad> necesidades;
        try {
            necesidades = donaciones.necesidades(entidadId).stream().filter(Necesidad::abierta).toList();
        } catch (BackendException e) {
            log.warn("Ficha de la entidad {} sin necesidades: {}", entidadId, e.getMessage());
            necesidades = null;
        }

        Pagina<DonacionDelDonante> mias = donaciones.donaciones(donanteActual.id(), java.util.Map.of(
                "entidadId", String.valueOf(entidadId), "page", "0", "size", String.valueOf(DONACIONES_EN_FICHA)));

        model.addAttribute("entidad", entidad);
        model.addAttribute("necesidadesNoDisponibles", necesidades == null);
        model.addAttribute("extraordinarias", necesidades == null ? List.of()
                : necesidades.stream().filter(Necesidad::esExtraordinaria).toList());
        model.addAttribute("recurrentes", necesidades == null ? List.of()
                : necesidades.stream().filter(n -> !n.esExtraordinaria()).toList());
        model.addAttribute("misDonaciones", mias.content());
        model.addAttribute("totalMisDonaciones", mias.totalElements());
        model.addAttribute("migas", List.of(
                Miga.clave("nav.donante.panel", "/donante"),
                Miga.clave("nav.donante.entidades", RUTA),
                Miga.texto(entidad.nombre(), null)));
        return "donante/entidad-detalle";
    }

    private static boolean coincide(Entidad e, String texto) {
        return Stream.of(e.nombre(), e.descripcion(), e.rubro(), e.tipo(), e.ubicacion())
                .filter(Objects::nonNull)
                .anyMatch(campo -> normalizar(campo).contains(texto));
    }

    /** Minúsculas y sin tildes, para que "cordoba" encuentre "Córdoba". */
    static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT).strip();
    }

    private static int paginaPedida(String page) {
        try {
            return Math.max(0, Integer.parseInt(page == null ? "0" : page.strip()));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String enlace(String q, String tipo, int pagina) {
        UriComponentsBuilder uri = UriComponentsBuilder.fromPath(RUTA);
        if (!q.isEmpty()) uri.queryParam("q", q);
        if (!tipo.isEmpty()) uri.queryParam("tipo", tipo);
        if (pagina > 0) uri.queryParam("page", pagina);
        return uri.build().encode().toUriString();
    }
}
