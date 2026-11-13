package ar.edu.utn.frba.dds.logistica.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.logistica.domain.rutas.EstadoEntrega;
import ar.edu.utn.frba.dds.logistica.repository.EntregaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

class ControlDeFotosTest {

    private static final String ID = "6730a1f2c3d4e5f607182930";
    private static final String URL = "http://logistica.ejemplo.org/api/fotos/" + ID;

    private final FotoService fotos = mock(FotoService.class);
    private final EntregaRepository entregas = mock(EntregaRepository.class);
    private final ControlDeFotos control = new ControlDeFotos(fotos, entregas);

    private static JwtAuthenticationToken token(String sujeto, String rol) {
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").subject(sujeto)
                .issuedAt(Instant.parse("2026-11-13T00:00:00Z")).build();
        return new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_" + rol)));
    }

    // ---- quien confirma solo puede usar sus propias fotos ----

    @Test
    void laBeneficiariaPuedeConfirmarConUnaFotoQueSubioElla() {
        when(fotos.idDeUrl(URL)).thenReturn(Optional.of(ID));
        when(fotos.subidaPor(ID)).thenReturn(Optional.of("cuenta-1"));
        when(entregas.existsByFotosUrl(URL)).thenReturn(false);

        assertThatCode(() -> control.exigirPropias(token("cuenta-1", "BENEFICIARIA"), List.of(URL))).doesNotThrowAnyException();
    }

    @Test
    void unaFotoSubidaPorOtraCuentaEsUn403() {
        when(fotos.idDeUrl(URL)).thenReturn(Optional.of(ID));
        when(fotos.subidaPor(ID)).thenReturn(Optional.of("cuenta-de-otra-entidad"));

        assertThatThrownBy(() -> control.exigirPropias(token("cuenta-1", "BENEFICIARIA"), List.of(URL)))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void unaFotoSinDuenoRegistradoNoSePuedeUsar() {
        when(fotos.idDeUrl(URL)).thenReturn(Optional.of(ID));
        when(fotos.subidaPor(ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> control.exigirPropias(token("cuenta-1", "BENEFICIARIA"), List.of(URL)))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void unaUrlQueNoEsDeUnaFotoSubidaAEstaApiSeRechaza() {
        when(fotos.idDeUrl("https://sitio.externo/foto.jpg")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> control.exigirPropias(token("cuenta-1", "BENEFICIARIA"), List.of("https://sitio.externo/foto.jpg")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void unaFotoYaAsociadaAUnaEntregaNoSeReutiliza() {
        when(fotos.idDeUrl(URL)).thenReturn(Optional.of(ID));
        when(fotos.subidaPor(ID)).thenReturn(Optional.of("cuenta-1"));
        when(entregas.existsByFotosUrl(URL)).thenReturn(true);

        assertThatThrownBy(() -> control.exigirPropias(token("cuenta-1", "BENEFICIARIA"), List.of(URL)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void unaPersonaAdministradoraNoPasaPorElControlDeLaSubida() {
        assertThatCode(() -> control.exigirPropias(token("admin", "ADMIN"), List.of("https://cualquiera/foto.jpg")))
                .doesNotThrowAnyException();
        verify(fotos, never()).idDeUrl(anyString());
    }

    @Test
    void sinTokenDeKeycloakNoSeControla() {
        assertThatCode(() -> control.exigirPropias(null, List.of(URL))).doesNotThrowAnyException();
        assertThatCode(() -> control.exigirPropias(
                new UsernamePasswordAuthenticationToken("anonimo", "x"), List.of(URL))).doesNotThrowAnyException();
        verify(fotos, never()).idDeUrl(anyString());
    }

    // ---- una foto es publica solo si esta en una entrega ENTREGADA ----

    @Test
    void unaFotoDeUnaEntregaEntregadaEsPublica() {
        when(entregas.existsByEstadoEntregaAndFotosUrlEndingWith(EstadoEntrega.ENTREGADA, "/api/fotos/" + ID)).thenReturn(true);

        assertThat(control.esPublica(ID)).isTrue();
    }

    @Test
    void unaFotoSubidaPeroSinConfirmarNoEsPublica() {
        when(entregas.existsByEstadoEntregaAndFotosUrlEndingWith(EstadoEntrega.ENTREGADA, "/api/fotos/" + ID)).thenReturn(false);

        assertThat(control.esPublica(ID)).isFalse();
    }

    @Test
    void unIdInvalidoNoEsPublicoNiConsultaLaBase() {
        assertThat(control.esPublica("no-es-un-id")).isFalse();
        assertThat(control.esPublica(null)).isFalse();
        verify(entregas, never()).existsByEstadoEntregaAndFotosUrlEndingWith(
                org.mockito.ArgumentMatchers.any(), anyString());
    }
}
