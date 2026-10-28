package ar.edu.utn.frba.dds.incentivos.ranking;

import ar.edu.utn.frba.dds.incentivos.donante.Donante;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // requerido por JPA
@Entity
@Table(name = "actividad_mensual_donante", uniqueConstraints = {
        @UniqueConstraint(name = "uk_actividad_mensual_ranking_posicion", columnNames = {"id_ranking", "posicion"})
})
public class ActividadMensualDonante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_actividad_mensual_donante")
    private Integer idActividadMensualDonante;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_donante", nullable = false)
    private Donante donanteAsociado;

    @Column(name = "cantidad", nullable = false)
    private int cantidad;

    // Null = contador del mes en curso (HistorialRanking.donantesMisionesMensuales);
    // con valor = ya forma parte de un Ranking.topDonantes cerrado.
    @Setter
    @ManyToOne
    @JoinColumn(name = "id_ranking")
    private Ranking ranking;

    @Setter
    @Column(name = "posicion")
    private Integer posicion;

    public ActividadMensualDonante(Donante donanteAsociado) {
        this.donanteAsociado = donanteAsociado;
        this.cantidad = 0;
    }

    public void incrementar() {
        cantidad++;
    }
}
