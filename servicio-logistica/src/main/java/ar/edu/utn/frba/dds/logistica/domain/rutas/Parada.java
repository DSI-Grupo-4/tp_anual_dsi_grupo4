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
