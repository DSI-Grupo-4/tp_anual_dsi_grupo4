package ar.edu.utn.frba.dds.logistica.service;

import ar.edu.utn.frba.dds.logistica.domain.rutas.Entrega;
import ar.edu.utn.frba.dds.logistica.dto.DonacionDTO;
import ar.edu.utn.frba.dds.logistica.repository.EntregaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class LoteService {

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
}