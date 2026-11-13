package ar.edu.utn.frba.dds.web.donante;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class DonanteActualTest {
    @AfterEach void limpiar() { SecurityContextHolder.clearContext(); }

    @Test void usaElDonanteDeLaCuentaEnLugarDelProvisorio() {
        var usuario = mock(OidcUser.class);
        when(usuario.getClaim("donante_id")).thenReturn("27");
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(usuario, "", "ROLE_DONANTE"));
        assertThat(new DonanteActualProvisorio(1).id()).isEqualTo(27);
    }

    @Test void unaCuentaSinVinculoNoUsaElDonanteDeOtraPersona() {
        var usuario = mock(OidcUser.class);
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(usuario, "", "ROLE_DONANTE"));
        assertThatThrownBy(() -> new DonanteActualProvisorio(1).id()).isInstanceOf(AccessDeniedException.class);
    }

    @Test void elProvisorioSoloEstaDisponibleSinSeguridad() {
        var actual = new DonanteActualProvisorio(1);
        assertThat(actual.id()).isEqualTo(1);
        ReflectionTestUtils.setField(actual, "seguridadHabilitada", true);
        assertThatThrownBy(actual::id).isInstanceOf(AccessDeniedException.class);
    }
}
