package ar.edu.utn.frba.dds.logistica.domain.rutas;

import java.util.List;

public interface EstrategiaPlanificacion {
    List<Ruta> planificar(List<Entrega> entregas, List<Camion> camionesDisponibles, Integer idRutaInicial);
}
