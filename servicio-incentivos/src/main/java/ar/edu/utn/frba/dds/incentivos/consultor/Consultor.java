package ar.edu.utn.frba.dds.incentivos.consultor;

import ar.edu.utn.frba.dds.incentivos.donacion.DatosDonacion;
import ar.edu.utn.frba.dds.incentivos.donante.Donante;
import ar.edu.utn.frba.dds.incentivos.donante.GestorDonante;
import ar.edu.utn.frba.dds.incentivos.dto.PublicacionInsigniaDTO;
import ar.edu.utn.frba.dds.incentivos.metricas.MetricasActividad;
import ar.edu.utn.frba.dds.incentivos.metricas.Periodo;
import ar.edu.utn.frba.dds.incentivos.misiones.GestorMisiones;
import ar.edu.utn.frba.dds.incentivos.misiones.Insignia;
import ar.edu.utn.frba.dds.incentivos.misiones.Mision;
import ar.edu.utn.frba.dds.incentivos.progreso.ProgresoCategoria;
import ar.edu.utn.frba.dds.incentivos.progreso.ProgresoInsignia;
import ar.edu.utn.frba.dds.incentivos.progreso.ProgresoMision;
import ar.edu.utn.frba.dds.incentivos.ranking.HistorialRanking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Consultor {

    private static final Logger log = LoggerFactory.getLogger(Consultor.class);

    private static Consultor instancia;

    private final GestorMisiones misiones;
    private final GestorDonante donantes;
    private final HistorialRanking rankings;
    private final Set<Beneficiario> beneficiarios;
    private final RestTemplate restTemplate;
    private String webhookN8nUrl;

    private Consultor() {
        this.misiones = GestorMisiones.getInstance();
        this.donantes = GestorDonante.getInstance();
        this.rankings = HistorialRanking.getInstance();
        this.beneficiarios = new HashSet<>();
        this.restTemplate = new RestTemplate();
        this.webhookN8nUrl = "";
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

    public MetricasActividad obtenerMetricasActividad(Donante donante, Periodo periodo) {
        int misionesCompletadas = donante.getProgresoAsociado().getCategoriasObtenidas().stream()
                .mapToInt(ProgresoCategoria::cantMisionesCompletadas)
                .sum();

        return new MetricasActividad(
                periodo,
                donante.getSolicitudesDonacionHechas(),
                donante.getBeneficiariosAyudados().size(),
                misionesCompletadas,
                misionesCompletadas
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
                .filter(java.util.Objects::nonNull)
                .map(ProgresoInsignia::getInsigniaAsociada)
                .toList();
    }

    public ProgresoInsignia registrarActividadDonacion(Donante donante, DatosDonacion datosDonacion) {
        ProgresoInsignia obtenida = donante.registrarActividadDonacion(datosDonacion);
        if (obtenida != null) {
            rankings.registrarMisionCompletada(donante);
            publicarInsignia(donante, obtenida.getInsigniaAsociada());
        }
        return obtenida;
    }

    public void ejecutarVerificacionVigenciaMisiones() {
        donantes.verificarVigenciaMisiones();
    }

    public void ejecutarCierreMensualRanking() {
        rankings.generarRanking();
    }

    public void publicarInsignia(Donante donante, Insignia insignia) {
        PublicacionInsigniaDTO dto = new PublicacionInsigniaDTO();
        dto.setDonanteId(donante.getId());
        dto.setInsigniaNombre(insignia.getNombre());
        dto.setImagenUrl(insignia.getImagenUrl());
        dto.setFechaObtencion(java.time.LocalDate.now());

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
}
