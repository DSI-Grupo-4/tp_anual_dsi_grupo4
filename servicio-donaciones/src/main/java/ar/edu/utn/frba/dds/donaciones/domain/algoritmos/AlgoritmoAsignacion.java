package ar.edu.utn.frba.dds.donaciones.domain.algoritmos;

import ar.edu.utn.frba.dds.donaciones.domain.donaciones.Donacion;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;

import java.util.List;

public interface AlgoritmoAsignacion {
    List<EntidadBeneficiaria> ejecutarAlgoritmo(
            Donacion donacion,
            List<EntidadBeneficiaria> entidades
    );
}
