package ar.edu.utn.frba.dds.donaciones.controller;

import ar.edu.utn.frba.dds.donaciones.dto.DonanteDTO;
import ar.edu.utn.frba.dds.donaciones.dto.PersonaHumanaDTO;
import ar.edu.utn.frba.dds.donaciones.dto.PersonaJuridicaDTO;
import ar.edu.utn.frba.dds.donaciones.service.DonanteService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.List;

@RestController
@RequestMapping("/api/donantes")
public class DonanteController {

    private final DonanteService donanteService;

    public DonanteController(DonanteService donanteService) {
        this.donanteService = donanteService;
    }

    @Operation(
            summary = "Obtener todos los donantes",
            description = "Obtiene la lista de todos los donantes registrados en el sistema."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Donantes obtenidos correctamente"
            )
    })
    @GetMapping
    public List<DonanteDTO> obtenerTodos() {
        return donanteService.obtenerTodos();
    }

    @Operation(
            summary = "Obtener un donante",
            description = "Obtiene un donante a partir de su identificador."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Donante encontrado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No se encontró un donante con el ID indicado"
            )
    })
    @GetMapping("/{id}")
    public DonanteDTO obtenerPorId(@PathVariable Long id) {
        return donanteService.buscarPorId(id);
    }

    @Operation(
            summary = "Crear un donante humano",
            description = "Registra un nuevo donante correspondiente a una persona humana."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Donante humano creado correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos del donante inválidos"
            )
    })
    @PostMapping("/humanos")
    @ResponseStatus(HttpStatus.CREATED)
    public DonanteDTO crearHumano(@RequestBody PersonaHumanaDTO dto) {
        return donanteService.crearDonanteHumano(dto);
    }

    @Operation(
            summary = "Crear un donante jurídico",
            description = "Registra un nuevo donante correspondiente a una persona jurídica."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Donante jurídico creado correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos del donante inválidos"
            )
    })
    @PostMapping("/juridicos")
    @ResponseStatus(HttpStatus.CREATED)
    public DonanteDTO crearJuridico(@RequestBody PersonaJuridicaDTO dto) {
        return donanteService.crearDonanteJuridico(dto);
    }

    @Operation(
            summary = "Actualizar un donante",
            description = "Actualiza los datos de un donante existente según su tipo."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Donante actualizado correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos del donante inválidos"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Donante no encontrado"
            )
    })
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

    @Operation(
            summary = "Eliminar un donante",
            description = "Elimina un donante utilizando su identificador."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Donante eliminado correctamente"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Donante no encontrado"
            )
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        donanteService.eliminar(id);
    }

    @Operation(
            summary = "Importar donantes desde un archivo CSV",
            description = "Importa donantes a partir de un archivo CSV."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Donantes importados correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Archivo inválido o formato incorrecto"
            )
    })
    @PostMapping("/importar")
    public List<DonanteDTO> importar(
            @RequestParam MultipartFile archivo) {

        return donanteService.importarCSV(archivo);
    }
}
