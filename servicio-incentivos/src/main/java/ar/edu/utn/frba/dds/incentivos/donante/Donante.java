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
    private String nombre;
    // Sin fuente real todavía -- nada en el sistema hoy le pasa a Incentivos
    // el contacto del donante (el endpoint de actividad-donacion que podría
    // traerlo ni siquiera está cableado desde Donaciones). Se deja la
    // plumbing lista, no se inventa un contacto.
    private String medioContactoPreferido;
    private String contactoPreferido;
    private int solicitudesDonacionHechas;
    private List<Beneficiario> beneficiariosAyudados;
    private ProgresoAsociado progresoAsociado;
    private List<DatosDonacion> historialDonaciones;

    public Donante(Long id) {
        this.id = id;
        this.solicitudesDonacionHechas = 0;
        this.beneficiariosAyudados = new ArrayList<>();
        this.progresoAsociado = new ProgresoAsociado(GestorMisiones.getInstance());
        this.historialDonaciones = new ArrayList<>();
    }

    public void actualizarNombreSiFalta(String nombre) {
        if (this.nombre == null && nombre != null && !nombre.isBlank()) {
            this.nombre = nombre;
        }
    }

    public void actualizarContactoSiFalta(String medio, String contacto) {
        if (this.contactoPreferido == null && medio != null && contacto != null && !contacto.isBlank()) {
            this.medioContactoPreferido = medio;
            this.contactoPreferido = contacto;
        }
    }

    public ProgresoInsignia registrarActividadDonacion(DatosDonacion datosDonacion) {
        solicitudesDonacionHechas++;
        historialDonaciones.add(datosDonacion);

        Beneficiario beneficiario = datosDonacion.getBeneficiario();
        if (beneficiario != null && !beneficiariosAyudados.contains(beneficiario)) {
            beneficiariosAyudados.add(beneficiario);
        }

        return progresoAsociado.actualizarProgreso(datosDonacion);
    }

    public void verificarVigenciaMisiones() {
        progresoAsociado.verificarVigenciaMisiones();
    }
}
