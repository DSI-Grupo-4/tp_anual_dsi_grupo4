package ar.edu.utn.donatrack.notificaciones.controller;

import ar.edu.utn.donatrack.notificaciones.dto.NotificacionRequestDTO;
import ar.edu.utn.donatrack.notificaciones.dto.NotificacionResponseDTO;
import ar.edu.utn.donatrack.notificaciones.model.Notificacion;
import ar.edu.utn.donatrack.notificaciones.service.NotificacionService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final NotificacionService notificacionService;

    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @Operation(summary = "Envia una notificacion por el medio indicado (EMAIL, SMS o WHATSAPP)")
    @PostMapping({"", "/"})
    public ResponseEntity<NotificacionResponseDTO> enviarNotificacion(
            @Valid @RequestBody NotificacionRequestDTO request) {

        Notificacion notificacion = notificacionService.enviarNotificacion(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(NotificacionResponseDTO.desde(notificacion));
    }

    @Operation(summary = "Obtiene una notificacion por id")
    @GetMapping("/{id}")
    public ResponseEntity<NotificacionResponseDTO> obtenerPorId(@PathVariable String id) {
        return notificacionService.buscarPorId(id)
                .map(NotificacionResponseDTO::desde)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Lista todas las notificaciones registradas")
    @GetMapping({"", "/"})
    public ResponseEntity<List<NotificacionResponseDTO>> listarTodas() {
        List<NotificacionResponseDTO> notificaciones = notificacionService.listarTodas().stream()
                .map(NotificacionResponseDTO::desde)
                .toList();
        return ResponseEntity.ok(notificaciones);
    }
}
