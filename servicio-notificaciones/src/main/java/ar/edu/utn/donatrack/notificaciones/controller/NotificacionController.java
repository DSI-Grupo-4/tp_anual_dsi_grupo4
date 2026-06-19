package ar.edu.utn.donatrack.notificaciones.controller;

import ar.edu.utn.donatrack.notificaciones.dto.NotificacionRequestDTO;
import ar.edu.utn.donatrack.notificaciones.dto.NotificacionResponseDTO;
import ar.edu.utn.donatrack.notificaciones.model.Notificacion;
import ar.edu.utn.donatrack.notificaciones.service.NotificacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Ejemplo de invocación desde el Servicio de Donaciones:
 *
 * POST /api/notificaciones
 * {
 *   "mensaje": "Tu donación fue asignada a la Escuela Rural N°10",
 *   "medio": "WHATSAPP",
 *   "contacto": "+54 11 5555-5555",
 *   "servicioOrigen": "SERVICIO_DONACIONES"
 * }
 */

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final NotificacionService notificacionService;

    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @Operation(summary = "Envía una notificación por el medio indicado (EMAIL, SMS o WHATSAPP)",
            description = "Recibe el mensaje, el medio de comunicación y el contacto del destinatario. " +
                    "En esta entrega el envío a los proveedores externos está simulado.")
    @PostMapping
    public ResponseEntity<NotificacionResponseDTO> enviarNotificacion(
            @Valid @RequestBody NotificacionRequestDTO request) {

        Notificacion notificacion = notificacionService.enviarNotificacion(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(NotificacionResponseDTO.desde(notificacion));
    }
}
