package ar.edu.utn.frba.dds.donaciones.domain.personas;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "persona")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "tipo_persona", discriminatorType = DiscriminatorType.STRING)
public abstract class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = jakarta.persistence.FetchType.EAGER)
    @JoinColumn(name = "persona_id", nullable = false)
    private List<MedioContacto> mediosContacto;

    public Persona() {
        this.mediosContacto = new ArrayList<>();
    }

    public void agregarMedio(MedioContacto medio) {
        mediosContacto.add(medio);
    }

    public void eliminarMedio(MedioContacto medio) {
        mediosContacto.remove(medio);
    }

    public Optional<MedioContacto> medioPreferido() {
        return mediosContacto.stream()
                .filter(MedioContacto::esPreferido)
                .findFirst();
    } //devuelve el medio de contacto preferido en caso de haber

}
