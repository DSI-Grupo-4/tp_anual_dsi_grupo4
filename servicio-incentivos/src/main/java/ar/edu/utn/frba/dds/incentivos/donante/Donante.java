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
    // Se sincroniza el contacto preferido con cada actividad recibida desde Donaciones.
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
        if (medio != null && contacto != null && !contacto.isBlank()) {
            this.medioContactoPreferido = medio;
            this.contactoPreferido = contacto;
        }
    }

    public synchronized ProgresoInsignia registrarActividadDonacion(DatosDonacion datosDonacion) {
        DatosDonacion previa = datosDonacion.getDonacionId() == null ? null : historialDonaciones.stream()
            .filter(d -> datosDonacion.getDonacionId().equals(d.getDonacionId())).findFirst().orElse(null);
        if (previa != null) {
            if (!datosDonacion.isDonacionExitosa() || previa.isDonacionExitosa()) return null;
            previa.setDonacionExitosa(true);
            previa.setBeneficiario(datosDonacion.getBeneficiario());
            if (previa.getBeneficiario() != null && !beneficiariosAyudados.contains(previa.getBeneficiario())) beneficiariosAyudados.add(previa.getBeneficiario());
            return progresoAsociado.getMisionActual() instanceof ar.edu.utn.frba.dds.incentivos.progreso.ProgresoMision.ProgresoDonacionesExitosas
                ? progresoAsociado.actualizarProgreso(datosDonacion) : null;
        }
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
