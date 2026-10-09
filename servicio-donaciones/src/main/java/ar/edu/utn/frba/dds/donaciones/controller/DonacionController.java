package ar.edu.utn.frba.dds.donaciones.controller;

import ar.edu.utn.frba.dds.donaciones.domain.donaciones.Donacion;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.ResultadoMatchmaking;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import ar.edu.utn.frba.dds.donaciones.dto.AsignarEntidadDTO;
import ar.edu.utn.frba.dds.donaciones.dto.ActualizarDonacionDTO;
import ar.edu.utn.frba.dds.donaciones.dto.CambioEstadoDTO;
import ar.edu.utn.frba.dds.donaciones.dto.CargaDonacionDTO;
import ar.edu.utn.frba.dds.donaciones.dto.DonacionDTO;
import ar.edu.utn.frba.dds.donaciones.dto.DonacionPendienteDTO;
import ar.edu.utn.frba.dds.donaciones.dto.ResultadoMatchmakingDTO;
import ar.edu.utn.frba.dds.donaciones.dto.TimeStampDTO;
import ar.edu.utn.frba.dds.donaciones.service.DonacionService;
import ar.edu.utn.frba.dds.donaciones.service.EntidadBeneficiariaService;
import ar.edu.utn.frba.dds.donaciones.service.MatchmakingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Donaciones", description = "Alta por carga múltiple, segmentación, estados y matchmaking de donaciones")
@RestController
@RequestMapping("/api/donaciones")
public class DonacionController {

    private final DonacionService donacionService;
    private final MatchmakingService matchmakingService;
    private final EntidadBeneficiariaService entidadBeneficiariaService;

    public DonacionController(
            DonacionService donacionService,
            MatchmakingService matchmakingService,
            EntidadBeneficiariaService entidadBeneficiariaService) {
        this.donacionService = donacionService;
        this.matchmakingService = matchmakingService;
        this.entidadBeneficiariaService = entidadBeneficiariaService;
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Crear", description = "Crear antes un donante. Valida toda la carga y agrupa por subcategoría, unidad, condición y vencimiento.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public List<DonacionDTO> crear(@Valid @RequestBody CargaDonacionDTO dto) {
        return donacionService.crear(dto);
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Obtener todas", description = "Usar los IDs devueltos por las operaciones de alta. Los datos de prueba se mantienen en memoria.")
    @GetMapping
    public List<DonacionDTO> obtenerTodas() {
        return donacionService.obtenerTodas();
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Obtener pendientes", description = "Usar los IDs devueltos por las operaciones de alta. Los datos de prueba se mantienen en memoria.")
    @GetMapping("/pendientes")
    public List<DonacionPendienteDTO> obtenerPendientes(
            @io.swagger.v3.oas.annotations.Parameter(description = "Página comenzando en cero.", example = "0") @RequestParam(defaultValue = "0") int page,
            @io.swagger.v3.oas.annotations.Parameter(description = "Cantidad por página (1 a 100).", example = "20") @RequestParam(defaultValue = "100") int size) {
        return donacionService.obtenerPendientes(page, size);
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Obtener por id", description = "Usar los IDs devueltos por las operaciones de alta. Los datos de prueba se mantienen en memoria.")
    @GetMapping("/{id}")
    public DonacionDTO obtenerPorId(@io.swagger.v3.oas.annotations.Parameter(description = "Reemplazar por un ID existente devuelto por el alta o listado.", example = "1") @PathVariable Long id) {
        return donacionService.obtenerPorId(id);
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Actualizar", description = "Reemplaza todos los items de una donación EN_DEPOSITO. Deben conservar una única clave de segmentación; conserva origen e historial.")
    @PutMapping("/{id}")
    public DonacionDTO actualizar(@io.swagger.v3.oas.annotations.Parameter(description = "Reemplazar por un ID existente devuelto por el alta o listado.", example = "1") @PathVariable Long id, @Valid @RequestBody ActualizarDonacionDTO dto) {
        return donacionService.actualizar(id, dto);
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Eliminar", description = "Usar los IDs devueltos por las operaciones de alta. Los datos de prueba se mantienen en memoria.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@io.swagger.v3.oas.annotations.Parameter(description = "Reemplazar por un ID existente devuelto por el alta o listado.", example = "1") @PathVariable Long id) {
        donacionService.eliminar(id);
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Cambiar estado", description = "Ejemplo posterior a asignar: LISTA_PARA_ENTREGAR. Luego EN_TRASLADO y ENTREGADA. ENTREGA_FALLIDA requiere justificación.")
    @PatchMapping("/{id}/estado")
    public DonacionDTO cambiarEstado(
            @io.swagger.v3.oas.annotations.Parameter(description = "Reemplazar por un ID existente devuelto por el alta o listado.", example = "1") @PathVariable Long id,
            @Valid @RequestBody CambioEstadoDTO dto) {
        return donacionService.cambiarEstado(id, dto);
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Historial", description = "Usar los IDs devueltos por las operaciones de alta. Los datos de prueba se mantienen en memoria.")
    @GetMapping("/{id}/historial")
    public List<TimeStampDTO> historial(@io.swagger.v3.oas.annotations.Parameter(description = "Reemplazar por un ID existente devuelto por el alta o listado.", example = "1") @PathVariable Long id) {
        return donacionService.obtenerHistorial(id);
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Candidatas", description = "Ejecuta a demanda los algoritmos de Compatibilidad Semántica y Prioridad a sub-atendidos sobre esta donación y guarda el resultado (las 3 listas, sin colapsar) como el matchmaking vigente para /asignar. Usar antes de asignar: /asignar solo acepta una entidad que haya aparecido en alguna de estas 3 listas.")
    @GetMapping("/{id}/candidatas")
    public ResultadoMatchmakingDTO candidatas(@io.swagger.v3.oas.annotations.Parameter(description = "Reemplazar por un ID existente devuelto por el alta o listado.", example = "1") @PathVariable Long id) {
        Donacion donacion = donacionService.obtenerDominioPorId(id);
        ResultadoMatchmaking resultado = matchmakingService.ejecutarMatchmaking(donacion);
        donacionService.registrarResultadoMatchmaking(donacion, resultado);
        return entidadBeneficiariaService.convertirResultadoADTO(
                resultado, donacion.getSubcategoria(), donacion.getUnidadMedida());
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Asignar", description = "Confirma la entidad final. Requiere haber corrido antes GET /{id}/candidatas: 409 si todavía no se ejecutó el matchmaking, 400 si la entidad no apareció en ninguna de sus 3 listas. Si la entidad tiene una necesidad pendiente de la misma subcategoría y unidad, se vincula y se descuenta la cantidad contra ella automáticamente.")
    @PostMapping("/{id}/asignar")
    public DonacionDTO asignar(
            @io.swagger.v3.oas.annotations.Parameter(description = "Reemplazar por un ID existente devuelto por el alta o listado.", example = "1") @PathVariable Long id,
            @Valid @RequestBody AsignarEntidadDTO dto) {
        EntidadBeneficiaria entidad =
                entidadBeneficiariaService.buscarEntidad(dto.getEntidadId());
        return donacionService.confirmarAsignacion(id, entidad);
    }
}
