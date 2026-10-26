package ar.edu.utn.frba.dds.logistica.domain.rutas;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED) // lo usa el ODM para instanciar al leer
@Document(collection = "choferes")
public class Chofer {

    // Id numérico: lo asigna GeneradorIds al guardar si es null.
    @Id
    private Integer idChofer;

    private String nombre;

    @Indexed(unique = true)
    private Integer dni;

    @Indexed
    private Boolean habilitado;

    public boolean estaHabilitado() {
        return Boolean.TRUE.equals(habilitado);
    }
}
