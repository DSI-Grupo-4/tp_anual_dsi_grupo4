package ar.edu.utn.frba.dds.web.beneficiaria;

import ar.edu.utn.frba.dds.web.client.donaciones.DonacionApi;
import ar.edu.utn.frba.dds.web.client.donaciones.DonacionesClient;
import ar.edu.utn.frba.dds.web.client.donaciones.NecesidadApi;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * Datos y cifras del portal de la entidad beneficiaria. Solo devuelve recursos de la entidad pedida.
 */
@Service
public class PortalBeneficiariaService {

    /** Estados en los que la donación ya está asignada a la entidad pero todavía no la recibió. */
    static final Set<String> EN_PROCESO = Set.of("ASIGNACION_REALIZADA", "LISTA_PARA_ENTREGAR", "EN_TRASLADO");
    static final String ENTREGADA = "ENTREGADA";

    private final DonacionesClient donaciones;

    public PortalBeneficiariaService(DonacionesClient donaciones) {
        this.donaciones = donaciones;
    }

    /**
     * Las donaciones asignadas a la entidad, de la más nueva a la más vieja.
     * Donaciones no filtra por entidad todavía, así que se filtra acá (pendiente: filtro en la API).
     */
    public List<DonacionApi> donacionesDe(Long entidadId) {
        return donaciones.donaciones().stream()
                .filter(d -> entidadId.equals(d.entidadBeneficiariaId()))
                .sorted(Comparator.comparing(DonacionApi::id, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    public List<DonacionApi> pendientes(List<DonacionApi> propias) {
        return propias.stream().filter(d -> EN_PROCESO.contains(d.estadoActual())).toList();
    }

    public List<DonacionApi> recibidas(List<DonacionApi> propias) {
        return propias.stream().filter(d -> ENTREGADA.equals(d.estadoActual())).toList();
    }

    public ResumenDonaciones resumenDonaciones(List<DonacionApi> propias) {
        return new ResumenDonaciones(propias.size(), pendientes(propias).size(), recibidas(propias).size());
    }

    public ResumenNecesidades resumenNecesidades(List<NecesidadApi> necesidades) {
        int satisfechas = (int) necesidades.stream().filter(NecesidadApi::satisfechaOk).count();
        int urgentes = (int) necesidades.stream().filter(NecesidadApi::urgente).count();
        return new ResumenNecesidades(necesidades.size(), necesidades.size() - satisfechas, satisfechas, urgentes);
    }
}
