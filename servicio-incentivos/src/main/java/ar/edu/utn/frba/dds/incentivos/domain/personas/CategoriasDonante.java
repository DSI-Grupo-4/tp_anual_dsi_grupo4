package ar.edu.utn.frba.dds.incentivos.domain.personas;

import ar.edu.utn.frba.dds.incentivos.domain.misiones.Mision;

import java.util.ArrayList;
import java.util.List;

public enum CategoriasDonante {
    COLABORADOR,
    SOSTENEDOR,
    TRANSFORMADOR;

    private List<Mision> misiones = new ArrayList<>();

    public List<Mision> getMisiones() {
        return misiones;
    }

    public void setMisiones(List<Mision> misiones) {
        this.misiones = misiones;
    }
}
