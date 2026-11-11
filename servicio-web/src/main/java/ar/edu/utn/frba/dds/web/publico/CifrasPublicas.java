package ar.edu.utn.frba.dds.web.publico;

/**
 * Cifras de la landing. Cada una es null si el servicio que la provee no respondió,
 * para que la vista muestre "—" en esa tarjeta sin perder el resto.
 */
public record CifrasPublicas(
        Integer entregadasUltimoMes,
        Integer enCamino,
        Integer entidadesActivas,
        Integer donantesActivos) {
}
