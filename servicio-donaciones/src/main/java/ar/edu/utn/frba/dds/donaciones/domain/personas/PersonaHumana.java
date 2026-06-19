package ar.edu.utn.frba.dds.donaciones.domain.personas;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersonaHumana extends Persona {
    private String nombre;
    private String apellido;
    private TipoDoc tipo;
    private int ley;
    private Genero genero;

    public PersonaHumana(String nombre, String apellido, TipoDoc tipo, int ley, Genero genero) {
        super();
        this.nombre = nombre;
        this.apellido = apellido;
        this.tipo = tipo;
        this.ley = ley;
        this.genero = genero;
    }

    public String nombreCompleto() {
        return nombre + " " + apellido;
    }
}
