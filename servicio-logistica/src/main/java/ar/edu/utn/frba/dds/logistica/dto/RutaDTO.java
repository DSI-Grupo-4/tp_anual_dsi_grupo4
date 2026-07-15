package ar.edu.utn.frba.dds.logistica.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RutaDTO {
    private Integer camionId;
    private List<Integer> entregasIds;
}
