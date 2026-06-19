package ar.edu.utn.frba.dds.incentivos.domain.personas;

import java.util.ArrayList;
import java.util.List;

import ar.edu.utn.frba.dds.incentivos.domain.insignias.Insignia;
import ar.edu.utn.frba.dds.incentivos.domain.misiones.Mision;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Donante {
    private Id idDonante;
    private CategoriaDonante categoriaDonante;
    private List<Insignia> insignias;
    private Mision misionActual;
    private Integer solicitudesDonacionHechas;
    private List <Beneficiario> beneficiariosAyudados;

    public void completarMision(){
        boolean completoMision = this.misionActual.completoMision(this);
        if (completoMision) {
            Insignia insigniaNueva = this.misionActual.cumplirMision();
            recibirInsignia(insigniaNueva);
            avanzarMision();
        }
    }

    public Donante(Id idDonante,
                   CategoriaDonante categoriaDonante,
                   Mision misionActual,
                   Integer solicitudesDonacionHechas,
                   List<Beneficiario> beneficiariosAyudados) {

        this.idDonante = idDonante;
        this.categoriaDonante = categoriaDonante;
        this.misionActual = misionActual;
        this.solicitudesDonacionHechas = solicitudesDonacionHechas;
        this.beneficiariosAyudados = beneficiariosAyudados;

        this.insignias = new ArrayList<>();
    }

    public void recibirInsignia(Insignia insignia) {
        this.insignias.add(insignia);
    }

    public void avanzarMision(){
        this.misionActual = // mision siguiente
    }
}