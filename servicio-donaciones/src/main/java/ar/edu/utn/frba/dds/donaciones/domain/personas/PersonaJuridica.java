package ar.edu.utn.frba.dds.donaciones.domain.personas;

public class PersonaJuridica extends Persona {
    private String razonSocial;
    private TipoOrganizacion tipo;
    private String rubro;

    private PersonaHumana representante;

    public PersonaJuridica(
            String razonSocial,
            TipoOrganizacion tipo,
            String rubro,
            PersonaHumana representante) {

        super();

        this.razonSocial = razonSocial;
        this.tipo = tipo;
        this.rubro = rubro;
        this.representante = representante;
    }
}
