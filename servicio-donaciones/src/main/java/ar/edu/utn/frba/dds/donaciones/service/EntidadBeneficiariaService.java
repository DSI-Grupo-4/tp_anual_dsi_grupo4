package ar.edu.utn.frba.dds.donaciones.service;

import ar.edu.utn.frba.dds.donaciones.domain.donaciones.Donacion;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.EstadoTrack;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.GestorDonaciones;
import ar.edu.utn.frba.dds.donaciones.domain.lugares.Ciudad;
import ar.edu.utn.frba.dds.donaciones.domain.lugares.Direccion;
import ar.edu.utn.frba.dds.donaciones.domain.lugares.Provincia;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import ar.edu.utn.frba.dds.donaciones.domain.personas.PersonaHumana;
import ar.edu.utn.frba.dds.donaciones.domain.personas.PersonaJuridica;
import ar.edu.utn.frba.dds.donaciones.dto.CiudadDTO;
import ar.edu.utn.frba.dds.donaciones.dto.DireccionDTO;
import ar.edu.utn.frba.dds.donaciones.dto.EntidadBeneficiariaDTO;
import ar.edu.utn.frba.dds.donaciones.dto.PersonaJuridicaDTO;
import ar.edu.utn.frba.dds.donaciones.dto.ProvinciaDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
public class EntidadBeneficiariaService {
    // Estados en los que una donación asignada a esta entidad todavía está
    // "en curso" — bloquean el borrado para no dejar una referencia
    // colgante (confirmado en vivo: sin esto, /api/donaciones/pendientes
    // seguía ofreciendo entregas a una entidad ya eliminada).
    private static final Set<EstadoTrack> ESTADOS_ACTIVOS = Set.of(
            EstadoTrack.ASIGNACION_REALIZADA,
            EstadoTrack.LISTA_PARA_ENTREGAR,
            EstadoTrack.EN_TRASLADO);

    private List<EntidadBeneficiaria> entidades =
            new ArrayList<>();

    private Long siguienteId = 1L;

    private final GestorDonaciones gestorDonaciones;

    public EntidadBeneficiariaService(GestorDonaciones gestorDonaciones) {
        this.gestorDonaciones = gestorDonaciones;
    }

    public EntidadBeneficiariaDTO crear(EntidadBeneficiariaDTO dto) {
        PersonaJuridicaDTO personaJuridicaDTO = dto.getPersonaJuridica();

        PersonaJuridica personaJuridica =
                new PersonaJuridica(
                        personaJuridicaDTO.getRazonSocial(),
                        personaJuridicaDTO.getTipo(),
                        personaJuridicaDTO.getRubro(),
                        null
                );

        EntidadBeneficiaria entidad =
                new EntidadBeneficiaria(
                        siguienteId++,
                        personaJuridica,
                        dto.getDescripcion()
                );
        entidad.setDireccion(convertirDireccionDominio(dto.getDireccion()));
        entidades.add(entidad);

        return convertirADTO(entidad);
    }

    public List<EntidadBeneficiariaDTO> obtenerTodas() {
        return entidades.stream().map(this::convertirADTO).toList();
    }

    public EntidadBeneficiariaDTO obtenerPorId(Long id) {

        EntidadBeneficiaria entidad = entidades.stream()
                .filter(e -> e.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No existe la entidad beneficiaria " + id));

        return convertirADTO(entidad);
    }

    public EntidadBeneficiaria buscarEntidad(Long id) {

        return entidades.stream()
                .filter(e -> e.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No existe la entidad beneficiaria " + id));
    }

    public void eliminar(Long id) {

        long donacionesActivas = gestorDonaciones.getDonaciones().stream()
                .filter(d -> d.getEntidadBeneficiaria() != null
                        && d.getEntidadBeneficiaria().getId().equals(id)
                        && ESTADOS_ACTIVOS.contains(d.getEstadoActual()))
                .count();

        if (donacionesActivas > 0) {
            throw new IllegalStateException(
                    "No se puede eliminar la entidad " + id + ": tiene "
                            + donacionesActivas + " donación(es) activa(s) asignada(s)");
        }

        entidades.removeIf(
                e -> e.getId().equals(id)
        );
    }

    public EntidadBeneficiariaDTO convertirADTO(
            EntidadBeneficiaria entidad) {

        EntidadBeneficiariaDTO dto =
                new EntidadBeneficiariaDTO();

        PersonaJuridicaDTO personaDTO =
                new PersonaJuridicaDTO();

        personaDTO.setRazonSocial(
                entidad.getEntidad().getRazonSocial()
        );

        personaDTO.setTipo(
                entidad.getEntidad().getTipo()
        );

        personaDTO.setRubro(
                entidad.getEntidad().getRubro()
        );

        dto.setPersonaJuridica(personaDTO);
        dto.setDescripcion(entidad.getDescripcion());
        dto.setId(entidad.getId());
        dto.setDireccion(convertirDireccionADTO(entidad.getDireccion()));

        return dto;
    }

    private Direccion convertirDireccionDominio(DireccionDTO direccionDTO) {
        if (direccionDTO == null) {
            return null;
        }

        Ciudad ciudad = null;
        if (direccionDTO.getCiudad() != null) {
            Provincia provincia = null;
            if (direccionDTO.getCiudad().getProvincia() != null) {
                provincia = new Provincia(direccionDTO.getCiudad().getProvincia().getNombre());
            }
            ciudad = new Ciudad(direccionDTO.getCiudad().getNombre(), provincia);
        }

        return new Direccion(direccionDTO.getCalle(), direccionDTO.getNumero(), ciudad);
    }

    private DireccionDTO convertirDireccionADTO(Direccion direccion) {
        if (direccion == null) {
            return null;
        }

        DireccionDTO dto = new DireccionDTO();
        dto.setCalle(direccion.getCalle());
        dto.setNumero(direccion.getNumero());

        if (direccion.getCiudad() != null) {
            CiudadDTO ciudadDTO = new CiudadDTO();
            ciudadDTO.setNombre(direccion.getCiudad().getNombre());

            if (direccion.getCiudad().getProvincia() != null) {
                ProvinciaDTO provinciaDTO = new ProvinciaDTO();
                provinciaDTO.setNombre(direccion.getCiudad().getProvincia().getNombre());
                ciudadDTO.setProvincia(provinciaDTO);
            }

            dto.setCiudad(ciudadDTO);
        }

        return dto;
    }

    public EntidadBeneficiariaDTO actualizar(
            Long id,
            EntidadBeneficiariaDTO dto) {

        EntidadBeneficiaria entidad =
                buscarEntidad(id);

        entidad.setDescripcion(
                dto.getDescripcion()
        );

        PersonaJuridica persona =
                entidad.getEntidad();

        persona.setRazonSocial(
                dto.getPersonaJuridica().getRazonSocial()
        );

        persona.setTipo(
                dto.getPersonaJuridica().getTipo()
        );

        persona.setRubro(
                dto.getPersonaJuridica().getRubro()
        );

        entidad.setDireccion(convertirDireccionDominio(dto.getDireccion()));

        return convertirADTO(entidad);
    }

        public List<EntidadBeneficiaria> obtenerEntidadesDominio() {
           return entidades;
        }
}
