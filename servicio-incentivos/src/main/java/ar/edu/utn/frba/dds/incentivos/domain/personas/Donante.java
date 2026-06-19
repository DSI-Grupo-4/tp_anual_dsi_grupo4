package ar.edu.utn.frba.dds.incentivos.domain.personas;

import ar.edu.utn.frba.dds.incentivos.domain.insignias.Insignia;
import ar.edu.utn.frba.dds.incentivos.domain.misiones.Mision;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Donante {
    private Id idDonante;
    private CategoriaDonante categoriaDonante;
    private List<Insignia> insignias = new ArrayList<>();
    private Mision misionActual;
    private List<Mision> misionesCompletadas = new ArrayList<>();

    public Donante() {}

    public Donante(Id idDonante, CategoriaDonante categoriaDonante, Mision misionActual) {
        this.idDonante = idDonante;
        this.categoriaDonante = categoriaDonante;
        this.misionActual = misionActual;
    }

    public void recibirInsignia(Insignia insignia) {
        if (insignia != null) insignias.add(insignia);
    }

    public void completarMision() {
        if (misionActual == null) return;
        misionActual.ejecutar();
        if (misionActual.isEstaCompleta()) {
            recibirInsignia(misionActual.cumplirMision());
            misionesCompletadas.add(misionActual);
            misionActual = null;
        }
    }
}
