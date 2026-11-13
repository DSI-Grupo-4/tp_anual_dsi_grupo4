package ar.edu.utn.frba.dds.logistica.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.Map;

/**
 * Errores de la carga de fotos. Va aparte de GlobalExceptionHandler para no tocarlo: maneja solo estos tipos,
 * así que no se superpone. El formato es el mismo ({"error": "..."}), que el cliente web ya sabe leer.
 */
@RestControllerAdvice
public class FotoExceptionHandler {

    @ExceptionHandler({ArchivoDemasiadoGrandeException.class, MaxUploadSizeExceededException.class})
    public ResponseEntity<Map<String, String>> demasiadoGrande(Exception ex) {
        String mensaje = ex instanceof ArchivoDemasiadoGrandeException
                ? ex.getMessage()
                : "El archivo supera el tamaño máximo permitido";
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(Map.of("error", mensaje));
    }

    @ExceptionHandler({MissingServletRequestPartException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<Map<String, String>> faltaElArchivo(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Falta el archivo: se espera una parte multipart llamada \"file\""));
    }
}
