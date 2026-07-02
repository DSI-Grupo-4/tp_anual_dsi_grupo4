package ar.edu.utn.frba.dds.incentivos.progreso;

import ar.edu.utn.frba.dds.incentivos.donacion.DatosDonacion;
import ar.edu.utn.frba.dds.incentivos.misiones.Categoria;
import ar.edu.utn.frba.dds.incentivos.misiones.GestorMisiones;
import ar.edu.utn.frba.dds.incentivos.misiones.Mision;
import lombok.Getter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
public class ProgresoAsociado {

    private List<ProgresoCategoria> categoriasObtenidas;
    private GestorMisiones gestorAsociado;
    private ProgresoMision misionActual;
    private int indiceMisionActual;

    public ProgresoAsociado(GestorMisiones gestorAsociado) {
        this.gestorAsociado = gestorAsociado;
        this.categoriasObtenidas = new ArrayList<>();
        if (gestorAsociado.cantCategorias() > 0) {
            iniciarCategoria(gestorAsociado.siguienteCategoria(0));
        }
    }

    private void iniciarCategoria(Categoria categoria) {
        ProgresoCategoria progresoCategoria = new ProgresoCategoria(categoria);
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
