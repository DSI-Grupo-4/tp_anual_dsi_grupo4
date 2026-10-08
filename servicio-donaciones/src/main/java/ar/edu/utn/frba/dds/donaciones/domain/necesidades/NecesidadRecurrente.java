package ar.edu.utn.frba.dds.donaciones.domain.necesidades;

import ar.edu.utn.frba.dds.donaciones.domain.categorias.Subcategoria;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class NecesidadRecurrente extends Necesidad {
    private Periodicidad periodicidad;
    // Arranque del período que se está evaluando actualmente -- al vencer,
    // la cantidad recibida se reinicia para el período siguiente. Sin esto,
    // una necesidad recurrente quedaba satisfecha para siempre la primera
    // vez que se cubría, contradiciendo que se evalúa "dentro de cada período".
    private LocalDate inicioPeriodoActual;

    public NecesidadRecurrente(
            Long id,
            String descripcion,
            Subcategoria subcategoria,
            Integer cantidadRequerida,
            Periodicidad periodicidad) {

        super(id, descripcion, subcategoria, cantidadRequerida);
        this.periodicidad = periodicidad;
        this.inicioPeriodoActual = LocalDate.now();
    }

    @Override
    public void recibir(Integer cantidad) {
        avanzarPeriodoSiCorresponde();
        super.recibir(cantidad);
    }

    @Override
    public boolean satisfecha() {
        avanzarPeriodoSiCorresponde();
        return super.satisfecha();
    }

    private void avanzarPeriodoSiCorresponde() {
        LocalDate hoy = LocalDate.now();
        while (!hoy.isBefore(finDePeriodoDesde(inicioPeriodoActual))) {
            inicioPeriodoActual = finDePeriodoDesde(inicioPeriodoActual);
            setCantidadRecibida(0);
        }
    }

    private LocalDate finDePeriodoDesde(LocalDate inicio) {
        return switch (periodicidad) {
            case DIARIA -> inicio.plusDays(1);
            case SEMANAL -> inicio.plusWeeks(1);
            case MENSUAL -> inicio.plusMonths(1);
            case ANUAL -> inicio.plusYears(1);
        };
    }
}
