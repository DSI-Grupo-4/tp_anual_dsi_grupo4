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

    @PostMapping("/solicitud-donacion")
    public ResponseEntity<Void> solicitudDonacion(@RequestBody SolicitudDonacionRequest req) {
        cuentaDonanteService.registrarDonacion(req.getDonanteId(), req.getCantItems(), req.getCantTypes(), req.getFecha());
        n8nWebhookClient.notificarSolicitudDonacion(req.getDonanteId(), req.getCantItems(), req.getCantTypes(), LocalDate.parse(req.getFecha()));
        return ResponseEntity.ok().build();
    }

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
