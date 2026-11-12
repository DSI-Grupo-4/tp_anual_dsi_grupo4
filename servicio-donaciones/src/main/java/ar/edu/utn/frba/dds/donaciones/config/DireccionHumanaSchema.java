package ar.edu.utn.frba.dds.donaciones.config;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Ampliación aditiva e idempotente para instalaciones existentes con ddl-auto=validate. */
@Configuration
public class DireccionHumanaSchema {
    @Bean
    static BeanFactoryPostProcessor direccionAntesDeJpa() {
        return factory -> {
            if (factory.containsBeanDefinition("entityManagerFactory")) {
                var bean = factory.getBeanDefinition("entityManagerFactory");
                var dependencias = new ArrayList<>(Arrays.asList(bean.getDependsOn() == null ? new String[0] : bean.getDependsOn()));
                dependencias.add("ampliarDireccionHumana");
                bean.setDependsOn(dependencias.toArray(String[]::new));
            }
        };
    }
    @Bean
    Boolean ampliarDireccionHumana(DataSource source) throws SQLException {
        try (var c=source.getConnection()) {
            // En pruebas con create-drop la tabla todavía no existe: la crea Hibernate.
            var columnas = new HashSet<String>();
            try (var rs=c.getMetaData().getColumns(c.getCatalog(),null,"persona_humana",null)) {
                while (rs.next()) columnas.add(rs.getString("COLUMN_NAME").toLowerCase(Locale.ROOT));
            }
            if (columnas.isEmpty()) return true;
            for (String columna : List.of("calle","numero","nombre_ciudad","nombre_provincia")) {
                if (!columnas.contains(columna)) {
                    try (var stmt=c.createStatement()) {
                        stmt.executeUpdate("ALTER TABLE persona_humana ADD COLUMN " + columna + " VARCHAR(255) NULL");
                    }
                }
            }
        }
        return true;
    }
}
