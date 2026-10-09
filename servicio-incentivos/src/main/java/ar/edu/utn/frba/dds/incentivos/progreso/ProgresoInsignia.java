package ar.edu.utn.frba.dds.incentivos.progreso;

import ar.edu.utn.frba.dds.incentivos.misiones.Insignia;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // requerido por JPA
@Entity
@Table(name = "progreso_insignia")
public class ProgresoInsignia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_progreso_insignia")
    private Integer idProgresoInsignia;

    @ManyToOne(optional = false, cascade = CascadeType.MERGE)
    @JoinColumn(name = "id_insignia", nullable = false)
    private Insignia insigniaAsociada;

    @Column(name = "fecha_obtencion", nullable = false)
    private LocalDate fechaObtencion;

    @Column(name = "visible", nullable = false)
    private boolean visible;

    public ProgresoInsignia(Insignia insigniaAsociada) {
        this.insigniaAsociada = insigniaAsociada;
        this.fechaObtencion = LocalDate.now();
        this.visible = true;
    }

    public void marcarVisible() {
        this.visible = true;
    }

    public void ocultarInsignia() {
        this.visible = false;
    }
}
