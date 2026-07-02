package ar.edu.utn.frba.dds.logistica.domain.planificacion;

import ar.edu.utn.frba.dds.logistica.domain.rutas.Entrega;
import ar.edu.utn.frba.dds.logistica.dto.DonacionDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class SolicitudPlanificacion {

    private EstadoPlanificacion estado;
    private List<Lote> donacionesAEntregar;

    // buffer interno de entregas antes de dividir en lotes
    private List<Entrega> entregas = new ArrayList<>();

    public SolicitudPlanificacion(EstadoPlanificacion estadoPlanificacion, ArrayList<Lote> donacionesAEntregar) {
    }


    public void recibirDonaciones(List<DonacionDTO> donacionesDTO) {

        for (DonacionDTO dto : donacionesDTO) {

            Entrega entrega = new Entrega(
                    null,
                    dto.getIdDonacion(),
                    LocalDate.now(),
                    dto.getPesoKG(),
                    dto.getVolumenM3(),
                    dto.getAlturaM()
            );

            entregas.add(entrega);
        }
    }

    public void dividirEnLotes() {

        List<Lote> lotes = new ArrayList<>();

        int tamanioMax = 100;
        int total = entregas.size();

        for (int i = 0; i < total; i += tamanioMax) {

            int fin = Math.min(i + tamanioMax, total);

            List<Entrega> subLista = entregas.subList(i, fin);

            Lote lote = new Lote(new ArrayList<>(subLista));

            lotes.add(lote);
        }

        this.donacionesAEntregar = lotes;
    }
}
