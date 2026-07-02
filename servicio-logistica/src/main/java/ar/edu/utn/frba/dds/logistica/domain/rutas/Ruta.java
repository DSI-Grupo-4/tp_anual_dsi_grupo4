package ar.edu.utn.frba.dds.logistica.domain.rutas;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class Ruta {
    private Integer idRuta;
    private Camion camionAsociado;
    private Chofer chofer;
    private LocalDate fecha;
    private List<Parada> paradas;
    private EstadoRuta estadoRuta;

    public void iniciarRuta(){};

    public void finalizarRuta(){};

    public Boolean completoTodasLasEntregas() {

        return paradas.stream()
                .flatMap(parada -> parada.getEntregas().stream())
                .allMatch(entrega -> entrega.getEstadoEntrega() == EstadoEntrega.ENTREGADA);
    }

}
