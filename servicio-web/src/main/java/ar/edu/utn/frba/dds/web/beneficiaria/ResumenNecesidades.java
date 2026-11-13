package ar.edu.utn.frba.dds.web.beneficiaria;

/** Cifras de las necesidades de una entidad. Urgentes son las extraordinarias que todavía no se cubrieron. */
public record ResumenNecesidades(int total, int activas, int satisfechas, int urgentes) {
}
