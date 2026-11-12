package ar.edu.utn.frba.dds.donaciones.controller;
import ar.edu.utn.frba.dds.donaciones.dto.DonacionDestacadaDTO;
import ar.edu.utn.frba.dds.donaciones.service.ConsultaAdministracionService;
import java.util.List;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/publico/donaciones")
public class DonacionesPublicasController {
    private final ConsultaAdministracionService consultas;
    public DonacionesPublicasController(ConsultaAdministracionService consultas) { this.consultas=consultas; }
    @GetMapping("/destacadas")
    public List<DonacionDestacadaDTO> destacadas(@RequestParam(defaultValue="6") int limite) { return consultas.destacadas(limite); }
}
