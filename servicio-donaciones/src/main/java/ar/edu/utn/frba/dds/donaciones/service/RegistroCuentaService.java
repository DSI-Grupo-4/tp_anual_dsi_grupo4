package ar.edu.utn.frba.dds.donaciones.service;

import ar.edu.utn.frba.dds.donaciones.domain.personas.TipoContacto;
import ar.edu.utn.frba.dds.donaciones.dto.DireccionDTO;
import ar.edu.utn.frba.dds.donaciones.dto.DonanteDTO;
import ar.edu.utn.frba.dds.donaciones.dto.EntidadBeneficiariaDTO;
import ar.edu.utn.frba.dds.donaciones.dto.MedioContactoDTO;
import ar.edu.utn.frba.dds.donaciones.dto.PersonaHumanaDTO;
import ar.edu.utn.frba.dds.donaciones.dto.PersonaJuridicaDTO;
import ar.edu.utn.frba.dds.donaciones.integracion.CuentasKeycloak;
import ar.edu.utn.frba.dds.donaciones.seguridad.PropiedadRecursos;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

/**
 * Autorregistro (D-047). Reutiliza el alta de donante humano, el alta de donante jurídico
 * y el alta de entidad:
 * no crea otro camino de persistencia. El correo que queda asociado es el verificado
 * del token; cualquier otro correo del body se descarta para no vincular a un tercero.
 * Nunca asigna ADMIN: la cuenta recibe DONANTE o BENEFICIARIA por {@link CuentasKeycloak}.
 */
@Service
public class RegistroCuentaService {

    private static final Logger log = LoggerFactory.getLogger(RegistroCuentaService.class);

    private final DonanteService donantes;
    private final EntidadBeneficiariaService entidades;
    private final PropiedadRecursos propiedad;
    private final CuentasKeycloak cuentas;

    public RegistroCuentaService(DonanteService donantes, EntidadBeneficiariaService entidades,
                                 PropiedadRecursos propiedad, CuentasKeycloak cuentas) {
        this.donantes = donantes;
        this.entidades = entidades;
        this.propiedad = propiedad;
        this.cuentas = cuentas;
    }

    public DonanteDTO registrarDonante(String usuarioId, String email, PersonaHumanaDTO dto) {
        email = normalizar(email);
        if (correoEnDonante(email)) {
            throw new IllegalStateException(
                    "Este correo ya está asociado a un donante. Usá Buscar mis datos, no hace falta registrarte de nuevo.");
        }
        exigirIdentidad(dto);
        if (dto.getEdad() == null || dto.getEdad() < 0 || dto.getEdad() > 130) {
            throw new IllegalArgumentException("Ingresá una edad entre 0 y 130");
        }
        if (dto.getGenero() == null) {
            throw new IllegalArgumentException("Elegí un género");
        }
        exigirDireccion(dto.getDireccion());
        String documento = dto.getDocumento().trim();
        dto.setDocumento(documento);
        if (!propiedad.donantesConDocumento(documento).isEmpty()) {
            throw new IllegalStateException(
                    "Ya hay un donante con ese documento. Si es el tuyo, pedile a la administración que lo cargue con este correo.");
        }
        dejarCorreoVerificado(dto, email);
        DonanteDTO creado = donantes.crearDonanteHumano(dto);
        vincularEnCaliente(usuarioId, email);
        return creado;
    }

    /**
     * Organización que dona. El dominio admite un único representante: es quien
     * tiene el correo verificado. No reclama una razón social ya cargada.
     */
    public DonanteDTO registrarDonanteJuridico(String usuarioId, String email, PersonaJuridicaDTO dto) {
        email = normalizar(email);
        if (correoEnDonante(email)) {
            throw new IllegalStateException(
                    "Este correo ya está asociado a un donante. Usá Buscar mis datos, no hace falta registrarte de nuevo.");
        }
        if (dto == null || dto.getRazonSocial() == null || dto.getRazonSocial().isBlank()) {
            throw new IllegalArgumentException("La razón social es obligatoria");
        }
        if (dto.getTipo() == null) {
            throw new IllegalArgumentException("Elegí un tipo de organización");
        }
        String razon = dto.getRazonSocial().trim();
        dto.setRazonSocial(razon);
        if (!propiedad.donantesConRazonSocial(razon).isEmpty()) {
            throw new IllegalStateException(
                    "Ya existe una organización donante con esa razón social. Admite un único representante: "
                            + "si es la tuya, pedile a la administración que cargue este correo.");
        }
        if (dto.getRepresentantes() == null || dto.getRepresentantes().isEmpty()) {
            throw new IllegalArgumentException("Indicá los datos del representante");
        }
        PersonaHumanaDTO representante = dto.getRepresentantes().get(0);
        exigirIdentidad(representante);
        dejarCorreoVerificado(representante, email);
        dto.setRepresentantes(List.of(representante));
        dejarCorreoVerificado(dto, email);
        DonanteDTO creado = donantes.crearDonanteJuridico(dto);
        vincularEnCaliente(usuarioId, email);
        return creado;
    }

    public EntidadBeneficiariaDTO registrarEntidad(String usuarioId, String email, EntidadBeneficiariaDTO dto) {
        email = normalizar(email);
        if (dto == null || dto.getPersonaJuridica() == null) {
            throw new IllegalArgumentException("Faltan los datos de la entidad");
        }
        if (correoEnEntidad(email)) {
            throw new IllegalStateException(
                    "Este correo ya figura en una entidad. No se puede reclamar una entidad existente: "
                            + "tiene que cargarte un representante autorizado o un administrador.");
        }
        PersonaJuridicaDTO juridica = dto.getPersonaJuridica();
        if (juridica.getRazonSocial() == null || juridica.getRazonSocial().isBlank()) {
            throw new IllegalArgumentException("La razón social es obligatoria");
        }
        String razon = juridica.getRazonSocial().trim();
        juridica.setRazonSocial(razon);
        if (!propiedad.entidadesConRazonSocial(razon).isEmpty()) {
            throw new IllegalStateException(
                    "Ya existe una entidad con esa razón social. Si la representás, un representante o un administrador tiene que autorizar tu correo.");
        }
        if (juridica.getRepresentantes() == null || juridica.getRepresentantes().isEmpty()) {
            throw new IllegalArgumentException("Indicá los datos del representante");
        }
        exigirDireccion(dto.getDireccion());
        PersonaHumanaDTO representante = juridica.getRepresentantes().get(0);
        exigirIdentidad(representante);
        dejarCorreoVerificado(representante, email);
        juridica.setRepresentantes(List.of(representante));
        dejarCorreoVerificado(juridica, email);
        boolean tieneTelefono = juridica.getMediosContacto().stream()
                .anyMatch(c -> c.getTipo() == TipoContacto.TELEFONO && c.getValor() != null && !c.getValor().isBlank());
        if (!tieneTelefono) {
            throw new IllegalArgumentException("Incluí un teléfono de la entidad");
        }
        EntidadBeneficiariaDTO creada = entidades.crear(dto);
        vincularEnCaliente(usuarioId, email);
        return creada;
    }

    private boolean correoEnDonante(String email) {
        return !propiedad.donantesConEmail(TipoContacto.EMAIL, email).isEmpty()
                || !propiedad.donantesConRepresentanteEmail(TipoContacto.EMAIL, email).isEmpty();
    }

    private boolean correoEnEntidad(String email) {
        return !propiedad.entidadesConEmail(TipoContacto.EMAIL, email).isEmpty()
                || !propiedad.entidadesConRepresentanteEmail(TipoContacto.EMAIL, email).isEmpty();
    }

    /** El alta ya está guardada: si Keycloak no responde ahora, la bandeja y "Buscar mis datos" lo reintentan. */
    private void vincularEnCaliente(String usuarioId, String email) {
        try {
            cuentas.vincularUsuario(usuarioId, email);
        } catch (RestClientException | IllegalStateException e) {
            log.warn("El registro quedó guardado pero no se pudo vincular la cuenta {} en el momento: {}", usuarioId, e.getMessage());
        }
    }

    private static void exigirIdentidad(PersonaHumanaDTO dto) {
        if (dto == null || blanco(dto.getNombre()) || blanco(dto.getApellido()) || blanco(dto.getDocumento())) {
            throw new IllegalArgumentException("Nombre, apellido y documento son obligatorios");
        }
        dto.setNombre(dto.getNombre().trim());
        dto.setApellido(dto.getApellido().trim());
        dto.setDocumento(dto.getDocumento().trim());
    }

    private static void exigirDireccion(DireccionDTO direccion) {
        if (direccion == null || blanco(direccion.getCalle()) || blanco(direccion.getNumero())
                || direccion.getCiudad() == null || blanco(direccion.getCiudad().getNombre())
                || direccion.getCiudad().getProvincia() == null || blanco(direccion.getCiudad().getProvincia().getNombre())) {
            throw new IllegalArgumentException("La dirección es obligatoria");
        }
    }

    /** Conserva teléfono y otros medios. El único EMAIL que queda es el verificado, y es el preferido. */
    private static void dejarCorreoVerificado(PersonaHumanaDTO dto, String email) {
        dto.setMediosContacto(conCorreoVerificado(dto.getMediosContacto(), email));
    }

    private static void dejarCorreoVerificado(PersonaJuridicaDTO dto, String email) {
        dto.setMediosContacto(conCorreoVerificado(dto.getMediosContacto(), email));
    }

    private static List<MedioContactoDTO> conCorreoVerificado(List<MedioContactoDTO> originales, String email) {
        List<MedioContactoDTO> contactos = new ArrayList<>();
        MedioContactoDTO mail = new MedioContactoDTO();
        mail.setTipo(TipoContacto.EMAIL);
        mail.setValor(email);
        mail.setEsPreferido(true);
        contactos.add(mail);
        if (originales != null) {
            for (MedioContactoDTO contacto : originales) {
                if (contacto != null && contacto.getTipo() != null && contacto.getTipo() != TipoContacto.EMAIL) {
                    contacto.setEsPreferido(false);
                    contactos.add(contacto);
                }
            }
        }
        return contactos;
    }

    private static String normalizar(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalStateException("La cuenta no tiene un email verificado");
        }
        return email.trim().toLowerCase();
    }

    private static boolean blanco(String valor) {
        return valor == null || valor.isBlank();
    }
}
