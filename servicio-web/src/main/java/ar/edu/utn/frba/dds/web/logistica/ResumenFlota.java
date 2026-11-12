package ar.edu.utn.frba.dds.web.logistica;

/** Cifras de la flota. «En uso» son los camiones asignados a una ruta o en ruta. */
public record ResumenFlota(int total, int disponibles, int enUso, int fueraDeServicio) {
}
