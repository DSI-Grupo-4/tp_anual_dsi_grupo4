package ar.edu.utn.frba.dds.donaciones.domain.algoritmos;

import ar.edu.utn.frba.dds.donaciones.domain.donaciones.Donacion;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.EstadoTrack;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public class PrioridadSubatendidos implements AlgoritmoAsignacion {

    @Override
    public List<EntidadBeneficiaria> ejecutarAlgoritmo(
            Donacion donacion,
            List<EntidadBeneficiaria> entidades) {

        if (donacion.estaVencida()) return List.of();
        LocalDateTime hace90dias = LocalDateTime.now().minusDays(90);

        return entidades.stream()
                .filter(entidad -> tieneNecesidadCompatible(entidad, donacion))
                .sorted(Comparator.comparingLong(
                        entidad -> donacionesEntregadasEnTrimestre(entidad, hace90dias)))
                .limit(10)
                .toList();
    }

    private boolean tieneNecesidadCompatible(EntidadBeneficiaria entidad, Donacion donacion) {
        return entidad.necesidadesPendientes().stream()
                .anyMatch(necesidad -> necesidad.getSubcategoria() == donacion.getSubcategoria()
                        && necesidad.getUnidadMedida() == donacion.getUnidadMedida());
    }

    private long donacionesEntregadasEnTrimestre(
            EntidadBeneficiaria entidad,
            LocalDateTime desde) {

        return entidad.getDonacionesRecibidas().stream()
                .filter(d -> d.getEstadoActual() == EstadoTrack.ENTREGADA)
                .filter(d -> d.getFechaCreacion() != null
                        && d.getFechaCreacion().isAfter(desde))
                .count();
    }
}
