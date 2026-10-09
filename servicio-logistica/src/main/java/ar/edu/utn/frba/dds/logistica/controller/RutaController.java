package ar.edu.utn.frba.dds.logistica.controller;

import ar.edu.utn.frba.dds.logistica.domain.eventos.GestorEventos;
import ar.edu.utn.frba.dds.logistica.domain.eventos.TipoEvento;
import ar.edu.utn.frba.dds.logistica.domain.rutas.GestorRutas;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Ruta;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.List;

@RestController
@RequestMapping("/api/rutas")
public class RutaController {

    private final GestorRutas gestorRutas;
    private final GestorEventos gestorEventos;
    @org.springframework.beans.factory.annotation.Value("${logistica.public-base-url:http://localhost:8083}")
    private String publicBaseUrl;

    public RutaController(GestorRutas gestorRutas, GestorEventos gestorEventos) {
        this.gestorRutas = gestorRutas;
        this.gestorEventos = gestorEventos;
    }

    @Operation(
            summary = "Obtener todas las rutas",
            description = "Obtiene la lista de todas las rutas registradas en el sistema."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Rutas obtenidas correctamente"
            )
    })
    @GetMapping
    public List<Ruta> obtenerRutas() {
        return gestorRutas.getRutas();
    }

    @Operation(
            summary = "Obtener una ruta por ID",
            description = "Obtiene la información de una ruta a partir de su identificador."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Ruta encontrada correctamente"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No se encontró una ruta con el ID indicado"
            )
    })
    @GetMapping("/{id}")
    public Ruta obtenerRuta(@io.swagger.v3.oas.annotations.Parameter(description = "Reemplazar por un ID existente devuelto por el alta o listado.", example = "1") @PathVariable Integer id) {
        return gestorRutas.buscarPorId(id);
    }

    @Operation(
            summary = "Iniciar una ruta",
            description = "Inicia el recorrido de una ruta e informa mediante eventos que las entregas asociadas a la ruta han comenzado su recorrido."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Ruta iniciada correctamente"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No se encontró una ruta con el ID indicado"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "La ruta no puede ser iniciada en su estado actual"
            )
    })
    // el chofer informa el comienzo de su recorrido
    @PostMapping("/{id}/iniciar")
    public Ruta iniciarRuta(@io.swagger.v3.oas.annotations.Parameter(description = "Reemplazar por un ID existente devuelto por el alta o listado.", example = "1") @PathVariable Integer id) {
        Ruta ruta = gestorRutas.buscarPorId(id);
        ruta.iniciarRuta();
        ruta.getParadas().forEach(p -> p.getEntregas().forEach(e ->
            e.setSeguimientoUrl(publicBaseUrl + "/api/rutas/" + id + "/seguimiento")));
        ruta.getParadas().forEach(p -> p.getEntregas().forEach(e ->
                gestorEventos.crearEvento(TipoEvento.RUTA_INICIADA, e)));
        return ruta;
    }
    @Operation(summary = "Seguimiento de ruta", description = "Esquema interno de paradas y estados actualizados cada 15 segundos, sin GPS.")
    @GetMapping(value = "/{id}/seguimiento", produces = "text/html;charset=UTF-8")
    public String seguimiento(@PathVariable Integer id) {
        Ruta ruta = gestorRutas.buscarPorId(id);
        StringBuilder html = new StringBuilder("<!doctype html><html lang='es'><meta charset='utf-8'><meta http-equiv='refresh' content='15'><title>Seguimiento DonaTrack</title><style>body{font:18px system-ui;max-width:800px;margin:40px auto}li{padding:20px;border-left:5px solid #297a62;list-style:none;background:#eef8f3;margin-bottom:12px}</style><body><h1>Seguimiento de ruta " + id + "</h1>");
        html.append("<p>Estado: ").append(ruta.getEstadoRuta()).append(". Camión: ").append(escapar(ruta.getCamionAsociado().getPatente())).append("</p><p>Estados actualizados cada 15 segundos. Esquema de paradas sin GPS.</p><ol>");
        ruta.getParadas().forEach(p -> {
            html.append("<li>");
            p.getEntregas().forEach(e -> {
                html.append("<p>Donación ").append(e.getIdDonacionAsociada()).append(": ").append(e.getEstadoEntrega()).append("</p>");
                if (e.getDireccionDestino() != null) html.append("<p>").append(escapar(e.getDireccionDestino().getCalle() + " " + e.getDireccionDestino().getNumero())).append("</p>");
            });
            html.append("</li>");
        });
        return html.append("</ol></body></html>").toString();
    }
    private String escapar(String texto) {
        return texto == null ? "" : texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
