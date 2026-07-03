package ar.edu.utn.frba.dds.logistica.domain.rutas;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class Entrega {
    private Integer idEntrega;
    private Integer idDonacionAsociada;
    private EstadoEntrega estadoEntrega;
    private LocalDate fecha;
    private FotoEntrega fotoEntrega;
    private Camion camionEntrega;
    private Integer pesoKG;
    private Integer volumenM3;
    private Integer alturaM;

    public Entrega(Integer idEntrega,
                   Integer idDonacionAsociada,
                   LocalDate fecha,
                   Integer pesoKG,
                   Integer volumenM3,
                   Integer alturaM) {

        this.idEntrega = idEntrega;
        this.idDonacionAsociada = idDonacionAsociada;
        this.fecha = fecha;
        this.pesoKG = pesoKG;
        this.volumenM3 = volumenM3;
        this.alturaM = alturaM;
        this.estadoEntrega = EstadoEntrega.PENDIENTE;
    }

    public void cambiarEstado (EstadoEntrega estadoNuevo){
        this.estadoEntrega = estadoNuevo;
    }

    public void iniciarTranslado(){
        cambiarEstado(EstadoEntrega.EN_TRASLADO);
    }
}
