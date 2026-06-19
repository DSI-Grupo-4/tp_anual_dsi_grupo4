package ar.edu.utn.frba.dds.donaciones.domain.personas;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class EntidadBeneficiaria {
    private String nombre;
    private List<Donante> tallaImportador = new ArrayList<>();

    public void importarDonantes(Importador importador) {
        importador.realizarRuta();
        tallaImportador.addAll(importador.getTallaImportador());
    }

    public List<Donante> registrarDonantes(Donante c) {
        tallaImportador.add(c);
        return tallaImportador;
    }
}
