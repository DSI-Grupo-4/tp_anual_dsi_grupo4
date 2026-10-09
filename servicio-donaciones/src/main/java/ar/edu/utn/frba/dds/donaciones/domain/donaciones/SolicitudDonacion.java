package ar.edu.utn.frba.dds.donaciones.domain.donaciones;
import ar.edu.utn.frba.dds.donaciones.domain.personas.Donante;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // requerido por JPA
@Entity
@Table(name = "solicitud_donacion")
public class SolicitudDonacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Setter
    private Long id;

    @Column(name = "descripcion", nullable = false, length = 255)
    private String descripcion;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @OneToMany(mappedBy = "solicitudOrigen", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ItemDonado> items = new ArrayList<>();

    @Setter
    @ManyToOne
    @JoinColumn(name = "donante_id", nullable = false)
    private Donante donante;

    public SolicitudDonacion(String descripcion) {
        if (descripcion == null || descripcion.isBlank()) throw new IllegalArgumentException("descripcion es obligatoria");
        this.descripcion = descripcion;
    }
    public List<ItemDonado> getItems() { return List.copyOf(items); }
    public void agregarItem(ItemDonado item) {
        if (item == null) throw new IllegalArgumentException("El item es obligatorio");
        item.asignarSolicitudOrigen(this);
        items.add(item);
    }
    public List<Donacion> segmentar() {
        List<List<ItemDonado>> grupos = new ArrayList<>();
        for (ItemDonado item : items) {
            List<ItemDonado> grupo = grupos.stream().filter(g -> g.get(0).mismaSegmentacion(item))
                    .findFirst().orElseGet(() -> { List<ItemDonado> nuevo = new ArrayList<>(); grupos.add(nuevo); return nuevo; });
            grupo.add(item);
        }
        return grupos.stream().map(grupo -> {
            Donacion donacion = new Donacion(null, grupo, this);
            donacion.setDonante(donante);
            return donacion;
        }).toList();
    }
}
