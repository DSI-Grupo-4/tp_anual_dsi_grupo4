package ar.edu.utn.frba.dds.donaciones.domain.donaciones;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Idempotencia de eventos de Logística -- antes un Set&lt;String&gt; en memoria en DonacionService. */
@Getter
@NoArgsConstructor
@Entity
@Table(name = "evento_aplicado")
public class EventoAplicado {

    @Id
    @Column(name = "evento_id", length = 36)
    private String eventoId;

    @Column(name = "aplicado_en", nullable = false)
    private LocalDateTime aplicadoEn;

    public EventoAplicado(String eventoId) {
        this.eventoId = eventoId;
        this.aplicadoEn = LocalDateTime.now();
    }
}
