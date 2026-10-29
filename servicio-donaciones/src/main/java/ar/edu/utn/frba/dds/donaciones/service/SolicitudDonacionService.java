package ar.edu.utn.frba.dds.donaciones.service;

import ar.edu.utn.frba.dds.donaciones.domain.donaciones.GestorDonaciones;
import org.springframework.stereotype.Service;

@Service
public class SolicitudDonacionService {

    private final GestorDonaciones gestorDonaciones;

    public SolicitudDonacionService(GestorDonaciones gestorDonaciones) {
        this.gestorDonaciones = gestorDonaciones;
    }

    /** Ver GestorDonaciones.eliminarSolicitud(): solo borra si todavía no generó ninguna Donacion. */
    public void eliminar(Long id) {
        gestorDonaciones.eliminarSolicitud(id);
    }
}
