package ar.edu.utn.frba.dds.web.controller.beneficiaria;

import ar.edu.utn.frba.dds.web.beneficiaria.EntidadActual;
import ar.edu.utn.frba.dds.web.client.logistica.FotosClient;
import ar.edu.utn.frba.dds.web.client.logistica.RutasClient;
import ar.edu.utn.frba.dds.web.error.BackendRechazoException;
import ar.edu.utn.frba.dds.web.error.RecursoNoEncontradoException;
import ar.edu.utn.frba.dds.web.error.ServicioBackend;
import ar.edu.utn.frba.dds.web.logistica.EnvioEntidad;
import ar.edu.utn.frba.dds.web.logistica.SeguimientoService;
import ar.edu.utn.frba.dds.web.mensajes.Toasts;
import ar.edu.utn.frba.dds.web.navegacion.Miga;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * La entidad confirma que recibió lo que le llevaba un camión, con fotos (/beneficiaria/seguimiento/{idRuta}).
 *
 * La parada que se confirma NUNCA viene del pedido: se busca entre los envíos de la entidad. Así no se puede confirmar la
 * parada de otra entidad cambiando un número en el formulario.
 */
@Controller
@RequestMapping("/beneficiaria/seguimiento")
public class RecepcionController {

    static final int MAX_FOTOS = 5;
    static final long MAX_BYTES_POR_FOTO = 5L * 1024 * 1024;
    private static final Set<String> TIPOS = Set.of("image/jpeg", "image/png", "image/webp");
    private static final String SEGUIMIENTO = "/beneficiaria/seguimiento";

    private final EntidadActual entidadActual;
    private final SeguimientoService seguimiento;
    private final FotosClient fotos;
    private final RutasClient rutas;
    private final MessageSource mensajes;

    public RecepcionController(EntidadActual entidadActual, SeguimientoService seguimiento, FotosClient fotos,
                               RutasClient rutas, MessageSource mensajes) {
        this.entidadActual = entidadActual;
        this.seguimiento = seguimiento;
        this.fotos = fotos;
        this.rutas = rutas;
        this.mensajes = mensajes;
    }

    @GetMapping("/{idRuta}")
    public String formulario(@PathVariable Integer idRuta, Model model) {
        return pantalla(buscar(idRuta), null, model);
    }

    @PostMapping("/{idRuta}/recepcion")
    public String confirmar(@PathVariable Integer idRuta,
                            @RequestParam(name = "fotos", required = false) List<MultipartFile> archivos,
                            Model model, RedirectAttributes attrs) {
        EnvioEntidad envio = buscar(idRuta);
        if (!envio.puedeConfirmarRecepcion()) {
            Toasts.error(attrs, texto("logistica.recepcion.nada-para-confirmar"));
            return "redirect:" + SEGUIMIENTO;
        }
        List<MultipartFile> elegidas = archivos == null ? List.of() : archivos.stream().filter(a -> !a.isEmpty()).toList();
        String error = validar(elegidas);
        if (error != null) {
            return pantalla(envio, error, model);
        }
        try {
            List<String> urls = new ArrayList<>();
            for (MultipartFile archivo : elegidas) {
                urls.add(fotos.subir(archivo.getOriginalFilename(), archivo.getContentType(), archivo.getBytes()).url());
            }
            rutas.confirmarRecepcion(idRuta, envio.idParada(), urls);
            Toasts.exito(attrs, texto("logistica.recepcion.toast.confirmada"));
            return "redirect:" + SEGUIMIENTO;
        } catch (BackendRechazoException e) {
            // Por ejemplo, Logística rechazó una foto (no es una imagen) o la ruta ya no está en curso: se explica en el formulario.
            return pantalla(envio, e.getMessage(), model);
        } catch (IOException e) {
            return pantalla(envio, texto("logistica.recepcion.error.lectura"), model);
        }
    }

    // ---- auxiliares ----

    private EnvioEntidad buscar(Integer idRuta) {
        return seguimiento.envioDe(entidadActual.id(), idRuta)
                .orElseThrow(() -> new RecursoNoEncontradoException(ServicioBackend.LOGISTICA, "No existe la ruta " + idRuta));
    }

    private String pantalla(EnvioEntidad envio, String errorFotos, Model model) {
        model.addAttribute("envio", envio);
        model.addAttribute("errorFotos", errorFotos);
        model.addAttribute("maxFotos", MAX_FOTOS);
        model.addAttribute("migas", List.of(
                Miga.clave("nav.beneficiaria.panel", "/beneficiaria"),
                Miga.clave("nav.beneficiaria.seguimiento", SEGUIMIENTO),
                Miga.clave("logistica.recepcion.titulo", null)));
        return "beneficiaria/recepcion";
    }

    /** El primer problema que encuentre en las fotos elegidas, o null si están bien. Logística vuelve a verificar el contenido. */
    private String validar(List<MultipartFile> elegidas) {
        if (elegidas.isEmpty()) {
            return texto("logistica.recepcion.error.sin-fotos");
        }
        if (elegidas.size() > MAX_FOTOS) {
            return texto("logistica.recepcion.error.demasiadas", MAX_FOTOS);
        }
        for (MultipartFile archivo : elegidas) {
            String tipo = archivo.getContentType() == null ? "" : archivo.getContentType().toLowerCase(Locale.ROOT);
            if (!TIPOS.contains(tipo)) {
                return texto("logistica.recepcion.error.tipo", archivo.getOriginalFilename());
            }
            if (archivo.getSize() > MAX_BYTES_POR_FOTO) {
                return texto("logistica.recepcion.error.tamanio", archivo.getOriginalFilename(), MAX_BYTES_POR_FOTO / (1024 * 1024));
            }
        }
        return null;
    }

    private String texto(String clave, Object... argumentos) {
        return mensajes.getMessage(clave, argumentos, LocaleContextHolder.getLocale());
    }
}
