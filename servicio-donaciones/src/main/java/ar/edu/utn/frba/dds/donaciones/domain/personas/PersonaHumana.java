package ar.edu.utn.frba.dds.donaciones.domain.personas;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // requerido por JPA
@Entity
@Table(name = "persona_humana")
@DiscriminatorValue("HUMANA")
public class PersonaHumana extends Persona {

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "apellido", nullable = false, length = 100)
    private String apellido;

    @Column(name = "edad")
    private Integer edad;

    @Column(name = "documento", length = 20)
    private String documento;

    @Enumerated(EnumType.STRING)
    @Column(name = "genero")
    private Genero genero;

    public PersonaHumana(
            String nombre,
            String apellido,
            Integer edad,
            String documento,
            Genero genero) {
        super(); //hereda todos los atributos de la abstracta

        this.nombre = nombre;
        this.apellido = apellido;
        this.edad = edad;
        this.documento = documento;
        this.genero = genero;
    }

    public String nombreCompleto() {
        return nombre + " " + apellido;
    }
}
