package ar.edu.utn.frba.dds.logistica.service;

import ar.edu.utn.frba.dds.logistica.domain.eventos.GestorEventos;
import ar.edu.utn.frba.dds.logistica.domain.eventos.TipoEvento;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Entrega;
import ar.edu.utn.frba.dds.logistica.domain.rutas.EstadoRuta;
import ar.edu.utn.frba.dds.logistica.domain.rutas.FotoEntrega;
import ar.edu.utn.frba.dds.logistica.domain.rutas.GestorRutas;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Parada;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Ruta;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Confirmación de la recepción de una parada con fotos, hecha por la entidad beneficiaria.
 *
 * La regla es una sola para todos los caminos (POST .../recepcion y POST .../confirmar, con o sin ADMIN): la ruta tiene
 * que estar en curso (INICIADA) y solo se confirman las entregas EN_TRASLADO de la parada, así que no se puede confirmar
 * dos veces ni antes de que el camión salga. El estado de la entrega lo decide Logística (lo cambia iniciarRuta);
 * no se consulta a Donaciones: Donaciones se entera después por el evento ENTREGA_CONFIRMADA.
 */
@Service
public class RecepcionService {

    static final int MAX_FOTOS = 5;

    private final GestorRutas gestorRutas;
    private final GestorEventos gestorEventos;

    public RecepcionService(GestorRutas gestorRutas, GestorEventos gestorEventos) {
        this.gestorRutas = gestorRutas;
        this.gestorEventos = gestorEventos;
    }

    /** Sin límite de entidad: lo usa la administración. Las reglas de estado se aplican igual. */
    public void confirmar(Integer idRuta, Integer idParada, List<FotoEntrega> fotos) {
        confirmar(idRuta, idParada, fotos, null);
    }

    /**
     * Única puerta de la confirmación de recepción (la usan POST .../recepcion y POST .../confirmar).
     * Todas las validaciones van antes de tocar nada: si alguna falla no cambia ningún estado, no se guardan fotos
     * ni se generan eventos.
     *
     * @param entidadesAutorizadas entidades que puede confirmar quien llama (las del token de una beneficiaria);
     *                             {@code null} = sin límite de entidad (administración o seguridad deshabilitada)
     * @throws IllegalArgumentException fotos inválidas (400)
     * @throws NoSuchElementException   la ruta o la parada no existen (404)
     * @throws IllegalStateException    la ruta no está INICIADA o la parada no tiene entregas EN_TRASLADO (409)
     * @throws ResponseStatusException  403 si las entregas en traslado de la parada no son de la entidad que confirma
     */
    public void confirmar(Integer idRuta, Integer idParada, List<FotoEntrega> fotos, Set<Long> entidadesAutorizadas) {
        validarFotos(fotos);
        Ruta ruta = gestorRutas.buscarPorId(idRuta);
        Parada parada = ruta.getParadas().stream()
                .filter(p -> idParada.equals(p.getIdParada()))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No existe la parada con id: " + idParada));
        if (ruta.getEstadoRuta() != EstadoRuta.INICIADA) {
            throw new IllegalStateException("La ruta " + idRuta + " no está en curso: no se puede confirmar la recepción");
        }
        if (parada.entregasEnTraslado().isEmpty()) {
            throw new IllegalStateException("La parada " + idParada + " no tiene entregas en traslado para confirmar");
        }
        if (parada.entregasEnTrasladoPara(entidadesAutorizadas).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Las entregas en traslado de la parada " + idParada + " no son de la entidad que confirma");
        }

        List<Entrega> confirmadas = parada.confirmarRecepcionConFotos(fotos, entidadesAutorizadas);
        if (Boolean.TRUE.equals(ruta.completoTodasLasEntregas())) {
            ruta.finalizarRuta();
        }
        gestorRutas.guardar(ruta); // ruta + camión + entregas (Mongo no cascadea ni hace dirty checking)
        confirmadas.forEach(entrega -> gestorEventos.crearEvento(TipoEvento.ENTREGA_CONFIRMADA, entrega));
    }

    private static void validarFotos(List<FotoEntrega> fotos) {
        if (fotos == null || fotos.isEmpty()) {
            throw new IllegalArgumentException("Hace falta al menos una foto para confirmar la recepción");
        }
        if (fotos.size() > MAX_FOTOS) {
            throw new IllegalArgumentException("Se aceptan hasta " + MAX_FOTOS + " fotos");
        }
        for (FotoEntrega foto : fotos) {
            String url = foto.getUrl() == null ? "" : foto.getUrl().trim().toLowerCase(Locale.ROOT);
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                throw new IllegalArgumentException("La URL de cada foto debe empezar con http:// o https://");
            }
        }
    }
}
