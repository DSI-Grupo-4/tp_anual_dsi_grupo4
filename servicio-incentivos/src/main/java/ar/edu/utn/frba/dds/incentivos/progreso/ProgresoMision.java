package ar.edu.utn.frba.dds.incentivos.progreso;

import ar.edu.utn.frba.dds.incentivos.donacion.DatosDonacion;
import ar.edu.utn.frba.dds.incentivos.misiones.Categoria;
import ar.edu.utn.frba.dds.incentivos.misiones.Mision;
import lombok.Getter;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

@Getter
public abstract class ProgresoMision {

    private Mision misionAsociada;
    private ProgresoInsignia insigniaObtenida;

    protected ProgresoMision(Mision misionAsociada) {
        this.misionAsociada = misionAsociada;
    }

    public ProgresoInsignia completarMision() {
        if (insigniaObtenida != null) {
            return null;
        }
        if (misionAsociada.validarCumplimiento(this)) {
            asignarInsignia();
            return insigniaObtenida;
        }
        return null;
    }

    public void asignarInsignia() {
        this.insigniaObtenida = new ProgresoInsignia(misionAsociada.getInsigniaAsociada());
    }

    public abstract void actualizarProgresoMision(DatosDonacion datosDonacion);

    public void evaluarVigencia(LocalDate fechaReferencia) {
        // Por defecto las misiones no tienen vencimiento; sólo la Racha lo redefine.
    }

    public abstract int obtenerProgresoActual();

    public abstract int distanciaRestante();

    @Getter
    public static class ProgresoRacha extends ProgresoMision {
        private int mesesConsecutivosActuales;
        private LocalDate ultimaDonacionRegistrada;

        public ProgresoRacha(Mision misionAsociada) {
            super(misionAsociada);
            this.mesesConsecutivosActuales = 0;
        }

        @Override
        public void actualizarProgresoMision(DatosDonacion datosDonacion) {
            LocalDate fecha = datosDonacion.getFecha();
            Mision.Racha racha = (Mision.Racha) getMisionAsociada();
            if (ultimaDonacionRegistrada == null
                    || ChronoUnit.MONTHS.between(ultimaDonacionRegistrada, fecha) > racha.getPeriodicidad().getMeses()) {
                mesesConsecutivosActuales = 1;
            } else {
                mesesConsecutivosActuales++;
            }
            ultimaDonacionRegistrada = fecha;
        }

        @Override
        public void evaluarVigencia(LocalDate fechaReferencia) {
            if (ultimaDonacionRegistrada == null) {
                return;
            }
            Mision.Racha racha = (Mision.Racha) getMisionAsociada();
            if (ChronoUnit.MONTHS.between(ultimaDonacionRegistrada, fechaReferencia) > racha.getPeriodicidad().getMeses()) {
                mesesConsecutivosActuales = 0;
            }
        }

        @Override
        public int obtenerProgresoActual() {
            return mesesConsecutivosActuales;
        }

        @Override
        public int distanciaRestante() {
            Mision.Racha racha = (Mision.Racha) getMisionAsociada();
            return Math.max(0, racha.getMesesRequeridos() - mesesConsecutivosActuales);
        }
    }

    @Getter
    public static class ProgresoCompletitud extends ProgresoMision {
        private final Set<Categoria> categoriasCubiertas;

        public ProgresoCompletitud(Mision misionAsociada) {
            super(misionAsociada);
            this.categoriasCubiertas = new HashSet<>();
        }

        @Override
        public void actualizarProgresoMision(DatosDonacion datosDonacion) {
            if (datosDonacion.getCategoria() != null) {
                categoriasCubiertas.add(datosDonacion.getCategoria());
            }
        }

        @Override
        public int obtenerProgresoActual() {
            return categoriasCubiertas.size();
        }

        @Override
        public int distanciaRestante() {
            Mision.Completitud completitud = (Mision.Completitud) getMisionAsociada();
            return Math.max(0, completitud.getCategoriasRequeridas() - categoriasCubiertas.size());
        }
    }

    @Getter
    public static class ProgresoHabilDonador extends ProgresoMision {
        private int mejorDonacionRegistrada;

        public ProgresoHabilDonador(Mision misionAsociada) {
            super(misionAsociada);
            this.mejorDonacionRegistrada = 0;
        }

        @Override
        public void actualizarProgresoMision(DatosDonacion datosDonacion) {
            mejorDonacionRegistrada = Math.max(mejorDonacionRegistrada, datosDonacion.getCantidadBienes());
        }

        @Override
        public int obtenerProgresoActual() {
            return mejorDonacionRegistrada;
        }

        @Override
        public int distanciaRestante() {
            Mision.HabilDonador habilDonador = (Mision.HabilDonador) getMisionAsociada();
            return Math.max(0, habilDonador.getCantidadBienesRequerida() - mejorDonacionRegistrada);
        }
    }

    @Getter
    public static class ProgresoDonacionesExitosas extends ProgresoMision {
        private int donacionesExitosasActuales;

        public ProgresoDonacionesExitosas(Mision misionAsociada) {
            super(misionAsociada);
            this.donacionesExitosasActuales = 0;
        }

        @Override
        public void actualizarProgresoMision(DatosDonacion datosDonacion) {
            if (datosDonacion.isDonacionExitosa()) {
                donacionesExitosasActuales++;
            }
        }

        @Override
        public int obtenerProgresoActual() {
            return donacionesExitosasActuales;
        }

        @Override
        public int distanciaRestante() {
            Mision.DonacionesExitosas donacionesExitosas = (Mision.DonacionesExitosas) getMisionAsociada();
            return Math.max(0, donacionesExitosas.getDonacionesRequeridas() - donacionesExitosasActuales);
        }
    }
}
