package ar.edu.utn.frba.dds.donaciones.domain.algoritmos;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.Donacion;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import java.util.Comparator;
import java.util.List;
public class CompatibilidadSemantica implements AlgoritmoAsignacion {
    public List<EntidadBeneficiaria> ejecutarAlgoritmo(Donacion donacion, List<EntidadBeneficiaria> entidades) {
        if (donacion.estaVencida()) return List.of();
        return entidades.stream().filter(e -> puntaje(e, donacion) > 0)
                .sorted(Comparator.comparingInt(e -> -puntaje(e, donacion))).limit(10).toList();
    }
    private int puntaje(EntidadBeneficiaria entidad, Donacion donacion) {
        return (int) entidad.necesidadesPendientes().stream()
                .filter(n -> n.getSubcategoria() == donacion.getSubcategoria()
                        && n.getUnidadMedida() == donacion.getUnidadMedida()).count();
    }
}
