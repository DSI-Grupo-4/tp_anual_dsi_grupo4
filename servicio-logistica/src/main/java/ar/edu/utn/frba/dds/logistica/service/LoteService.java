package ar.edu.utn.frba.dds.logistica.service;

import ar.edu.utn.frba.dds.logistica.domain.rutas.Entrega;
import ar.edu.utn.frba.dds.logistica.dto.DonacionDTO;
import ar.edu.utn.frba.dds.logistica.repository.EntregaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class LoteService {

    private static final Logger logger = LoggerFactory.getLogger(LoteService.class);

    private final EntregaRepository entregaRepository;

    public LoteService(EntregaRepository entregaRepository) {
        this.entregaRepository = entregaRepository;
    }

    // Donaciones hace POST de hasta 100 donaciones "Asignación Realizada" por vez (restricción del enunciado)
    public List<Entrega> recibirLote(List<DonacionDTO> donaciones) {
        if (donaciones.size() > 100) {
            throw new IllegalArgumentException("El lote no puede superar las 100 donaciones");
        }
        return donaciones.stream()
                .filter(this::esNueva)
                .map(dto -> entregaRepository.guardar(new Entrega(
                        null,
                        dto.getIdDonacion(),
                        dto.getEntidadBeneficiariaAsociadaID(),
                        dto.getDireccionDestino(),
                        LocalDate.now(),
                        dto.getPesoKG(),
                        dto.getVolumenM3(),
                        dto.getAlturaM()
                )))
                .toList();
    }

    // Idempotencia del lote: si Donaciones reenvía la misma donación (ej.
    // porque de su lado todavía figuraba pendiente), no se crea una segunda
    // Entrega -- antes una donación ya entregada podía terminar duplicada
    // en PENDIENTE y volver a planificarse para una nueva ruta.
    private boolean esNueva(DonacionDTO dto) {
        boolean yaExiste = entregaRepository.buscarPorDonacion(dto.getIdDonacion()).isPresent();
        if (yaExiste) {
            logger.warn("Se ignoró la donación {} del lote: ya existe una entrega registrada para ella.",
                    dto.getIdDonacion());
        }
        return !yaExiste;
    }
}
