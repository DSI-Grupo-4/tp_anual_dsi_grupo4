package ar.edu.utn.frba.dds.logistica.controller;

import ar.edu.utn.frba.dds.logistica.domain.eventos.GestorEventos;
import ar.edu.utn.frba.dds.logistica.domain.eventos.TipoEvento;
import ar.edu.utn.frba.dds.logistica.domain.rutas.GestorRutas;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Ruta;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rutas")
public class RutaController {

    private final GestorRutas gestorRutas;
    private final GestorEventos gestorEventos;

    public RutaController(GestorRutas gestorRutas, GestorEventos gestorEventos) {
        this.gestorRutas = gestorRutas;
        this.gestorEventos = gestorEventos;
    }

    @GetMapping
    public List<Ruta> obtenerRutas() {
        return gestorRutas.getRutas();
    }

    @GetMapping("/{id}")
    public Ruta obtenerRuta(@PathVariable Integer id) {
        return gestorRutas.buscarPorId(id);
    }

    // el chofer informa el comienzo de su recorrido
    @PostMapping("/{id}/iniciar")
    public Ruta iniciarRuta(@PathVariable Integer id) {
        Ruta ruta = gestorRutas.buscarPorId(id);
        ruta.iniciarRuta();
        ruta.getParadas().forEach(p -> p.getEntregas().forEach(e ->
                gestorEventos.crearEvento(TipoEvento.RUTA_INICIADA, e)));
        return ruta;
    }
}
