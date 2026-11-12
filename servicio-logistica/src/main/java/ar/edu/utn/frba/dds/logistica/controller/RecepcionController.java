package ar.edu.utn.frba.dds.logistica.controller;

import ar.edu.utn.frba.dds.logistica.dto.ConfirmacionRecepcionDTO;
import ar.edu.utn.frba.dds.logistica.service.RecepcionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Confirmación de la recepción de una parada con una o varias fotos (la hace la entidad beneficiaria). */
@RestController
@RequestMapping("/api/rutas/{idRuta}/paradas/{idParada}")
public class RecepcionController {

    private final RecepcionService recepcion;

    public RecepcionController(RecepcionService recepcion) {
        this.recepcion = recepcion;
    }

    @Operation(
            summary = "Confirmar la recepción de una parada con fotos",
            description = "Marca como entregadas las entregas en traslado de la parada y les agrega las fotos (de 1 a 5, con las URL "
                    + "que devuelve POST /api/fotos). La ruta tiene que estar en curso. Genera el evento ENTREGA_CONFIRMADA de cada entrega "
                    + "y, si era la última pendiente, finaliza la ruta y libera el camión."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recepción confirmada"),
            @ApiResponse(responseCode = "400", description = "Fotos inválidas (ninguna, más de 5 o con URL que no es http/https)"),
            @ApiResponse(responseCode = "404", description = "No existe la ruta o la parada"),
            @ApiResponse(responseCode = "409", description = "La ruta no está en curso o la parada no tiene entregas en traslado")
    })
    @PostMapping("/recepcion")
    public void confirmar(@PathVariable Integer idRuta, @PathVariable Integer idParada,
                          @Valid @RequestBody ConfirmacionRecepcionDTO pedido) {
        recepcion.confirmar(idRuta, idParada, pedido.getFotos());
    }
}
