package ar.edu.utn.frba.dds.incentivos.progreso;

import ar.edu.utn.frba.dds.incentivos.misiones.Categoria;
import ar.edu.utn.frba.dds.incentivos.misiones.Mision;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // requerido por JPA
@Entity
@Table(name = "progreso_categoria", uniqueConstraints = {
        @UniqueConstraint(name = "uk_progreso_categoria_asociado_orden", columnNames = {"id_progreso_asociado", "orden"})
})
public class ProgresoCategoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_progreso_categoria")
    private Integer idProgresoCategoria;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_progreso_asociado", nullable = false)
    private ProgresoAsociado progresoAsociado;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    // Posición en ProgresoAsociado.categoriasObtenidas -- asignada por quien
    // crea este ProgresoCategoria (ProgresoAsociado.iniciarCategoria()).
    @Column(name = "orden", nullable = false)
    private Integer orden;

    @OneToMany(mappedBy = "progresoCategoriaAsociado", cascade = CascadeType.ALL, orphanRemoval = true, fetch = jakarta.persistence.FetchType.EAGER)
    @OrderBy("orden ASC")
    private List<ProgresoMision> misiones = new ArrayList<>();

    public ProgresoCategoria(ProgresoAsociado progresoAsociado, Categoria categoria, int orden) {
        this.progresoAsociado = progresoAsociado;
        this.orden = orden;
        this.nombre = categoria.getNombre();
        for (int i = 0; i < categoria.cantMisiones(); i++) {
            Mision mision = categoria.indexMision(i);
            ProgresoMision progresoMision = mision.crearProgreso();
            progresoMision.asignarProgresoCategoria(this, i);
            this.misiones.add(progresoMision);
        }
    }

    public int cantMisionesCompletadas() {
        return (int) misiones.stream()
                .filter(progresoMision -> progresoMision.getInsigniaObtenida() != null)
                .count();
    }
}
