package ar.edu.utn.frba.dds.incentivos.progreso;

import ar.edu.utn.frba.dds.incentivos.donacion.DatosDonacion;
import ar.edu.utn.frba.dds.incentivos.misiones.Categoria;
import ar.edu.utn.frba.dds.incentivos.misiones.GestorMisiones;
import ar.edu.utn.frba.dds.incentivos.misiones.Mision;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // requerido por JPA
@Entity
@Table(name = "progreso_asociado")
public class ProgresoAsociado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_progreso_asociado")
    private Integer idProgresoAsociado;

    @OneToMany(mappedBy = "progresoAsociado", cascade = CascadeType.ALL, orphanRemoval = true, fetch = jakarta.persistence.FetchType.EAGER)
    @OrderBy("orden ASC")
    private List<ProgresoCategoria> categoriasObtenidas = new ArrayList<>();

    // No es un bean de Spring ni se persiste: cada ProgresoAsociado conoce
    // el catálogo a través del singleton, no por relación de base de datos.
    @Transient
    private GestorMisiones gestorAsociado;

    @OneToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "id_progreso_mision_actual")
    private ProgresoMision misionActual;

    @Column(name = "indice_mision_actual", nullable = false)
    private int indiceMisionActual;

    public ProgresoAsociado(GestorMisiones gestorAsociado) {
        this.gestorAsociado = gestorAsociado;
        if (gestorAsociado.cantCategorias() > 0) {
            iniciarCategoria(gestorAsociado.siguienteCategoria(0));
        }
    }

    private void iniciarCategoria(Categoria categoria) {
        ProgresoCategoria progresoCategoria = new ProgresoCategoria(this, categoria, categoriasObtenidas.size());
        categoriasObtenidas.add(progresoCategoria);
        indiceMisionActual = 0;
        misionActual = progresoCategoria.getMisiones().isEmpty()
                ? null
                : progresoCategoria.getMisiones().get(0);
    }

    public void subirCategoria() {
        int siguienteIndice = categoriasObtenidas.size();
        if (siguienteIndice < gestorAsociado.cantCategorias()) {
            iniciarCategoria(gestorAsociado.siguienteCategoria(siguienteIndice));
        }
    }

    public Mision siguienteMision() {
        ProgresoCategoria categoriaActual = categoriasObtenidas.get(categoriasObtenidas.size() - 1);
        if (indiceMisionActual + 1 < categoriaActual.getMisiones().size()) {
            indiceMisionActual++;
            misionActual = categoriaActual.getMisiones().get(indiceMisionActual);
        } else {
            subirCategoria();
        }
        return misionActual == null ? null : misionActual.getMisionAsociada();
    }

    public ProgresoInsignia actualizarProgreso(DatosDonacion datosDonacion) {
        if (misionActual == null) {
            return null;
        }
        misionActual.actualizarProgresoMision(datosDonacion);
        ProgresoInsignia obtenida = misionActual.completarMision();
        if (obtenida != null) {
            siguienteMision();
        }
        return obtenida;
    }

    public void verificarVigenciaMisiones() {
        LocalDate hoy = LocalDate.now();
        for (ProgresoCategoria progresoCategoria : categoriasObtenidas) {
            for (ProgresoMision progresoMision : progresoCategoria.getMisiones()) {
                progresoMision.evaluarVigencia(hoy);
            }
        }
    }
}
