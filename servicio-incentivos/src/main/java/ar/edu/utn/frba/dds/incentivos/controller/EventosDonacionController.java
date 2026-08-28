package ar.edu.utn.frba.dds.incentivos.controller;

import ar.edu.utn.frba.dds.incentivos.client.N8nWebhookClient;
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

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/incentivos/eventos")
public class EventosDonacionController {

    private final CuentaDonanteService cuentaDonanteService;
    private final N8nWebhookClient n8nWebhookClient;

    public EventosDonacionController(CuentaDonanteService cuentaDonanteService,
                                     N8nWebhookClient n8nWebhookClient) {
        this.cuentaDonanteService = cuentaDonanteService;
        this.n8nWebhookClient = n8nWebhookClient;
    }

    @Operation(
            summary = "Registrar solicitud de donación",
            description = "Registra una solicitud de donación realizada por un donante y notifica el evento mediante un webhook."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Solicitud de donación registrada correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de la solicitud inválidos"
            )
    })
    @PostMapping("/solicitud-donacion")
    public ResponseEntity<Void> solicitudDonacion(@RequestBody SolicitudDonacionRequest req) {
        cuentaDonanteService.registrarDonacion(req.getDonanteId(), req.getCantItems(), req.getCantTypes(), req.getFecha());
        n8nWebhookClient.notificarSolicitudDonacion(req.getDonanteId(), req.getCantItems(), req.getCantTypes(), LocalDate.parse(req.getFecha()));
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Registrar donación entregada",
            description = "Registra la entrega de una donación para los donantes indicados y notifica el evento mediante un webhook."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Donación entregada registrada correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de la entrega inválidos"
            )
    })
    @PostMapping("/donacion-entregada")
    public ResponseEntity<Void> donacionEntregada(@RequestBody DonacionEntregadaRequest req) {
        req.getDonanteIds().forEach(id -> cuentaDonanteService.registrarEntrega(id, req.getBeneficiario()));
        n8nWebhookClient.notificarDonacionEntregada(req.getDonanteIds(), req.getBeneficiario(), LocalDate.parse(req.getFecha()));
        return ResponseEntity.ok().build();
    }

    @Getter @Setter
    public static class SolicitudDonacionRequest {
        private int donanteId;
        private int cantItems;
        private int cantTypes;
        private String fecha;
    }

    @Getter @Setter
    public static class DonacionEntregadaRequest {
        private List<Integer> donanteIds;
        private String beneficiario;
        private String fecha;
    }
}
