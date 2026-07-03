package ar.edu.utn.frba.dds.incentivos.consultor;

import ar.edu.utn.frba.dds.incentivos.donacion.DatosDonacion;
import ar.edu.utn.frba.dds.incentivos.donante.Donante;
import ar.edu.utn.frba.dds.incentivos.donante.GestorDonante;
import ar.edu.utn.frba.dds.incentivos.dto.NotificacionEventoDTO;
import ar.edu.utn.frba.dds.incentivos.dto.PublicacionInsigniaDTO;
import ar.edu.utn.frba.dds.incentivos.metricas.EvolucionMensual;
import ar.edu.utn.frba.dds.incentivos.metricas.MetricasActividad;
import ar.edu.utn.frba.dds.incentivos.metricas.MetricasSistema;
import ar.edu.utn.frba.dds.incentivos.metricas.Periodo;
import ar.edu.utn.frba.dds.incentivos.misiones.GestorMisiones;
import ar.edu.utn.frba.dds.incentivos.misiones.Insignia;
import ar.edu.utn.frba.dds.incentivos.misiones.Mision;
import ar.edu.utn.frba.dds.incentivos.progreso.ProgresoCategoria;
import ar.edu.utn.frba.dds.incentivos.progreso.ProgresoInsignia;
import ar.edu.utn.frba.dds.incentivos.progreso.ProgresoMision;
import ar.edu.utn.frba.dds.incentivos.ranking.HistorialRanking;
import ar.edu.utn.frba.dds.incentivos.ranking.Ranking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Consultor {

    private static final Logger log = LoggerFactory.getLogger(Consultor.class);

    private static Consultor instancia;

    private final GestorMisiones misiones;
    private final GestorDonante donantes;
    private final HistorialRanking rankings;
    private final Set<Beneficiario> beneficiarios;
    private final RestTemplate restTemplate;
    private String webhookN8nUrl;
    private String notificacionesBaseUrl;

    private Consultor() {
        this.misiones = GestorMisiones.getInstance();
        this.donantes = GestorDonante.getInstance();
        this.rankings = HistorialRanking.getInstance();
        this.beneficiarios = new HashSet<>();
        this.restTemplate = new RestTemplate();
        this.webhookN8nUrl = "";
        this.notificacionesBaseUrl = "";
    }

    public static synchronized Consultor getInstance() {
        if (instancia == null) {
            instancia = new Consultor();
        }
        return instancia;
    }

    public void configurarWebhookN8n(String webhookN8nUrl) {
        this.webhookN8nUrl = webhookN8nUrl;
    }

    public void configurarNotificaciones(String notificacionesBaseUrl) {
        this.notificacionesBaseUrl = notificacionesBaseUrl;
    }

    public MetricasActividad obtenerMetricasActividad(Donante donante, Periodo periodo) {
        LocalDate hoy = LocalDate.now();
        LocalDate desde = periodo.fechaDesde(hoy);

        List<DatosDonacion> donacionesEnPeriodo = donante.getHistorialDonaciones().stream()
                .filter(d -> !d.getFecha().isBefore(desde))
                .toList();

        int solicitudes = donacionesEnPeriodo.size();
        long beneficiariosDistintos = donacionesEnPeriodo.stream()
                .map(DatosDonacion::getBeneficiario)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        int impactoAcumulado = donacionesEnPeriodo.stream()
                .mapToInt(DatosDonacion::getCantidadBienes)
                .sum();

        int misionesCompletadas = (int) donante.getProgresoAsociado().getCategoriasObtenidas().stream()
                .flatMap(categoria -> categoria.getMisiones().stream())
                .map(ProgresoMision::getInsigniaObtenida)
                .filter(Objects::nonNull)
                .filter(insignia -> !insignia.getFechaObtencion().isBefore(desde))
                .count();

        Integer posicionRanking = rankings.obtenerPosicionActual(donante).orElse(null);
        List<EvolucionMensual> evolucionMensual = calcularEvolucionMensual(donacionesEnPeriodo);

        return new MetricasActividad(
                donante.getNombre(),
                categoriaActualDe(donante),
                periodo,
                solicitudes,
                (int) beneficiariosDistintos,
                misionesCompletadas,
                misionesCompletadas,
                impactoAcumulado,
                posicionRanking,
                evolucionMensual
        );
    }

    private String categoriaActualDe(Donante donante) {
        List<ProgresoCategoria> categoriasObtenidas = donante.getProgresoAsociado().getCategoriasObtenidas();
        if (categoriasObtenidas.isEmpty()) {
            return null;
        }
        return categoriasObtenidas.get(categoriasObtenidas.size() - 1).getNombre();
    }

    private List<EvolucionMensual> calcularEvolucionMensual(List<DatosDonacion> donaciones) {
        Map<YearMonth, List<DatosDonacion>> porMes = donaciones.stream()
                .collect(Collectors.groupingBy(d -> YearMonth.from(d.getFecha())));

        return porMes.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new EvolucionMensual(
                        entry.getKey(),
                        entry.getValue().size(),
                        entry.getValue().stream().mapToInt(DatosDonacion::getCantidadBienes).sum()))
                .toList();
    }

    public MetricasSistema obtenerMetricasSistema() {
        List<Donante> todosLosDonantes = donantes.listarDonantes();

        int donantesActivos = (int) todosLosDonantes.stream()
                .filter(donante -> donante.getSolicitudesDonacionHechas() > 0)
                .count();

        int solicitudesTotales = todosLosDonantes.stream()
                .mapToInt(Donante::getSolicitudesDonacionHechas)
                .sum();

        int misionesCompletadasTotales = todosLosDonantes.stream()
                .mapToInt(donante -> (int) donante.getProgresoAsociado().getCategoriasObtenidas().stream()
                        .flatMap(categoria -> categoria.getMisiones().stream())
                        .map(ProgresoMision::getInsigniaObtenida)
                        .filter(Objects::nonNull)
                        .count())
                .sum();

        List<DatosDonacion> todasLasDonaciones = todosLosDonantes.stream()
                .flatMap(donante -> donante.getHistorialDonaciones().stream())
                .toList();
        List<EvolucionMensual> evolucionMensual = calcularEvolucionMensual(todasLasDonaciones);

        return new MetricasSistema(
                donantesActivos,
                solicitudesTotales,
                misionesCompletadasTotales,
                misionesCompletadasTotales,
                evolucionMensual
        );
    }

    public List<Mision> obtenerMisionesDisponibles(Donante donante) {
        List<ProgresoCategoria> categoriasObtenidas = donante.getProgresoAsociado().getCategoriasObtenidas();
        if (categoriasObtenidas.isEmpty()) {
            return List.of();
        }
        ProgresoCategoria categoriaActual = categoriasObtenidas.get(categoriasObtenidas.size() - 1);
        return categoriaActual.getMisiones().stream()
                .map(ProgresoMision::getMisionAsociada)
                .toList();
    }

    public List<Insignia> obtenerInsignias(Donante donante) {
        return donante.getProgresoAsociado().getCategoriasObtenidas().stream()
                .flatMap(categoria -> categoria.getMisiones().stream())
                .map(ProgresoMision::getInsigniaObtenida)
                .filter(Objects::nonNull)
                .filter(ProgresoInsignia::isVisible)
                .map(ProgresoInsignia::getInsigniaAsociada)
                .toList();
    }

    public void marcarInsigniaVisible(Donante donante, String insigniaNombre) {
        buscarProgresoInsignia(donante, insigniaNombre).marcarVisible();
    }

    public void ocultarInsignia(Donante donante, String insigniaNombre) {
        buscarProgresoInsignia(donante, insigniaNombre).ocultarInsignia();
    }

    private ProgresoInsignia buscarProgresoInsignia(Donante donante, String insigniaNombre) {
        return donante.getProgresoAsociado().getCategoriasObtenidas().stream()
                .flatMap(categoria -> categoria.getMisiones().stream())
                .map(ProgresoMision::getInsigniaObtenida)
                .filter(Objects::nonNull)
                .filter(progresoInsignia -> progresoInsignia.getInsigniaAsociada().getNombre().equals(insigniaNombre))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(
                        "El donante no tiene la insignia: " + insigniaNombre));
    }

    public Beneficiario obtenerOCrearBeneficiario(Long id, String nombre) {
        return beneficiarios.stream()
                .filter(beneficiario -> beneficiario.getId().equals(id))
                .findFirst()
                .orElseGet(() -> {
                    Beneficiario nuevo = new Beneficiario(id, nombre);
                    beneficiarios.add(nuevo);
                    return nuevo;
                });
    }

    public ProgresoInsignia registrarActividadDonacion(Donante donante, DatosDonacion datosDonacion) {
        int categoriasAntes = donante.getProgresoAsociado().getCategoriasObtenidas().size();

        ProgresoInsignia obtenida = donante.registrarActividadDonacion(datosDonacion);

        if (obtenida != null) {
            rankings.registrarMisionCompletada(donante);
            publicarInsignia(donante, obtenida.getInsigniaAsociada());
            notificarMisionCompletada(donante, obtenida);
        }

        int categoriasDespues = donante.getProgresoAsociado().getCategoriasObtenidas().size();
        if (categoriasDespues > categoriasAntes) {
            String nuevaCategoria = donante.getProgresoAsociado().getCategoriasObtenidas()
                    .get(categoriasDespues - 1).getNombre();
            notificarCambioCategoria(donante, nuevaCategoria);
        }

        return obtenida;
    }

    public void ejecutarVerificacionVigenciaMisiones() {
        donantes.verificarVigenciaMisiones();
    }

    public void ejecutarCierreMensualRanking() {
        rankings.generarRanking();
    }

    public List<Ranking> obtenerHistorialRanking() {
        return rankings.obtenerHistorial();
    }

    public Ranking obtenerRankingDeMes(LocalDate fecha) {
        return rankings.obtenerRankingDeMes(fecha);
    }

    public Ranking obtenerUltimoRanking() {
        return rankings.obtenerUltimoRanking();
    }

    public void publicarInsignia(Donante donante, Insignia insignia) {
        PublicacionInsigniaDTO dto = new PublicacionInsigniaDTO();
        dto.setDonanteId(donante.getId());
        dto.setDonanteNombre(donante.getNombre());
        dto.setInsigniaNombre(insignia.getNombre());
        dto.setImagenUrl(insignia.getImagenUrl());
        dto.setFechaObtencion(LocalDate.now());

        if (webhookN8nUrl == null || webhookN8nUrl.isBlank()) {
            log.info("Webhook n8n no configurado, se omite la publicación de la insignia {}", insignia.getNombre());
            return;
        }
        try {
            restTemplate.postForEntity(webhookN8nUrl, dto, Void.class);
        } catch (RuntimeException ex) {
            log.warn("No se pudo publicar la insignia {} en el webhook n8n: {}", insignia.getNombre(), ex.getMessage());
        }
    }

    public void notificarMisionCompletada(Donante donante, ProgresoInsignia obtenida) {
        enviarNotificacion("MISION_COMPLETADA", donante,
                "El donante completó una misión y obtuvo la insignia " + obtenida.getInsigniaAsociada().getNombre());
    }

    public void notificarCambioCategoria(Donante donante, String categoriaNombre) {
        enviarNotificacion("CAMBIO_CATEGORIA", donante,
                "El donante ascendió a la categoría " + categoriaNombre);
    }

    private void enviarNotificacion(String tipo, Donante donante, String mensaje) {
        if (notificacionesBaseUrl == null || notificacionesBaseUrl.isBlank()) {
            log.info("Servicio de notificaciones no configurado, se omite el evento {} del donante {}", tipo, donante.getId());
            return;
        }
        NotificacionEventoDTO dto = new NotificacionEventoDTO(tipo, donante.getId(), mensaje, LocalDate.now());
        try {
            restTemplate.postForEntity(notificacionesBaseUrl, dto, Void.class);
        } catch (RuntimeException ex) {
            log.warn("No se pudo enviar la notificación {} del donante {}: {}", tipo, donante.getId(), ex.getMessage());
        }
    }
}
