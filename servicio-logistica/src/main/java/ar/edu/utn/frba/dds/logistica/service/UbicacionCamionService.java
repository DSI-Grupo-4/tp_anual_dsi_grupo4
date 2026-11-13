package ar.edu.utn.frba.dds.logistica.service;

import ar.edu.utn.frba.dds.logistica.domain.rutas.Camion;
import ar.edu.utn.frba.dds.logistica.dto.UbicacionCamionDTO;
import ar.edu.utn.frba.dds.logistica.dto.UbicacionRequestDTO;
import ar.edu.utn.frba.dds.logistica.repository.CamionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Ubicación de los camiones. La informa el chofer (o el simulador) y se deja disponible para consulta:
 * Logística no llama a ningún otro servicio, quien la necesita (el cliente web) la pide.
 */
@Service
public class UbicacionCamionService {

    private final CamionRepository camionRepository;

    public UbicacionCamionService(CamionRepository camionRepository) {
        this.camionRepository = camionRepository;
    }

    /** Registra la ubicación informada por el chofer; la fecha y hora las pone el servidor (UTC). */
    public UbicacionCamionDTO reportar(Integer idCamion, UbicacionRequestDTO pedido) {
        Camion camion = buscar(idCamion);
        camion.reportarUbicacion(pedido.getLatitud(), pedido.getLongitud(), LocalDateTime.now(ZoneOffset.UTC), false);
        return UbicacionCamionDTO.de(camionRepository.save(camion)); // Mongo no hace dirty checking: se guarda explícitamente
    }

    public UbicacionCamionDTO ubicacion(Integer idCamion) {
        Camion camion = buscar(idCamion);
        if (!camion.tieneUbicacion()) {
            throw new NoSuchElementException("El camión " + idCamion + " todavía no informó su ubicación");
        }
        return UbicacionCamionDTO.de(camion);
    }

    /** Los camiones que ya informaron una ubicación, ordenados por id. */
    public List<UbicacionCamionDTO> ubicaciones() {
        return camionRepository.findAll().stream()
                .filter(Camion::tieneUbicacion)
                .sorted(Comparator.comparing(Camion::getIdCamion, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(UbicacionCamionDTO::de)
                .toList();
    }

    private Camion buscar(Integer idCamion) {
        return camionRepository.findById(idCamion)
                .orElseThrow(() -> new NoSuchElementException("No existe el camión " + idCamion));
    }
}
