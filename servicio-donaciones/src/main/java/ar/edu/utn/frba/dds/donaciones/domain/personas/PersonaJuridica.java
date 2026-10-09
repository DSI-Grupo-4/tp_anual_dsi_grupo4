package ar.edu.utn.frba.dds.donaciones.domain.personas;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
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
@Table(name = "persona_juridica")
@DiscriminatorValue("JURIDICA")
public class PersonaJuridica extends Persona {

    @Column(name = "razon_social", nullable = false, length = 150)
    private String razonSocial;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_organizacion")
    private TipoOrganizacion tipo;

    @Column(name = "rubro", length = 100)
    private String rubro;

    // Lista siempre (nunca un único representante fijo): la cardinalidad
    // real depende del rol que cumpla esta persona jurídica (1 si actúa
    // como donante, N si actúa como entidad beneficiaria) y se valida en
    // Donante/EntidadBeneficiaria, que son quienes conocen ese rol.
    @ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JoinTable(name = "representante",
            joinColumns = @JoinColumn(name = "persona_juridica_id"),
            inverseJoinColumns = @JoinColumn(name = "persona_humana_id"))
    private List<PersonaHumana> representantes;

    public PersonaJuridica(
            String razonSocial,
            TipoOrganizacion tipo,
            String rubro,
            List<PersonaHumana> representantes) {

        super();

        this.razonSocial = razonSocial;
        this.tipo = tipo;
        this.rubro = rubro;
        this.representantes = representantes != null ? representantes : new ArrayList<>();
    }

    public void agregarRepresentante(PersonaHumana representante) {
        representantes.add(representante);
    }

    public String nombreRepresentante() {
        return representantes.isEmpty() ? null : representantes.get(0).nombreCompleto();
    }
}
