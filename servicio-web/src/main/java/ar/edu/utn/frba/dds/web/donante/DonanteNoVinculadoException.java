package ar.edu.utn.frba.dds.web.donante;

/**
 * El usuario entró al portal del donante, pero Donaciones no tiene un Donante con su ID.
 * Pasa con un usuario autenticado que todavía no se vinculó a un Donante, o, sin
 * autenticación, si DONANTE_PROVISORIO_ID apunta a un donante que no existe.
 */
public class DonanteNoVinculadoException extends RuntimeException {

    private final long donanteId;

    public DonanteNoVinculadoException(long donanteId, Throwable causa) {
        super("No existe el donante " + donanteId, causa);
        this.donanteId = donanteId;
    }

    public long getDonanteId() {
        return donanteId;
    }
}
