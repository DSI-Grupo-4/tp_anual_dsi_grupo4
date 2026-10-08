package ar.edu.utn.frba.dds.donaciones.domain.donaciones;

import ar.edu.utn.frba.dds.donaciones.domain.personas.Donante;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class SolicitudDonacion {
    private String descripcion;
    private LocalDateTime fechaRegistro;
    private List<ItemDonado> items;
    // Opcional a nivel constructor para no romper los tests existentes que
    // no necesitan donante — DonacionService.crear() lo setea siempre.
    private Donante donante;

    public SolicitudDonacion(String descripcion) {

        this.descripcion = descripcion;
        this.fechaRegistro = LocalDateTime.now();

        this.items = new ArrayList<>();
    }

    public void agregarItem(ItemDonado item) {
        items.add(item);
    }

    /**
     * Segmenta la carga única en donaciones independientes: cada ItemDonado
     * ya llega con su propia subcategoría (y, si es perecedero, su propio
     * valor de fechaVencimiento vía AtributoValor), así que alcanza con
     * generar una Donacion por ítem para que cada donación resultante
     * quede asociada a una única subcategoría y que los perecederos con
     * vencimientos distintos queden en donaciones separadas. No se
     * fusionan ítems de igual subcategoría entre sí: fusionar
     * cantidades/fotos de ítems distintos agregaría una regla no pedida
     * explícitamente.
     */
    public List<Donacion> segmentar() {
        return items.stream()
                .map(item -> {
                    Donacion donacion = new Donacion(null, item, item.getCantidad());
                    donacion.setDonante(donante);
                    return donacion;
                })
                .collect(Collectors.toList());
    }
}
