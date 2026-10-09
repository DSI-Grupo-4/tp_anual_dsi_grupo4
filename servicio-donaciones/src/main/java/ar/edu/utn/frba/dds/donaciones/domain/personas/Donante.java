package ar.edu.utn.frba.dds.donaciones.domain.personas;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import ar.edu.utn.frba.dds.donaciones.domain.donaciones.Donacion;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // requerido por JPA
@Entity
@Table(name = "donante")
public class Donante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "persona_id", nullable = false, unique = true)
    private Persona persona;

    @OneToMany(mappedBy = "donante", fetch = jakarta.persistence.FetchType.EAGER)
    private List<Donacion> donaciones;

    @Column(name = "ultima_actividad", nullable = false)
    private LocalDate ultimaActividad;

    // Evita re-notificar todos los días una vez cruzado el umbral -- se
    // resetea apenas hay actividad nueva, así cada episodio de inactividad
    // se notifica una sola vez (ver InactividadScheduler).
    @Column(name = "notificado_por_inactividad", nullable = false)
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
        return ultimaActividad.isBefore(LocalDate.now().minusDays(dias));
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
