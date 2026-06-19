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

    @PostMapping("/misiones/completar")
    public ResponseEntity<Void> completarMision(@RequestBody CompletarMisionRequest req) {
        cuentaDonanteService.completarMision(req.getDonanteId(), req.getMisionId());
        notificacionesClient.notificarMisionCompletada(req.getDonanteId(), "Misión #" + req.getMisionId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/insignias/otorgar")
    public ResponseEntity<Void> otorgarInsignia(@RequestBody OtorgarInsigniaRequest req) {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/categoria/subir")
    public ResponseEntity<Void> subirCategoria(@RequestBody SubirCategoriaRequest req) {
        CategoriasDonante nueva = CategoriasDonante.valueOf(req.getNuevaCategoria().toUpperCase());
        cuentaDonanteService.subirCategoria(req.getDonanteId(), nueva);
        notificacionesClient.notificarSubidaCategoria(req.getDonanteId(), req.getNuevaCategoria());
        return ResponseEntity.ok().build();
    }

    @Getter @Setter
    public static class CompletarMisionRequest {
        private int donanteId;
        private int misionId;
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
    }
}
