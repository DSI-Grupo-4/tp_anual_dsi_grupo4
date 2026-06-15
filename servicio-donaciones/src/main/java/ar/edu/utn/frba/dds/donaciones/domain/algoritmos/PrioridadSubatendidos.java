package ar.edu.utn.frba.dds.donaciones.domain.algoritmos;

import ar.edu.utn.frba.dds.donaciones.domain.donaciones.ItemDonado;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;

import java.util.Comparator;
import java.util.List;

public class PrioridadSubatendidos implements AlgoritmoAsignacion{
    @Override
    public List<EntidadBeneficiaria> generarRanking(ItemDonado item, List<EntidadBeneficiaria> entidades) {
        return entidades.stream()
                .sorted(
                        Comparator.comparingInt(
                                entidad -> entidad.getDonacionesRecibidas().size()
                        )
                )
                .limit(10)
                .toList();
    } //prioriza organizaciones que recibieron menos donaciones
}
