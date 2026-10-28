package ar.edu.utn.frba.dds.incentivos.config;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import java.nio.file.*;
import java.util.Map;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
class BandejaNotificacionesTest {
    @TempDir Path dir;
    @Test void conservaPendienteAnteFalloYLoRecuperaTrasReinicio() throws Exception {
        RabbitTemplate rabbit = mock(RabbitTemplate.class);
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        BandejaNotificaciones original = new BandejaNotificaciones(rabbit, mapper, dir.toString());
        original.guardar(Map.of("eventoId", "estable-1", "mensaje", "Prueba", "medio", "EMAIL", "contacto", "ana@example.org"));
        doThrow(new IllegalStateException("Rabbit caído")).when(rabbit).convertAndSend(anyString(), anyString(), any(Object.class), any(CorrelationData.class));
        original.publicarPendientes();
        try(var archivos=Files.list(dir)){ assertThat(archivos.filter(p->p.toString().endsWith(".json")).count()).isEqualTo(1); }
        reset(rabbit);
        doAnswer(inv -> { inv.<CorrelationData>getArgument(3).getFuture().complete(new CorrelationData.Confirm(true, null)); return null; })
            .when(rabbit).convertAndSend(anyString(), anyString(), any(Object.class), any(CorrelationData.class));
        new BandejaNotificaciones(rabbit,mapper,dir.toString()).publicarPendientes();
        try(var archivos=Files.list(dir)){ assertThat(archivos.count()).isZero(); }
    }
    @Test void unNackNoBorraElPendienteYDuplicadoNoCreaOtraEntrada() throws Exception {
        RabbitTemplate rabbit=mock(RabbitTemplate.class);
        BandejaNotificaciones bandeja=new BandejaNotificaciones(rabbit,new ObjectMapper(),dir.toString());
        bandeja.guardar(Map.of("eventoId","estable")); bandeja.guardar(Map.of("eventoId","estable"));
        doAnswer(inv -> { inv.<CorrelationData>getArgument(3).getFuture().complete(new CorrelationData.Confirm(false,"error")); return null; })
            .when(rabbit).convertAndSend(anyString(),anyString(),any(Object.class),any(CorrelationData.class));
        bandeja.publicarPendientes();
        try(var archivos=Files.list(dir)){ assertThat(archivos.filter(p->p.toString().endsWith(".json")).count()).isEqualTo(1); }
    }
}
