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
    public List<Ruta> planificar(List<Entrega> entregas, List<Camion> camionesDisponibles, Integer idRutaInicial) {
        List<Ruta> rutas = new ArrayList<>();

        // Agrupamos las entregas por entidad beneficiaria -> cada grupo será una Parada
        Map<Integer, List<Entrega>> entregasPorEntidad = entregas.stream()
                .collect(Collectors.groupingBy(Entrega::getIdEntidadBeneficiariaAsociada));

        Iterator<Camion> camiones = camionesDisponibles.iterator();
        if (!camiones.hasNext()) {
            return rutas; // no hay camiones disponibles, no se puede planificar
        }

        Camion camionActual = camiones.next();
        List<Parada> paradasCamionActual = new ArrayList<>();
        int idRuta = idRutaInicial;
        int idParada = 1;

        for (Map.Entry<Integer, List<Entrega>> grupo : entregasPorEntidad.entrySet()) {
            List<Entrega> entregasDeLaParada = grupo.getValue();

            int pesoTotal = entregasDeLaParada.stream().mapToInt(Entrega::getPesoKG).sum();
            int volumenTotal = entregasDeLaParada.stream().mapToInt(Entrega::getVolumenM3).sum();
            int alturaMax = entregasDeLaParada.stream().mapToInt(Entrega::getAlturaM).max().orElse(0);

            // si no entra en el camión actual, cerramos su ruta y probamos con el siguiente camión
            if (!camionActual.puedeCargar(pesoTotal, volumenTotal, alturaMax) && !paradasCamionActual.isEmpty()) {
                rutas.add(new Ruta(idRuta++, camionActual, null, java.time.LocalDate.now().plusDays(1), paradasCamionActual));
                paradasCamionActual = new ArrayList<>();
                if (!camiones.hasNext()) break; // no quedan más camiones
                camionActual = camiones.next();
            }

            Parada parada = new Parada(idParada++, grupo.getKey(),
                    entregasDeLaParada.get(0).getDireccionDestino(), entregasDeLaParada);
            entregasDeLaParada.forEach(e -> e.asignarARuta(camionActual));
            paradasCamionActual.add(parada);
        }

        if (!paradasCamionActual.isEmpty()) {
            rutas.add(new Ruta(idRuta, camionActual, null, java.time.LocalDate.now().plusDays(1), paradasCamionActual));
        }

        return rutas;
    }
}
