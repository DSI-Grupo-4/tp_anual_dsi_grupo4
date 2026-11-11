package ar.edu.utn.frba.dds.web.publico;

import ar.edu.utn.frba.dds.web.mapa.Marcador;
import ar.edu.utn.frba.dds.web.mapa.MarcadoresMapa;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Datos inventados para ver el mapa funcionando en desarrollo (perfil dev).
 * Entidades tomadas de los ejemplos de la consigna; las coordenadas son aproximadas.
 */
@Service
@Primary
@Profile("dev")
public class MapaPublicoEjemplo implements MapaPublico {

    @Override
    public MarcadoresMapa entregasRealizadas() {
        LocalDateTime base = LocalDateTime.of(2026, 11, 3, 17, 30);
        return new MarcadoresMapa(List.of(
                Marcador.de(-34.3497, -58.7930, "Comedor Escobar Sonrisas", "Arroz 1 kg × 20 · Alimentos", base, Marcador.ENTREGA),
                Marcador.de(-34.2650, -59.0950, "Escuela Rural N°10", "Sillas × 12 · Mobiliario", base.minusDays(2), Marcador.ENTREGA),
                Marcador.de(-34.6420, -58.5650, "Hogar Los Pinos", "Frazadas × 30 · Vestimenta", base.minusDays(5), Marcador.ENTREGA),
                Marcador.de(-34.7210, -58.2620, "Merendero Arcos Plateados", "Aceite vegetal 15,5 l · Alimentos", base.minusDays(9), Marcador.ENTREGA)),
                MarcadoresMapa.Origen.EJEMPLO);
    }
}
