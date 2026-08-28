package ar.edu.utn.frba.dds.logistica.domain.rutas;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Parada {
    private Integer idParada;
    private Integer idEntidadBeneficiariaAsociada;
    private Direccion direccion;
    private List<Entrega> entregas;

    public Parada(Integer idParada, Integer idEntidadBeneficiariaAsociada,
                  Direccion direccion, List<Entrega> entregas) {
        this.idParada = idParada;
        this.idEntidadBeneficiariaAsociada = idEntidadBeneficiariaAsociada;
        this.direccion = direccion;
        this.entregas = entregas;
    }

    public void confirmarRecepcion(FotoEntrega foto) {
        entregas.forEach(entrega -> entrega.confirmarEntrega(foto));
    }

    public void marcarNoRecibida(String justificacion) {
        entregas.forEach(entrega -> entrega.marcarNoRecibida(justificacion));
    }
}