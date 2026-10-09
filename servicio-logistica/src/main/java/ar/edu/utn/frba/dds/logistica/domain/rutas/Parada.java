package ar.edu.utn.frba.dds.logistica.domain.rutas;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // requerido por JPA
@Entity
@Table(name = "parada", uniqueConstraints = {
        @UniqueConstraint(name = "uk_parada_ruta_numero", columnNames = {"id_ruta", "numero_parada"})
})
public class Parada {
    // Id propio de la base (único global). Es el que usan los endpoints /api/rutas/{idRuta}/paradas/{idParada}.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_parada")
    private Integer idParada;

    // Posición dentro de la ruta (1..n): define el orden de visita.
    @Column(name = "numero_parada", nullable = false)
    private Integer numeroParada;

    // Referencia lógica al servicio de Donaciones: solo el id, sin FK.
    @Column(name = "id_entidad_beneficiaria", nullable = false)
    private Integer idEntidadBeneficiariaAsociada;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "calle", column = @Column(name = "direccion_calle", nullable = false, length = 120)),
            @AttributeOverride(name = "numero", column = @Column(name = "direccion_numero", nullable = false, length = 10)),
            @AttributeOverride(name = "ciudad.nombre", column = @Column(name = "direccion_ciudad", nullable = false, length = 80)),
            @AttributeOverride(name = "ciudad.provincia.nombre", column = @Column(name = "direccion_provincia", nullable = false, length = 80))
    })
    private Direccion direccion;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_ruta", nullable = false)
    private Ruta ruta;

    // Lado inverso: la FK vive en entrega.id_parada
    @OneToMany(mappedBy = "parada")
    private List<Entrega> entregas = new ArrayList<>();

    public Parada(Integer numeroParada, Integer idEntidadBeneficiariaAsociada,
                  Direccion direccion, List<Entrega> entregas) {
        this.numeroParada = numeroParada;
        this.idEntidadBeneficiariaAsociada = idEntidadBeneficiariaAsociada;
        this.direccion = direccion;
        this.entregas = new ArrayList<>(entregas);
        this.entregas.forEach(e -> e.setParada(this));
    }

    public void confirmarRecepcion(FotoEntrega foto) {
        entregas.forEach(entrega -> entrega.confirmarEntrega(foto));
    }

    public void marcarNoRecibida(String justificacion) {
        if (justificacion == null || justificacion.isBlank()) {
            throw new IllegalArgumentException("La justificación de la entrega no recibida es obligatoria");
        }
        entregas.forEach(entrega -> entrega.marcarNoRecibida(justificacion));
    }

    public void marcarFallida(String motivo) {
        entregas.forEach(entrega -> entrega.marcarFallida(motivo));
    }
}
