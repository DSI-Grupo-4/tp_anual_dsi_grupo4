package ar.edu.utn.frba.dds.web.donante;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Donante fijo mientras no haya autenticación. Se elige con la variable de entorno
 * DONANTE_PROVISORIO_ID (por defecto, el 1). La reemplaza la implementación de la Persona 2.
 */
@Component
public class DonanteActualProvisorio implements DonanteActual {

    private final long id;

    public DonanteActualProvisorio(@Value("${donatrack.web.donante-provisorio-id:${DONANTE_PROVISORIO_ID:1}}") long id) {
        this.id = id;
    }

    @Override
    public long id() {
        return id;
    }
}
