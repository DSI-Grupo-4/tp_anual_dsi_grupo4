package ar.edu.utn.frba.dds.donaciones.domain.personas;

import ar.edu.utn.frba.dds.donaciones.domain.donaciones.Donacion;
import ar.edu.utn.frba.dds.donaciones.domain.lugares.Direccion;
import ar.edu.utn.frba.dds.donaciones.domain.necesidades.Necesidad;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // requerido por JPA
@Entity
@Table(name = "entidad_beneficiaria")
public class EntidadBeneficiaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "persona_juridica_id", nullable = false, unique = true)
    private PersonaJuridica entidad;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "calle", column = @Column(name = "dir_calle", length = 150)),
            @AttributeOverride(name = "numero", column = @Column(name = "dir_numero", length = 20)),
            @AttributeOverride(name = "ciudad.nombre", column = @Column(name = "dir_ciudad", length = 100)),
            @AttributeOverride(name = "ciudad.provincia.nombre", column = @Column(name = "dir_provincia", length = 100))
    })
    private Direccion direccion;

    @OneToMany(mappedBy = "entidadBeneficiaria", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Necesidad> necesidades;

    @OneToMany(mappedBy = "entidadBeneficiaria", fetch = FetchType.EAGER)
    private List<Donacion> donacionesRecibidas;

    public EntidadBeneficiaria(Long id, PersonaJuridica entidad, String descripcion) {
        this.id = id;
        this.entidad = entidad;
        this.descripcion = descripcion;
        this.necesidades = new ArrayList<>();
        this.donacionesRecibidas = new ArrayList<>();
    }

    public void agregarNecesidad(Necesidad necesidad) {
        necesidad.setEntidadBeneficiaria(this);
        this.necesidades.add(necesidad);
    }

    public void registrarAyuda(Donacion donacion) {
        donacionesRecibidas.add(donacion);
    }

    public Integer cantidadAyudasRecibidas() {
        return donacionesRecibidas.size();
    }

    public List<Necesidad> necesidadesPendientes() {
        return necesidades.stream()
                .filter(n -> !n.satisfecha())
                .toList();
    }

    /**
     * A diferencia de un donante jurídico (1 solo representante, ver
     * Donante.agregarRepresentante), una entidad beneficiaria admite varios
     * representantes sin restricción de cardinalidad.
     */
    public void agregarRepresentante(PersonaHumana representante) {
        entidad.agregarRepresentante(representante);
    }
}
