package ar.edu.utn.frba.dds.donaciones.domain.donaciones;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SolicitudDonacion {
    private String descripcion;
    private LocalDateTime fechaRegistro;
    private List<ItemDonado> items;

    public SolicitudDonacion(String descripcion) {

        this.descripcion = descripcion;
        this.fechaRegistro = LocalDateTime.now();

        this.items = new ArrayList<>();
    }

    public void agregarItem(ItemDonado item) {
        items.add(item);
    }

    public List<Donacion> segmentar(){
        List<Donacion> Donacion = List.of();
        return Donacion;
    }
}
