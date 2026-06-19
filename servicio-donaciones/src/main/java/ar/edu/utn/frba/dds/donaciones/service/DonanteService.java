package ar.edu.utn.frba.dds.donaciones.service;

import ar.edu.utn.frba.dds.donaciones.domain.personas.Donante;
import ar.edu.utn.frba.dds.donaciones.domain.personas.PersonaHumana;
import ar.edu.utn.frba.dds.donaciones.domain.personas.PersonaJuridica;
import ar.edu.utn.frba.dds.donaciones.dto.DonanteDTO;
import ar.edu.utn.frba.dds.donaciones.dto.PersonaHumanaDTO;
import ar.edu.utn.frba.dds.donaciones.dto.PersonaJuridicaDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DonanteService {
    private List<Donante> donantes = new ArrayList<>();

    private Long siguienteId = 1L; //por ahora asignamos ids, despues con la persistencia esto cambia

    public DonanteDTO convertirADTO(Donante donante) {

        DonanteDTO dto = new DonanteDTO();

        if(donante.getPersona() instanceof PersonaHumana humana) {

            dto.setTipo("HUMANA");
            dto.setNombre(humana.getNombre());
            dto.setApellido(humana.getApellido());
            dto.setDocumento(humana.getDocumento());
        }

        if(donante.getPersona() instanceof PersonaJuridica juridica) {

            dto.setTipo("JURIDICA");
            dto.setRazonSocial(juridica.getRazonSocial());
        }

        dto.setId(donante.getId());
        return dto;
    }

    public DonanteDTO crearDonanteHumano(PersonaHumanaDTO dto) {
       PersonaHumana persona =
        new PersonaHumana(
                dto.getNombre(),
                dto.getApellido(),
                dto.getEdad(),
                dto.getDocumento(),
                dto.getGenero()
        );
        Donante donante = new Donante(siguienteId++, persona);
        donantes.add(donante);
        return convertirADTO(donante);
    }

    public List<DonanteDTO> obtenerTodos() {
        return donantes.stream()
                .map(this::convertirADTO)
                .toList();
    }

    public DonanteDTO crearDonanteJuridico(
            PersonaJuridicaDTO dto) {
        PersonaJuridica persona =
                new PersonaJuridica(
                        dto.getRazonSocial(),
                        dto.getTipo(),
                        dto.getRubro(),
                        null
                );

        Donante donante = new Donante(siguienteId++, persona);

        donantes.add(donante);
        return convertirADTO(donante);
    }

    public DonanteDTO buscarPorId(Long id) {
        Donante donante = donantes.stream()
                .filter(d -> d.getId().equals(id))
                .findFirst()
                .orElseThrow();
        return convertirADTO(donante);
    }

    public void eliminar(Long id) {

        donantes.removeIf(
                d -> d.getId().equals(id)
        );
    }

    private Donante buscarPorIdDominio(Long id) {

        return donantes.stream()
                .filter(d -> d.getId().equals(id))
                .findFirst()
                .orElseThrow();
    }

    public DonanteDTO actualizarHumano(
            Long id,
            PersonaHumanaDTO dto) {

        Donante donante = buscarPorIdDominio(id);

        PersonaHumana persona =
                (PersonaHumana) donante.getPersona();

        persona.setNombre(
                dto.getNombre()
        );

        persona.setApellido(
                dto.getApellido()
        );

        persona.setEdad(
                dto.getEdad()
        );

        persona.setDocumento(
                dto.getDocumento()
        );

        persona.setGenero(
                dto.getGenero()
        );

        return convertirADTO(donante);
    }

    public DonanteDTO actualizarJuridico(
            Long id,
            PersonaJuridicaDTO dto) {

        Donante donante = buscarPorIdDominio(id);

        PersonaJuridica persona =
                (PersonaJuridica) donante.getPersona();

        persona.setRazonSocial(
                dto.getRazonSocial()
        );

        persona.setTipo(
                dto.getTipo()
        );

        persona.setRubro(
                dto.getRubro()
        );

        return convertirADTO(donante);
    }
}