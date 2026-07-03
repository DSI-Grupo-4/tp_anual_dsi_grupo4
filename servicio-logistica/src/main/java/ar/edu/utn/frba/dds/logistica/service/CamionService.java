package ar.edu.utn.frba.dds.logistica.service;

import ar.edu.utn.frba.dds.logistica.domain.rutas.Camion;
import ar.edu.utn.frba.dds.logistica.domain.rutas.EstadoCamion;
import ar.edu.utn.frba.dds.logistica.dto.CamionDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CamionService {

    private final List<Camion> camiones;

    // Esto después lo vas a reemplazar por repo
    public CamionService() {
        this.camiones = List.of(); // mock o inyección real más adelante
    }

    public List<CamionDTO> obtenerCamiones() {
        return camiones.stream()
                .map(this::toDTO)
                .toList();
    }

    public List<CamionDTO> obtenerCamionesDisponibles() {
        return camiones.stream()
                .filter(c -> c.getEstado() == EstadoCamion.DISPONIBLE)
                .map(this::toDTO)
                .toList();
    }

    private CamionDTO toDTO(Camion camion) {
        return new CamionDTO(
                camion.getIdCamion(),
                camion.getPatente(),
                camion.getEstado()
        );
    }
}
