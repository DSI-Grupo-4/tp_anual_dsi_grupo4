package ar.edu.utn.frba.dds.donaciones.domain.personas;

import java.util.ArrayList;
import java.util.List;

public class GestorBeneficiarios {
    private static final GestorBeneficiarios INSTANCE = new GestorBeneficiarios();

    private List<Donante> donantesRegistrados = new ArrayList<>();
    private List<Donante> importadorRegistrado = new ArrayList<>();

    private GestorBeneficiarios() {
    }

    public static GestorBeneficiarios getInstance() {
        return INSTANCE;
    }

    public void importarDonantes(Importador importador) {
        importador.realizarRuta();
        importadorRegistrado.addAll(importador.getTallaImportador());
    }

    public List<Donante> registrarDonante(Donante c) {
        donantesRegistrados.add(c);
        return donantesRegistrados;
    }
}
