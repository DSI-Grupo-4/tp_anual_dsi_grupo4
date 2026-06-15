package ar.edu.utn.frba.dds.donaciones.domain.algoritmos;

import ar.edu.utn.frba.dds.donaciones.domain.donaciones.ItemDonado;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;

import java.util.List;

public interface AlgoritmoAsignacion {
    List<EntidadBeneficiaria> generarRanking(
            ItemDonado item,
            List<EntidadBeneficiaria> entidades
    );
}
