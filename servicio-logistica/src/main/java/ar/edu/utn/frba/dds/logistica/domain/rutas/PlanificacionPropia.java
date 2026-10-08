package ar.edu.utn.frba.dds.logistica.domain.rutas;

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
                                  List<Chofer> choferesDisponibles, Integer idRutaInicial) {
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
        int idRuta = idRutaInicial;
        int idParada = 1;

        for (Map.Entry<Integer, List<Entrega>> grupo : entregasPorEntidad.entrySet()) {
            List<Entrega> entregasDeLaParada = grupo.getValue();

            int pesoTotal = entregasDeLaParada.stream().mapToInt(Entrega::getPesoKG).sum();
            int volumenTotal = entregasDeLaParada.stream().mapToInt(Entrega::getVolumenM3).sum();
            int alturaMax = entregasDeLaParada.stream().mapToInt(Entrega::getAlturaM).max().orElse(0);

            // Si no entra en el camión actual, cerramos su ruta (si tiene paradas) y
            // probamos con el siguiente. Antes esto solo se chequeaba cuando el camión
            // ya tenía paradas asignadas (!paradasCamionActual.isEmpty()), así que la
            // primera parada de un camión que de entrada no podía cargarla pasaba sin
            // validar -- bug confirmado con un caso real (99.999kg en un camión de
            // 1200kg). Ahora se valida siempre, probando camiones siguientes hasta
            // encontrar uno que pueda, o se deja la parada sin planificar si ninguno puede.
            while (camionActual != null && !camionActual.puedeCargar(pesoTotal, volumenTotal, alturaMax)) {
                if (!paradasCamionActual.isEmpty()) {
                    rutas.add(new Ruta(idRuta++, camionActual, choferActual, java.time.LocalDate.now().plusDays(1), paradasCamionActual));
                    paradasCamionActual = new ArrayList<>();
                }
                camionActual = camiones.hasNext() ? camiones.next() : null;
                choferActual = choferesDisponibles.isEmpty() ? null
                        : choferesDisponibles.get(siguienteChoferIdx++ % choferesDisponibles.size());
            }

            if (camionActual == null) {
                // ningún camión disponible puede cargar esta parada -- queda sin planificar
                continue;
            }

            final Camion camionParaEstaParada = camionActual;
            Parada parada = new Parada(idParada++, grupo.getKey(),
                    entregasDeLaParada.get(0).getDireccionDestino(), entregasDeLaParada);
            entregasDeLaParada.forEach(e -> e.asignarARuta(camionParaEstaParada));
            paradasCamionActual.add(parada);
        }

        if (camionActual != null && !paradasCamionActual.isEmpty()) {
            rutas.add(new Ruta(idRuta, camionActual, choferActual, java.time.LocalDate.now().plusDays(1), paradasCamionActual));
        }

        return rutas;
    }
}
