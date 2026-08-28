package ar.edu.utn.frba.dds.logistica.service;

import ar.edu.utn.frba.dds.logistica.domain.rutas.Camion;
import ar.edu.utn.frba.dds.logistica.domain.rutas.EstadoCamion;
import ar.edu.utn.frba.dds.logistica.dto.CamionDTO;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CamionService {

    // TODO Entrega 4: reemplazar por repositorio con persistencia real
    private final List<Camion> camiones = new ArrayList<>();

    @PostConstruct
    public void seed() {
        camiones.add(new Camion(1, "AA123BB", 20, 3, 1500, EstadoCamion.DISPONIBLE));
        camiones.add(new Camion(2, "AC456DD", 15, 3, 1200, EstadoCamion.DISPONIBLE));
    }

    public List<CamionDTO> obtenerCamiones() {
        return camiones.stream().map(this::toDTO).toList();
    }

    public List<CamionDTO> obtenerCamionesDisponibles() {
        return camiones.stream()
                .filter(c -> c.getEstadoCamion() == EstadoCamion.DISPONIBLE)
                .map(this::toDTO)
                .toList();
    }

    // usado internamente por el planificador, que necesita las entidades de dominio, no DTOs
    public List<Camion> obtenerCamionesDisponiblesEntidad() {
        return camiones.stream()
                .filter(c -> c.getEstadoCamion() == EstadoCamion.DISPONIBLE)
                .toList();
    }

    private CamionDTO toDTO(Camion camion) {
        return new CamionDTO(camion.getIdCamion(), camion.getPatente(), camion.getEstadoCamion());
    }
}