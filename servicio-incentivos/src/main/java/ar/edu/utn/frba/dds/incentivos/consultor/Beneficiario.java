package ar.edu.utn.frba.dds.incentivos.consultor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // requerido por JPA
@Entity
@Table(name = "beneficiario")
public class Beneficiario {

    // Viene de Donaciones (Beneficiario.id): sin @GeneratedValue, Incentivos
    // no crea beneficiarios, solo los referencia por el id que le llega.
    @Id
    @Column(name = "id_beneficiario")
    private Long id;

    @Column(name = "nombre", length = 150)
    private String nombre;

    public Beneficiario(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Beneficiario)) {
            return false;
        }
        Beneficiario that = (Beneficiario) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
