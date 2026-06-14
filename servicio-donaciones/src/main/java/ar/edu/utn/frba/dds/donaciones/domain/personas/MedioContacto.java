package ar.edu.utn.frba.dds.donaciones.domain.personas;


import java.util.function.Predicate;

public class MedioContacto {
    private TipoContacto tipo;
    private String valor;
    public static Boolean esPreferido;

    public MedioContacto(
            TipoContacto tipo,
            String valor,
            boolean esPreferido) {

        this.tipo = tipo;
        this.valor = valor;
        this.esPreferido = esPreferido;
    }

    public void marcarComoPreferido() {
        this.esPreferido = true;
    }

    public Boolean esPreferido() {
        return esPreferido;
    }
}
