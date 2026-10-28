package ar.edu.utn.frba.dds.incentivos.donante;

import ar.edu.utn.frba.dds.incentivos.consultor.Beneficiario;
import ar.edu.utn.frba.dds.incentivos.donacion.DatosDonacion;
import ar.edu.utn.frba.dds.incentivos.misiones.GestorMisiones;
import ar.edu.utn.frba.dds.incentivos.progreso.ProgresoAsociado;
import ar.edu.utn.frba.dds.incentivos.progreso.ProgresoInsignia;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // requerido por JPA
@Entity
@Table(name = "donante")
public class Donante {

    // Viene de Donaciones (Donante.id): sin @GeneratedValue, Incentivos no
    // da de alta donantes, solo los conoce por su primera actividad.
    @Id
    @Column(name = "id_donante")
    private Long id;

    @Column(name = "nombre", length = 100)
    private String nombre;

    // Se sincroniza el contacto preferido con cada actividad recibida desde Donaciones.
    @Column(name = "medio_contacto_preferido", length = 20)
    private String medioContactoPreferido;

    @Column(name = "contacto_preferido", length = 150)
    private String contactoPreferido;

    @Column(name = "solicitudes_donacion_hechas", nullable = false)
    private int solicitudesDonacionHechas;

    @ManyToMany(cascade = CascadeType.MERGE, fetch = jakarta.persistence.FetchType.EAGER)
    @JoinTable(name = "donante_beneficiario",
            joinColumns = @JoinColumn(name = "id_donante"),
            inverseJoinColumns = @JoinColumn(name = "id_beneficiario"))
    private Set<Beneficiario> beneficiariosAyudados = new HashSet<>();

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "id_progreso_asociado", nullable = false, unique = true)
    private ProgresoAsociado progresoAsociado;

    @OneToMany(mappedBy = "donante", cascade = CascadeType.ALL, orphanRemoval = true, fetch = jakarta.persistence.FetchType.EAGER)
    private List<DatosDonacion> historialDonaciones = new ArrayList<>();

    public Donante(Long id) {
        this.id = id;
        this.solicitudesDonacionHechas = 0;
        this.progresoAsociado = new ProgresoAsociado(GestorMisiones.getInstance());
    }

    public void actualizarNombreSiFalta(String nombre) {
        if (nombre != null && !nombre.isBlank()) {
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
        datosDonacion.setDonante(this);
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
