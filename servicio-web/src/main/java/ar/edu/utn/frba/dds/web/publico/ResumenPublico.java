package ar.edu.utn.frba.dds.web.publico;

import java.util.List;

/**
 * Datos de la landing.
 *
 * @param destacadas         donaciones entregadas en los últimos 30 días, primero las que tienen foto
 * @param entregasDisponibles false si Logística no respondió (la vista lo avisa en vez de mostrar "no hay entregas")
 */
public record ResumenPublico(CifrasPublicas cifras, List<DonacionPublica> destacadas, boolean entregasDisponibles) {
}
