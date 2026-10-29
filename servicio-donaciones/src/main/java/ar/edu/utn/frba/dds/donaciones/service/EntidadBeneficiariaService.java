package ar.edu.utn.frba.dds.donaciones.service;

import ar.edu.utn.frba.dds.donaciones.domain.categorias.Subcategoria;
import ar.edu.utn.frba.dds.donaciones.domain.categorias.UnidadMedida;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.GestorDonaciones;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.ResultadoMatchmaking;
import ar.edu.utn.frba.dds.donaciones.domain.lugares.Ciudad;
import ar.edu.utn.frba.dds.donaciones.domain.lugares.Direccion;
import ar.edu.utn.frba.dds.donaciones.domain.lugares.Provincia;
import ar.edu.utn.frba.dds.donaciones.domain.necesidades.Necesidad;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import ar.edu.utn.frba.dds.donaciones.domain.personas.PersonaHumana;
import ar.edu.utn.frba.dds.donaciones.domain.personas.PersonaJuridica;
import ar.edu.utn.frba.dds.donaciones.dto.CiudadDTO;
import ar.edu.utn.frba.dds.donaciones.dto.DireccionDTO;
import ar.edu.utn.frba.dds.donaciones.dto.EntidadBeneficiariaDTO;
import ar.edu.utn.frba.dds.donaciones.dto.EntidadCandidataDTO;
import ar.edu.utn.frba.dds.donaciones.dto.PersonaJuridicaDTO;
import ar.edu.utn.frba.dds.donaciones.dto.ProvinciaDTO;
import ar.edu.utn.frba.dds.donaciones.dto.ResultadoMatchmakingDTO;
import ar.edu.utn.frba.dds.donaciones.repository.DonacionRepository;
import ar.edu.utn.frba.dds.donaciones.repository.EntidadBeneficiariaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class EntidadBeneficiariaService {

    private final EntidadBeneficiariaRepository entidadBeneficiariaRepository;
    private final GestorDonaciones gestorDonaciones;
    private final DonacionRepository donacionRepository;

    public EntidadBeneficiariaService(EntidadBeneficiariaRepository entidadBeneficiariaRepository,
                                       GestorDonaciones gestorDonaciones,
                                       DonacionRepository donacionRepository) {
        this.entidadBeneficiariaRepository = entidadBeneficiariaRepository;
        this.gestorDonaciones = gestorDonaciones;
        this.donacionRepository = donacionRepository;
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

        personaJuridica.setMediosContacto(ar.edu.utn.frba.dds.donaciones.dto.MedioContactoDTO.validar(personaJuridicaDTO.getMediosContacto()));
        EntidadBeneficiaria entidad =
                new EntidadBeneficiaria(
                        null,
                        personaJuridica,
                        dto.getDescripcion()
                );
        entidad.setDireccion(convertirDireccionDominio(dto.getDireccion()));
        entidad = entidadBeneficiariaRepository.save(entidad);

        return convertirADTO(entidad);
    }

    public List<EntidadBeneficiariaDTO> obtenerTodas() {
        return entidadBeneficiariaRepository.findAll().stream().map(this::convertirADTO).toList();
    }

    public EntidadBeneficiariaDTO obtenerPorId(Long id) {
        return convertirADTO(buscarEntidad(id));
    }

    public EntidadBeneficiaria buscarEntidad(Long id) {
        return entidadBeneficiariaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No existe la entidad beneficiaria " + id));
    }

    /**
     * Baja lógica -- la entidad NUNCA se borra físicamente. Necesidad no
     * tiene estado de cancelación: las que nunca tuvieron una Donacion
     * (necesidadAsignada) ni cantidad recibida se quitan de la colección
     * y orphanRemoval las borra. Las vinculadas a una donación, en
     * cualquier estado, o con cantidad recibida, se conservan. No se toca
     * donacion ni su FK: el borrado solo ocurre si ninguna donación las
     * referencia (RESTRICT en fk_donacion_necesidad_asignada). La entidad
     * inactiva ya queda fuera de matchmaking y de confirmarAsignacion, así
     * que las necesidades conservadas no reciben asignaciones nuevas. Las
     * entregas ya iniciadas siguen su curso.
     */
    @Transactional
    public void eliminar(Long id) {
        EntidadBeneficiaria entidad = buscarEntidad(id);
        if (entidad.getNecesidades() != null) {
            List<Necesidad> sinHistorial = entidad.getNecesidades().stream()
                    .filter(this::sinDonacionNiActividad)
                    .toList();
            sinHistorial.forEach(necesidad -> {
                entidad.getNecesidades().remove(necesidad);
                necesidad.setEntidadBeneficiaria(null);
            });
        }
        entidad.desactivar();
        entidadBeneficiariaRepository.save(entidad);
    }

    private boolean sinDonacionNiActividad(Necesidad necesidad) {
        if (necesidad.getCantidadRecibida() != null && necesidad.getCantidadRecibida().signum() > 0) {
            return false;
        }
        return necesidad.getId() == null || !donacionRepository.existsByNecesidadAsignada_Id(necesidad.getId());
    }

    /** Única operación de modificación permitida sobre una entidad inactiva. */
    public void reactivar(Long id) {
        EntidadBeneficiaria entidad = buscarEntidad(id);
        entidad.reactivar();
        entidadBeneficiariaRepository.save(entidad);
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

        personaDTO.setMediosContacto(ar.edu.utn.frba.dds.donaciones.dto.MedioContactoDTO.desde(entidad.getEntidad()));
        dto.setPersonaJuridica(personaDTO);
        dto.setDescripcion(entidad.getDescripcion());
        dto.setId(entidad.getId());
        dto.setActiva(entidad.isActiva());
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

        // Una entidad inactiva no admite ninguna modificación -- la única
        // operación permitida sobre ella es reactivar() (ver reactivar()).
        if (!entidad.isActiva()) {
            throw new IllegalStateException(
                    "No se puede modificar la entidad " + id + ": está inactiva. Reactívela primero.");
        }

        var contactos = ar.edu.utn.frba.dds.donaciones.dto.MedioContactoDTO.validar(dto.getPersonaJuridica().getMediosContacto());
        entidad.getEntidad().setMediosContacto(contactos);
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

        /**
         * Solo entidades activas -- usado como universo de candidatas para
         * matchmaking/asignación (ver MatchmakingService, AsignacionService):
         * una entidad inactiva debe quedar excluida de nuevas asignaciones.
         * obtenerTodas()/obtenerPorId() sí devuelven inactivas (hace falta
         * poder verlas para reactivarlas).
         */
        public List<EntidadBeneficiaria> obtenerEntidadesDominio() {
           return entidadBeneficiariaRepository.findAll().stream()
                   .filter(EntidadBeneficiaria::isActiva)
                   .toList();
        }

    /**
     * Conversor único de un ResultadoMatchmaking a su DTO -- usado tanto por
     * el matchmaking real (DonacionController, sobre una Donacion existente)
     * como por la exploración a demanda con un ítem hipotético
     * (AsignacionService), para que ambos caminos calculen el puntaje de
     * la misma forma y no diverjan entre sí.
     */
    public ResultadoMatchmakingDTO convertirResultadoADTO(
            ResultadoMatchmaking resultado, Subcategoria subcategoria, UnidadMedida unidadMedida) {

        ResultadoMatchmakingDTO dto = new ResultadoMatchmakingDTO();
        dto.setPorCompatibilidad(convertirCandidatas(resultado.getPorCompatibilidad(), subcategoria, unidadMedida));
        dto.setPorSubatencion(convertirCandidatas(resultado.getPorSubatencion(), subcategoria, unidadMedida));
        dto.setInterseccion(convertirCandidatas(resultado.getInterseccion(), subcategoria, unidadMedida));
        return dto;
    }

    private List<EntidadCandidataDTO> convertirCandidatas(
            List<EntidadBeneficiaria> entidades, Subcategoria subcategoria, UnidadMedida unidadMedida) {

        return entidades.stream()
                .map(entidad -> convertirACandidataDTO(entidad, subcategoria, unidadMedida))
                .toList();
    }

    private EntidadCandidataDTO convertirACandidataDTO(
            EntidadBeneficiaria entidad, Subcategoria subcategoria, UnidadMedida unidadMedida) {

        EntidadCandidataDTO dto = new EntidadCandidataDTO();
        dto.setEntidadBeneficiariaId(entidad.getId());
        dto.setDescripcion(entidad.getDescripcion());
        if (entidad.getEntidad() != null) {
            dto.setRazonSocial(entidad.getEntidad().getRazonSocial());
        }
        dto.setPuntaje((int) entidad.necesidadesPendientes().stream()
                .filter(n -> n.getSubcategoria() == subcategoria && n.getUnidadMedida() == unidadMedida)
                .count());
        return dto;
    }
}
