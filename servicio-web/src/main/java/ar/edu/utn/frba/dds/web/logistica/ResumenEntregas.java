package ar.edu.utn.frba.dds.web.logistica;

/**
 * Cifras de las entregas. «Sin salir» son las pendientes de planificar o a replanificar; «en camino», las asignadas a una
 * ruta o en traslado; «requieren atención», las que no se pudieron concretar y esperan que se las reingrese.
 */
public record ResumenEntregas(int sinSalir, int enCamino, int entregadas, int requierenAtencion) {
}
