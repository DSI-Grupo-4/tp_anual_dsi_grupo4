package domain.personas;

public class MedioContacto {
    private TipoContacto tipo;
    private String valor;
    private Boolean preferido;

    public MedioContacto(TipoContacto tipo, String valor, boolean preferido) {
        this.tipo = tipo;
        this.valor = valor;
        this.preferido = preferido;
    }

    public TipoContacto getTipo() {
        return tipo;
    }

    public String getValor() {
        return valor;
    }

    public void marcarComoPreferido() {
        this.preferido = true;
    }

    public boolean esPreferido() {
        return preferido;
    }
}
