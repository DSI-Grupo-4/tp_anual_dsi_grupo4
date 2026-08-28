package ar.edu.utn.frba.dds.logistica.controller;

import ar.edu.utn.frba.dds.logistica.domain.eventos.GestorEventos;
import ar.edu.utn.frba.dds.logistica.domain.eventos.TipoEvento;
import ar.edu.utn.frba.dds.logistica.domain.rutas.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rutas/{idRuta}/paradas/{idParada}")
public class ParadaController {

    private final GestorRutas gestorRutas;
    private final GestorEventos gestorEventos;

    public ParadaController(GestorRutas gestorRutas, GestorEventos gestorEventos) {
        this.gestorRutas = gestorRutas;
        this.gestorEventos = gestorEventos;
    }

    @PostMapping("/confirmar")
    public void confirmarRecepcion(@PathVariable Integer idRuta, @PathVariable Integer idParada,
                                   @RequestBody FotoEntrega foto) {
        Parada parada = buscarParada(idRuta, idParada);
        parada.confirmarRecepcion(foto);
        parada.getEntregas().forEach(e -> gestorEventos.crearEvento(TipoEvento.ENTREGA_CONFIRMADA, e));
    }

    @PostMapping("/no-recibida")
    public void marcarNoRecibida(@PathVariable Integer idRuta, @PathVariable Integer idParada,
                                 @RequestBody String justificacion) {
        Parada parada = buscarParada(idRuta, idParada);
        parada.marcarNoRecibida(justificacion);
        parada.getEntregas().forEach(e -> gestorEventos.crearEvento(TipoEvento.ENTREGA_NO_RECIBIDA, e));
    }

    private Parada buscarParada(Integer idRuta, Integer idParada) {
        Ruta ruta = gestorRutas.buscarPorId(idRuta);
        return ruta.getParadas().stream()
                .filter(p -> p.getIdParada().equals(idParada))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No existe la parada con id: " + idParada));
    }
}
