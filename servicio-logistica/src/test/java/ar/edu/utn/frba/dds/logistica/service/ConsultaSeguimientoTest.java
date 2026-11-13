package ar.edu.utn.frba.dds.logistica.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.logistica.controller.SeguimientoConsultaController;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Camion;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Direccion;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Entrega;
import ar.edu.utn.frba.dds.logistica.domain.rutas.EstadoRuta;
import ar.edu.utn.frba.dds.logistica.domain.rutas.GestorRutas;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Parada;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Ruta;
import ar.edu.utn.frba.dds.logistica.dto.EnvioPropioDTO;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

class ConsultaSeguimientoTest {

    @Test
    void unaEntidadNoVeLasParadasNiLaCalleDeLasOtras() {
        Entrega propia = new Entrega(2, 102, 5, null, LocalDate.of(2026, 11, 12), 1, 1, 1);
        propia.iniciarTraslado();
        Entrega ajena = new Entrega(1, 101, 9, null, LocalDate.of(2026, 11, 12), 1, 1, 1);
        ajena.iniciarTraslado();
        Parada deOtra = new Parada(1, 9, new Direccion("Calle Ajena", "1", null), List.of(ajena));
        Parada deLaEntidad = new Parada(2, 5, new Direccion("Calle Propia", "2", null), List.of(propia));
        Ruta ruta = new Ruta(7, camion(), null, LocalDate.of(2026, 11, 12), List.of(deOtra, deLaEntidad));
        ruta.setEstadoRuta(EstadoRuta.INICIADA);
        Ruta cancelada = new Ruta(8, camion(), null, LocalDate.of(2026, 11, 12),
                List.of(new Parada(1, 5, new Direccion("No mostrar", "3", null), List.of())));
        cancelada.setEstadoRuta(EstadoRuta.CANCELADA);

        List<EnvioPropioDTO> envios = ConsultaSeguimiento.envios(List.of(ruta, cancelada), 5L);

        assertThat(envios).hasSize(1);
        assertThat(envios.get(0).idParada()).isEqualTo(2);
        assertThat(envios.get(0).entregas()).extracting(EnvioPropioDTO.EntregaVista::idDonacionAsociada).containsExactly(102);
        assertThat(envios.get(0).toString()).doesNotContain("Calle Ajena", "Calle Propia");
        assertThat(ConsultaSeguimiento.envios(List.of(ruta), 9L))
                .extracting(EnvioPropioDTO::idParada).containsExactly(1);
    }

    @Test
    void elCamionSoloCuentaLasDonacionesPedidas() {
        Entrega mia = new Entrega(2, 15, 5, null, LocalDate.of(2026, 11, 12), 1, 1, 1);
        mia.iniciarTraslado();
        Entrega ajena = new Entrega(3, 999, 5, null, LocalDate.of(2026, 11, 12), 1, 1, 1);
        ajena.iniciarTraslado();
        Ruta ruta = new Ruta(7, camion(), null, LocalDate.of(2026, 11, 12),
                List.of(new Parada(1, 5, null, List.of(mia, ajena))));
        ruta.setEstadoRuta(EstadoRuta.INICIADA);

        var camiones = ConsultaSeguimiento.camiones(List.of(ruta), Set.of(15));

        assertThat(camiones).hasSize(1);
        assertThat(camiones.get(0).donacionesEnTraslado()).isEqualTo(1);
        assertThat(camiones.get(0).patente()).isEqualTo("AA123BB");
        assertThat(ConsultaSeguimiento.camiones(List.of(ruta), Set.of(404))).isEmpty();
    }

    @Test
    void unaBeneficiariaNoConsultaLosEnviosDeOtraEntidad() {
        GestorRutas rutas = Mockito.mock(GestorRutas.class);
        when(rutas.getRutas()).thenReturn(List.of());
        SeguimientoConsultaController controller = new SeguimientoConsultaController(rutas);
        JwtAuthenticationToken ajena = token(List.of("9"));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> controller.envios(5L, ajena))
                .isInstanceOf(ResponseStatusException.class);
        assertThat(controller.envios(9L, ajena)).isEmpty();
    }

    private static Camion camion() {
        Camion camion = new Camion();
        camion.setPatente("AA123BB");
        camion.setLatitud(-34.6);
        camion.setLongitud(-58.4);
        camion.setUbicacionActualizadaEn(LocalDateTime.of(2026, 11, 12, 18, 0));
        camion.setUbicacionSimulada(false);
        return camion;
    }

    private static JwtAuthenticationToken token(List<String> entidades) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .issuedAt(Instant.parse("2026-11-13T00:00:00Z"))
                .claim("entidad_ids", entidades)
                .build();
        return new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_BENEFICIARIA")));
    }
}
