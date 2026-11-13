package ar.edu.utn.frba.dds.web.vista;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

/**
 * Serializa datos a JSON para pasarlos a un script de la vista mediante un atributo data-*:
 * <pre>th:attr="data-marcadores=${@json.escribir(marcadores)}"</pre>
 * El atributo lo escapa Thymeleaf como HTML, así que ningún texto cargado por un usuario
 * puede cerrar la etiqueta ni inyectar marcado (a diferencia de un JSON dentro de &lt;script&gt;).
 */
@Component("json")
public class VistaJson {

    private final ObjectMapper mapper;

    public VistaJson(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public String escribir(Object valor) {
        try {
            return mapper.writeValueAsString(valor);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("No se pudo serializar a JSON para la vista", e);
        }
    }
}
