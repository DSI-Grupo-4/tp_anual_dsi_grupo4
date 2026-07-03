package ar.edu.utn.frba.dds.donaciones.service;

import ar.edu.utn.frba.dds.donaciones.domain.personas.Donante;
import ar.edu.utn.frba.dds.donaciones.domain.personas.ImportadorCSV;
import ar.edu.utn.frba.dds.donaciones.domain.personas.MedioContacto;
import ar.edu.utn.frba.dds.donaciones.domain.personas.Persona;
import ar.edu.utn.frba.dds.donaciones.domain.personas.PersonaHumana;
import ar.edu.utn.frba.dds.donaciones.domain.personas.PersonaJuridica;
import ar.edu.utn.frba.dds.donaciones.domain.personas.TipoContacto;
import ar.edu.utn.frba.dds.donaciones.dto.DonanteDTO;
import ar.edu.utn.frba.dds.donaciones.dto.PersonaHumanaDTO;
import ar.edu.utn.frba.dds.donaciones.dto.PersonaJuridicaDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
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
        cargarMediosContacto(persona, dto.getEmail(), dto.getTelefono(), dto.getWhatsapp());
        Donante donante = new Donante(siguienteId++, persona);
        donantes.add(donante);
        return convertirADTO(donante);
    }

    /**
     * Carga los medios de contacto de una persona a partir de los datos del DTO.
     * El email es obligatorio (Entrega 1) y queda como medio preferido salvo que
     * se indique explícitamente otro; teléfono/whatsapp son opcionales.
     * Sin esto, el donante quedaba sin ningún MedioContacto y era imposible
     * resolver a quién notificar (Entrega 2).
     */
    private void cargarMediosContacto(Persona persona, String email, String telefono, String whatsapp) {
        if (email != null && !email.isBlank()) {
            persona.agregarMedio(new MedioContacto(TipoContacto.EMAIL, email, true));
        }
        if (telefono != null && !telefono.isBlank()) {
            persona.agregarMedio(new MedioContacto(TipoContacto.TELEFONO, telefono, false));
        }
        if (whatsapp != null && !whatsapp.isBlank()) {
            persona.agregarMedio(new MedioContacto(TipoContacto.WHATSAPP, whatsapp, false));
        }
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

        cargarMediosContacto(persona, dto.getEmail(), dto.getTelefono(), null);

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

    /**
     * Expone el objeto de dominio (no el DTO) para que otros servicios del
     * mismo módulo (DonacionService, schedulers) puedan operar sobre él,
     * por ejemplo para vincular una donación a su donante o para leer sus
     * medios de contacto al notificar.
     */
    public Donante obtenerDominioPorId(Long id) {
        return buscarPorIdDominio(id);
    }

    /**
     * Usado por el scheduler de inactividad (Entrega 2) para recorrer a
     * todos los donantes y detectar quiénes llevan más de 20 días sin
     * interactuar con la plataforma.
     */
    public List<Donante> obtenerTodosDominio() {
        return donantes;
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

    public List<DonanteDTO> importarCSV(MultipartFile archivo) {

        try {

            ImportadorCSV importador =
                    new ImportadorCSV("Importador CSV");

            importador.importar(archivo.getInputStream());

            donantes.addAll(importador.getListaDonantes());

            return importador.getListaDonantes()
                    .stream()
                    .map(this::convertirADTO)
                    .toList();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}