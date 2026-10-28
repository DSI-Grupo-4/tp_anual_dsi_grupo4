package ar.edu.utn.frba.dds.donaciones.domain.personas;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // requerido por JPA
@Entity
@Table(name = "medio_contacto")
public class MedioContacto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoContacto tipo;

    @Column(name = "valor", nullable = false, length = 150)
    private String valor;

    @Column(name = "es_preferido", nullable = false)
    private Boolean esPreferido;

    public MedioContacto(
            TipoContacto tipo,
            String valor,
            Boolean esPreferido) {

        this.tipo = tipo;
        this.valor = valor;
        this.esPreferido = esPreferido;
    }

    public void marcarComoPreferido() {
        this.esPreferido = true;
    }

    public void desmarcarPreferido() {
        this.esPreferido = false;
    }

    public Boolean esPreferido() {
        return esPreferido;
    }
}
