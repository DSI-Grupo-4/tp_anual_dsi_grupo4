package ar.edu.utn.frba.dds.donaciones.controller;

import ar.edu.utn.frba.dds.donaciones.dto.DonanteDTO;
import ar.edu.utn.frba.dds.donaciones.dto.EntidadBeneficiariaDTO;
import ar.edu.utn.frba.dds.donaciones.dto.PersonaHumanaDTO;
import ar.edu.utn.frba.dds.donaciones.dto.PersonaJuridicaDTO;
import ar.edu.utn.frba.dds.donaciones.integracion.CuentasKeycloak;
import ar.edu.utn.frba.dds.donaciones.seguridad.UsuarioToken;
import ar.edu.utn.frba.dds.donaciones.service.RegistroCuentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Cuentas", description = "Vínculo entre las cuentas de Keycloak y los donantes o entidades beneficiarias")
@RestController
@RequestMapping("/api/cuentas")
public class CuentaController {

    private final CuentasKeycloak cuentas;
    private final RegistroCuentaService registro;

    public CuentaController(CuentasKeycloak cuentas, RegistroCuentaService registro) {
        this.cuentas = cuentas;
        this.registro = registro;
    }

    @Operation(summary = "Vincular mi cuenta",
            description = "Busca donantes y entidades cuyo email (propio o de un representante) coincide con el email verificado del token "
                    + "y agrega a la cuenta el rol y el atributo correspondiente. Hay que volver a iniciar sesión para ver los cambios en el token.")
    @PostMapping("/vincular")
    public CuentasKeycloak.Vinculacion vincular(Authentication auth) {
        if (!(auth instanceof JwtAuthenticationToken token)) {
            throw new IllegalStateException("La vinculación de cuentas requiere la seguridad habilitada");
        }
        String email = UsuarioToken.emailVerificado(auth)
                .orElseThrow(() -> new IllegalStateException("La cuenta no tiene un email verificado"));
        return cuentas.vincularUsuario(token.getToken().getSubject(), email);
    }

    @Operation(summary = "Registrar mi usuario como donante",
            description = "Crea un donante humano con el email verificado del token y le asigna el rol DONANTE. "
                    + "No reclama un donante ya cargado ni otorga ADMIN. Hay que volver a iniciar sesión para ver el rol.")
    @PostMapping("/registro/donante")
    @ResponseStatus(HttpStatus.CREATED)
    public DonanteDTO registrarDonante(@Valid @RequestBody PersonaHumanaDTO dto, Authentication auth) {
        if (UsuarioToken.tieneRol(auth, UsuarioToken.DONANTE) || UsuarioToken.donanteId(auth).isPresent()) {
            throw new IllegalStateException("Tu cuenta ya está vinculada a un donante");
        }
        return registro.registrarDonante(sujeto(auth), emailVerificado(auth), dto);
    }

    @Operation(summary = "Registrar una organización donante",
            description = "Crea un donante jurídico con un único representante: el email verificado del token. "
                    + "No reclama una organización ya cargada ni otorga ADMIN. Hay que volver a iniciar sesión para ver el rol.")
    @PostMapping("/registro/donante-juridico")
    @ResponseStatus(HttpStatus.CREATED)
    public DonanteDTO registrarDonanteJuridico(@Valid @RequestBody PersonaJuridicaDTO dto, Authentication auth) {
        if (UsuarioToken.tieneRol(auth, UsuarioToken.DONANTE) || UsuarioToken.donanteId(auth).isPresent()) {
            throw new IllegalStateException("Tu cuenta ya está vinculada a un donante");
        }
        return registro.registrarDonanteJuridico(sujeto(auth), emailVerificado(auth), dto);
    }

    @Operation(summary = "Registrar una entidad nueva",
            description = "Crea una entidad beneficiaria cuyo primer representante es el email verificado del token. "
                    + "No reclama una entidad ya cargada ni otorga ADMIN. Hay que volver a iniciar sesión para ver el rol.")
    @PostMapping("/registro/entidad")
    @ResponseStatus(HttpStatus.CREATED)
    public EntidadBeneficiariaDTO registrarEntidad(@Valid @RequestBody EntidadBeneficiariaDTO dto, Authentication auth) {
        if (UsuarioToken.tieneRol(auth, UsuarioToken.BENEFICIARIA) || !UsuarioToken.entidadIds(auth).isEmpty()) {
            throw new IllegalStateException(
                    "Tu cuenta ya representa a una entidad. Otra entidad la carga un administrador, o un representante autorizado suma tu correo.");
        }
        return registro.registrarEntidad(sujeto(auth), emailVerificado(auth), dto);
    }

    private static String sujeto(Authentication auth) {
        if (auth instanceof JwtAuthenticationToken token) {
            return token.getToken().getSubject();
        }
        throw new IllegalStateException("El autorregistro requiere la seguridad habilitada");
    }

    private static String emailVerificado(Authentication auth) {
        return UsuarioToken.emailVerificado(auth)
                .orElseThrow(() -> new IllegalStateException("La cuenta no tiene un email verificado"));
    }

    @Operation(summary = "Sincronizar todas las cuentas",
            description = "Encola todos los donantes y entidades para crear o completar sus cuentas en Keycloak. "
                    + "Sirve para los datos cargados antes de habilitar la seguridad.")
    @PostMapping("/sincronizar")
    public Map<String, Integer> sincronizar() {
        return Map.of("encolados", cuentas.encolarTodos());
    }
}
