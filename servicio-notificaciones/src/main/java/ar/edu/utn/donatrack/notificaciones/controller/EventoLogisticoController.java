package ar.edu.utn.donatrack.notificaciones.controller;

import ar.edu.utn.donatrack.notificaciones.dto.NotificacionEntregaExitosaDTO;
import ar.edu.utn.donatrack.notificaciones.dto.NotificacionEntregaFallidaDTO;
import ar.edu.utn.donatrack.notificaciones.dto.NotificacionInicioRutaDTO;
import ar.edu.utn.donatrack.notificaciones.dto.NotificacionResponseDTO;
import ar.edu.utn.donatrack.notificaciones.model.Notificacion;
import ar.edu.utn.donatrack.notificaciones.service.EventoLogisticoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints específicos para los eventos logísticos que requieren notificaciones
 * según la Entrega 3 del TP.
 *
 * Estos endpoints los llama el Servicio de Donaciones después de consumir
 * los EventoLogistico que Logística deja disponibles por polling.
 *
 * Los 3 eventos requeridos por el enunciado son:
 *   1. Inicio de ruta  → notifica a donantes y entidades de esa ruta (con enlace al mapa)
 *   2. Entrega exitosa → notifica a donante y entidad beneficiaria (con comprobante)
 *   3. Entrega fallida → notifica a donante, entidad y administradoras (con motivo)
 *
 * ARCHIVO NUEVO - Entrega 3.
 */
@Tag(name = "Eventos Logísticos", description = "Notificaciones disparadas por eventos del Servicio de Logística")
@RestController
@RequestMapping("/api/notificaciones/eventos-logisticos")
public class EventoLogisticoController {

    private final EventoLogisticoService eventoLogisticoService;

    public EventoLogisticoController(EventoLogisticoService eventoLogisticoService) {
        this.eventoLogisticoService = eventoLogisticoService;
    }

    @Operation(
            summary = "Notificar inicio de ruta",
            description = "Notifica a todos los donantes y entidades beneficiarias cuyas entregas forman " +
                    "parte de la ruta iniciada. Incluye enlace al mapa interactivo para seguimiento en tiempo real."
    )
    @PostMapping("/inicio-ruta")
    public ResponseEntity<List<NotificacionResponseDTO>> notificarInicioRuta(
            @Valid @RequestBody NotificacionInicioRutaDTO dto) {

        List<Notificacion> enviadas = eventoLogisticoService.notificarInicioRuta(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(enviadas.stream().map(NotificacionResponseDTO::desde).toList());
    }

    @Operation(
            summary = "Notificar entrega exitosa",
            description = "Notifica al donante y a la entidad beneficiaria que la entrega fue confirmada. " +
                    "Incluye comprobante con fecha, hora y patente del camión responsable."
    )
    @PostMapping("/entrega-exitosa")
    public ResponseEntity<List<NotificacionResponseDTO>> notificarEntregaExitosa(
            @Valid @RequestBody NotificacionEntregaExitosaDTO dto) {

        List<Notificacion> enviadas = eventoLogisticoService.notificarEntregaExitosa(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(enviadas.stream().map(NotificacionResponseDTO::desde).toList());
    }

    @Operation(
            summary = "Notificar entrega no satisfactoria",
            description = "Notifica al donante, a la entidad beneficiaria y a las personas administradoras " +
                    "que la entrega no pudo concretarse. Incluye el motivo e indica si puede replanificarse."
    )
    @PostMapping("/entrega-fallida")
    public ResponseEntity<List<NotificacionResponseDTO>> notificarEntregaFallida(
            @Valid @RequestBody NotificacionEntregaFallidaDTO dto) {

        List<Notificacion> enviadas = eventoLogisticoService.notificarEntregaFallida(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(enviadas.stream().map(NotificacionResponseDTO::desde).toList());
    }
}
