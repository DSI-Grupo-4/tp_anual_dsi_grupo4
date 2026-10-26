package ar.edu.utn.frba.dds.logistica.domain.rutas;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // lo usa el ODM para instanciar al leer
@Document(collection = "entregas")
public class Entrega {

    // Id numérico: lo asigna GeneradorIds al guardar si es null.
    @Id
    private Integer idEntrega;

    // Referencia lógica al servicio de Donaciones: solo el id (Logística no invoca a Donaciones).
    // Índice único: una donación genera una sola entrega.
    @Indexed(unique = true)
    private Integer idDonacionAsociada;

    // Referencia lógica al servicio de Donaciones: solo el id.
    @Indexed
    private Integer idEntidadBeneficiariaAsociada;

    // Value object embebido en el documento.
    private Direccion direccionDestino;

    @Indexed
    private EstadoEntrega estadoEntrega;

    @Indexed
    private LocalDate fecha;

    // Fotos embebidas: se leen siempre junto con la entrega y no existen sin ella.
    private List<FotoEntrega> fotos = new ArrayList<>();

    // Camión que realizó/realiza la entrega. Denormalizado a propósito respecto de Ruta.camionAsociado.
    // Referencia (DBRef) a la colección "camiones": el estado del camión no se copia, se lee siempre actual.
    @DBRef
    private Camion camionEntrega;

    // Navegación hacia la parada que contiene esta entrega. No se persiste: el vínculo
    // vive del lado de Parada (lista de referencias a entregas). Null hasta asignarla a una ruta.
    @JsonIgnore
    @Transient
    private Parada parada;

    private Integer pesoKG;

    private Integer volumenM3;

    private Integer alturaM;

    private String justificacionFallo; // pedido por el enunciado ("Tocamos timbre pero nadie respondió")

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

    /**
     * Una parada confirma varias entregas con la misma foto del request: cada entrega
     * guarda su propia copia de la foto (cada entrega lleva su lista de fotos embebida).
     */
    public void confirmarEntrega(FotoEntrega foto) {
        FotoEntrega copia = new FotoEntrega(foto.getUrl(), foto.getFecha() != null ? foto.getFecha() : LocalDate.now());
        this.fotos.add(copia);
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
