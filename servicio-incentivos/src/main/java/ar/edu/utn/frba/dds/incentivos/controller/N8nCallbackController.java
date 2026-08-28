package ar.edu.utn.frba.dds.incentivos.controller;

import ar.edu.utn.frba.dds.incentivos.client.NotificacionesClient;
import ar.edu.utn.frba.dds.incentivos.domain.personas.CategoriasDonante;
import ar.edu.utn.frba.dds.incentivos.service.CuentaDonanteService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/incentivos")
public class N8nCallbackController {

    private final CuentaDonanteService cuentaDonanteService;
    private final NotificacionesClient notificacionesClient;

    public N8nCallbackController(CuentaDonanteService cuentaDonanteService,
                                 NotificacionesClient notificacionesClient) {
        this.cuentaDonanteService = cuentaDonanteService;
        this.notificacionesClient = notificacionesClient;
    }

    @Operation(
            summary = "Completar una misión",
            description = "Registra la finalización de una misión para un donante y notifica el evento."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Misión completada correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de la solicitud inválidos"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Donante o misión no encontrada"
            )
    })
    @PostMapping("/misiones/completar")
    public ResponseEntity<Void> completarMision(@RequestBody CompletarMisionRequest req) {
        cuentaDonanteService.completarMision(req.getDonanteId(), req.getMisionId());
        notificacionesClient.notificarMisionCompletada(req.getDonanteId(), "Mision #" + req.getMisionId(), req.getMedio(), req.getContacto());
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Otorgar una insignia",
            description = "Registra el otorgamiento de una insignia a un donante."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Insignia otorgada correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de la solicitud inválidos"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Donante o insignia no encontrada"
            )
    })
    @PostMapping("/insignias/otorgar")
    public ResponseEntity<Void> otorgarInsignia(@RequestBody OtorgarInsigniaRequest req) {
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Subir categoría de un donante",
            description = "Actualiza la categoría de un donante y notifica el cambio."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Categoría actualizada correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Categoría o datos de la solicitud inválidos"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Donante no encontrado"
            )
    })
    @PostMapping("/categoria/subir")
    public ResponseEntity<Void> subirCategoria(@RequestBody SubirCategoriaRequest req) {
        CategoriasDonante nueva = CategoriasDonante.valueOf(req.getNuevaCategoria().toUpperCase());
        cuentaDonanteService.subirCategoria(req.getDonanteId(), nueva);
        notificacionesClient.notificarSubidaCategoria(req.getDonanteId(), req.getNuevaCategoria(), req.getMedio(), req.getContacto());
        return ResponseEntity.ok().build();
    }

    @Getter @Setter
    public static class CompletarMisionRequest {
        private int donanteId;
        private int misionId;
        private String medio;
        private String contacto;
    }

    @Getter @Setter
    public static class OtorgarInsigniaRequest {
        private int donanteId;
        private int insigniaId;
    }

    @Getter @Setter
    public static class SubirCategoriaRequest {
        private int donanteId;
        private String nuevaCategoria;
        private String medio;
        private String contacto;
    }
}
