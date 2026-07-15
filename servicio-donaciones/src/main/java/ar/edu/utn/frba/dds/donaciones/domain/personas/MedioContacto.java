package ar.edu.utn.frba.dds.donaciones.domain.personas;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MedioContacto {
    private TipoContacto tipo;
    private String valor;
    private Boolean esPreferido;

    public MedioContacto(
            TipoContacto tipo,
            String valor,
            Boolean esPreferido) {

        this.tipo = tipo;
        this.valor = valor;
        this.esPreferido = esPreferido;
    }

    public void marcarComoPreferido() {
        this.esPreferido = true;
    }

    public void desmarcarPreferido() {
        this.esPreferido = false;
    }

    public Boolean esPreferido() {
        return esPreferido;
    }
}
