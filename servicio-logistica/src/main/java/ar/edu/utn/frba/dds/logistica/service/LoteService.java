package ar.edu.utn.frba.dds.logistica.service;

import org.springframework.transaction.annotation.Transactional;

import ar.edu.utn.frba.dds.logistica.domain.rutas.Entrega;
import ar.edu.utn.frba.dds.logistica.domain.rutas.EstadoEntrega;
import ar.edu.utn.frba.dds.logistica.dto.DonacionDTO;
import ar.edu.utn.frba.dds.logistica.repository.EntregaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class LoteService {

    private static final Logger logger = LoggerFactory.getLogger(LoteService.class);

    // Una donación reenviada solo puede actualizar su Entrega mientras esa
    // Entrega todavía no está comprometida con una ruta real -- después de
    // ASIGNADA_A_RUTA, pisar sus datos (entidad/dirección) rompería una
    // planificación o un traslado ya en curso.
    private static final Set<EstadoEntrega> ACTUALIZABLES = Set.of(EstadoEntrega.PENDIENTE, EstadoEntrega.REPLANIFICABLE);

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
                .map(this::registrarOActualizar)
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    /**
     * Idempotencia del lote: si Donaciones reenvía la misma donación (ej.
     * porque se reasignó a otra entidad tras una entrega fallida), no se
     * crea una segunda Entrega -- se actualizan los datos de la que ya
     * existe, siempre que todavía no esté comprometida con una ruta.
     * Si ya está en curso o entregada, se ignora el reenvío entero (no
     * tiene sentido pisarla con datos potencialmente viejos).
     */
    private Entrega registrarOActualizar(DonacionDTO dto) {
        Optional<Entrega> existente = entregaRepository.buscarPorDonacion(dto.getIdDonacion());
        if (existente.isEmpty()) {
            return entregaRepository.guardar(new Entrega(
                    null,
                    dto.getIdDonacion(),
                    dto.getEntidadBeneficiariaAsociadaID(),
                    dto.getDireccionDestino(),
                    LocalDate.now(),
                    dto.getPesoKG(),
                    dto.getVolumenM3(),
                    dto.getAlturaM()
            ));
        }

        Entrega entrega = existente.get();
        if (!ACTUALIZABLES.contains(entrega.getEstadoEntrega())) {
            logger.warn("Se ignoró la donación {} del lote: ya tiene una entrega en curso (estado {}).",
                    dto.getIdDonacion(), entrega.getEstadoEntrega());
            return null;
        }

        logger.info("La donación {} ya tenía una entrega ({}); se actualizan sus datos.",
                dto.getIdDonacion(), entrega.getEstadoEntrega());
        entrega.setIdEntidadBeneficiariaAsociada(dto.getEntidadBeneficiariaAsociadaID());
        entrega.setDireccionDestino(dto.getDireccionDestino());
        entrega.setPesoKG(dto.getPesoKG());
        entrega.setVolumenM3(dto.getVolumenM3());
        entrega.setAlturaM(dto.getAlturaM());
        return entrega;
    }
}
