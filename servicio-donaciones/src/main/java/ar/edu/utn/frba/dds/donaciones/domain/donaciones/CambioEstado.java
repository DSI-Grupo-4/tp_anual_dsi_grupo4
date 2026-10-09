package ar.edu.utn.frba.dds.donaciones.domain.donaciones;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/** Auditoría de estados. Se lee ordenado por id. */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // requerido por JPA
@Entity
@Table(name = "cambio_estado")
public class CambioEstado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "donacion_id", nullable = false)
    private Donacion donacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", nullable = false)
    private EstadoTrack estadoNuevo;

    @Column(name = "fecha_cambio", nullable = false)
    private LocalDateTime fechaCambio;

    @Column(name = "justificacion", length = 255)
    private String justificacion;

    public CambioEstado(EstadoTrack estadoNuevo, String justificacion) {
        this.estadoNuevo = estadoNuevo;
        this.fechaCambio = LocalDateTime.now();
        this.justificacion = justificacion;
    }

    void asignarDonacion(Donacion donacion) {
        this.donacion = donacion;
    }
}
