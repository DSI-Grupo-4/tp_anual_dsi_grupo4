package ar.edu.utn.frba.dds.donaciones.controller;

import ar.edu.utn.frba.dds.donaciones.dto.DonanteDTO;
import ar.edu.utn.frba.dds.donaciones.dto.PersonaHumanaDTO;
import ar.edu.utn.frba.dds.donaciones.dto.PersonaJuridicaDTO;
import ar.edu.utn.frba.dds.donaciones.service.DonanteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Donantes", description = "Alta, edición e importación masiva por CSV de personas donantes (humanas o jurídicas)")
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
    public DonanteDTO crearHumano(@Valid @RequestBody PersonaHumanaDTO dto) {
        return donanteService.crearDonanteHumano(dto);
    }

    @PostMapping("/juridicos")
    @ResponseStatus(HttpStatus.CREATED)
    public DonanteDTO crearJuridico(@Valid @RequestBody PersonaJuridicaDTO dto) {
        return donanteService.crearDonanteJuridico(dto);
    }

    @PutMapping("/{id}")
    public DonanteDTO actualizar(@PathVariable Long id, @RequestBody DonanteDTO dto) {
        // PUT reemplaza el estado completo: el cliente debe mandar todos los
        // campos que quiere conservar (semántica estándar de PUT). El tipo
        // (humano/jurídico) NO lo decide el body — es inmutable y se
        // determina en DonanteService a partir del donante real ya guardado;
        // antes se confiaba en dto.getTipo() y un mismatch tiraba
        // ClassCastException (500 sin manejar, ver scan de calidad).
        return donanteService.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        donanteService.eliminar(id);
    }

    @PostMapping("/importar")
    public List<DonanteDTO> importar(
            @RequestParam MultipartFile archivo) {

        return donanteService.importarCSV(archivo);
    }
}
