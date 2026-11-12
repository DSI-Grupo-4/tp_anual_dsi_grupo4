package ar.edu.utn.frba.dds.logistica.domain.geo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

class GeoTest {

    private static final Punto OBELISCO = new Punto(-34.6037, -58.3816);
    private static final Punto LA_PLATA = new Punto(-34.9215, -57.9545);

    @Test
    void ladistanciaEntreBuenosAiresYLaPlataEsDeUnos53Km() {
        assertThat(Geo.distanciaKm(OBELISCO, LA_PLATA)).isBetween(50.0, 55.0);
    }

    @Test
    void laDistanciaDeUnPuntoAConsigoMismoEsCero() {
        assertThat(Geo.distanciaKm(OBELISCO, OBELISCO)).isZero();
    }

    @Test
    void avanzarUnCuartoDelCaminoDejaElPuntoAUnCuartoDelTrayecto() {
        Punto p = Geo.avanzar(OBELISCO, LA_PLATA, 0.25);

        assertThat(p.latitud()).isCloseTo(-34.6037 + 0.25 * (-34.9215 + 34.6037), within(1e-9));
        assertThat(p.longitud()).isCloseTo(-58.3816 + 0.25 * (-57.9545 + 58.3816), within(1e-9));
    }

    @Test
    void laFraccionSeAcotaEntreCeroYUno() {
        assertThat(Geo.avanzar(OBELISCO, LA_PLATA, 5.0)).isEqualTo(LA_PLATA);
        assertThat(Geo.avanzar(OBELISCO, LA_PLATA, -1.0)).isEqualTo(OBELISCO);
    }

    @Test
    void elPuntoDeEjemploEsSiempreElMismoParaLaMismaEntidadYQuedaCercaDelDeposito() {
        Punto a = Geo.puntoDeEjemplo(7, OBELISCO);
        Punto b = Geo.puntoDeEjemplo(7, OBELISCO);
        Punto otro = Geo.puntoDeEjemplo(8, OBELISCO);

        assertThat(a).isEqualTo(b);
        assertThat(a).isNotEqualTo(otro);
        assertThat(Math.abs(a.latitud() - OBELISCO.latitud())).isLessThanOrEqualTo(0.09);
        assertThat(Math.abs(a.longitud() - OBELISCO.longitud())).isLessThanOrEqualTo(0.09);
    }

    @Test
    void unaEntidadSinIdTambienTieneUnPuntoDeEjemplo() {
        assertThat(Geo.puntoDeEjemplo(null, OBELISCO)).isNotNull();
    }
}
