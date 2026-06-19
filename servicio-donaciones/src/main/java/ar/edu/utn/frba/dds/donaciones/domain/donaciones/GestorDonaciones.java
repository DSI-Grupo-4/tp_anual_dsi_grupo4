package ar.edu.utn.frba.dds.donaciones.domain.donaciones;

import ar.edu.utn.frba.dds.donaciones.domain.algoritmos.Algoritmo;
import ar.edu.utn.frba.dds.donaciones.domain.necesidades.Necesidad;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class GestorDonaciones {
    private static final GestorDonaciones INSTANCE = new GestorDonaciones();

    private Deposito depositoItems;
    private List<Necesidad> necesidadesRegistradas;
    private List<Donacion> donacionesRegistradas;
    private List<Algoritmo> algoritmos;

    private GestorDonaciones() {
        this.depositoItems = Deposito.getInstance();
        this.necesidadesRegistradas = new ArrayList<>();
        this.donacionesRegistradas = new ArrayList<>();
        this.algoritmos = new ArrayList<>();
    }

    public static GestorDonaciones getInstance() {
        return INSTANCE;
    }

    public void crearDonacion(Donacion donacion, Necesidad necesidad) {
        donacionesRegistradas.add(donacion);
    }

    public void registrarNecesidad() {
    }

    public void ejecutarAsignacionNecesidadesRegistradas(
            List<Algoritmo> algoritmos,
            Deposito depositoItems) {
        algoritmos.forEach(Algoritmo::ejecutarAlgoritmo);
    }

    public void confirmarAsignacion() {
    }
}
