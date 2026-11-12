package ar.edu.utn.frba.dds.web.logistica;

/** Cifras del seguimiento: rutas por estado y entregas que todavía no tienen resultado. */
public record ResumenSeguimiento(int planificadas, int iniciadas, int finalizadas, int entregasPendientes) {
}
