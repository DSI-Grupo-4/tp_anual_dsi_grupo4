package ar.edu.utn.frba.dds.logistica.persistencia;

import ar.edu.utn.frba.dds.logistica.domain.rutas.Camion;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Chofer;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Entrega;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Ruta;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

/**
 * Asigna ids numéricos consecutivos (1, 2, 3...) a Camion, Chofer, Entrega y Ruta justo antes de
 * guardarlos por primera vez. Mantiene los ids enteros que ya usan la API REST, Donaciones y Postman,
 * en lugar de pasar a ObjectId. El incremento es atómico en MongoDB ($inc con findAndModify).
 */
@Component
public class GeneradorIds extends AbstractMongoEventListener<Object> {

    private final MongoOperations mongo;

    public GeneradorIds(MongoOperations mongo) {
        this.mongo = mongo;
    }

    @Override
    public void onBeforeConvert(BeforeConvertEvent<Object> event) {
        Object entidad = event.getSource();
        if (entidad instanceof Camion camion && camion.getIdCamion() == null) {
            camion.setIdCamion(siguiente("camion"));
        } else if (entidad instanceof Chofer chofer && chofer.getIdChofer() == null) {
            chofer.setIdChofer(siguiente("chofer"));
        } else if (entidad instanceof Entrega entrega && entrega.getIdEntrega() == null) {
            entrega.setIdEntrega(siguiente("entrega"));
        } else if (entidad instanceof Ruta ruta && ruta.getIdRuta() == null) {
            ruta.setIdRuta(siguiente("ruta"));
        }
    }

    private int siguiente(String nombre) {
        Secuencia secuencia = mongo.findAndModify(
                Query.query(Criteria.where("_id").is(nombre)),
                new Update().inc("valor", 1),
                FindAndModifyOptions.options().returnNew(true).upsert(true),
                Secuencia.class);
        return (int) secuencia.getValor();
    }
}
