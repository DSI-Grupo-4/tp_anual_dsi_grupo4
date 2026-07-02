package ar.edu.utn.frba.dds.incentivos.progreso;

import ar.edu.utn.frba.dds.incentivos.misiones.Categoria;
import ar.edu.utn.frba.dds.incentivos.misiones.Mision;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ProgresoCategoria {

    private String nombre;
    private List<ProgresoMision> misiones;

    public ProgresoCategoria(Categoria categoria) {
        this.nombre = categoria.getNombre();
        this.misiones = new ArrayList<>();
        for (int i = 0; i < categoria.cantMisiones(); i++) {
            Mision mision = categoria.indexMision(i);
            this.misiones.add(mision.crearProgreso());
        }
    }

    public int cantMisionesCompletadas() {
        return (int) misiones.stream()
                .filter(progresoMision -> progresoMision.getInsigniaObtenida() != null)
                .count();
    }
}
