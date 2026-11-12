package ar.edu.utn.frba.dds.logistica.service;

import ar.edu.utn.frba.dds.logistica.domain.geo.Geo;
import ar.edu.utn.frba.dds.logistica.domain.geo.Punto;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Camion;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Direccion;
import ar.edu.utn.frba.dds.logistica.domain.rutas.EstadoEntrega;
import ar.edu.utn.frba.dds.logistica.domain.rutas.EstadoRuta;
import ar.edu.utn.frba.dds.logistica.domain.rutas.GestorRutas;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Parada;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Ruta;
import ar.edu.utn.frba.dds.logistica.dto.SimulacionAvanceDTO;
import ar.edu.utn.frba.dds.logistica.dto.UbicacionCamionDTO;
import ar.edu.utn.frba.dds.logistica.repository.CamionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.Optional;

/**
 * Simulador de la demo: en cada paso mueve el camión de una ruta en curso hacia su próxima parada pendiente.
 * No confirma entregas ni cambia estados: solo actualiza la ubicación del camión (marcada como simulada).
 *
 * Si la parada tiene coordenadas, avanza hacia ellas. Si no, y mientras estén habilitados, usa un punto de
 * ejemplo cercano al depósito (el mismo siempre para la misma entidad); si no están habilitados, rechaza el paso.
 */
@Service
public class SimuladorRutaService {

    private final GestorRutas gestorRutas;
    private final CamionRepository camionRepository;
    private final Punto deposito;
    private final double fraccionPorPaso;
    private final double umbralLlegadaKm;
    private final boolean usarPuntosDeEjemplo;

    public SimuladorRutaService(GestorRutas gestorRutas,
                                CamionRepository camionRepository,
                                @Value("${logistica.simulador.deposito-lat:-34.6037}") double depositoLat,
                                @Value("${logistica.simulador.deposito-lng:-58.3816}") double depositoLng,
                                @Value("${logistica.simulador.fraccion-por-paso:0.25}") double fraccionPorPaso,
                                @Value("${logistica.simulador.umbral-llegada-km:0.15}") double umbralLlegadaKm,
                                @Value("${logistica.simulador.usar-puntos-de-ejemplo:true}") boolean usarPuntosDeEjemplo) {
        this.gestorRutas = gestorRutas;
        this.camionRepository = camionRepository;
        this.deposito = new Punto(depositoLat, depositoLng);
        this.fraccionPorPaso = fraccionPorPaso;
        this.umbralLlegadaKm = umbralLlegadaKm;
        this.usarPuntosDeEjemplo = usarPuntosDeEjemplo;
    }

    public SimulacionAvanceDTO avanzar(Integer idRuta) {
        Ruta ruta = gestorRutas.buscarPorId(idRuta);
        if (ruta.getEstadoRuta() != EstadoRuta.INICIADA) {
            throw new IllegalStateException("La ruta " + idRuta + " no está en curso: solo se simula el avance de rutas INICIADAS");
        }
        Parada destino = proximaParadaPendiente(ruta)
                .orElseThrow(() -> new IllegalStateException("La ruta " + idRuta + " no tiene paradas pendientes de entrega"));

        boolean deEjemplo = !tieneCoordenadas(destino);
        Punto puntoDestino = puntoDe(destino);

        Camion camion = ruta.getCamionAsociado();
        Punto origen = camion.tieneUbicacion() ? new Punto(camion.getLatitud(), camion.getLongitud()) : deposito;

        Punto nuevo = Geo.avanzar(origen, puntoDestino, fraccionPorPaso);
        boolean llego = Geo.distanciaKm(nuevo, puntoDestino) <= umbralLlegadaKm;
        if (llego) {
            nuevo = puntoDestino;
        }
        camion.reportarUbicacion(nuevo.latitud(), nuevo.longitud(), LocalDateTime.now(ZoneOffset.UTC), true);
        camionRepository.save(camion); // Mongo no hace dirty checking: se guarda explícitamente

        double restante = llego ? 0.0 : Geo.distanciaKm(nuevo, puntoDestino);
        return new SimulacionAvanceDTO(UbicacionCamionDTO.de(camion), destino.getNumeroParada(), restante, llego, deEjemplo);
    }

    /** La primera parada, por número, que todavía tiene entregas en traslado. */
    private Optional<Parada> proximaParadaPendiente(Ruta ruta) {
        return ruta.getParadas().stream()
                .filter(p -> p.getEntregas().stream().anyMatch(e -> e.getEstadoEntrega() == EstadoEntrega.EN_TRASLADO))
                .min(Comparator.comparing(Parada::getNumeroParada, Comparator.nullsLast(Comparator.naturalOrder())));
    }

    private static boolean tieneCoordenadas(Parada parada) {
        Direccion direccion = parada.getDireccion();
        return direccion != null && direccion.tieneCoordenadas();
    }

    private Punto puntoDe(Parada parada) {
        if (tieneCoordenadas(parada)) {
            return new Punto(parada.getDireccion().getLatitud(), parada.getDireccion().getLongitud());
        }
        if (usarPuntosDeEjemplo) {
            return Geo.puntoDeEjemplo(parada.getIdEntidadBeneficiariaAsociada(), deposito);
        }
        throw new IllegalStateException("La parada " + parada.getNumeroParada()
                + " no tiene coordenadas: cargá la ubicación de la sede de la entidad para poder simular el avance");
    }
}
