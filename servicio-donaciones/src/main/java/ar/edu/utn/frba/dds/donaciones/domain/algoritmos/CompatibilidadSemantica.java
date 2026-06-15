package ar.edu.utn.frba.dds.donaciones.domain.algoritmos;

import ar.edu.utn.frba.dds.donaciones.domain.donaciones.ItemDonado;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;

import java.util.Comparator;
import java.util.List;

public class CompatibilidadSemantica implements AlgoritmoAsignacion {
    @Override
    public List<EntidadBeneficiaria> generarRanking(ItemDonado item, List<EntidadBeneficiaria> entidades) {
        return entidades.stream()
                .sorted(
                        Comparator.comparingInt(
                                entidad -> -puntaje(entidad, item)
                        )
                )
                .limit(10)
                .toList();
    }

    private Integer puntaje(
            EntidadBeneficiaria entidad,
            ItemDonado item) {
        return (int) entidad.getNecesidades()
                .stream()
                .filter(n -> !n.satisfecha())
                .filter(n ->
                        n.getSubcategoria()
                                .equals(item.getSubcategoria())
                )
                .count();
    }
    //si una entidad tiene una necesidad pendiente de la misma subcategoria, suma puntos

}
