package ar.edu.utn.frba.dds.donaciones.domain.personas;

public class MedioContacto {
    private TipoMedio type;
    private String valor;

    public MedioContacto(TipoMedio type, String valor) {
        this.type = type;
        this.valor = valor;
    }

    public TipoMedio getType() {
        return type;
    }

    public void setType(TipoMedio type) {
        this.type = type;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }
}
