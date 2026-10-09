package ar.edu.utn.frba.dds.donaciones.domain.donaciones;

import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.IdClass;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

/**
 * Fila suelta de ResultadoMatchmaking (una por entidad candidata, dentro de
 * una de las 3 listas): Donacion.getResultadoMatchmaking() las agrupa de
 * nuevo por lista para reconstruir el value object en memoria.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // requerido por JPA
@Entity
@Table(name = "candidata_matchmaking")
@IdClass(CandidataMatchmaking.Id.class)
public class CandidataMatchmaking {

    @jakarta.persistence.Id
    @ManyToOne
    @JoinColumn(name = "donacion_id", nullable = false)
    private Donacion donacion;

    @jakarta.persistence.Id
    @Enumerated(EnumType.STRING)
    @Column(name = "lista", nullable = false)
    private ListaMatchmaking lista;

    @jakarta.persistence.Id
    @Column(name = "posicion", nullable = false)
    private Integer posicion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "entidad_beneficiaria_id", nullable = false)
    private EntidadBeneficiaria entidadBeneficiaria;

    public CandidataMatchmaking(Donacion donacion, ListaMatchmaking lista, int posicion, EntidadBeneficiaria entidadBeneficiaria) {
        this.donacion = donacion;
        this.lista = lista;
        this.posicion = posicion;
        this.entidadBeneficiaria = entidadBeneficiaria;
    }

    @Getter
    @NoArgsConstructor
    public static class Id implements Serializable {
        private Long donacion;
        private ListaMatchmaking lista;
        private Integer posicion;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Id id)) return false;
            return Objects.equals(donacion, id.donacion) && lista == id.lista && Objects.equals(posicion, id.posicion);
        }

        @Override
        public int hashCode() {
            return Objects.hash(donacion, lista, posicion);
        }
    }
}
