package ar.edu.utn.frba.dds.donaciones.domain.algoritmos;

import ar.edu.utn.frba.dds.donaciones.domain.donaciones.Donacion;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;

import java.util.Comparator;
import java.util.List;

public class CompatibilidadSemantica implements AlgoritmoAsignacion {

    @Override
    public List<EntidadBeneficiaria> ejecutarAlgoritmo(
            Donacion donacion,
            List<EntidadBeneficiaria> entidades) {

        return entidades.stream()
                .filter(e -> puntaje(e, donacion) > 0)
                .sorted(Comparator.comparingInt(e -> -puntaje(e, donacion)))
                .limit(10)
                .toList();
    }

    private int puntaje(EntidadBeneficiaria entidad, Donacion donacion) {
        if (donacion.getItemDonado() == null
                || donacion.getItemDonado().getSubcategoria() == null
                || donacion.getItemDonado().getSubcategoria().getNombre() == null) {
            // Confirmado en vivo: sin este último chequeo, una donación con
            // subcategoría de nombre null provocaba NPE acá abajo en vez de
            // simplemente no matchear con ninguna necesidad.
            return 0;
        }
        String subcatDonacion = donacion.getItemDonado().getSubcategoria().getNombre();

        return (int) entidad.getNecesidades().stream()
                .filter(n -> !n.satisfecha())
                .filter(n -> n.getSubcategoria() != null
                        && subcatDonacion.equalsIgnoreCase(n.getSubcategoria().getNombre()))
                .count();
    }
}
