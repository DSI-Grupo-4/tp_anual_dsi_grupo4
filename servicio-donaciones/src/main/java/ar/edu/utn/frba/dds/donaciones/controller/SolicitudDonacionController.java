package ar.edu.utn.frba.dds.donaciones.controller;

import ar.edu.utn.frba.dds.donaciones.service.SolicitudDonacionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Solicitudes de donación", description = "Carga original (antes de segmentar) de una donación")
@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudDonacionController {

    private final SolicitudDonacionService solicitudDonacionService;

    public SolicitudDonacionController(SolicitudDonacionService solicitudDonacionService) {
        this.solicitudDonacionService = solicitudDonacionService;
    }

    @io.swagger.v3.oas.annotations.Operation(
            summary = "Eliminar",
            description = "Borrado físico, solo si la solicitud todavía no generó ninguna Donacion (el alta actual segmenta y registra la/s Donacion en el mismo paso, así que esto es poco frecuente en el flujo normal). Devuelve 409 si ya generó alguna. Borra también sus ItemDonado.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@io.swagger.v3.oas.annotations.Parameter(description = "Reemplazar por un ID existente devuelto por el alta.", example = "1") @PathVariable Long id) {
        solicitudDonacionService.eliminar(id);
    }
}
