package ar.edu.utn.frba.dds.web.beneficiaria;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.util.ReflectionTestUtils;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class EntidadActualTest {
    @AfterEach void limpiar() { SecurityContextHolder.clearContext(); }

    @Test void usaUnaEntidadVinculadaEnLugarDelIdFijo() {
        var usuario = mock(OidcUser.class);
        when(usuario.getClaim("entidad_ids")).thenReturn(List.of("27", "28"));
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(usuario, "", "ROLE_BENEFICIARIA"));
        assertThat(new EntidadActualConfigurada(1L).id()).isEqualTo(27L);
    }

    @Test void unaCuentaSinVinculoNoUsaOtraEntidad() {
        var usuario = mock(OidcUser.class);
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(usuario, "", "ROLE_BENEFICIARIA"));
        assertThatThrownBy(() -> new EntidadActualConfigurada(1L).id()).isInstanceOf(AccessDeniedException.class);
    }

    @Test void elIdFijoSoloEstaDisponibleSinSeguridad() {
        var actual = new EntidadActualConfigurada(1L);
        assertThat(actual.id()).isEqualTo(1L);
        ReflectionTestUtils.setField(actual, "seguridadHabilitada", true);
        assertThatThrownBy(actual::id).isInstanceOf(AccessDeniedException.class);
    }
}
