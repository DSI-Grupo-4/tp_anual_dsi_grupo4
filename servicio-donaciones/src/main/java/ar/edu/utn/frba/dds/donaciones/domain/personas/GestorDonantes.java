package ar.edu.utn.frba.dds.donaciones.domain.personas;

import java.util.ArrayList;
import java.util.List;

public class GestorDonantes {
    private List<Donante> donantesRegistrados = new ArrayList<>();
    private List<Importador> importadores = new ArrayList<>();

    public List<Donante> getDonantesRegistrados() {
        return donantesRegistrados;
    }

    public void agregarImportador(Importador importador) {
        importadores.add(importador);
    }

    public void importarDonantes(String nombreImportador) {
        importadores.stream()
                .filter(imp -> imp.getNombre().equals(nombreImportador))
                .findFirst()
                .ifPresent(imp -> donantesRegistrados.addAll(imp.getListaDonantes()));
    }
}
