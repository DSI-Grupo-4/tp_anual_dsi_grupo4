package ar.edu.utn.frba.dds.logistica.service;

import ar.edu.utn.frba.dds.logistica.dto.CamionDTO;
import ar.edu.utn.frba.dds.logistica.dto.DonacionDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlanificadorService {

    private final DonacionClienteService donacionClient;
    private final CamionService camionService;

    public PlanificadorService(DonacionClienteService donacionClient,
                               CamionService camionService) {
        this.donacionClient = donacionClient;
        this.camionService = camionService;
    }

    public void procesarDonaciones() {
        int page = 0;
        List<DonacionDTO> lote;

        do {
            lote = donacionClient.obtenerDonaciones(page);

            if (!lote.isEmpty()) {
                procesarLote(lote);
            }

            page++;
        } while (!lote.isEmpty());
    }

    private void procesarLote(List<DonacionDTO> donaciones) {

        List<CamionDTO> camionesDisponibles =
                camionService.obtenerCamionesDisponibles();

        if (camionesDisponibles.isEmpty()) {
            throw new RuntimeException("No hay camiones disponibles");
        }
        int i = 0;

        for (DonacionDTO donacion : donaciones) {
            CamionDTO camion = camionesDisponibles.get(i % camionesDisponibles.size());

            asignarDonacion(camion, donacion);

            i++;
        }
    }

    private void asignarDonacion(CamionDTO camion, DonacionDTO donacion) {
        //logica para asignar donacion
    }
}
