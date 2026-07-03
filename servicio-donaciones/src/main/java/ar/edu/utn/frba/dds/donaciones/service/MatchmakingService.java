package ar.edu.utn.frba.dds.donaciones.service;

import ar.edu.utn.frba.dds.donaciones.domain.donaciones.Donacion;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.EstadoTrack;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.GestorDonaciones;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.ResultadoMatchmaking;
import ar.edu.utn.frba.dds.donaciones.domain.personas.Donante;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Service
public class MatchmakingService {

    private final EntidadBeneficiariaService entidadBeneficiariaService;
    private final NotificacionClienteService notificacionClienteService;
    private final GestorDonaciones gestorDonaciones = new GestorDonaciones();

    public MatchmakingService(
            EntidadBeneficiariaService entidadBeneficiariaService,
            NotificacionClienteService notificacionClienteService) {
        this.entidadBeneficiariaService = entidadBeneficiariaService;
        this.notificacionClienteService = notificacionClienteService;
    }

    public List<EntidadBeneficiaria> ejecutarMatchmaking(Donacion donacion) {
        ResultadoMatchmaking resultado = gestorDonaciones.ejecutarMatchmaking(
                donacion,
                entidadBeneficiariaService.obtenerEntidadesDominio()
        );

        if (!resultado.getInterseccion().isEmpty()) {
            return resultado.getInterseccion();
        }

        return Stream.concat(
                        resultado.getPorCompatibilidad().stream(),
                        resultado.getPorSubatencion().stream())
                .distinct()
                .limit(10)
                .toList();
    }

    public void confirmarAsignacion(Donacion donacion, EntidadBeneficiaria entidad) {
        donacion.cambiarEstado(EstadoTrack.ASIGNACION_REALIZADA, null);
        donacion.setEntidadBeneficiaria(entidad);

        notificarAsignacion(donacion, entidad);
    }

    /**
     * Dispara las dos notificaciones que el enunciado pide para este evento
     * (Entrega 2 - "Eventos e integración con medios de notificación"):
     *   - A la entidad beneficiaria, porque se le asignó una donación en
     *     base a sus necesidades.
     *   - Al donante, porque la donación que hizo acaba de ser asignada.
     *
     * Antes de este cambio, confirmarAsignacion() solo actualizaba el estado
     * de la donación y nunca avisaba a nadie del sistema de notificaciones.
     */
    private void notificarAsignacion(Donacion donacion, EntidadBeneficiaria entidad) {
        String descripcionItem = donacion.getItemDonado() != null
                ? donacion.getItemDonado().getDescripcion()
                : "una donación";

        if (entidad.getEntidad() != null) {
            String mensajeEntidad = "Se te asignó una nueva donación (" + descripcionItem
                    + ") en base a tus necesidades registradas.";
            notificacionClienteService.notificarPersona(entidad.getEntidad(), mensajeEntidad);
        }

        Donante donante = donacion.getDonante();
        if (donante != null && donante.getPersona() != null) {
            String nombreEntidad = entidad.getEntidad() != null
                    ? entidad.getEntidad().getRazonSocial()
                    : "una entidad beneficiaria";
            String mensajeDonante = "¡Gracias! Tu donación (" + descripcionItem
                    + ") fue asignada a " + nombreEntidad + ".";
            notificacionClienteService.notificarPersona(donante.getPersona(), mensajeDonante);
        }
    }
}
