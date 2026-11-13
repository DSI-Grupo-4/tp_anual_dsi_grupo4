package ar.edu.utn.frba.dds.web.logistica;

import ar.edu.utn.frba.dds.web.client.logistica.CamionApi;
import ar.edu.utn.frba.dds.web.client.logistica.EntregaAdminApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutaApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutasClient;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

/** Arma lo que muestran las pantallas de seguimiento de rutas y entregas. */
@Service
public class SeguimientoService {

    /** Orden en que se muestran los envíos de una entidad: primero lo que está en camino. */
    private static final Map<String, Integer> PRIORIDAD = Map.of("INICIADA", 0, "PLANIFICADA", 1, "FINALIZADA", 2);
    private static final Set<String> SIN_RESULTADO = Set.of("PENDIENTE", "ASIGNADA_A_RUTA", "EN_TRASLADO", "REPLANIFICABLE");

    private final RutasClient rutas;

    public SeguimientoService(RutasClient rutas) {
        this.rutas = rutas;
    }

    // ---- administración ----

    /** Todas las rutas, de la más nueva a la más vieja; con estado = null no se filtra. */
    public List<RutaApi> rutas(String estado) {
        return rutas.rutas().stream()
                .filter(r -> estado == null || estado.equals(r.estadoRuta()))
                .sorted(Comparator.comparing(RutaApi::idRuta, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    public ResumenSeguimiento resumen(List<RutaApi> todas) {
        int planificadas = 0;
        int iniciadas = 0;
        int finalizadas = 0;
        int pendientes = 0;
        for (RutaApi ruta : todas) {
            switch (ruta.estadoRuta() == null ? "" : ruta.estadoRuta()) {
                case "PLANIFICADA" -> planificadas++;
                case "INICIADA" -> iniciadas++;
                case "FINALIZADA" -> finalizadas++;
                default -> { }
            }
            if (!"CANCELADA".equals(ruta.estadoRuta())) {
                pendientes += (int) ruta.entregas().stream().filter(e -> SIN_RESULTADO.contains(e.estadoEntrega())).count();
            }
        }
        return new ResumenSeguimiento(planificadas, iniciadas, finalizadas, pendientes);
    }

    // ---- entidad beneficiaria ----

    /**
     * Las paradas de las rutas que le corresponden a la entidad (las canceladas no se muestran).
     * Solo se devuelve lo de la entidad pedida: nada de las otras paradas de la ruta.
     */
    public List<EnvioEntidad> enviosDe(Long entidadId) {
        List<EnvioEntidad> envios = new ArrayList<>(rutas.enviosPropios(entidadId));
        envios.sort(Comparator
                .comparing((EnvioEntidad e) -> PRIORIDAD.getOrDefault(e.estadoRuta(), 3))
                .thenComparing(EnvioEntidad::idRuta, Comparator.nullsLast(Comparator.reverseOrder())));
        return envios;
    }

    /** El envío de la entidad en una ruta (su parada), o vacío si esa ruta no le lleva nada. */
    public java.util.Optional<EnvioEntidad> envioDe(Long entidadId, Integer idRuta) {
        return enviosDe(entidadId).stream().filter(e -> idRuta.equals(e.idRuta())).findFirst();
    }

    public ResumenSeguimiento resumenDe(List<EnvioEntidad> envios) {
        int planificadas = (int) envios.stream().filter(e -> "PLANIFICADA".equals(e.estadoRuta())).count();
        int iniciadas = (int) envios.stream().filter(e -> "INICIADA".equals(e.estadoRuta())).count();
        int finalizadas = (int) envios.stream().filter(e -> "FINALIZADA".equals(e.estadoRuta())).count();
        int pendientes = envios.stream()
                .mapToInt(e -> (int) e.entregas().stream().filter(x -> SIN_RESULTADO.contains(x.estadoEntrega())).count())
                .sum();
        return new ResumenSeguimiento(planificadas, iniciadas, finalizadas, pendientes);
    }

    /** Cifras del listado de entregas de la administración. */
    public ResumenEntregas resumenEntregas(List<EntregaAdminApi> entregas) {
        int sinSalir = 0;
        int enCamino = 0;
        int entregadas = 0;
        int requierenAtencion = 0;
        for (EntregaAdminApi entrega : entregas) {
            switch (entrega.estadoEntrega() == null ? "" : entrega.estadoEntrega()) {
                case "PENDIENTE", "REPLANIFICABLE" -> sinSalir++;
                case "ASIGNADA_A_RUTA", "EN_TRASLADO" -> enCamino++;
                case "ENTREGADA" -> entregadas++;
                case "NO_RECIBIDA", "FALLIDA" -> requierenAtencion++;
                default -> { }
            }
        }
        return new ResumenEntregas(sinSalir, enCamino, entregadas, requierenAtencion);
    }

    public ResumenFlota resumenFlota(List<CamionApi> camiones) {
        int disponibles = (int) camiones.stream().filter(c -> "DISPONIBLE".equals(c.estadoCamion())).count();
        int enUso = (int) camiones.stream()
                .filter(c -> "ASIGNADO".equals(c.estadoCamion()) || "EN_RUTA".equals(c.estadoCamion())).count();
        int fuera = (int) camiones.stream().filter(c -> "FUERA_DE_SERVICIO".equals(c.estadoCamion())).count();
        return new ResumenFlota(camiones.size(), disponibles, enUso, fuera);
    }
}
