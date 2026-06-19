package ar.edu.utn.frba.dds.incentivos.service;

import java.util.List;

import org.springframework.stereotype.Service;
import ar.edu.utn.frba.dds.incentivos.dto.InsigniaDTO;

@Service
public class InsigniasService {

    public List<InsigniaDTO> obtenerInsignias(Long id) {
        return List.of(new InsigniaDTO());
    }
}