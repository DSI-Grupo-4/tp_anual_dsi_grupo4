package ar.edu.utn.frba.dds.logistica.domain.rutas;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Estrategia propia: agrupa las entregas pendientes por entidad beneficiaria
 * (cada grupo es una Parada) y las reparte entre los camiones disponibles
 * respetando su capacidad. Genera una lista de puntos de entrega por camión
 * (no calcula un "camino" óptimo entre ellos, según lo aclarado por la cátedra).
 */
public class PlanificacionPropia implements EstrategiaPlanificacion {

    @Override
    public List<Ruta> planificar(List<Entrega> entregas, List<Camion> camionesDisponibles,
                                  List<Chofer> choferesDisponibles) {
        List<Ruta> rutas = new ArrayList<>();

        // Agrupamos las entregas por entidad beneficiaria -> cada grupo será una Parada
        Map<Integer, List<Entrega>> entregasPorEntidad = entregas.stream()
                .collect(Collectors.groupingBy(Entrega::getIdEntidadBeneficiariaAsociada));

        Iterator<Camion> camiones = camionesDisponibles.iterator();
        if (!camiones.hasNext()) {
            return rutas; // no hay camiones disponibles, no se puede planificar
        }

        Camion camionActual = camiones.next();
        // No hay EstadoChofer que distinga "ocupado"/"libre", así que se asigna de forma
        // cíclica entre los choferes habilitados (un chofer puede terminar en más de una
        // ruta si hay más camiones que choferes); si no hay ninguno, la ruta queda sin
        // chofer asignado en vez de romper la planificación.
        int siguienteChoferIdx = 0;
        Chofer choferActual = choferesDisponibles.isEmpty() ? null : choferesDisponibles.get(siguienteChoferIdx++ % choferesDisponibles.size());
        List<Parada> paradasCamionActual = new ArrayList<>();
        // Posición de la próxima parada dentro de la ruta en armado (se reinicia con cada ruta).
        int numeroParada = 1;
        // Carga ya comprometida en camionActual por paradas anteriores -- sin esto,
        // cada parada se validaba contra la capacidad TOTAL del camión en vez de la
        // RESTANTE, y un camión podía terminar sobrecargado por la suma de varias
        // paradas que individualmente entraban (bug confirmado, Tier 3 #9b).
        int pesoAcumulado = 0;
        int volumenAcumulado = 0;
        int alturaMaxAcumulada = 0;

        for (Map.Entry<Integer, List<Entrega>> grupo : entregasPorEntidad.entrySet()) {
            List<Entrega> entregasDeLaParada = grupo.getValue();

            int pesoTotal = entregasDeLaParada.stream().mapToInt(Entrega::getPesoKG).sum();
            int volumenTotal = entregasDeLaParada.stream().mapToInt(Entrega::getVolumenM3).sum();
            int alturaMax = entregasDeLaParada.stream().mapToInt(Entrega::getAlturaM).max().orElse(0);

            // Si no entra en el camión actual (carga ya acumulada + esta parada),
            // cerramos su ruta (si tiene paradas) y probamos con el siguiente, que
            // arranca con los acumuladores en cero.
            while (camionActual != null && !camionActual.puedeCargar(
                    pesoAcumulado + pesoTotal, volumenAcumulado + volumenTotal, Math.max(alturaMaxAcumulada, alturaMax))) {
                if (!paradasCamionActual.isEmpty()) {
                    rutas.add(new Ruta(null, camionActual, choferActual, LocalDate.now().plusDays(1), paradasCamionActual));
                    paradasCamionActual = new ArrayList<>();
                    numeroParada = 1;
                }
                camionActual = camiones.hasNext() ? camiones.next() : null;
                choferActual = choferesDisponibles.isEmpty() ? null
                        : choferesDisponibles.get(siguienteChoferIdx++ % choferesDisponibles.size());
                pesoAcumulado = 0;
                volumenAcumulado = 0;
                alturaMaxAcumulada = 0;
            }

            if (camionActual == null) {
                // ningún camión disponible puede cargar esta parada -- queda sin planificar
                continue;
            }

            final Camion camionParaEstaParada = camionActual;
            Parada parada = new Parada(numeroParada++, grupo.getKey(),
                    entregasDeLaParada.get(0).getDireccionDestino(), entregasDeLaParada);
            entregasDeLaParada.forEach(e -> e.asignarARuta(camionParaEstaParada));
            paradasCamionActual.add(parada);
            pesoAcumulado += pesoTotal;
            volumenAcumulado += volumenTotal;
            alturaMaxAcumulada = Math.max(alturaMaxAcumulada, alturaMax);
        }

        if (camionActual != null && !paradasCamionActual.isEmpty()) {
            rutas.add(new Ruta(null, camionActual, choferActual, LocalDate.now().plusDays(1), paradasCamionActual));
        }

        return rutas;
    }
}
