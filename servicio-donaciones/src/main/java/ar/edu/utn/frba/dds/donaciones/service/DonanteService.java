package ar.edu.utn.frba.dds.donaciones.service;

import ar.edu.utn.frba.dds.donaciones.domain.personas.Donante;
import ar.edu.utn.frba.dds.donaciones.domain.personas.GestorDonantes;
import ar.edu.utn.frba.dds.donaciones.domain.personas.ImportadorCSV;
import ar.edu.utn.frba.dds.donaciones.domain.personas.PersonaHumana;
import ar.edu.utn.frba.dds.donaciones.domain.personas.PersonaJuridica;
import ar.edu.utn.frba.dds.donaciones.dto.DonanteDTO;
import ar.edu.utn.frba.dds.donaciones.dto.ImportacionDonantesDTO;
import ar.edu.utn.frba.dds.donaciones.dto.PersonaHumanaDTO;
import ar.edu.utn.frba.dds.donaciones.dto.PersonaJuridicaDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class DonanteService {

    private final GestorDonantes gestorDonantes;

    public DonanteService(GestorDonantes gestorDonantes) {
        this.gestorDonantes = gestorDonantes;
    }

    public DonanteDTO convertirADTO(Donante donante) {

        DonanteDTO dto = new DonanteDTO();

        if(donante.getPersona() instanceof PersonaHumana humana) {

            dto.setTipo("HUMANA");
            dto.setNombre(humana.getNombre());
            dto.setApellido(humana.getApellido());
            dto.setEdad(humana.getEdad());
            dto.setGenero(humana.getGenero());
            dto.setDocumento(humana.getDocumento());
        }

        if(donante.getPersona() instanceof PersonaJuridica juridica) {

            dto.setTipo("JURIDICA");
            dto.setRazonSocial(juridica.getRazonSocial());
            dto.setTipoOrganizacion(juridica.getTipo());
            dto.setRubro(juridica.getRubro());
        }

        dto.setMediosContacto(donante.getPersona().getMediosContacto().stream().map(m -> {
            ar.edu.utn.frba.dds.donaciones.dto.MedioContactoDTO contacto = new ar.edu.utn.frba.dds.donaciones.dto.MedioContactoDTO();
            contacto.setTipo(m.getTipo());
            contacto.setValor(m.getValor());
            contacto.setEsPreferido(m.esPreferido());
            return contacto;
        }).toList());
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
        persona.setMediosContacto(validarContactos(dto.getMediosContacto()));
        Donante donante = gestorDonantes.registrarDonante(persona);
        return convertirADTO(donante);
    }

    public List<DonanteDTO> obtenerTodos() {
        return gestorDonantes.getDonantesRegistrados().stream()
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

        persona.setMediosContacto(validarContactos(dto.getMediosContacto()));
        Donante donante = gestorDonantes.registrarDonante(persona);

        return convertirADTO(donante);
    }

    public DonanteDTO buscarPorId(Long id) {
        return convertirADTO(gestorDonantes.buscarPorId(id));
    }

    public void eliminar(Long id) {
        gestorDonantes.eliminar(id);
    }

    /**
     * El tipo de un donante (humano/jurídico) es inmutable una vez creado —
     * el cliente no decide la rama en un PUT, se determina acá a partir del
     * tipo real ya guardado. Antes DonanteController confiaba en dto.getTipo()
     * (lo que mandaba el cliente); un mismatch producía un ClassCastException
     * (500 sin manejar) — confirmado en vivo en el scan de calidad.
     */
    public DonanteDTO actualizar(Long id, DonanteDTO dto) {
        Donante donante = gestorDonantes.buscarPorId(id);

        if (donante.getPersona() instanceof PersonaHumana) {
            PersonaHumanaDTO humanaDTO = new PersonaHumanaDTO();
            humanaDTO.setMediosContacto(dto.getMediosContacto());
            humanaDTO.setNombre(dto.getNombre());
            humanaDTO.setApellido(dto.getApellido());
            humanaDTO.setEdad(dto.getEdad());
            humanaDTO.setGenero(dto.getGenero());
            humanaDTO.setDocumento(dto.getDocumento());
            return actualizarHumano(id, humanaDTO);
        }

        PersonaJuridicaDTO juridicaDTO = new PersonaJuridicaDTO();
        juridicaDTO.setMediosContacto(dto.getMediosContacto());
        juridicaDTO.setRazonSocial(dto.getRazonSocial());
        juridicaDTO.setTipo(dto.getTipoOrganizacion());
        juridicaDTO.setRubro(dto.getRubro());
        return actualizarJuridico(id, juridicaDTO);
    }

    public DonanteDTO actualizarHumano(
            Long id,
            PersonaHumanaDTO dto) {

        Donante donante = gestorDonantes.buscarPorId(id);

        PersonaHumana persona =
                (PersonaHumana) donante.getPersona();
        var contactos = validarContactos(dto.getMediosContacto());
        gestorDonantes.actualizarContactos(donante, contactos);

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

        Donante donante = gestorDonantes.buscarPorId(id);

        PersonaJuridica persona =
                (PersonaJuridica) donante.getPersona();
        var contactos = validarContactos(dto.getMediosContacto());
        gestorDonantes.actualizarContactos(donante, contactos);

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

    /**
     * Devuelve solo la cantidad importada, no el detalle de cada donante --
     * con archivos grandes (el CSV de ejemplo trae 20000 filas) armar y
     * serializar esa cantidad de DonanteDTO agregaba una latencia que no
     * tiene que ver con el trabajo real del alta. Quien necesite el detalle
     * ya puede pedirlo por GET /api/donantes.
     */
    private List<ar.edu.utn.frba.dds.donaciones.domain.personas.MedioContacto> validarContactos(
            List<ar.edu.utn.frba.dds.donaciones.dto.MedioContactoDTO> contactos) {
        if (contactos == null || contactos.isEmpty()) throw new IllegalArgumentException("Debe incluir un EMAIL y un medio preferido");
        var resultado = contactos.stream().map(c -> {
            if (c == null || c.getTipo() == null || c.getValor() == null || c.getValor().isBlank() || c.getEsPreferido() == null)
                throw new IllegalArgumentException("Cada contacto requiere tipo, valor y esPreferido");
            String valor = c.getValor().trim();
            if (c.getTipo() == ar.edu.utn.frba.dds.donaciones.domain.personas.TipoContacto.EMAIL &&
                    !valor.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))
                throw new IllegalArgumentException("El EMAIL debe ser válido");
            return new ar.edu.utn.frba.dds.donaciones.domain.personas.MedioContacto(c.getTipo(), valor, c.getEsPreferido());
        }).toList();
        if (resultado.stream().noneMatch(c -> c.getTipo() == ar.edu.utn.frba.dds.donaciones.domain.personas.TipoContacto.EMAIL))
            throw new IllegalArgumentException("El EMAIL es obligatorio");
        if (resultado.stream().filter(c -> Boolean.TRUE.equals(c.esPreferido())).count() != 1)
            throw new IllegalArgumentException("Debe elegir exactamente un medio de contacto preferido");
        return resultado;
    }

    public ImportacionDonantesDTO importarCSV(MultipartFile archivo) {

        try {

            ImportadorCSV importador =
                    new ImportadorCSV("Importador CSV");

            importador.importar(archivo.getInputStream());

            gestorDonantes.agregarImportador(importador);

            int cantidadImportados = gestorDonantes.importarDonantes(importador.getNombre()).size();

            return new ImportacionDonantesDTO(cantidadImportados);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
