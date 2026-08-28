package ar.edu.utn.frba.dds.logistica.domain.rutas;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class Ruta {
    private Integer idRuta;
    private Camion camionAsociado;
    private Chofer chofer;
    private LocalDate fecha;
    private List<Parada> paradas;
    private EstadoRuta estadoRuta;

    public Ruta(Integer idRuta, Camion camionAsociado, Chofer chofer,
                LocalDate fecha, List<Parada> paradas) {
        this.idRuta = idRuta;
        this.camionAsociado = camionAsociado;
        this.chofer = chofer;
        this.fecha = fecha;
        this.paradas = paradas;
        this.estadoRuta = EstadoRuta.PLANIFICADA;
    }

    public void iniciarRuta() {
        this.estadoRuta = EstadoRuta.INICIADA;
        this.camionAsociado.cambiarEstado(EstadoCamion.EN_RUTA);
        paradas.stream()
                .flatMap(p -> p.getEntregas().stream())
                .forEach(Entrega::iniciarTraslado);
    }

    public void finalizarRuta() {
        this.estadoRuta = EstadoRuta.FINALIZADA;
        this.camionAsociado.cambiarEstado(EstadoCamion.DISPONIBLE);
    }

    public Boolean completoTodasLasEntregas() {
        return paradas.stream()
                .flatMap(p -> p.getEntregas().stream())
                .allMatch(e -> e.getEstadoEntrega() == EstadoEntrega.ENTREGADA);
    }
}
