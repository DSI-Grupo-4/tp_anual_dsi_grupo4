package ar.edu.utn.frba.dds.logistica.domain.eventos;

import ar.edu.utn.frba.dds.logistica.domain.rutas.Entrega;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // lo usa el ODM para instanciar al leer
@Document(collection = "eventos_logisticos")
public class EventoLogistico {

    // UUID propio (se genera en el constructor). Requiere spring.data.mongodb.uuid-representation=standard.
    @Id
    private UUID idEvento;

    private TipoEvento tipoEvento;

    // Donaciones consulta solo los no publicados.
    @Indexed
    private boolean publicado;

    // Referencia a la entrega del evento: Donaciones lee de ahí el id de donación y el camión.
    @DBRef
    private Entrega entregaAsociada;

    @Indexed
    private LocalDateTime fechaGeneracion;

    public EventoLogistico(TipoEvento tipoEvento, Entrega entregaAsociada) {
        this.idEvento = UUID.randomUUID();
        this.tipoEvento = tipoEvento;
        this.entregaAsociada = entregaAsociada;
        this.publicado = false;
        this.fechaGeneracion = LocalDateTime.now();
    }

    public void marcarPublicado() {
        this.publicado = true;
    }
}
