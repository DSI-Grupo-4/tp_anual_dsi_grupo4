package ar.edu.utn.frba.dds.logistica.domain.rutas;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "foto_entrega")
@Schema(example = """
{
  "url": "https://example.org/entrega.jpg",
  "fecha": "2026-10-08"
}
""")
public class FotoEntrega {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_foto")
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Integer idFoto;

    @NotBlank(message = "url es obligatoria")
    @Column(name = "url", nullable = false, length = 500)
    private String url;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_entrega", nullable = false)
    private Entrega entrega;

    public FotoEntrega() {}

    public FotoEntrega(String url, LocalDate fecha) {
        this.url = url;
        this.fecha = fecha;
    }
}
