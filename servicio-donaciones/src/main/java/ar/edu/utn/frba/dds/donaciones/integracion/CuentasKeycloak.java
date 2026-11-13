package ar.edu.utn.frba.dds.donaciones.integracion;

import ar.edu.utn.frba.dds.donaciones.domain.personas.Donante;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import ar.edu.utn.frba.dds.donaciones.domain.personas.MedioContacto;
import ar.edu.utn.frba.dds.donaciones.domain.personas.Persona;
import ar.edu.utn.frba.dds.donaciones.domain.personas.PersonaHumana;
import ar.edu.utn.frba.dds.donaciones.domain.personas.PersonaJuridica;
import ar.edu.utn.frba.dds.donaciones.domain.personas.TipoContacto;
import ar.edu.utn.frba.dds.donaciones.repository.DonanteRepository;
import ar.edu.utn.frba.dds.donaciones.repository.EntidadBeneficiariaRepository;
import ar.edu.utn.frba.dds.donaciones.seguridad.PropiedadRecursos;
import ar.edu.utn.frba.dds.donaciones.seguridad.TokenServicio;
import ar.edu.utn.frba.dds.donaciones.seguridad.UsuarioToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Cuentas de Keycloak para donantes y representantes de entidades (D-046).
 *
 * El alta administrativa y la importación CSV encolan el donante o la entidad;
 * en segundo plano se busca en Keycloak un usuario por cada email cargado:
 * si no existe se crea y se le manda el mail para elegir contraseña; si existe
 * y su email está verificado, se le agrega el atributo (donante_id o entidad_ids)
 * y el rol. A un usuario con email sin verificar no se le asocia nada.
 * Los emails que ya no figuran en el donante o la entidad pierden el vínculo.
 * Nunca se asigna ADMIN.
 *
 * La bandeja en disco sigue el patrón de IncentivosClient: si Keycloak no
 * responde, los pendientes se reintentan sin perder altas.
 */
@Component
public class CuentasKeycloak {

    private static final Logger logger = LoggerFactory.getLogger(CuentasKeycloak.class);
    private static final String ATRIBUTO_DONANTE = "donante_id";
    private static final String ATRIBUTO_ENTIDADES = "entidad_ids";
    private static final int LOTE = 200;

    private final TokenServicio token;
    private final PropiedadRecursos propiedad;
    private final DonanteRepository donantes;
    private final EntidadBeneficiariaRepository entidades;
    private final TransactionTemplate transaccion;
    private final RestClient admin;
    private final String webUrl;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, JsonNode> roles = new ConcurrentHashMap<>();

    @Value("${cuentas.pendientes-dir:.datos/donaciones/cuentas-pendientes}")
    private Path pendientes = Path.of(".datos/donaciones/cuentas-pendientes");

    public CuentasKeycloak(TokenServicio token, PropiedadRecursos propiedad, DonanteRepository donantes,
                           EntidadBeneficiariaRepository entidades, PlatformTransactionManager transacciones,
                           @Value("${donatrack.seguridad.keycloak-url:http://localhost:8180}") String keycloakUrl,
                           @Value("${donatrack.web-url:http://localhost:8085}") String webUrl) {
        this.token = token;
        this.propiedad = propiedad;
        this.donantes = donantes;
        this.entidades = entidades;
        this.transaccion = new TransactionTemplate(transacciones);
        this.transaccion.setReadOnly(true);
        this.webUrl = webUrl;
        var transporte = new SimpleClientHttpRequestFactory();
        transporte.setConnectTimeout(3000);
        transporte.setReadTimeout(10000);
        this.admin = RestClient.builder().requestFactory(transporte)
                .baseUrl(keycloakUrl + "/admin/realms/donatrack")
                .requestInterceptor(token.interceptor())
                .build();
    }

    public void encolarDonante(Long id) {
        encolar("donante", id);
    }

    public void encolarEntidad(Long id) {
        encolar("entidad", id);
    }

    /** Para donantes y entidades cargados antes de habilitar la seguridad. */
    public int encolarTodos() {
        List<Long> idsDonantes = propiedad.idsDonantes();
        List<Long> idsEntidades = propiedad.idsEntidades();
        idsDonantes.forEach(this::encolarDonante);
        idsEntidades.forEach(this::encolarEntidad);
        return idsDonantes.size() + idsEntidades.size();
    }

    /** Vincula al usuario del token con los donantes y entidades que tienen su email. */
    public Vinculacion vincularUsuario(String usuarioId, String email) {
        List<Long> donantesDelEmail = sinRepetidos(
                propiedad.donantesConEmail(TipoContacto.EMAIL, email),
                propiedad.donantesConRepresentanteEmail(TipoContacto.EMAIL, email));
        List<Long> entidadesDelEmail = sinRepetidos(
                propiedad.entidadesConEmail(TipoContacto.EMAIL, email),
                propiedad.entidadesConRepresentanteEmail(TipoContacto.EMAIL, email));
        ObjectNode usuario = leerUsuario(usuarioId);
        Long donante = null;
        if (donantesDelEmail.size() == 1) {
            donante = donantesDelEmail.get(0);
            if (!asociar(usuario, ATRIBUTO_DONANTE, donante, UsuarioToken.DONANTE)) {
                donante = null;
            }
        } else if (donantesDelEmail.size() > 1) {
            logger.warn("El email de la cuenta {} figura en {} donantes: no se vincula ninguno.", usuarioId, donantesDelEmail.size());
        }
        for (Long entidad : entidadesDelEmail) {
            asociar(usuario, ATRIBUTO_ENTIDADES, entidad, UsuarioToken.BENEFICIARIA);
        }
        return new Vinculacion(donante, entidadesDelEmail);
    }

    public record Vinculacion(Long donanteId, List<Long> entidadIds) {
    }

    @Scheduled(fixedDelayString = "${cuentas.reintento-ms:10000}")
    public synchronized void procesarPendientes() {
        if (!token.habilitada() || !Files.exists(pendientes)) {
            return;
        }
        List<Path> lote;
        try (var archivos = Files.list(pendientes)) {
            lote = archivos.filter(p -> p.toString().endsWith(".json")).sorted().limit(LOTE).toList();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer la bandeja de cuentas", e);
        }
        for (Path archivo : lote) {
            String nombre = archivo.getFileName().toString();
            try {
                Long id = Long.parseLong(nombre.substring(nombre.indexOf('-') + 1, nombre.length() - ".json".length()));
                boolean existe = nombre.startsWith("donante-") ? sincronizarDonante(id) : sincronizarEntidad(id);
                // Un registro que todavía no se ve puede ser de una importación CSV cuya
                // transacción no terminó: se espera hasta una hora antes de descartarlo.
                if (existe || Files.getLastModifiedTime(archivo).toInstant().isBefore(Instant.now().minus(Duration.ofHours(1)))) {
                    Files.deleteIfExists(archivo);
                }
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode().value() == 401 || e.getStatusCode().value() == 403) {
                    logger.warn("Keycloak rechazó al cliente servicio-donaciones ({}): revisar secret y roles de realm-management.",
                            e.getStatusCode().value());
                    return;
                }
                logger.error("Keycloak rechazó la cuenta {} ({}): {}", nombre, e.getStatusCode().value(), e.getMessage());
                borrar(archivo);
            } catch (RestClientException e) {
                logger.warn("Cuentas de Keycloak pendientes ({}): {}", nombre, e.getMessage());
                return;
            } catch (IOException | RuntimeException e) {
                logger.error("No se pudo sincronizar la cuenta {}: {}", nombre, e.getMessage(), e);
                borrar(archivo);
            }
        }
    }

    private void encolar(String tipo, Long id) {
        if (!token.habilitada() || id == null) {
            return;
        }
        try {
            Files.createDirectories(pendientes);
            Path temporal = Files.createTempFile(pendientes, tipo + "-", ".tmp");
            Files.move(temporal, pendientes.resolve(String.format("%s-%020d.json", tipo, id)),
                    StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo encolar la cuenta pendiente", e);
        }
    }

    private boolean sincronizarDonante(Long id) {
        Optional<List<Cuenta>> cuentas = transaccion.execute(s -> donantes.findById(id).map(CuentasKeycloak::cuentasDelDonante));
        if (cuentas == null || cuentas.isEmpty()) {
            return false;
        }
        sincronizar(cuentas.get(), ATRIBUTO_DONANTE, id, UsuarioToken.DONANTE);
        return true;
    }

    private boolean sincronizarEntidad(Long id) {
        Optional<List<Cuenta>> cuentas = transaccion.execute(s -> entidades.findById(id).map(CuentasKeycloak::cuentasDeLaEntidad));
        if (cuentas == null || cuentas.isEmpty()) {
            return false;
        }
        sincronizar(cuentas.get(), ATRIBUTO_ENTIDADES, id, UsuarioToken.BENEFICIARIA);
        return true;
    }

    private void sincronizar(List<Cuenta> cuentas, String atributo, Long id, String rol) {
        Set<String> vigentes = new LinkedHashSet<>();
        for (Cuenta cuenta : cuentas) {
            vigentes.add(cuenta.email());
            Optional<ObjectNode> existente = buscarPorEmail(cuenta.email());
            if (existente.isEmpty()) {
                ObjectNode nuevo = crear(cuenta, atributo, id);
                asignarRol(nuevo.get("id").asText(), rol);
                enviarAcceso(nuevo.get("id").asText());
                continue;
            }
            ObjectNode usuario = existente.get();
            if (!usuario.path("emailVerified").asBoolean(false)) {
                logger.info("No se asocia {} {} a una cuenta con email sin verificar.", atributo, id);
                continue;
            }
            asociar(leerUsuario(usuario.get("id").asText()), atributo, id, rol);
        }
        desasociarSobrantes(atributo, id, rol, vigentes);
    }

    /** Devuelve false si la cuenta ya pertenece a otro donante (donante_id es único por cuenta). */
    private boolean asociar(ObjectNode usuario, String atributo, Long id, String rol) {
        String valor = String.valueOf(id);
        List<String> actuales = valores(usuario, atributo);
        if (ATRIBUTO_DONANTE.equals(atributo) && !actuales.isEmpty() && !actuales.contains(valor)) {
            logger.warn("La cuenta {} ya está vinculada a otro donante: no se la asocia al donante {}.",
                    usuario.path("id").asText(), id);
            return false;
        }
        if (!actuales.contains(valor)) {
            List<String> nuevos = new ArrayList<>(actuales);
            nuevos.add(valor);
            guardarAtributo(usuario, atributo, nuevos);
        }
        asignarRol(usuario.get("id").asText(), rol);
        return true;
    }

    private void desasociarSobrantes(String atributo, Long id, String rol, Set<String> vigentes) {
        String valor = String.valueOf(id);
        JsonNode encontrados = admin.get().uri(u -> u.path("/users").queryParam("q", atributo + ":" + valor)
                .queryParam("max", 100).build()).retrieve().body(JsonNode.class);
        if (encontrados == null) {
            return;
        }
        for (JsonNode encontrado : encontrados) {
            String email = encontrado.path("email").asText("").toLowerCase();
            if (vigentes.contains(email)) {
                continue;
            }
            ObjectNode usuario = leerUsuario(encontrado.get("id").asText());
            List<String> restantes = new ArrayList<>(valores(usuario, atributo));
            if (!restantes.remove(valor)) {
                continue;
            }
            guardarAtributo(usuario, atributo, restantes);
            if (restantes.isEmpty()) {
                admin.method(org.springframework.http.HttpMethod.DELETE)
                        .uri("/users/{id}/role-mappings/realm", usuario.get("id").asText())
                        .body(List.of(rol(rol))).retrieve().toBodilessEntity();
            }
        }
    }

    private Optional<ObjectNode> buscarPorEmail(String email) {
        JsonNode lista = admin.get().uri(u -> u.path("/users").queryParam("email", email)
                .queryParam("exact", true).build()).retrieve().body(JsonNode.class);
        if (lista == null) {
            return Optional.empty();
        }
        for (JsonNode usuario : lista) {
            if (email.equalsIgnoreCase(usuario.path("email").asText())) {
                return Optional.of((ObjectNode) usuario);
            }
        }
        return Optional.empty();
    }

    private ObjectNode crear(Cuenta cuenta, String atributo, Long id) {
        // El email lo cargó la administración (alta en el depósito o histórico de la ONG).
        // La cuenta no sirve hasta que quien tiene ese buzón elige la contraseña con el link del mail.
        ObjectNode nuevo = mapper.createObjectNode()
                .put("username", cuenta.email())
                .put("email", cuenta.email())
                .put("emailVerified", true)
                .put("enabled", true);
        if (cuenta.nombre() != null) {
            nuevo.put("firstName", cuenta.nombre());
        }
        if (cuenta.apellido() != null) {
            nuevo.put("lastName", cuenta.apellido());
        }
        nuevo.putObject("attributes").putArray(atributo).add(String.valueOf(id));
        admin.post().uri("/users").body(nuevo).retrieve().toBodilessEntity();
        return buscarPorEmail(cuenta.email())
                .orElseThrow(() -> new IllegalStateException("Keycloak no devolvió la cuenta recién creada"));
    }

    private ObjectNode leerUsuario(String usuarioId) {
        JsonNode usuario = admin.get().uri("/users/{id}", usuarioId).retrieve().body(JsonNode.class);
        if (!(usuario instanceof ObjectNode objeto)) {
            throw new IllegalStateException("Keycloak no devolvió la cuenta " + usuarioId);
        }
        return objeto;
    }

    private void guardarAtributo(ObjectNode usuario, String atributo, List<String> valores) {
        ObjectNode atributos = usuario.has("attributes") && usuario.get("attributes").isObject()
                ? (ObjectNode) usuario.get("attributes")
                : usuario.putObject("attributes");
        if (valores.isEmpty()) {
            atributos.remove(atributo);
        } else {
            ArrayNode lista = atributos.putArray(atributo);
            valores.forEach(lista::add);
        }
        admin.put().uri("/users/{id}", usuario.get("id").asText()).body(usuario).retrieve().toBodilessEntity();
    }

    private void asignarRol(String usuarioId, String nombreRol) {
        admin.post().uri("/users/{id}/role-mappings/realm", usuarioId)
                .body(List.of(rol(nombreRol))).retrieve().toBodilessEntity();
    }

    private JsonNode rol(String nombre) {
        return roles.computeIfAbsent(nombre, n -> {
            JsonNode rol = admin.get().uri("/roles/{nombre}", n).retrieve().body(JsonNode.class);
            if (rol == null) {
                throw new IllegalStateException("No existe el rol " + n + " en el realm donatrack");
            }
            return mapper.createObjectNode().put("id", rol.get("id").asText()).put("name", n);
        });
    }

    /** Mail de Keycloak con el link para elegir contraseña. Si falla el envío, queda "¿Olvidaste tu contraseña?". */
    private void enviarAcceso(String usuarioId) {
        try {
            admin.put().uri(u -> u.path("/users/{id}/execute-actions-email")
                            .queryParam("client_id", "servicio-web")
                            .queryParam("redirect_uri", webUrl + "/ingresar")
                            .queryParam("lifespan", 7 * 24 * 3600)
                            .build(usuarioId))
                    .body(List.of("UPDATE_PASSWORD")).retrieve().toBodilessEntity();
        } catch (RestClientException e) {
            logger.warn("No se pudo enviar el mail de acceso a la cuenta {}: {}", usuarioId, e.getMessage());
        }
    }

    private static List<String> valores(ObjectNode usuario, String atributo) {
        List<String> valores = new ArrayList<>();
        usuario.path("attributes").path(atributo).forEach(v -> valores.add(v.asText()));
        return valores;
    }

    private void borrar(Path archivo) {
        try {
            Files.deleteIfExists(archivo);
        } catch (IOException e) {
            logger.warn("No se pudo borrar {}: {}", archivo.getFileName(), e.getMessage());
        }
    }

    @SafeVarargs
    private static List<Long> sinRepetidos(List<Long>... listas) {
        Set<Long> ids = new LinkedHashSet<>();
        for (List<Long> lista : listas) {
            ids.addAll(lista);
        }
        return List.copyOf(ids);
    }

    // ---------------- emails de cada registro ----------------

    record Cuenta(String email, String nombre, String apellido) {
    }

    static List<Cuenta> cuentasDelDonante(Donante donante) {
        Map<String, Cuenta> cuentas = new LinkedHashMap<>();
        agregar(cuentas, donante.getPersona());
        if (donante.getPersona() instanceof PersonaJuridica juridica) {
            juridica.getRepresentantes().forEach(r -> agregar(cuentas, r));
        }
        return List.copyOf(cuentas.values());
    }

    static List<Cuenta> cuentasDeLaEntidad(EntidadBeneficiaria entidad) {
        Map<String, Cuenta> cuentas = new LinkedHashMap<>();
        agregar(cuentas, entidad.getEntidad());
        entidad.getEntidad().getRepresentantes().forEach(r -> agregar(cuentas, r));
        return List.copyOf(cuentas.values());
    }

    private static void agregar(Map<String, Cuenta> cuentas, Persona persona) {
        if (persona == null || persona.getMediosContacto() == null) {
            return;
        }
        String nombre = null;
        String apellido = null;
        if (persona instanceof PersonaHumana humana) {
            nombre = humana.getNombre();
            apellido = humana.getApellido();
        } else if (persona instanceof PersonaJuridica juridica) {
            nombre = juridica.getRazonSocial();
        }
        for (MedioContacto medio : persona.getMediosContacto()) {
            if (medio.getTipo() == TipoContacto.EMAIL && medio.getValor() != null && !medio.getValor().isBlank()) {
                String email = medio.getValor().trim().toLowerCase();
                cuentas.putIfAbsent(email, new Cuenta(email, nombre, apellido));
            }
        }
    }
}
