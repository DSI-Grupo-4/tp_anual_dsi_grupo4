package ar.edu.utn.frba.dds.logistica.controller;

import ar.edu.utn.frba.dds.logistica.domain.eventos.GestorEventos;
import ar.edu.utn.frba.dds.logistica.domain.eventos.TipoEvento;
import ar.edu.utn.frba.dds.logistica.domain.rutas.*;
import ar.edu.utn.frba.dds.logistica.seguridad.UsuarioToken;
import ar.edu.utn.frba.dds.logistica.service.ControlDeFotos;
import ar.edu.utn.frba.dds.logistica.service.RecepcionService;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/rutas/{idRuta}/paradas/{idParada}")
public class ParadaController {

    private final GestorRutas gestorRutas;
    private final GestorEventos gestorEventos;

    private final ControlDeFotos controlDeFotos;
    private final RecepcionService recepcion;

    @org.springframework.beans.factory.annotation.Autowired
    public ParadaController(GestorRutas gestorRutas, GestorEventos gestorEventos, ControlDeFotos controlDeFotos,
                            RecepcionService recepcion) {
        this.gestorRutas = gestorRutas;
        this.gestorEventos = gestorEventos;
        this.controlDeFotos = controlDeFotos;
        this.recepcion = recepcion;
    }

    /** Sin control de fotos: lo usan los tests de las resoluciones de parada, que no confirman con fotos. */
    public ParadaController(GestorRutas gestorRutas, GestorEventos gestorEventos) {
        this(gestorRutas, gestorEventos, null, new RecepcionService(gestorRutas, gestorEventos));
    }

    @Operation(
            summary = "Confirmar recepción de una parada (una foto)",
            description = "Misma regla que POST .../recepcion, con una sola foto: la ruta tiene que estar INICIADA y solo se confirman "
                    + "las entregas EN_TRASLADO de la parada (si la cuenta es de una entidad, solo las suyas). No se puede confirmar "
                    + "antes de iniciar la ruta ni repetir una confirmación. Genera ENTREGA_CONFIRMADA por cada entrega confirmada."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Recepción confirmada correctamente"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "La foto no es de quien confirma o las entregas de la parada son de otra entidad"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No se encontró la ruta o la parada indicada"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "La ruta no está en curso o la parada no tiene entregas en traslado"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Los datos de la recepción son inválidos"
            )
    })
    @PostMapping("/confirmar")
    public void confirmarRecepcion(@io.swagger.v3.oas.annotations.Parameter(description = "Reemplazar por un ID existente devuelto por el alta o listado.", example = "1") @PathVariable Integer idRuta, @io.swagger.v3.oas.annotations.Parameter(description = "Reemplazar por un ID existente devuelto por el alta o listado.", example = "1") @PathVariable Integer idParada,
                                   @jakarta.validation.Valid @RequestBody FotoEntrega foto,
                                   org.springframework.security.core.Authentication auth) {
        if (controlDeFotos != null) {
            controlDeFotos.exigirPropias(auth, java.util.List.of(foto.getUrl()));
        }
        // Mismas reglas de estado y de entidad que /recepcion: no hay un segundo camino sin validar.
        recepcion.confirmar(idRuta, idParada, java.util.List.of(foto), UsuarioToken.entidadesQuePuedeConfirmar(auth));
    }

    @Operation(
            summary = "Marcar una parada como no recibida",
            description = "Registra que las entregas asociadas a una parada no fueron recibidas, indicando una justificación, y genera los eventos correspondientes."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Parada marcada como no recibida correctamente"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No se encontró la ruta o la parada indicada"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "La justificación proporcionada no es válida"
            )
    })
    @PostMapping("/no-recibida")
    public void marcarNoRecibida(@io.swagger.v3.oas.annotations.Parameter(description = "Reemplazar por un ID existente devuelto por el alta o listado.", example = "1") @PathVariable Integer idRuta, @io.swagger.v3.oas.annotations.Parameter(description = "Reemplazar por un ID existente devuelto por el alta o listado.", example = "1") @PathVariable Integer idParada,
                                 @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "text/plain", examples = @io.swagger.v3.oas.annotations.media.ExampleObject(value = "Nadie respondió en el domicilio"))) @RequestBody String justificacion) {
        Ruta ruta = gestorRutas.buscarPorId(idRuta);
        Parada parada = buscarParada(ruta, idParada);
        exigirRutaEnCurso(ruta);
        // Solo las entregas que seguían en traslado: las que ya tenían un resultado no se pisan.
        java.util.List<Entrega> afectadas = parada.marcarNoRecibidasLasEnTraslado(justificacion);
        liberarCamionSiCorresponde(ruta); // finaliza si corresponde y guarda el agregado
        afectadas.forEach(e -> gestorEventos.crearEvento(TipoEvento.ENTREGA_NO_RECIBIDA, e));
    }

    @Operation(
            summary = "Reportar un incidente logístico en una parada",
            description = "Para cuando la entrega no se pudo concretar por un motivo distinto a que la entidad no la haya recibido (vencimiento de los bienes antes de llegar, incidente logístico en el camino, etc.). Lo reporta el chofer o una persona administradora."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Incidente registrado correctamente"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No se encontró la ruta o la parada indicada"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "El motivo proporcionado no es válido"
            )
    })
    @PostMapping("/incidente")
    public void marcarFallida(@io.swagger.v3.oas.annotations.Parameter(description = "Reemplazar por un ID existente devuelto por el alta o listado.", example = "1") @PathVariable Integer idRuta, @io.swagger.v3.oas.annotations.Parameter(description = "Reemplazar por un ID existente devuelto por el alta o listado.", example = "1") @PathVariable Integer idParada,
                              @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "text/plain", examples = @io.swagger.v3.oas.annotations.media.ExampleObject(value = "Los bienes vencieron antes de llegar a destino"))) @RequestBody String motivo) {
        Ruta ruta = gestorRutas.buscarPorId(idRuta);
        Parada parada = buscarParada(ruta, idParada);
        exigirRutaEnCurso(ruta);
        // Solo las entregas que seguían en traslado: las que ya tenían un resultado no se pisan.
        java.util.List<Entrega> afectadas = parada.marcarFalladasLasEnTraslado(motivo);
        liberarCamionSiCorresponde(ruta); // finaliza si corresponde y guarda el agregado
        afectadas.forEach(e -> gestorEventos.crearEvento(TipoEvento.ENTREGA_FALLIDA, e));
    }

    /** «No recibida» e incidente solo tienen sentido mientras el camión está en la calle. */
    private void exigirRutaEnCurso(Ruta ruta) {
        if (ruta.getEstadoRuta() != EstadoRuta.INICIADA) {
            throw new IllegalStateException("La ruta " + ruta.getIdRuta() + " no está en curso: solo se pueden registrar resultados de una ruta INICIADA");
        }
    }

    private Parada buscarParada(Ruta ruta, Integer idParada) {
        return ruta.getParadas().stream()
                .filter(p -> p.getIdParada().equals(idParada))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No existe la parada con id: " + idParada));
    }

    // El camión queda ASIGNADO desde que se planifica su ruta (ver Ruta);
    // recién vuelve a estar DISPONIBLE cuando no queda nada pendiente de
    // resolver en ninguna de sus paradas -- antes finalizarRuta() nunca se
    // llamaba y el camión quedaba comprometido para siempre tras su primera ruta.
    private void liberarCamionSiCorresponde(Ruta ruta) {
        if (Boolean.TRUE.equals(ruta.completoTodasLasEntregas())) {
            ruta.finalizarRuta();
        }
        gestorRutas.guardar(ruta); // ruta + camión + entregas (Mongo no cascadea ni hace dirty checking)
    }
}
