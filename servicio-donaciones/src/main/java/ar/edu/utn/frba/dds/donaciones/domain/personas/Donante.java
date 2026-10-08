package ar.edu.utn.frba.dds.donaciones.domain.personas;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import ar.edu.utn.frba.dds.donaciones.domain.donaciones.Donacion;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Donante {
    private Long id;
    private Persona persona;
    private List<Donacion> donaciones;
    private LocalDate ultimaActividad;
    // Evita re-notificar todos los días una vez cruzado el umbral -- se
    // resetea apenas hay actividad nueva, así cada episodio de inactividad
    // se notifica una sola vez (ver InactividadScheduler).
    private boolean notificadoPorInactividad;

    public Donante(Long id, Persona persona) {
        this.id = id;
        this.persona = persona;
        this.ultimaActividad = LocalDate.now();
        this.donaciones = new ArrayList<>();
    }

    public Persona getPersona() {
        return persona;
    }

    // Registrarse ya cuenta como interacción con la plataforma; cada
    // donación nueva también la renueva (ver DonacionService.crear()).
    public void registrarActividad() {
        this.ultimaActividad = LocalDate.now();
        this.notificadoPorInactividad = false;
    }

    public boolean debeNotificarsePorInactividad(int dias) {
        if (notificadoPorInactividad || ultimaActividad == null) {
            return false;
        }
        return !ultimaActividad.isAfter(LocalDate.now().minusDays(dias));
    }

    public void marcarNotificadoPorInactividad() {
        this.notificadoPorInactividad = true;
    }

    public void agregarDonacion(Donacion donacion) {
        donaciones.add(donacion);
    }

    public Integer cantidadDonaciones() {
        return donaciones.size();
    }

    public boolean realizoDonaciones() {
        return !donaciones.isEmpty();
    }

    /**
     * Una persona jurídica que actúa como donante admite un único
     * representante (a diferencia de una entidad beneficiaria, que admite
     * varios) — cardinalidad por rol acordada en el diseño.
     */
    public void agregarRepresentante(PersonaHumana representante) {
        if (!(persona instanceof PersonaJuridica juridica)) {
            throw new IllegalStateException(
                    "Solo las personas jurídicas admiten representantes");
        }
        if (!juridica.getRepresentantes().isEmpty()) {
            throw new IllegalStateException(
                    "Una persona jurídica donante admite un único representante");
        }
        juridica.agregarRepresentante(representante);
    }
}
