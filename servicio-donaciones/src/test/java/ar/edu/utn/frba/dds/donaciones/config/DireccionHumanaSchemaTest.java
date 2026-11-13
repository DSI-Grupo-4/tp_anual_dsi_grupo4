package ar.edu.utn.frba.dds.donaciones.config;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.assertj.core.api.Assertions.*;
class DireccionHumanaSchemaTest {
    @Test void ampliacionEsAditivaConservaFilasYSePuedeRepetir() throws Exception {
        var source=new DriverManagerDataSource("jdbc:h2:mem:direccion;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1","sa","");
        var jdbc=new JdbcTemplate(source);
        jdbc.execute("CREATE TABLE persona_humana(id bigint primary key, nombre varchar(100))");
        jdbc.update("INSERT INTO persona_humana VALUES(1,'Ana')");
        var schema=new DireccionHumanaSchema();
        assertThat(schema.ampliarDireccionHumana(source)).isTrue();
        assertThat(schema.ampliarDireccionHumana(source)).isTrue();
        assertThat(jdbc.queryForObject("SELECT nombre FROM persona_humana WHERE id=1",String.class)).isEqualTo("Ana");
        jdbc.update("UPDATE persona_humana SET calle='San Martín',numero='123',nombre_ciudad='Córdoba',nombre_provincia='Córdoba' WHERE id=1");
        assertThat(jdbc.queryForObject("SELECT nombre_ciudad FROM persona_humana WHERE id=1",String.class)).isEqualTo("Córdoba");
    }
}
