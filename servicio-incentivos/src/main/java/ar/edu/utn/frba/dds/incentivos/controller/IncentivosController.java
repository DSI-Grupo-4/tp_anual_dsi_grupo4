package ar.edu.utn.frba.dds.incentivos.controller;

import ar.edu.utn.frba.dds.incentivos.consultor.Consultor;
import ar.edu.utn.frba.dds.incentivos.donacion.DatosDonacion;
import ar.edu.utn.frba.dds.incentivos.donante.Donante;
import ar.edu.utn.frba.dds.incentivos.donante.GestorDonante;
import ar.edu.utn.frba.dds.incentivos.dto.DatosDonacionDTO;
import ar.edu.utn.frba.dds.incentivos.dto.InsigniaDTO;
import ar.edu.utn.frba.dds.incentivos.dto.MetricasActividadDTO;
import ar.edu.utn.frba.dds.incentivos.dto.MisionDisponibleDTO;
import ar.edu.utn.frba.dds.incentivos.dto.ProgresoInsigniaDTO;
import ar.edu.utn.frba.dds.incentivos.metricas.MetricasActividad;
import ar.edu.utn.frba.dds.incentivos.metricas.Periodo;
import ar.edu.utn.frba.dds.incentivos.misiones.Categoria;
import ar.edu.utn.frba.dds.incentivos.misiones.GestorMisiones;
import ar.edu.utn.frba.dds.incentivos.misiones.Insignia;
import ar.edu.utn.frba.dds.incentivos.misiones.Mision;
import ar.edu.utn.frba.dds.incentivos.progreso.ProgresoCategoria;
import ar.edu.utn.frba.dds.incentivos.progreso.ProgresoInsignia;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/donantes")
public class IncentivosController {

    private final Consultor consultor = Consultor.getInstance();
    private final GestorDonante gestorDonante = GestorDonante.getInstance();
    private final GestorMisiones gestorMisiones = GestorMisiones.getInstance();

    @GetMapping("/{id}/metricas")
    public MetricasActividadDTO obtenerMetricas(
            @PathVariable Long id,
            @RequestParam(defaultValue = "HISTORICO") String periodo) {

        Donante donante = gestorDonante.obtenerDonante(id);
        Periodo periodoSolicitado = Periodo.valueOf(periodo.toUpperCase());
        MetricasActividad metricas = consultor.obtenerMetricasActividad(donante, periodoSolicitado);
        return convertirADTO(metricas);
    }

    @GetMapping("/{id}/misiones")
    public List<MisionDisponibleDTO> obtenerMisionesDisponibles(@PathVariable Long id) {
        Donante donante = gestorDonante.obtenerDonante(id);
        String categoriaActual = categoriaActualDe(donante);

        return consultor.obtenerMisionesDisponibles(donante).stream()
                .map(mision -> convertirADTO(mision, categoriaActual))
                .toList();
    }

    @GetMapping("/{id}/insignias")
    public List<InsigniaDTO> obtenerInsignias(@PathVariable Long id) {
        Donante donante = gestorDonante.obtenerDonante(id);
        return consultor.obtenerInsignias(donante).stream()
                .map(this::convertirADTO)
                .toList();
    }

    @PostMapping("/{id}/actividad-donacion")
    public ResponseEntity<ProgresoInsigniaDTO> registrarActividadDonacion(
            @PathVariable Long id,
            @RequestBody DatosDonacionDTO dto) {

        Donante donante = gestorDonante.obtenerDonante(id);
        DatosDonacion datosDonacion = convertirADominio(dto);

        ProgresoInsignia obtenida = consultor.registrarActividadDonacion(donante, datosDonacion);
        if (obtenida == null) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.ok(convertirADTO(obtenida));
    }

    private String categoriaActualDe(Donante donante) {
        List<ProgresoCategoria> categoriasObtenidas = donante.getProgresoAsociado().getCategoriasObtenidas();
        if (categoriasObtenidas.isEmpty()) {
            return null;
        }
        return categoriasObtenidas.get(categoriasObtenidas.size() - 1).getNombre();
    }

    private DatosDonacion convertirADominio(DatosDonacionDTO dto) {
        Categoria categoria = gestorMisiones.buscarCategoriaPorNombre(dto.getCategoriaNombre())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe la categoría: " + dto.getCategoriaNombre()));

        return new DatosDonacion(dto.getFecha(), categoria, dto.getCantidadBienes(), dto.isDonacionExitosa());
    }

    private MetricasActividadDTO convertirADTO(MetricasActividad metricas) {
        MetricasActividadDTO dto = new MetricasActividadDTO();
        dto.setPeriodo(metricas.getPeriodo().name());
        dto.setSolicitudesDonacionHechas(metricas.getSolicitudesDonacionHechas());
        dto.setBeneficiariosAyudados(metricas.getBeneficiariosAyudados());
        dto.setMisionesCompletadas(metricas.getMisionesCompletadas());
        dto.setInsigniasObtenidas(metricas.getInsigniasObtenidas());
        return dto;
    }

    private MisionDisponibleDTO convertirADTO(Mision mision, String categoriaNombre) {
        MisionDisponibleDTO dto = new MisionDisponibleDTO();
        dto.setNombreMision(mision.getNombreMision());
        dto.setCategoriaNombre(categoriaNombre);
        dto.setInsigniaNombre(mision.getInsigniaAsociada().getNombre());
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
