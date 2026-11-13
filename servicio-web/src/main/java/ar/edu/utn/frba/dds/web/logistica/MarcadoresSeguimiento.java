package ar.edu.utn.frba.dds.web.logistica;

import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteApi.CamionEnCurso;
import ar.edu.utn.frba.dds.web.client.logistica.CamionApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutaApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutaApi.DireccionApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutaApi.ParadaApi;
import ar.edu.utn.frba.dds.web.mapa.Marcador;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * Convierte los datos de Logística al contrato de marcadores del componente de mapa.
 * Solo se dibujan los camiones que ya informaron su ubicación: nunca se inventa una posición.
 */
@Component
public class MarcadoresSeguimiento {

    private static final String INICIADA = "INICIADA";
    private static final String EN_TRASLADO = "EN_TRASLADO";

    private final MessageSource mensajes;

    public MarcadoresSeguimiento(MessageSource mensajes) {
        this.mensajes = mensajes;
    }

    /** Administración, lista: el camión de cada ruta en curso que ya informó su ubicación. */
    public List<Marcador> camionesEnCurso(List<RutaApi> rutas) {
        return rutas.stream()
                .filter(r -> INICIADA.equals(r.estadoRuta()))
                .map(this::camion)
                .flatMap(Optional::stream)
                .toList();
    }

    /** Administración, detalle: el camión de la ruta y las paradas que tienen coordenadas (como sedes de las entidades). */
    public List<Marcador> deRuta(RutaApi ruta, Map<Integer, String> nombresDeEntidades) {
        List<Marcador> marcadores = new ArrayList<>();
        camion(ruta).ifPresent(marcadores::add);
        for (ParadaApi parada : ruta.paradas()) {
            DireccionApi direccion = parada.direccion();
            if (direccion != null && direccion.tieneCoordenadas()) {
                String entidad = nombresDeEntidades.get(parada.idEntidadBeneficiariaAsociada());
                if (entidad == null) {
                    entidad = texto("logistica.seguimiento.entidad-sin-nombre", parada.idEntidadBeneficiariaAsociada());
                }
                marcadores.add(new Marcador(direccion.latitud(), direccion.longitud(),
                        texto("logistica.mapa.parada", parada.numeroParada(), entidad),
                        direccion.texto(), null, Marcador.SEDE));
            }
        }
        return marcadores;
    }

    /** Entidad: solo los camiones de las rutas en curso que le llevan algo, sin las otras paradas. */
    public List<Marcador> paraEntidad(List<EnvioEntidad> envios) {
        return envios.stream()
                .filter(e -> INICIADA.equals(e.estadoRuta()) && e.tieneUbicacion())
                .map(e -> Marcador.de(e.latitud(), e.longitud(),
                        texto("logistica.mapa.camion", e.patente()),
                        texto("logistica.mapa.detalle-parada", e.idRuta(), e.numeroParada(), e.totalParadas())
                                + (e.ubicacionSimulada() ? " · " + texto("logistica.mapa.simulada") : ""),
                        e.ubicacionActualizadaEn(), Marcador.CAMION))
                .toList();
    }

    /** Donante: los camiones que Donaciones ya limitó a sus donaciones. */
    public List<Marcador> deCamiones(List<CamionEnCurso> camiones) {
        if (camiones == null) {
            return List.of();
        }
        return camiones.stream()
                .filter(c -> c.latitud() != null && c.longitud() != null)
                .map(c -> Marcador.de(c.latitud(), c.longitud(),
                        texto("logistica.mapa.camion", c.patente()),
                        texto("logistica.mapa.detalle-donante", c.donacionesEnTraslado() == null ? 0 : c.donacionesEnTraslado())
                                + (Boolean.TRUE.equals(c.ubicacionSimulada()) ? " · " + texto("logistica.mapa.simulada") : ""),
                        c.ubicacionActualizadaEn(), Marcador.CAMION))
                .toList();
    }

    public List<Marcador> paraDonaciones(Collection<? extends Number> idsDeDonacion, List<RutaApi> rutas) {
        Set<Long> propias = new HashSet<>();
        idsDeDonacion.forEach(id -> propias.add(id.longValue()));
        List<Marcador> resultado = new ArrayList<>();
        for (RutaApi ruta : rutas) {
            CamionApi camion = ruta.camionAsociado();
            if (!INICIADA.equals(ruta.estadoRuta()) || camion == null || !camion.tieneUbicacion()) {
                continue;
            }
            long llevadas = ruta.entregas().stream()
                    .filter(e -> EN_TRASLADO.equals(e.estadoEntrega()) && e.idDonacionAsociada() != null
                            && propias.contains(e.idDonacionAsociada().longValue()))
                    .count();
            if (llevadas > 0) {
                resultado.add(Marcador.de(camion.latitud(), camion.longitud(),
                        texto("logistica.mapa.camion", camion.patente()),
                        texto("logistica.mapa.detalle-donante", llevadas)
                                + (camion.simulada() ? " · " + texto("logistica.mapa.simulada") : ""),
                        camion.ubicacionActualizadaEn(), Marcador.CAMION));
            }
        }
        return resultado;
    }

    private Optional<Marcador> camion(RutaApi ruta) {
        CamionApi camion = ruta.camionAsociado();
        if (camion == null || !camion.tieneUbicacion()) {
            return Optional.empty();
        }
        long pendientes = ruta.entregas().stream().filter(e -> EN_TRASLADO.equals(e.estadoEntrega())).count();
        String detalle = texto("logistica.mapa.detalle-ruta", ruta.idRuta(), pendientes)
                + (camion.simulada() ? " · " + texto("logistica.mapa.simulada") : "");
        return Optional.of(Marcador.de(camion.latitud(), camion.longitud(),
                texto("logistica.mapa.camion", camion.patente()), detalle, camion.ubicacionActualizadaEn(), Marcador.CAMION));
    }

    private String texto(String clave, Object... argumentos) {
        return mensajes.getMessage(clave, argumentos, LocaleContextHolder.getLocale());
    }
}
