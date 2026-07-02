package ar.edu.utn.frba.dds.logistica.service;

import ar.edu.utn.frba.dds.logistica.dto.DonacionDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class DonacionClienteService {

    private final RestTemplate restTemplate;

    public DonacionClient() {
        this.restTemplate = new RestTemplate();
    }

    public List<DonacionDTO> obtenerDonaciones(int page) {
        String url = "http://donaciones-service/api/donaciones/pendientes?page="
                + page + "&size=100";

        ResponseEntity<DonacionDTO[]> response =
                restTemplate.getForEntity(url, DonacionDTO[].class);

        return Arrays.asList(response.getBody());
    }
}
