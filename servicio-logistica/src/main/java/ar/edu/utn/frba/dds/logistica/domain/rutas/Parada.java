package ar.edu.utn.frba.dds.logistica.domain.rutas;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.ArrayList;
import java.util.List;

// Documento EMBEBIDO dentro de Ruta (sin colección propia): una ruta se lee y se escribe con sus paradas.
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // lo usa el ODM para instanciar al leer
public class Parada {

    // Identifica la parada DENTRO de su ruta (igual a numeroParada): es el que usan los endpoints
    // /api/rutas/{idRuta}/paradas/{idParada}. Ya no es un id global único.
    private Integer idParada;

    // Posición dentro de la ruta (1..n): define el orden de visita.
    private Integer numeroParada;

    // Referencia lógica al servicio de Donaciones: solo el id.
    private Integer idEntidadBeneficiariaAsociada;

    private Direccion direccion;

    // Navegación hacia la ruta contenedora. No se persiste (está embebida en ella).
    @JsonIgnore
    @Transient
    private Ruta ruta;

    // Referencias a documentos de la colección "entregas": cada entrega conserva su propio estado y
    // se consulta por separado (pendientes, por fecha, por donación), por eso no se embebe.
    @DBRef
    private List<Entrega> entregas = new ArrayList<>();

    public Parada(Integer numeroParada, Integer idEntidadBeneficiariaAsociada,
                  Direccion direccion, List<Entrega> entregas) {
        this.numeroParada = numeroParada;
        this.idParada = numeroParada; // la parada es un documento embebido: se identifica por su posición dentro de la ruta
        this.idEntidadBeneficiariaAsociada = idEntidadBeneficiariaAsociada;
        this.direccion = direccion;
        this.entregas = new ArrayList<>(entregas);
        this.entregas.forEach(e -> e.setParada(this));
    }

    public void confirmarRecepcion(FotoEntrega foto) {
        entregas.forEach(entrega -> entrega.confirmarEntrega(foto));
    }

    /**
     * «No recibida»: nadie atendió en la parada. Solo afecta a las entregas que seguían en traslado; las que ya tenían
     * un resultado (por ejemplo, ya recibidas) no se tocan. A diferencia de {@link #marcarNoRecibida(String)}, que marca
     * todas las entregas de la parada sin mirar su estado.
     *
     * @return las entregas afectadas, para generar sus eventos
     * @throws IllegalArgumentException si falta la justificación
     * @throws IllegalStateException    si la parada no tiene entregas en traslado
     */
    public List<Entrega> marcarNoRecibidasLasEnTraslado(String justificacion) {
        if (justificacion == null || justificacion.isBlank()) {
            throw new IllegalArgumentException("La justificación de la entrega no recibida es obligatoria");
        }
        List<Entrega> afectadas = entregasEnTrasladoOFalla();
        afectadas.forEach(entrega -> entrega.marcarNoRecibida(justificacion));
        return afectadas;
    }

    /**
     * Incidente: el camión no pudo entregar (calle cortada, avería). Solo afecta a las entregas que seguían en traslado.
     *
     * @return las entregas afectadas, para generar sus eventos
     * @throws IllegalArgumentException si falta el motivo
     * @throws IllegalStateException    si la parada no tiene entregas en traslado
     */
    public List<Entrega> marcarFalladasLasEnTraslado(String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo de la entrega fallida es obligatorio");
        }
        List<Entrega> afectadas = entregasEnTrasladoOFalla();
        afectadas.forEach(entrega -> entrega.marcarFallida(motivo));
        return afectadas;
    }

    private List<Entrega> entregasEnTrasladoOFalla() {
        List<Entrega> enTraslado = entregasEnTraslado();
        if (enTraslado.isEmpty()) {
            throw new IllegalStateException("La parada " + numeroParada + " no tiene entregas en traslado");
        }
        return enTraslado;
    }

    /** Las entregas de la parada que todavía no se recibieron (siguen en traslado). */
    public List<Entrega> entregasEnTraslado() {
        return entregas.stream().filter(e -> e.getEstadoEntrega() == EstadoEntrega.EN_TRASLADO).toList();
    }

    /**
     * La entidad confirma la recepción con una o varias fotos. Solo se confirman las entregas que estaban en traslado:
     * las que ya tenían un resultado (recibidas, no recibidas o fallidas) no se tocan.
     *
     * @return las entregas que se confirmaron, para generar sus eventos
     */
    public List<Entrega> confirmarRecepcionConFotos(List<FotoEntrega> fotos) {
        List<Entrega> confirmadas = entregasEnTraslado();
        confirmadas.forEach(entrega -> entrega.confirmarEntregaConFotos(fotos));
        return confirmadas;
    }

    public void marcarNoRecibida(String justificacion) {
        if (justificacion == null || justificacion.isBlank()) {
            throw new IllegalArgumentException("La justificación de la entrega no recibida es obligatoria");
        }
        entregas.forEach(entrega -> entrega.marcarNoRecibida(justificacion));
    }

    public void marcarFallida(String motivo) {
        entregas.forEach(entrega -> entrega.marcarFallida(motivo));
    }
}
