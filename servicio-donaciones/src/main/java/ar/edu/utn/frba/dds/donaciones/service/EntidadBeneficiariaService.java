package ar.edu.utn.frba.dds.donaciones.service;

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

@Service
public class EntidadBeneficiariaService {
    private List<EntidadBeneficiaria> entidades =
            new ArrayList<>();

    private Long siguienteId = 1L;

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
                .orElseThrow();

        return convertirADTO(entidad);
    }

    public EntidadBeneficiaria buscarEntidad(Long id) {

        return entidades.stream()
                .filter(e -> e.getId().equals(id))
                .findFirst()
                .orElseThrow();
    }

    public void eliminar(Long id) {

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
