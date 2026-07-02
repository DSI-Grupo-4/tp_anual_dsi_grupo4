package ar.edu.utn.frba.dds.incentivos.donante;

import ar.edu.utn.frba.dds.incentivos.consultor.Beneficiario;
import ar.edu.utn.frba.dds.incentivos.donacion.DatosDonacion;
import ar.edu.utn.frba.dds.incentivos.misiones.GestorMisiones;
import ar.edu.utn.frba.dds.incentivos.progreso.ProgresoAsociado;
import ar.edu.utn.frba.dds.incentivos.progreso.ProgresoInsignia;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Donante {

    private final Long id;
    private int solicitudesDonacionHechas;
    private List<Beneficiario> beneficiariosAyudados;
    private ProgresoAsociado progresoAsociado;

    public Donante(Long id) {
        this.id = id;
        this.solicitudesDonacionHechas = 0;
        this.beneficiariosAyudados = new ArrayList<>();
        this.progresoAsociado = new ProgresoAsociado(GestorMisiones.getInstance());
    }

    public ProgresoInsignia registrarActividadDonacion(DatosDonacion datosDonacion) {
        solicitudesDonacionHechas++;
        return progresoAsociado.actualizarProgreso(datosDonacion);
    }

    public void verificarVigenciaMisiones() {
        progresoAsociado.verificarVigenciaMisiones();
    }
}
