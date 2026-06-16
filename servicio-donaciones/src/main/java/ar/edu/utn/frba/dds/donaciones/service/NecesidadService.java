package ar.edu.utn.frba.dds.donaciones.service;

import ar.edu.utn.frba.dds.donaciones.domain.categorias.Subcategoria;
import ar.edu.utn.frba.dds.donaciones.domain.necesidades.Necesidad;
import ar.edu.utn.frba.dds.donaciones.domain.necesidades.NecesidadExtraordinaria;
import ar.edu.utn.frba.dds.donaciones.domain.necesidades.NecesidadRecurrente;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import ar.edu.utn.frba.dds.donaciones.dto.NecesidadDTO;
import ar.edu.utn.frba.dds.donaciones.dto.NecesidadExtraordinariaDTO;
import ar.edu.utn.frba.dds.donaciones.dto.NecesidadRecurrenteDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NecesidadService {
    private List<Necesidad> necesidades = new ArrayList<>();
    private Long siguienteId = 1L;

    private final EntidadBeneficiariaService entidadBeneficiariaService;

    public NecesidadService(EntidadBeneficiariaService entidadBeneficiariaService) {
        this.entidadBeneficiariaService = entidadBeneficiariaService;
    }

    public NecesidadDTO crearRecurrente(NecesidadRecurrenteDTO dto) {
        NecesidadRecurrente necesidad = new NecesidadRecurrente(
                siguienteId++,
                dto.getDescripcion(),
                new Subcategoria(dto.getSubcategoria()),
                dto.getCantidadRequerida(),
                dto.getPeriodicidad()
        );

        asignarEntidadBeneficiaria(necesidad, dto.getEntidadBeneficiariaId());
        necesidades.add(necesidad);

        return convertirADTO(necesidad);
    }

    public NecesidadDTO crearExtraordinaria(NecesidadExtraordinariaDTO dto) {
        NecesidadExtraordinaria necesidad = new NecesidadExtraordinaria(
                siguienteId++,
                dto.getDescripcion(),
                new Subcategoria(dto.getSubcategoria()),
                dto.getCantidadRequerida(),
                dto.getTipoExtraordinario()
        );

        asignarEntidadBeneficiaria(necesidad, dto.getEntidadBeneficiariaId());
        necesidades.add(necesidad);

        return convertirADTO(necesidad);
    }

    public List<NecesidadDTO> obtenerTodas() {
        return necesidades.stream()
                .map(this::convertirADTO)
                .toList();
    }

    public NecesidadDTO obtenerPorId(Long id) {
        return convertirADTO(buscarDominioPorId(id));
    }

    public NecesidadDTO actualizarRecurrente(Long id, NecesidadRecurrenteDTO dto) {
        Necesidad necesidad = buscarDominioPorId(id);

        necesidad.setDescripcion(dto.getDescripcion());
        necesidad.setSubcategoria(new Subcategoria(dto.getSubcategoria()));
        necesidad.setCantidadRequerida(dto.getCantidadRequerida());

        if (necesidad instanceof NecesidadRecurrente recurrente) {
            recurrente.setPeriodicidad(dto.getPeriodicidad());
        }

        return convertirADTO(necesidad);
    }

    public NecesidadDTO actualizarExtraordinaria(Long id, NecesidadExtraordinariaDTO dto) {
        Necesidad necesidad = buscarDominioPorId(id);

        necesidad.setDescripcion(dto.getDescripcion());
        necesidad.setSubcategoria(new Subcategoria(dto.getSubcategoria()));
        necesidad.setCantidadRequerida(dto.getCantidadRequerida());

        if (necesidad instanceof NecesidadExtraordinaria extraordinaria) {
            extraordinaria.setTipoExtraordinario(dto.getTipoExtraordinario());
        }

        return convertirADTO(necesidad);
    }

    public void eliminar(Long id) {
        necesidades.removeIf(n -> n.getId().equals(id));
    }

    public Necesidad buscarDominioPorId(Long id) {
        return necesidades.stream()
                .filter(n -> n.getId().equals(id))
                .findFirst()
                .orElseThrow();
    }

    private void asignarEntidadBeneficiaria(Necesidad necesidad, Long entidadBeneficiariaId) {
        if (entidadBeneficiariaId != null) {
            EntidadBeneficiaria entidad =
                    entidadBeneficiariaService.buscarEntidad(entidadBeneficiariaId);

            entidad.agregarNecesidad(necesidad);
        }
    }

    private NecesidadDTO convertirADTO(Necesidad necesidad) {
        NecesidadDTO dto = new NecesidadDTO();

        dto.setId(necesidad.getId());
        dto.setDescripcion(necesidad.getDescripcion());
        dto.setSubcategoria(necesidad.getSubcategoria().getNombre());
        dto.setCantidadRequerida(necesidad.getCantidadRequerida());
        dto.setCantidadRecibida(necesidad.getCantidadRecibida());
        dto.setSatisfecha(necesidad.satisfecha());

        if (necesidad.getEntidadBeneficiaria() != null) {
            dto.setEntidadBeneficiariaId(
                    necesidad.getEntidadBeneficiaria().getId()
            );
        }

        if (necesidad instanceof NecesidadRecurrente recurrente) {
            dto.setTipo("RECURRENTE");
            dto.setPeriodicidad(recurrente.getPeriodicidad());
        }

        if (necesidad instanceof NecesidadExtraordinaria extraordinaria) {
            dto.setTipo("EXTRAORDINARIA");
            dto.setTipoExtraordinario(extraordinaria.getTipoExtraordinario());
        }

        return dto;
    }
}