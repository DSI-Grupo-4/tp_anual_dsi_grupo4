package ar.edu.utn.frba.dds.donaciones.domain.categorias;

import ar.edu.utn.frba.dds.donaciones.domain.donaciones.Fecha;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class Perecedero {
    private Fecha fechaVencimiento;

    public Fecha getFechaVencimiento() {
        return fechaVencimiento;
    }

    public boolean estaVencido() {
        if (fechaVencimiento == null) return false;
        LocalDate hoy = LocalDate.now();
        LocalDate vencimiento = LocalDate.of(
                fechaVencimiento.getAnio(),
                fechaVencimiento.getMes(),
                fechaVencimiento.getDia()
        );
        return hoy.isAfter(vencimiento);
    }
}
