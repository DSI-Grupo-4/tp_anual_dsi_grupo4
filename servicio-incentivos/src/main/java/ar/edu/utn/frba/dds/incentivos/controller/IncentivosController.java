package ar.edu.utn.frba.dds.incentivos.controller;

import ar.edu.utn.frba.dds.incentivos.consultor.Beneficiario;
import ar.edu.utn.frba.dds.incentivos.consultor.Consultor;
import ar.edu.utn.frba.dds.incentivos.donacion.DatosDonacion;
import ar.edu.utn.frba.dds.incentivos.donante.Donante;
import ar.edu.utn.frba.dds.incentivos.donante.GestorDonante;
import ar.edu.utn.frba.dds.incentivos.dto.DatosDonacionDTO;
import ar.edu.utn.frba.dds.incentivos.dto.EvolucionMensualDTO;
import ar.edu.utn.frba.dds.incentivos.dto.InsigniaDTO;
import ar.edu.utn.frba.dds.incentivos.dto.MetricasActividadDTO;
import ar.edu.utn.frba.dds.incentivos.dto.MisionDisponibleDTO;
import ar.edu.utn.frba.dds.incentivos.dto.ProgresoInsigniaDTO;
import ar.edu.utn.frba.dds.incentivos.dto.VisibilidadInsigniaDTO;
import ar.edu.utn.frba.dds.incentivos.metricas.EvolucionMensual;
import ar.edu.utn.frba.dds.incentivos.metricas.MetricasActividad;
import ar.edu.utn.frba.dds.incentivos.metricas.Periodo;
import ar.edu.utn.frba.dds.incentivos.misiones.Insignia;
import ar.edu.utn.frba.dds.incentivos.misiones.Mision;
import ar.edu.utn.frba.dds.incentivos.progreso.ProgresoCategoria;
import ar.edu.utn.frba.dds.incentivos.progreso.ProgresoInsignia;
import ar.edu.utn.frba.dds.incentivos.progreso.ProgresoMision;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/api/donantes")
public class IncentivosController {

    private final Consultor consultor = Consultor.getInstance();
    private final GestorDonante gestorDonante = GestorDonante.getInstance();
    private org.springframework.web.client.RestClient donacionesClient;

    @org.springframework.beans.factory.annotation.Value("${donaciones.base-url:http://localhost:8080}")
    public void configurarDonaciones(String url) {
        var transporte = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        transporte.setConnectTimeout(3000); transporte.setReadTimeout(5000);
        donacionesClient = org.springframework.web.client.RestClient.builder().requestFactory(transporte).baseUrl(url).build();
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Sincronizar perfil de donante", description = "Integración desde Donaciones: crea o actualiza identidad y contacto sin sumar donaciones, insignias ni progreso.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @io.swagger.v3.oas.annotations.media.Content(
        schema = @io.swagger.v3.oas.annotations.media.Schema(example = """
        {"nombre":"Ana Perez","medioContactoPreferido":"EMAIL","contactoPreferido":"ana@example.org"}
        """)))
    @PutMapping("/{id}/perfil")
    public org.springframework.http.ResponseEntity<Void> sincronizarPerfil(@PathVariable Long id, @RequestBody java.util.Map<String,String> perfil) {
        if (id == null || id <= 0 || perfil.get("nombre") == null || perfil.get("nombre").isBlank())
            throw new IllegalArgumentException("ID positivo y nombre son obligatorios");
        var medio = perfil.get("medioContactoPreferido"); var contacto = perfil.get("contactoPreferido");
        if (medio == null || !java.util.Set.of("EMAIL","SMS","WHATSAPP").contains(medio) || contacto == null || contacto.isBlank())
            throw new IllegalArgumentException("Medio y contacto preferidos son obligatorios");
        var donante = gestorDonante.obtenerDonante(id,perfil.get("nombre"));
        donante.actualizarNombreSiFalta(perfil.get("nombre"));
        donante.actualizarContactoSiFalta(medio,contacto);
        return org.springframework.http.ResponseEntity.noContent().build();
    }

    private Donante resolverDonante(Long id) {
        try { return gestorDonante.buscarDonante(id); }
        catch (java.util.NoSuchElementException ausente) {
            if (donacionesClient == null) throw ausente;
            try {
                var perfil = donacionesClient.get().uri("/api/donantes/{id}", id).retrieve().body(com.fasterxml.jackson.databind.JsonNode.class);
                if (perfil == null || !perfil.hasNonNull("id") || perfil.get("id").asLong() != id) throw ausente;
                String nombre = "JURIDICA".equals(perfil.path("tipo").asText()) ? perfil.path("razonSocial").asText() :
                    perfil.path("nombre").asText() + " " + perfil.path("apellido").asText();
                var donante = gestorDonante.obtenerDonante(id,nombre);
                for (var contacto : perfil.path("mediosContacto")) {
                    if (contacto.path("esPreferido").asBoolean()) {
                        String medio = contacto.path("tipo").asText();
                        donante.actualizarContactoSiFalta("TELEFONO".equals(medio) ? "SMS" : medio, contacto.path("valor").asText());
                    }
                }
                return donante;
            } catch (org.springframework.web.client.RestClientResponseException e) {
                if (e.getStatusCode().value() == 404) throw ausente;
                throw new org.springframework.web.server.ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"No se pudo consultar el donante en Donaciones",e);
            } catch (org.springframework.web.client.RestClientException e) {
                throw new org.springframework.web.server.ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Donaciones no está disponible para recuperar el perfil",e);
            }
        }
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Obtener metricas", description = "Disponible desde el alta del donante en Donaciones, aunque todavía no haya donado. Si falta el perfil, se recupera desde Donaciones; 404 para un ID inexistente.")
    @GetMapping("/{id}/metricas")
    public MetricasActividadDTO obtenerMetricas(
            @io.swagger.v3.oas.annotations.Parameter(description = "Reemplazar por un ID existente devuelto por el alta o listado.", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.Parameter(description = "HISTORICO, MENSUAL, TRIMESTRAL, SEMESTRAL o ANUAL.", example = "HISTORICO") @RequestParam(defaultValue = "HISTORICO") String periodo) {

        Donante donante = resolverDonante(id);
        Periodo periodoSolicitado = Periodo.valueOf(periodo.toUpperCase());
        MetricasActividad metricas = consultor.obtenerMetricasActividad(donante, periodoSolicitado);
        return convertirADTO(metricas);
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Obtener misiones disponibles", description = "Disponible desde el alta del donante en Donaciones, aunque todavía no haya donado. Si falta el perfil, se recupera desde Donaciones; 404 para un ID inexistente.")
    @GetMapping("/{id}/misiones")
    public List<MisionDisponibleDTO> obtenerMisionesDisponibles(@io.swagger.v3.oas.annotations.Parameter(description = "Reemplazar por un ID existente devuelto por el alta o listado.", example = "1") @PathVariable Long id) {
        Donante donante = resolverDonante(id);
        List<ProgresoCategoria> categoriasObtenidas = donante.getProgresoAsociado().getCategoriasObtenidas();
        String categoriaActual = categoriaActualDe(donante);
        ProgresoMision misionActual = donante.getProgresoAsociado().getMisionActual();

        List<Mision> misionesDisponibles = consultor.obtenerMisionesDisponibles(donante);
        List<ProgresoMision> progresoMisiones = categoriasObtenidas.isEmpty()
                ? List.of()
                : categoriasObtenidas.get(categoriasObtenidas.size() - 1).getMisiones();

        return IntStream.range(0, misionesDisponibles.size())
                .mapToObj(i -> convertirADTO(misionesDisponibles.get(i), progresoMisiones.get(i),
                        categoriaActual, misionActual))
                .toList();
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Obtener insignias", description = "Disponible desde el alta del donante en Donaciones, aunque todavía no haya donado. Si falta el perfil, se recupera desde Donaciones; 404 para un ID inexistente.")
    @GetMapping("/{id}/insignias")
    public List<InsigniaDTO> obtenerInsignias(@io.swagger.v3.oas.annotations.Parameter(description = "Reemplazar por un ID existente devuelto por el alta o listado.", example = "1") @PathVariable Long id) {
        Donante donante = resolverDonante(id);
        return consultor.obtenerInsignias(donante).stream()
                .map(this::convertirADTO)
                .toList();
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Registrar actividad donacion", description = "Usar los IDs devueltos por las operaciones de alta. Los datos de prueba se mantienen en memoria.")
    @PostMapping("/{id}/actividad-donacion")
    public ResponseEntity<ProgresoInsigniaDTO> registrarActividadDonacion(
            @io.swagger.v3.oas.annotations.Parameter(description = "Reemplazar por un ID existente devuelto por el alta o listado.", example = "1") @PathVariable Long id,
            @Valid @RequestBody DatosDonacionDTO dto) {

        Donante donante = gestorDonante.obtenerDonante(id, dto.getDonanteNombre());
        donante.actualizarContactoSiFalta(dto.getDonanteMedioContacto(), dto.getDonanteContacto());
        DatosDonacion datosDonacion = convertirADominio(dto);

        ProgresoInsignia obtenida = consultor.registrarActividadDonacion(donante, datosDonacion);
        if (obtenida == null) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.ok(convertirADTO(obtenida));
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Cambiar visibilidad insignia", description = "404 si el donante todavía no tiene actividad registrada en Incentivos, o si no tiene esa insignia.")
    @PatchMapping("/{id}/insignias/{insigniaNombre}/visibilidad")
    public ResponseEntity<Void> cambiarVisibilidadInsignia(
            @io.swagger.v3.oas.annotations.Parameter(description = "Reemplazar por un ID existente devuelto por el alta o listado.", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.Parameter(description = "Usar una insignia ya obtenida por el donante.", example = "Racha Colaboradora") @PathVariable String insigniaNombre,
            @RequestBody VisibilidadInsigniaDTO dto) {

        Donante donante = resolverDonante(id);
        if (dto.isVisible()) {
            consultor.marcarInsigniaVisible(donante, insigniaNombre);
        } else {
            consultor.ocultarInsignia(donante, insigniaNombre);
        }
        return ResponseEntity.noContent().build();
    }

    private String categoriaActualDe(Donante donante) {
        List<ProgresoCategoria> categoriasObtenidas = donante.getProgresoAsociado().getCategoriasObtenidas();
        if (categoriasObtenidas.isEmpty()) {
            return null;
        }
        return categoriasObtenidas.get(categoriasObtenidas.size() - 1).getNombre();
    }

    private DatosDonacion convertirADominio(DatosDonacionDTO dto) {
        Beneficiario beneficiario = dto.getBeneficiarioId() != null
                ? consultor.obtenerOCrearBeneficiario(dto.getBeneficiarioId(), dto.getBeneficiarioNombre())
                : null;

        DatosDonacion datos = new DatosDonacion(dto.getFecha(), dto.getCategoriaNombre(), dto.getCantidadBienes(), dto.isDonacionExitosa(), beneficiario);
        datos.setDonacionId(dto.getDonacionId());
        return datos;
    }

    private MetricasActividadDTO convertirADTO(MetricasActividad metricas) {
        MetricasActividadDTO dto = new MetricasActividadDTO();
        dto.setDonanteNombre(metricas.getDonanteNombre());
        dto.setCategoriaActual(metricas.getCategoriaActual());
        dto.setPeriodo(metricas.getPeriodo().name());
        dto.setSolicitudesDonacionHechas(metricas.getSolicitudesDonacionHechas());
        dto.setBeneficiariosAyudados(metricas.getBeneficiariosAyudados());
        dto.setMisionesCompletadas(metricas.getMisionesCompletadas());
        dto.setInsigniasObtenidas(metricas.getInsigniasObtenidas());
        dto.setImpactoAcumulado(metricas.getImpactoAcumulado());
        dto.setPosicionRanking(metricas.getPosicionRanking());
        dto.setEvolucionMensual(metricas.getEvolucionMensual().stream()
                .map(this::convertirADTO)
                .toList());
        return dto;
    }

    private EvolucionMensualDTO convertirADTO(EvolucionMensual evolucionMensual) {
        EvolucionMensualDTO dto = new EvolucionMensualDTO();
        dto.setMes(evolucionMensual.getMes().toString());
        dto.setSolicitudes(evolucionMensual.getSolicitudes());
        dto.setImpacto(evolucionMensual.getImpacto());
        return dto;
    }

    private MisionDisponibleDTO convertirADTO(Mision mision, ProgresoMision progresoMision,
                                               String categoriaNombre, ProgresoMision misionActual) {
        MisionDisponibleDTO dto = new MisionDisponibleDTO();
        dto.setNombreMision(mision.getNombreMision());
        dto.setCategoriaNombre(categoriaNombre);
        dto.setInsigniaNombre(mision.getInsigniaAsociada().getNombre());
        dto.setProgresoActual(progresoMision.obtenerProgresoActual());
        dto.setDistanciaRestante(progresoMision.distanciaRestante());
        dto.setCompletada(progresoMision.getInsigniaObtenida() != null);
        dto.setActiva(progresoMision == misionActual);
        return dto;
    }

    private InsigniaDTO convertirADTO(Insignia insignia) {
        InsigniaDTO dto = new InsigniaDTO();
        dto.setNombre(insignia.getNombre());
        dto.setImagenUrl(insignia.getImagenUrl());
        return dto;
    }

    private ProgresoInsigniaDTO convertirADTO(ProgresoInsignia progresoInsignia) {
        ProgresoInsigniaDTO dto = new ProgresoInsigniaDTO();
        dto.setInsigniaNombre(progresoInsignia.getInsigniaAsociada().getNombre());
        dto.setImagenUrl(progresoInsignia.getInsigniaAsociada().getImagenUrl());
        dto.setFechaObtencion(progresoInsignia.getFechaObtencion());
        dto.setVisible(progresoInsignia.isVisible());
        return dto;
    }
}
