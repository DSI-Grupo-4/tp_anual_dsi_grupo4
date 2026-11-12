package ar.edu.utn.frba.dds.donaciones.domain.personas;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "persona")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "tipo_persona", discriminatorType = DiscriminatorType.STRING)
public abstract class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = jakarta.persistence.FetchType.EAGER)
    @JoinColumn(name = "persona_id", nullable = false)
    private List<MedioContacto> mediosContacto;

    public Persona() {
        this.mediosContacto = new ArrayList<>();
    }

    /**
     * Conserva la colección administrada por JPA y reutiliza los contactos
     * que mantienen tipo y valor. Si se reemplazaban todos por instancias
     * nuevas, Hibernate intentaba insertar antes de borrar los anteriores y
     * chocaba falsamente con el UNIQUE (persona_id, tipo, valor).
     */
    public void setMediosContacto(List<MedioContacto> contactos) {
        var actualesPorClave = new java.util.HashMap<String, MedioContacto>();
        this.mediosContacto.forEach(contacto ->
                actualesPorClave.put(claveContacto(contacto), contacto));

        var reconciliados = new ArrayList<MedioContacto>();
        var clavesRecibidas = new java.util.HashSet<String>();
        for (MedioContacto recibido : contactos) {
            String clave = claveContacto(recibido);
            if (!clavesRecibidas.add(clave)) {
                throw new IllegalArgumentException(
                        "No puede repetir un medio de contacto con el mismo tipo y valor");
            }

            MedioContacto existente = actualesPorClave.get(clave);
            if (existente == null) {
                reconciliados.add(recibido);
            } else {
                existente.setTipo(recibido.getTipo());
                existente.setValor(recibido.getValor());
                existente.setEsPreferido(recibido.getEsPreferido());
                reconciliados.add(existente);
            }
        }

        this.mediosContacto.removeIf(contacto -> !reconciliados.contains(contacto));
        reconciliados.stream()
                .filter(contacto -> !this.mediosContacto.contains(contacto))
                .forEach(this.mediosContacto::add);
    }

    private String claveContacto(MedioContacto contacto) {
        String valor = contacto.getValor() == null
                ? ""
                : contacto.getValor().trim().toLowerCase(java.util.Locale.ROOT);
        return contacto.getTipo() + "\u0000" + valor;
    }

    public void agregarMedio(MedioContacto medio) {
        mediosContacto.add(medio);
    }

    public void eliminarMedio(MedioContacto medio) {
        mediosContacto.remove(medio);
    }

    public Optional<MedioContacto> medioPreferido() {
        return mediosContacto.stream()
                .filter(MedioContacto::esPreferido)
                .findFirst();
    } //devuelve el medio de contacto preferido en caso de haber

}
