package ar.edu.utn.frba.dds.incentivos.service;

import java.util.List;

import org.springframework.stereotype.Service;
import ar.edu.utn.frba.dds.incentivos.dto.MisionDTO;

@Service
public class MisionesService {

    public List<MisionDTO> obtenerMisiones(Long id) {
        return List.of(new MisionDTO());
    }
}
