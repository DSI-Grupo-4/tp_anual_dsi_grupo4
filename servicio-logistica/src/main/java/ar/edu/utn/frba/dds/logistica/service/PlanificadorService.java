package ar.edu.utn.frba.dds.logistica.service;

import ar.edu.utn.frba.dds.logistica.domain.planificacion.EstadoPlanificacion;
import ar.edu.utn.frba.dds.logistica.domain.planificacion.Lote;
import ar.edu.utn.frba.dds.logistica.domain.planificacion.SolicitudPlanificacion;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Camion;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Entrega;
import ar.edu.utn.frba.dds.logistica.dto.CamionDTO;
import ar.edu.utn.frba.dds.logistica.dto.DonacionDTO;
import ar.edu.utn.frba.dds.logistica.dto.ResultadoPlanificacionDTO;
import ar.edu.utn.frba.dds.logistica.dto.RutaDTO;
import ar.edu.utn.frba.dds.logistica.service.CamionService;
import ar.edu.utn.frba.dds.logistica.service.DonacionClienteService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service PlanificadorService {

    private final DonacionClienteService donacionClient;
    private final CamionService camionService;

    // 🔥 almacenamiento en memoria para poder resolver callback
    private final List<Entrega> entregas = new ArrayList<>();
    private Integer contadorId = 1;

    public PlanificadorService(DonacionClienteService donacionClient,
                               CamionService camionService) {
        this.donacionClient = donacionClient;
        this.camionService = camionService;
    }

    public List<Entrega> procesarDonaciones() {
        int page = 0;
        List<DonacionDTO> loteDTO;
        List<Entrega> resultado = new ArrayList<>();

        SolicitudPlanificacion solicitud = new SolicitudPlanificacion(
                EstadoPlanificacion.SOLICITADA,
                new ArrayList<>()
        );

        try {
            do {
                loteDTO = DonacionClienteService.obtenerDonaciones(page);

                if (!loteDTO.isEmpty()) {
                    solicitud.recibirDonaciones(loteDTO);
                }

                page++;
            } while (!loteDTO.isEmpty());

            solicitud.dividirEnLotes();

            // 🔥 obtener entregas desde los lotes
            List<Entrega> entregasGeneradas = new ArrayList<>();

            for (Lote lote : solicitud.getDonacionesAEntregar()) {
                for (Entrega e : lote.getEntregas()) {
                    // asignar ID
                    e.setIdEntrega(contadorId++);
                    entregasGeneradas.add(e);
                }
            }

            // guardar para callback
            this.entregas.addAll(entregasGeneradas);

            solicitud.setEstado(EstadoPlanificacion.GENERADA);

            return entregasGeneradas;

        } catch (Exception e) {
            solicitud.setEstado(EstadoPlanificacion.FALLIDA);
            throw new RuntimeException("Error en planificación", e);
        }
    }

    //callback: procesar resultado del "planificador externo"
    public void procesarResultado(ResultadoPlanificacionDTO resultado) {

        for (RutaDTO ruta : resultado.getRutas()) {

            for (Integer entregaId : ruta.getEntregasIds()) {

                Entrega entrega = buscarEntrega(entregaId);

                entrega.iniciarTranslado();
            }
        }
    }

    private Entrega buscarEntrega(Integer id) {
        return entregas.stream()
                .filter(e -> e.getIdEntrega().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Entrega no encontrada"));
    }

    private Camion convertirCamion(CamionDTO dto) {
        Camion camion = new Camion();
        camion.setIdCamion(dto.getIdCamion());
        camion.setPatente(dto.getPatente());
        camion.setEstado(dto.getEstadoCamion());
        return camion;
    }
}

