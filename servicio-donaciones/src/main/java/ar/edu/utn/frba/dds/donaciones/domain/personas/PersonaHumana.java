package ar.edu.utn.frba.dds.donaciones.domain.personas;

public class PersonaHumana extends Persona {
    private String nombre;
    private String apellido;
    private Integer edad;
    private String documento;
    private String genero;

    public PersonaHumana(
            String nombre,
            String apellido,
            Integer edad,
            String documento,
            String genero) {
        super(); //hereda todos los atributos de la abstracta

        this.nombre = nombre;
        this.apellido = apellido;
        this.edad = edad;
        this.documento = documento;
        this.genero = genero;
    }
}
