package ar.edu.utn.frba.dds.logistica.exception;

/** El archivo subido supera el tamaño máximo permitido (se responde 413). */
public class ArchivoDemasiadoGrandeException extends RuntimeException {

    public ArchivoDemasiadoGrandeException(String mensaje) {
        super(mensaje);
    }
}
