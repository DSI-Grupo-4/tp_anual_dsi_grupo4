package ar.edu.utn.frba.dds.donaciones.domain.categorias;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
@Getter
@NoArgsConstructor(force = true)
@Embeddable
public final class Perecedero {
    @Column(name = "fecha_vencimiento")
    private final LocalDate fechaVencimiento;
    public Perecedero(LocalDate fechaVencimiento) {
        if (fechaVencimiento == null) throw new IllegalArgumentException("fechaVencimiento es obligatoria");
        this.fechaVencimiento = fechaVencimiento;
    }
    public boolean estaVencido() { return fechaVencimiento.isBefore(LocalDate.now()); }
}
