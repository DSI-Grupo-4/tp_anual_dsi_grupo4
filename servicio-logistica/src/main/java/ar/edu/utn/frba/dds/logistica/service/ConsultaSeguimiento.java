package ar.edu.utn.frba.dds.logistica.service;

import ar.edu.utn.frba.dds.logistica.domain.rutas.Camion;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Entrega;
import ar.edu.utn.frba.dds.logistica.domain.rutas.EstadoEntrega;
import ar.edu.utn.frba.dds.logistica.domain.rutas.EstadoRuta;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Parada;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Ruta;
import ar.edu.utn.frba.dds.logistica.dto.CamionEnCursoDTO;
import ar.edu.utn.frba.dds.logistica.dto.EnvioPropioDTO;
import ar.edu.utn.frba.dds.logistica.dto.EnvioPropioDTO.EntregaVista;
import ar.edu.utn.frba.dds.logistica.dto.EnvioPropioDTO.FotoVista;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Arma las vistas acotadas de seguimiento. No devuelve la ruta completa. */
public final class ConsultaSeguimiento {

    private ConsultaSeguimiento() {
    }

    public static List<EnvioPropioDTO> envios(List<Ruta> rutas, long entidadId) {
        List<EnvioPropioDTO> envios = new ArrayList<>();
        for (Ruta ruta : rutas) {
            if (ruta.getEstadoRuta() == EstadoRuta.CANCELADA) {
                continue;
            }
            List<Parada> paradas = ruta.getParadas() == null ? List.of() : ruta.getParadas();
            for (Parada parada : paradas) {
                Integer entidad = parada.getIdEntidadBeneficiariaAsociada();
                if (entidad == null || entidad.longValue() != entidadId) {
                    continue;
                }
                Camion camion = ruta.getCamionAsociado();
                boolean ubicado = camion != null && camion.getLatitud() != null && camion.getLongitud() != null;
                envios.add(new EnvioPropioDTO(
                        ruta.getIdRuta(),
                        parada.getIdParada(),
                        parada.getNumeroParada(),
                        paradas.size(),
                        ruta.getFecha(),
                        ruta.getEstadoRuta() == null ? null : ruta.getEstadoRuta().name(),
                        camion == null ? null : camion.getPatente(),
                        ubicado ? camion.getLatitud() : null,
                        ubicado ? camion.getLongitud() : null,
                        ubicado ? camion.getUbicacionActualizadaEn() : null,
                        ubicado && Boolean.TRUE.equals(camion.getUbicacionSimulada()),
                        entregas(parada)));
            }
        }
        return envios;
    }

    public static List<CamionEnCursoDTO> camiones(List<Ruta> rutas, Set<Integer> idsDonacion) {
        List<CamionEnCursoDTO> camiones = new ArrayList<>();
        for (Ruta ruta : rutas) {
            Camion camion = ruta.getCamionAsociado();
            if (ruta.getEstadoRuta() != EstadoRuta.INICIADA || camion == null
                    || camion.getLatitud() == null || camion.getLongitud() == null) {
                continue;
            }
            int llevadas = 0;
            for (Entrega entrega : ruta.getParadas() == null ? List.<Entrega>of() : ruta.getParadas().stream()
                    .flatMap(parada -> (parada.getEntregas() == null ? List.<Entrega>of() : parada.getEntregas()).stream())
                    .toList()) {
                if (entrega.getEstadoEntrega() == EstadoEntrega.EN_TRASLADO
                        && entrega.getIdDonacionAsociada() != null
                        && idsDonacion.contains(entrega.getIdDonacionAsociada())) {
                    llevadas++;
                }
            }
            if (llevadas > 0) {
                camiones.add(new CamionEnCursoDTO(
                        camion.getPatente(), camion.getLatitud(), camion.getLongitud(),
                        camion.getUbicacionActualizadaEn(), Boolean.TRUE.equals(camion.getUbicacionSimulada()), llevadas));
            }
        }
        return camiones;
    }

    private static List<EntregaVista> entregas(Parada parada) {
        if (parada.getEntregas() == null) {
            return List.of();
        }
        return parada.getEntregas().stream().map(entrega -> new EntregaVista(
                entrega.getIdEntrega(),
                entrega.getIdDonacionAsociada(),
                entrega.getEstadoEntrega() == null ? null : entrega.getEstadoEntrega().name(),
                entrega.getFechaHoraEntrega(),
                entrega.getJustificacionFallo(),
                entrega.getFotos() == null ? List.of() : entrega.getFotos().stream()
                        .map(foto -> new FotoVista(foto.getUrl(), foto.getFecha()))
                        .toList())).toList();
    }
}
