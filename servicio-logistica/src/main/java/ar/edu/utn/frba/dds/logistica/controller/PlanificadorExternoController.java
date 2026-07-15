package ar.edu.utn.frba.dds.logistica.controller;

import ar.edu.utn.frba.dds.logistica.domain.rutas.Entrega;
import ar.edu.utn.frba.dds.logistica.dto.ResultadoPlanificacionDTO;
import ar.edu.utn.frba.dds.logistica.dto.RutaDTO;
import ar.edu.utn.frba.dds.logistica.dto.SolicitudPlanificadorDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/planificador-externo")
public class PlanificadorExternoController {

    private final RestTemplate restTemplate = new RestTemplate();

    private ResultadoPlanificacionDTO generarResultado(SolicitudPlanificadorDTO request) {

        ResultadoPlanificacionDTO resultado = new ResultadoPlanificacionDTO();

        List<RutaDTO> rutas = new ArrayList<>();

        // 👉 estrategia simple: una sola ruta con todas las entregas
        RutaDTO ruta = new RutaDTO();

        List<Integer> idsEntregas = new ArrayList<>();

        for (Entrega entrega : request.getEntregas()) {
            idsEntregas.add(entrega.getIdEntrega());
        }

        Integer i = 1; //de ejemplo
        ruta.setCamionId(i); // simulado
        ruta.setEntregasIds(idsEntregas);

        rutas.add(ruta);

        resultado.setRutas(rutas);

        return resultado;
    }


    @PostMapping
    public void planificar(@RequestBody SolicitudPlanificadorDTO request) {

        // simulamos resultado
        ResultadoPlanificacionDTO resultado = generarResultado(request);

        // CALLBACK
        restTemplate.postForObject(
                request.getCallbackUrl(),
                resultado,
                Void.class
        );
    }
}
