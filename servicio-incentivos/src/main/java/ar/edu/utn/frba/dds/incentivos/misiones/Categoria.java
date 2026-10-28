package ar.edu.utn.frba.dds.incentivos.misiones;

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
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // requerido por JPA
@Entity
@Table(name = "categoria")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Integer idCategoria;

    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    private String nombre;

    // Posición en catalogoCategorias (GestorMisiones) -- define el orden real
    // de escalada (Colaborador < Sostenedor < Transformador), asignada por
    // GestorMisiones.sembrarCatalogo() al sembrar, no editable por fuera.
    @Column(name = "orden", nullable = false, unique = true)
    private Integer orden;

    @OneToMany(mappedBy = "categoriaAsociada", cascade = CascadeType.ALL, orphanRemoval = true, fetch = jakarta.persistence.FetchType.EAGER)
    @OrderBy("orden ASC")
    private List<Mision> misiones = new ArrayList<>();

    public Categoria(String nombre) {
        this.nombre = nombre;
    }

    public void agregarMision(Mision mision) {
        mision.asignarCategoria(this, misiones.size());
        misiones.add(mision);
    }

    public Mision indexMision(int indice) {
        return misiones.get(indice);
    }

    public int cantMisiones() {
        return misiones.size();
    }
}
