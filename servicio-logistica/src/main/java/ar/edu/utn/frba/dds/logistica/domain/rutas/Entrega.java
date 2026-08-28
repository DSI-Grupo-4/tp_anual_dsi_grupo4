package ar.edu.utn.frba.dds.logistica.domain.rutas;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class Entrega {
    private Integer idEntrega;
    private Integer idDonacionAsociada;
    private Integer idEntidadBeneficiariaAsociada; // NUEVO: necesario para agrupar en Paradas
    private Direccion direccionDestino;             // NUEVO: idem, sin consultar a Donaciones
    private EstadoEntrega estadoEntrega;
    private LocalDate fecha;
    private FotoEntrega fotoEntrega;
    private Camion camionEntrega;
    private Integer pesoKG;
    private Integer volumenM3;
    private Integer alturaM;
    private String justificacionFallo; // NUEVO: pedido por el enunciado ("Tocamos timbre pero nadie respondió")

    public Entrega(Integer idEntrega, Integer idDonacionAsociada, Integer idEntidadBeneficiariaAsociada,
                   Direccion direccionDestino, LocalDate fecha,
                   Integer pesoKG, Integer volumenM3, Integer alturaM) {
        this.idEntrega = idEntrega;
        this.idDonacionAsociada = idDonacionAsociada;
        this.idEntidadBeneficiariaAsociada = idEntidadBeneficiariaAsociada;
        this.direccionDestino = direccionDestino;
        this.fecha = fecha;
        this.pesoKG = pesoKG;
        this.volumenM3 = volumenM3;
        this.alturaM = alturaM;
        this.estadoEntrega = EstadoEntrega.PENDIENTE;
    }

    public void cambiarEstado(EstadoEntrega estadoNuevo) {
        this.estadoEntrega = estadoNuevo;
    }

    public void asignarARuta(Camion camion) {
        this.camionEntrega = camion;
        cambiarEstado(EstadoEntrega.ASIGNADA_A_RUTA);
    }

    public void iniciarTraslado() {
        cambiarEstado(EstadoEntrega.EN_TRASLADO);
    }

    public void confirmarEntrega(FotoEntrega foto) {
        this.fotoEntrega = foto;
        cambiarEstado(EstadoEntrega.ENTREGADA);
    }

    public void marcarNoRecibida(String justificacion) {
        this.justificacionFallo = justificacion;
        cambiarEstado(EstadoEntrega.NO_RECIBIDA);
    }
}
