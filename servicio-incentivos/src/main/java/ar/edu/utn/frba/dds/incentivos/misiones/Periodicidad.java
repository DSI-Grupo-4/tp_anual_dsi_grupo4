package ar.edu.utn.frba.dds.incentivos.misiones;

public enum Periodicidad {
    MENSUAL(1),
    TRIMESTRAL(3),
    SEMESTRAL(6),
    ANUAL(12);

    private final int meses;

    Periodicidad(int meses) {
        this.meses = meses;
    }

    public int getMeses() {
        return meses;
    }
}
