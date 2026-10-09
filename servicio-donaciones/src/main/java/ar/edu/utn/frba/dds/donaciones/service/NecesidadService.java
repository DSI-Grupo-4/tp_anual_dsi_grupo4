package ar.edu.utn.frba.dds.donaciones.service;

import ar.edu.utn.frba.dds.donaciones.domain.necesidades.Necesidad;
import ar.edu.utn.frba.dds.donaciones.domain.necesidades.NecesidadExtraordinaria;
import ar.edu.utn.frba.dds.donaciones.domain.necesidades.NecesidadRecurrente;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import ar.edu.utn.frba.dds.donaciones.dto.NecesidadDTO;
import ar.edu.utn.frba.dds.donaciones.dto.NecesidadExtraordinariaDTO;
import ar.edu.utn.frba.dds.donaciones.dto.NecesidadRecurrenteDTO;
import ar.edu.utn.frba.dds.donaciones.repository.NecesidadRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class NecesidadService {
    private final NecesidadRepository necesidadRepository;
    private final EntidadBeneficiariaService entidadBeneficiariaService;

    public NecesidadService(NecesidadRepository necesidadRepository, EntidadBeneficiariaService entidadBeneficiariaService) {
        this.necesidadRepository = necesidadRepository;
        this.entidadBeneficiariaService = entidadBeneficiariaService;
    }

    public NecesidadDTO crearRecurrente(NecesidadRecurrenteDTO dto) {
        NecesidadRecurrente necesidad = new NecesidadRecurrente(
                null,
                dto.getDescripcion(),
                dto.getSubcategoria(),
                dto.getUnidadMedida(),
                dto.getCantidadRequerida(),
                dto.getPeriodicidad()
        );

        asignarEntidadBeneficiaria(necesidad, dto.getEntidadBeneficiariaId());
        necesidad = (NecesidadRecurrente) necesidadRepository.save(necesidad);

        return convertirADTO(necesidad);
    }

    public NecesidadDTO crearExtraordinaria(NecesidadExtraordinariaDTO dto) {
        NecesidadExtraordinaria necesidad = new NecesidadExtraordinaria(
                null,
                dto.getDescripcion(),
                dto.getSubcategoria(),
                dto.getUnidadMedida(),
                dto.getCantidadRequerida(),
                dto.getTipoExtraordinario()
        );

        asignarEntidadBeneficiaria(necesidad, dto.getEntidadBeneficiariaId());
        necesidad = (NecesidadExtraordinaria) necesidadRepository.save(necesidad);

        return convertirADTO(necesidad);
    }

    public List<NecesidadDTO> obtenerTodas() {
        return necesidadRepository.findAll().stream()
                .map(this::convertirADTO)
                .toList();
    }

    public NecesidadDTO obtenerPorId(Long id) {
        return convertirADTO(buscarDominioPorId(id));
    }

    public NecesidadDTO actualizarRecurrente(Long entidadId, Long id, NecesidadRecurrenteDTO dto) {
        Necesidad necesidad = buscarDominioPorId(entidadId, id);
        if (!(necesidad instanceof NecesidadRecurrente) || dto.getPeriodicidad() == null)
            throw new IllegalArgumentException("Debe editar una necesidad RECURRENTE con periodicidad");

        necesidad.actualizar(dto.getDescripcion(), dto.getSubcategoria(), dto.getUnidadMedida(), dto.getCantidadRequerida());

        if (necesidad instanceof NecesidadRecurrente recurrente) {
            recurrente.setPeriodicidad(dto.getPeriodicidad());
        }

        necesidad = necesidadRepository.save(necesidad);
        return convertirADTO(necesidad);
    }

    public NecesidadDTO actualizarExtraordinaria(Long entidadId, Long id, NecesidadExtraordinariaDTO dto) {
        Necesidad necesidad = buscarDominioPorId(entidadId, id);
        if (!(necesidad instanceof NecesidadExtraordinaria) || dto.getTipoExtraordinario() == null)
            throw new IllegalArgumentException("Debe editar una necesidad EXTRAORDINARIA con tipoExtraordinario");

        necesidad.actualizar(dto.getDescripcion(), dto.getSubcategoria(), dto.getUnidadMedida(), dto.getCantidadRequerida());

        if (necesidad instanceof NecesidadExtraordinaria extraordinaria) {
            extraordinaria.setTipoExtraordinario(dto.getTipoExtraordinario());
        }

        necesidad = necesidadRepository.save(necesidad);
        return convertirADTO(necesidad);
    }

    public List<NecesidadDTO> obtenerPorEntidad(Long entidadId) {
        return necesidadRepository.findByEntidadBeneficiaria_Id(entidadId).stream()
                .map(this::convertirADTO)
                .toList();
    }

    public void eliminar(Long entidadId, Long id) {
        // Valida pertenencia antes de borrar: sin esto, cualquier
        // entidadId en el path (incluso uno inexistente) podía borrar una
        // necesidad de otra entidad — bug crítico confirmado en vivo.
        buscarDominioPorId(entidadId, id);
        necesidadRepository.deleteById(id);
    }

    public Necesidad buscarDominioPorId(Long id) {
        return necesidadRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No existe la necesidad " + id));
    }

    /**
     * A diferencia de buscarDominioPorId(id), valida que la necesidad
     * encontrada pertenezca de verdad a entidadId — usado por los
     * endpoints anidados bajo /api/entidades/{entidadId}/necesidades/{id}
     * para que un entidadId ajeno (o inexistente) no pueda tocar una
     * necesidad de otra entidad.
     */
    public Necesidad buscarDominioPorId(Long entidadId, Long necesidadId) {
        Necesidad necesidad = buscarDominioPorId(necesidadId);

        if (necesidad.getEntidadBeneficiaria() == null
                || !necesidad.getEntidadBeneficiaria().getId().equals(entidadId)) {
            throw new NoSuchElementException(
                    "No existe la necesidad " + necesidadId + " para la entidad " + entidadId);
        }

        return necesidad;
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
        dto.setSubcategoria(necesidad.getSubcategoria());
        dto.setUnidadMedida(necesidad.getUnidadMedida());
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
