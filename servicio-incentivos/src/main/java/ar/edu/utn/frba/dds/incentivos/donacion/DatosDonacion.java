package ar.edu.utn.frba.dds.incentivos.donacion;

import ar.edu.utn.frba.dds.incentivos.consultor.Beneficiario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // requerido por JPA
@Entity
@Table(name = "datos_donacion", indexes = {
        @Index(name = "idx_datos_donacion_donante_fecha", columnList = "id_donante, fecha")
})
public class DatosDonacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_datos_donacion")
    private Integer idDatosDonacion;

    // No está en el DER original (diagramas/der/incentivos.txt): el código
    // la necesita para no duplicar cantidades si Donaciones reenvía el mismo
    // evento (ver Donante.registrarActividadDonacion). DER desactualizado,
    // no el código -- mismo caso que D-027 con Logística.
    @Column(name = "donacion_id")
    private Long donacionId;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    // Nombre de la categoría de bienes donados (Alimentos/Mobiliario/Vestimenta,
    // tal como la modela el Servicio de Donaciones) -- no confundir con
    // misiones.Categoria (Colaborador/Sostenedor/Transformador), que es el
    // rango del donante, un concepto distinto. Solo se usa para la misión
    // "Completitud" (variedad de categorías donadas).
    @Column(name = "categoria_bien", length = 50)
    private String categoriaBien;

    @Column(name = "cantidad_bienes", nullable = false, precision = 12, scale = 3)
    private BigDecimal cantidadBienes;

    @Column(name = "donacion_exitosa", nullable = false)
    private boolean donacionExitosa;

    @ManyToOne(cascade = jakarta.persistence.CascadeType.MERGE)
    @JoinColumn(name = "id_beneficiario")
    private Beneficiario beneficiario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_donante", nullable = false)
    private ar.edu.utn.frba.dds.incentivos.donante.Donante donante;

    public DatosDonacion(LocalDate fecha, String categoriaBien, BigDecimal cantidadBienes,
                          boolean donacionExitosa, Beneficiario beneficiario) {
        if (fecha == null || cantidadBienes == null || cantidadBienes.signum() <= 0)
            throw new IllegalArgumentException("fecha y cantidadBienes positiva son obligatorias");
        this.fecha = fecha;
        this.categoriaBien = categoriaBien;
        this.cantidadBienes = cantidadBienes;
        this.donacionExitosa = donacionExitosa;
        this.beneficiario = beneficiario;
    }
}
