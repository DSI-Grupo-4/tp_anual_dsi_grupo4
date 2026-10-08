package ar.edu.utn.frba.dds.logistica.service;

import ar.edu.utn.frba.dds.logistica.domain.rutas.Chofer;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChoferService {

    // TODO: reemplazar por repositorio con persistencia real
    private final List<Chofer> choferes = new ArrayList<>();

    @PostConstruct
    public void seed() {
        choferes.add(new Chofer(1, "Juan Perez", 30123456, true));
        choferes.add(new Chofer(2, "Marcos Gomez", 28654321, true));
    }

    public List<Chofer> obtenerChoferesHabilitados() {
        return choferes.stream().filter(Chofer::estaHabilitado).toList();
    }
}
