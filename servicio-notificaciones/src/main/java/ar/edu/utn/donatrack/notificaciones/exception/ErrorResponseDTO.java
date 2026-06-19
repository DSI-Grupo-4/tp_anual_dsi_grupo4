package ar.edu.utn.donatrack.notificaciones.exception;

import java.time.LocalDateTime;
import java.util.List;

public class ErrorResponseDTO {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private List<String> mensajes;

    public ErrorResponseDTO(LocalDateTime timestamp, int status, String error, List<String> mensajes) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.mensajes = mensajes;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public List<String> getMensajes() {
        return mensajes;
    }
}
