package ar.edu.utn.frba.dds.logistica.controller;

import ar.edu.utn.frba.dds.logistica.dto.UbicacionCamionDTO;
import ar.edu.utn.frba.dds.logistica.dto.UbicacionRequestDTO;
import ar.edu.utn.frba.dds.logistica.service.UbicacionCamionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Ubicación de los camiones: la informa el chofer y la consultan las pantallas de seguimiento. */
@RestController
@RequestMapping("/api/camiones")
public class UbicacionCamionController {

    private final UbicacionCamionService ubicacionService;

    public UbicacionCamionController(UbicacionCamionService ubicacionService) {
        this.ubicacionService = ubicacionService;
    }

    @Operation(
            summary = "Informar la ubicación de un camión",
            description = "Guarda la latitud y la longitud del camión. La fecha y hora de la actualización las pone el servidor (UTC). "
                    + "Lo usa el chofer desde su dispositivo; no invoca a ningún otro servicio."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ubicación guardada"),
            @ApiResponse(responseCode = "400", description = "Coordenadas inválidas"),
            @ApiResponse(responseCode = "404", description = "No existe el camión")
    })
    @PutMapping("/{id}/ubicacion")
    public UbicacionCamionDTO informar(@PathVariable Integer id, @Valid @RequestBody UbicacionRequestDTO pedido) {
        return ubicacionService.reportar(id, pedido);
    }

    @Operation(
            summary = "Obtener la ubicación de un camión",
            description = "Devuelve la última ubicación informada del camión, con su fecha y hora (UTC)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ubicación obtenida"),
            @ApiResponse(responseCode = "404", description = "No existe el camión o todavía no informó su ubicación")
    })
    @GetMapping("/{id}/ubicacion")
    public UbicacionCamionDTO obtener(@PathVariable Integer id) {
        return ubicacionService.ubicacion(id);
    }

    @Operation(
            summary = "Obtener las ubicaciones de todos los camiones",
            description = "Devuelve la última ubicación de cada camión que ya informó una (los que nunca informaron no aparecen)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ubicaciones obtenidas")
    })
    @GetMapping("/ubicaciones")
    public List<UbicacionCamionDTO> obtenerTodas() {
        return ubicacionService.ubicaciones();
    }
}
