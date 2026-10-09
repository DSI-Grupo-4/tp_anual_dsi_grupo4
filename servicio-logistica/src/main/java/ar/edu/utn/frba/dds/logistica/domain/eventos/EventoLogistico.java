package ar.edu.utn.frba.dds.logistica.domain.eventos;

import ar.edu.utn.frba.dds.logistica.domain.rutas.Entrega;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // requerido por JPA
@Entity
@Table(name = "evento_logistico", indexes = {
        @Index(name = "idx_evento_logistico_publicado_fecha", columnList = "publicado, fecha_generacion")
})
public class EventoLogistico {
    // CHAR(36): sin esto Hibernate guarda el UUID como BINARY(16) en MySQL
    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id_evento", length = 36)
    private UUID idEvento;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento", nullable = false)
    private TipoEvento tipoEvento;

    @Column(name = "publicado", nullable = false)
    private boolean publicado;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_entrega", nullable = false)
    private Entrega entregaAsociada;

    @Column(name = "fecha_generacion", nullable = false)
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
