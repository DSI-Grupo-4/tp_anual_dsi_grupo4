package ar.edu.utn.frba.dds.donaciones.domain.categorias;
import lombok.Getter;
import java.time.LocalDate;
@Getter
public final class Perecedero {
    private final LocalDate fechaVencimiento;
    public Perecedero(LocalDate fechaVencimiento) {
        if (fechaVencimiento == null) throw new IllegalArgumentException("fechaVencimiento es obligatoria");
        this.fechaVencimiento = fechaVencimiento;
    }
    public boolean estaVencido() { return fechaVencimiento.isBefore(LocalDate.now()); }
}
