package ar.edu.utn.frba.dds.donaciones.controller;

import ar.edu.utn.frba.dds.donaciones.dto.DonanteDTO;
import ar.edu.utn.frba.dds.donaciones.dto.PersonaHumanaDTO;
import ar.edu.utn.frba.dds.donaciones.dto.PersonaJuridicaDTO;
import ar.edu.utn.frba.dds.donaciones.service.DonanteService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/donantes")
public class DonanteController {

    private final DonanteService donanteService;

    public DonanteController(DonanteService donanteService) {
        this.donanteService = donanteService;
    }

    @GetMapping
    public List<DonanteDTO> obtenerTodos() {
        return donanteService.obtenerTodos();
    }

    @GetMapping("/{id}")
    public DonanteDTO obtenerPorId(@PathVariable Long id) {
        return donanteService.buscarPorId(id);
    }

    @PostMapping("/humanos")
    @ResponseStatus(HttpStatus.CREATED)
    public DonanteDTO crearHumano(@RequestBody PersonaHumanaDTO dto) {
        return donanteService.crearDonanteHumano(dto);
    }

    @PostMapping("/juridicos")
    @ResponseStatus(HttpStatus.CREATED)
    public DonanteDTO crearJuridico(@RequestBody PersonaJuridicaDTO dto) {
        return donanteService.crearDonanteJuridico(dto);
    }

    @PutMapping("/{id}")
    public DonanteDTO actualizar(@PathVariable Long id, @RequestBody DonanteDTO dto) {
        if ("HUMANA".equalsIgnoreCase(dto.getTipo())) {
            PersonaHumanaDTO humanaDTO = new PersonaHumanaDTO();
            humanaDTO.setNombre(dto.getNombre());
            humanaDTO.setApellido(dto.getApellido());
            humanaDTO.setDocumento(dto.getDocumento());
            return donanteService.actualizarHumano(id, humanaDTO);
        }
        PersonaJuridicaDTO juridicaDTO = new PersonaJuridicaDTO();
        juridicaDTO.setRazonSocial(dto.getRazonSocial());
        return donanteService.actualizarJuridico(id, juridicaDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        donanteService.eliminar(id);
    }
}
