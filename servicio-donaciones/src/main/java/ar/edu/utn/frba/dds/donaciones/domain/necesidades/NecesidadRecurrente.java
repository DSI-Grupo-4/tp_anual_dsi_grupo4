package ar.edu.utn.frba.dds.donaciones.domain.necesidades;

import ar.edu.utn.frba.dds.donaciones.domain.categorias.Subcategoria;
import lombok.Getter;
import java.math.BigDecimal;
import ar.edu.utn.frba.dds.donaciones.domain.categorias.UnidadMedida;
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
            UnidadMedida unidadMedida,
            BigDecimal cantidadRequerida,
            Periodicidad periodicidad) {

        super(id, descripcion, subcategoria, unidadMedida, cantidadRequerida);
        setPeriodicidad(periodicidad);
        this.inicioPeriodoActual = LocalDate.now();
    }

    public void setPeriodicidad(Periodicidad periodicidad) {
        if (periodicidad == null) throw new IllegalArgumentException("periodicidad es obligatoria");
        this.periodicidad = periodicidad;
    }
    @Override
    public BigDecimal getCantidadRecibida() {
        avanzarPeriodoSiCorresponde();
        return super.getCantidadRecibida();
    }
    @Override
    public void recibir(BigDecimal cantidad) {
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
            cantidadRecibida = BigDecimal.ZERO;
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
