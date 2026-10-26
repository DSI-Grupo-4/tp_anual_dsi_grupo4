package ar.edu.utn.frba.dds.logistica.persistencia;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/** Contador para generar ids numéricos (MongoDB no tiene autoincrement). Un documento por colección. */
@Getter
@Setter
@NoArgsConstructor
@Document(collection = "secuencias")
public class Secuencia {

    @Id
    private String id; // nombre de la secuencia: "camion", "chofer", "entrega", "ruta"

    private long valor;
}
