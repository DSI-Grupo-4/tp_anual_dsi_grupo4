package ar.edu.utn.frba.dds.donaciones.controller;

import ar.edu.utn.frba.dds.donaciones.integracion.CuentasKeycloak;
import ar.edu.utn.frba.dds.donaciones.seguridad.UsuarioToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Cuentas", description = "Vínculo entre las cuentas de Keycloak y los donantes o entidades beneficiarias")
@RestController
@RequestMapping("/api/cuentas")
public class CuentaController {

    private final CuentasKeycloak cuentas;

    public CuentaController(CuentasKeycloak cuentas) {
        this.cuentas = cuentas;
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

    @Operation(summary = "Sincronizar todas las cuentas",
            description = "Encola todos los donantes y entidades para crear o completar sus cuentas en Keycloak. "
                    + "Sirve para los datos cargados antes de habilitar la seguridad.")
    @PostMapping("/sincronizar")
    public Map<String, Integer> sincronizar() {
        return Map.of("encolados", cuentas.encolarTodos());
    }
}
