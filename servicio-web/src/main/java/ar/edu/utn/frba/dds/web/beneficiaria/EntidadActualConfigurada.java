package ar.edu.utn.frba.dds.web.beneficiaria;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Provisoria: toma el id de la entidad de donatrack.web.beneficiaria.entidad-id (por defecto 1).
 * Se borra cuando la Persona 2 incorpore la autenticación y aporte su propia implementación de EntidadActual.
 */
@Component
public class EntidadActualConfigurada implements EntidadActual {

    private final Long id;

    public EntidadActualConfigurada(@Value("${donatrack.web.beneficiaria.entidad-id:1}") Long id) {
        this.id = id;
    }

    @Override
    public Long id() {
        return id;
    }
}
