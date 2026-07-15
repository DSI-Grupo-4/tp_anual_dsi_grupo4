package ar.edu.utn.frba.dds.donaciones.domain.personas;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersonaHumana extends Persona {
    private String nombre;
    private String apellido;
    private Integer edad;
    private String documento;
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
