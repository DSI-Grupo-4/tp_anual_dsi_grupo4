package ar.edu.utn.frba.dds.incentivos.ranking;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // requerido por JPA
@Entity
@Table(name = "ranking")
public class Ranking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ranking")
    private Integer idRanking;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    @OneToMany(mappedBy = "ranking", cascade = CascadeType.ALL, fetch = jakarta.persistence.FetchType.EAGER)
    @OrderBy("posicion ASC")
    private List<ActividadMensualDonante> topDonantes;

    public Ranking(LocalDate fechaEmision, List<ActividadMensualDonante> topDonantes) {
        this.fechaEmision = fechaEmision;
        this.topDonantes = topDonantes;
        for (int i = 0; i < topDonantes.size(); i++) {
            ActividadMensualDonante actividad = topDonantes.get(i);
            actividad.setRanking(this);
            actividad.setPosicion(i + 1);
        }
    }
}
