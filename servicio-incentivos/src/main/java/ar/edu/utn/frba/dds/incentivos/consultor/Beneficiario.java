package ar.edu.utn.frba.dds.incentivos.consultor;

import lombok.Getter;

import java.util.Objects;

@Getter
public class Beneficiario {

    private final Long id;
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
