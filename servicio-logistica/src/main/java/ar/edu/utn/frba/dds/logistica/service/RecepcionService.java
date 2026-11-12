package ar.edu.utn.frba.dds.logistica.service;

import ar.edu.utn.frba.dds.logistica.domain.eventos.GestorEventos;
import ar.edu.utn.frba.dds.logistica.domain.eventos.TipoEvento;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Entrega;
import ar.edu.utn.frba.dds.logistica.domain.rutas.EstadoRuta;
import ar.edu.utn.frba.dds.logistica.domain.rutas.FotoEntrega;
import ar.edu.utn.frba.dds.logistica.domain.rutas.GestorRutas;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Parada;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Ruta;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

/**
 * Confirmación de la recepción de una parada con fotos, hecha por la entidad beneficiaria.
 *
 * A diferencia de POST .../confirmar (que no valida el estado), acá la ruta tiene que estar en curso y la parada
 * tener entregas en traslado: así no se puede confirmar dos veces ni antes de que el camión salga.
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

    public void confirmar(Integer idRuta, Integer idParada, List<FotoEntrega> fotos) {
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

        List<Entrega> confirmadas = parada.confirmarRecepcionConFotos(fotos);
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
