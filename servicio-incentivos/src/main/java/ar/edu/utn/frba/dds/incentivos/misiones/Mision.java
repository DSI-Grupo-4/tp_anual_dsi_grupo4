package ar.edu.utn.frba.dds.incentivos.misiones;

import ar.edu.utn.frba.dds.incentivos.progreso.ProgresoMision;
import lombok.Getter;

@Getter
public abstract class Mision {

    private String nombreMision;
    private Insignia insigniaAsociada;

    protected Mision(String nombreMision, Insignia insigniaAsociada) {
        this.nombreMision = nombreMision;
        this.insigniaAsociada = insigniaAsociada;
    }

    public abstract boolean validarCumplimiento(ProgresoMision progresoMision);

    public abstract ProgresoMision crearProgreso();

    public static class Racha extends Mision {
        private final int mesesRequeridos;
        private final Periodicidad periodicidad;

        public Racha(String nombreMision, Insignia insigniaAsociada,
                     int mesesRequeridos, Periodicidad periodicidad) {
            super(nombreMision, insigniaAsociada);
            this.mesesRequeridos = mesesRequeridos;
            this.periodicidad = periodicidad;
        }

        public int getMesesRequeridos() {
            return mesesRequeridos;
        }

        public Periodicidad getPeriodicidad() {
            return periodicidad;
        }

        @Override
        public boolean validarCumplimiento(ProgresoMision progresoMision) {
            ProgresoMision.ProgresoRacha progreso = (ProgresoMision.ProgresoRacha) progresoMision;
            return progreso.getMesesConsecutivosActuales() >= mesesRequeridos;
        }

        @Override
        public ProgresoMision crearProgreso() {
            return new ProgresoMision.ProgresoRacha(this);
        }
    }

    public static class Completitud extends Mision {
        private final int categoriasRequeridas;

        public Completitud(String nombreMision, Insignia insigniaAsociada, int categoriasRequeridas) {
            super(nombreMision, insigniaAsociada);
            this.categoriasRequeridas = categoriasRequeridas;
        }

        public int getCategoriasRequeridas() {
            return categoriasRequeridas;
        }

        @Override
        public boolean validarCumplimiento(ProgresoMision progresoMision) {
            ProgresoMision.ProgresoCompletitud progreso = (ProgresoMision.ProgresoCompletitud) progresoMision;
            return progreso.getCategoriasCubiertas().size() >= categoriasRequeridas;
        }

        @Override
        public ProgresoMision crearProgreso() {
            return new ProgresoMision.ProgresoCompletitud(this);
        }
    }

    public static class HabilDonador extends Mision {
        private final int cantidadBienesRequerida;

        public HabilDonador(String nombreMision, Insignia insigniaAsociada, int cantidadBienesRequerida) {
            super(nombreMision, insigniaAsociada);
            this.cantidadBienesRequerida = cantidadBienesRequerida;
        }

        public int getCantidadBienesRequerida() {
            return cantidadBienesRequerida;
        }

        @Override
        public boolean validarCumplimiento(ProgresoMision progresoMision) {
            ProgresoMision.ProgresoHabilDonador progreso = (ProgresoMision.ProgresoHabilDonador) progresoMision;
            return progreso.getMejorDonacionRegistrada() >= cantidadBienesRequerida;
        }

        @Override
        public ProgresoMision crearProgreso() {
            return new ProgresoMision.ProgresoHabilDonador(this);
        }
    }

    public static class DonacionesExitosas extends Mision {
        private final int donacionesRequeridas;

        public DonacionesExitosas(String nombreMision, Insignia insigniaAsociada, int donacionesRequeridas) {
            super(nombreMision, insigniaAsociada);
            this.donacionesRequeridas = donacionesRequeridas;
        }

        public int getDonacionesRequeridas() {
            return donacionesRequeridas;
        }

        @Override
        public boolean validarCumplimiento(ProgresoMision progresoMision) {
            ProgresoMision.ProgresoDonacionesExitosas progreso = (ProgresoMision.ProgresoDonacionesExitosas) progresoMision;
            return progreso.getDonacionesExitosasActuales() >= donacionesRequeridas;
        }

        @Override
        public ProgresoMision crearProgreso() {
            return new ProgresoMision.ProgresoDonacionesExitosas(this);
        }
    }
}
