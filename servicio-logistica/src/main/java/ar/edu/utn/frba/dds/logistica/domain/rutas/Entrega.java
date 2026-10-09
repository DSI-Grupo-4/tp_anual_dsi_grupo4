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
    private java.time.LocalDateTime fechaHoraEntrega;
    private String seguimientoUrl;
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
        this.fechaHoraEntrega = java.time.LocalDateTime.now();
        cambiarEstado(EstadoEntrega.ENTREGADA);
    }

    public void marcarNoRecibida(String justificacion) {
        this.justificacionFallo = justificacion;
        cambiarEstado(EstadoEntrega.NO_RECIBIDA);
    }

    /**
     * El chofer o una persona administradora reporta que la entrega no se
     * pudo concretar por un motivo distinto a que la entidad no la haya
     * recibido -- vencimiento de los bienes antes de llegar, incidente
     * logístico en el camino, etc. (ejemplos textuales del enunciado).
     */
    public void marcarFallida(String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo de la entrega fallida es obligatorio");
        }
        this.justificacionFallo = motivo;
        cambiarEstado(EstadoEntrega.FALLIDA);
    }

    /**
     * La persona administradora revisó una entrega NO_RECIBIDA/FALLIDA y
     * determinó que se puede reintentar -- "se dejará constancia del estado
     * correspondiente" (texto del enunciado): queda en REPLANIFICABLE, que
     * EntregaRepository.obtenerPendientes() trata igual que PENDIENTE para
     * la próxima corrida de PlanificadorService.planificarRutasDelDia().
     */
    public void reingresarADeposito() {
        if (estadoEntrega != EstadoEntrega.NO_RECIBIDA && estadoEntrega != EstadoEntrega.FALLIDA) {
            throw new IllegalStateException(
                    "La entrega " + idEntrega + " no puede reingresar al depósito estando en estado " + estadoEntrega);
        }
        cambiarEstado(EstadoEntrega.REPLANIFICABLE);
    }
}
