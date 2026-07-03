package ar.edu.utn.frba.dds.donaciones.domain.personas;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import ar.edu.utn.frba.dds.donaciones.domain.donaciones.Donacion;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Donante {
    private Long id;
    private Persona persona;
    private List<Donacion> donaciones;
    private LocalDate ultimaActividad;
    // Evita reenviar la notificación de inactividad todos los días mientras
    // el donante siga inactivo. Se resetea cuando vuelve a donar
    // (ver DonacionService.crear).
    private boolean notificadoInactividad;

    public Donante(Long id, Persona persona) {
        this.id = id;
        this.persona = persona;
        // OJO: antes se llamaba a getUltimaActividad() acá mismo, que en ese
        // punto todavía era null porque el campo no se había asignado. Eso
        // dejaba a todo donante recién creado con ultimaActividad = null,
        // lo cual rompía el cálculo de inactividad (Entrega 2).
        this.ultimaActividad = LocalDate.now();
        this.donaciones = new ArrayList<>();
        this.notificadoInactividad = false;
    }

    public Persona getPersona() {
        return persona;
    }

    public void agregarDonacion(Donacion donacion) {
        donaciones.add(donacion);
        this.ultimaActividad = LocalDate.now();
        this.notificadoInactividad = false;
    }

    public Integer cantidadDonaciones() {
        return donaciones.size();
    }

    public boolean realizoDonaciones() {
        return !donaciones.isEmpty();
    }
}
