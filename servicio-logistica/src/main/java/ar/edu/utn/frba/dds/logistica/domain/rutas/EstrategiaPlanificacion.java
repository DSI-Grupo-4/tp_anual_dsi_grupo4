package ar.edu.utn.frba.dds.logistica.domain.rutas;

import java.util.List;

public interface EstrategiaPlanificacion {
    // Los ids (de ruta, de parada) los asigna la base al persistir: la estrategia no los numera.
    List<Ruta> planificar(List<Entrega> entregas, List<Camion> camionesDisponibles,
                           List<Chofer> choferesDisponibles);
}
