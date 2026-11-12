package ar.edu.utn.frba.dds.web.publico;

import ar.edu.utn.frba.dds.web.client.donaciones.DonacionApi;
import ar.edu.utn.frba.dds.web.client.donaciones.DonacionesClient;
import ar.edu.utn.frba.dds.web.client.donaciones.EntidadApi;
import ar.edu.utn.frba.dds.web.client.logistica.EntregaApi;
import ar.edu.utn.frba.dds.web.client.logistica.LogisticaClient;
import ar.edu.utn.frba.dds.web.config.CredencialesKeycloak;
import ar.edu.utn.frba.dds.web.error.BackendException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación provisoria de {@link FuentePublica} sobre los endpoints actuales:
 * GET /api/entregas (Logística), GET /api/donaciones, /api/entidades y /api/donantes (Donaciones).
 *
 * Una entrega se considera "realizada" si está ENTREGADA y tiene fechaHoraEntrega.
 * "Último mes" = últimos 30 días contados desde ahora (UTC, como guardan las APIs).
 */
@Service
public class FuentePublicaApis implements FuentePublica {

    private static final Logger log = LoggerFactory.getLogger(FuentePublicaApis.class);
    private static final Duration ULTIMO_MES = Duration.ofDays(30);
    private static final int MAX_DESTACADAS = 6;

    private final LogisticaClient logistica;
    private final DonacionesClient donaciones;
    private final Clock reloj;

    @Autowired
    public FuentePublicaApis(LogisticaClient logistica, DonacionesClient donaciones) {
        this(logistica, donaciones, Clock.systemUTC());
    }

    FuentePublicaApis(LogisticaClient logistica, DonacionesClient donaciones, Clock reloj) {
        this.logistica = logistica;
        this.donaciones = donaciones;
        this.reloj = reloj;
    }

    @Override
    public ResumenPublico resumen() {
        List<EntregaApi> entregas = intentar("entregas", logistica::entregas);
        List<EntidadApi> entidades = intentar("entidades", donaciones::entidades);
        Integer donantesActivos = contar(intentar("donantes", donaciones::donantes), d -> !Boolean.FALSE.equals(d.activo()));

        LocalDateTime desde = LocalDateTime.now(reloj).minus(ULTIMO_MES);
        List<EntregaApi> delMes = entregas == null ? List.of() : entregas.stream()
                .filter(FuentePublicaApis::realizada)
                .filter(e -> !e.fechaHoraEntrega().isBefore(desde))
                .toList();

        CifrasPublicas cifras = new CifrasPublicas(
                entregas == null ? null : delMes.size(),
                contar(entregas, e -> "EN_TRASLADO".equals(e.estadoEntrega())),
                contar(entidades, e -> !Boolean.FALSE.equals(e.activa())),
                donantesActivos);

        List<DonacionPublica> destacadas = List.of();
        if (!delMes.isEmpty()) {
            Contexto contexto = contexto(entidades);
            destacadas = delMes.stream()
                    .map(e -> aPublica(e, primeraFotoValida(e), contexto))
                    .sorted(Comparator.comparing(DonacionPublica::tieneFoto).reversed()
                            .thenComparing(DonacionPublica::entregadaEn, Comparator.reverseOrder()))
                    .limit(MAX_DESTACADAS)
                    .toList();
        }
        return new ResumenPublico(cifras, destacadas, entregas != null);
    }

    @Override
    public PaginaFotos fotos(int pagina, int tamanio) {
        // Sin try: si Logística no responde, la galería no tiene nada que mostrar → página 503.
        List<EntregaApi> entregadas = CredencialesKeycloak.comoServicio(logistica::entregas).stream()
                .filter(FuentePublicaApis::realizada)
                .filter(e -> e.fotos().stream().anyMatch(f -> esUrlSegura(f.url())))
                .sorted(Comparator.comparing(EntregaApi::fechaHoraEntrega).reversed())
                .toList();
        if (entregadas.isEmpty()) {
            return new PaginaFotos(List.of(), 1, 1, 0);
        }
        Contexto contexto = contexto(intentar("entidades", donaciones::entidades));
        List<DonacionPublica> fotos = new ArrayList<>();
        for (EntregaApi entrega : entregadas) {
            entrega.fotos().stream()
                    .map(EntregaApi.FotoApi::url)
                    .filter(FuentePublicaApis::esUrlSegura)
                    .forEach(url -> fotos.add(aPublica(entrega, url, contexto)));
        }
        int totalPaginas = Math.max(1, (fotos.size() + tamanio - 1) / tamanio);
        int actual = Math.min(Math.max(1, pagina), totalPaginas);
        int desde = (actual - 1) * tamanio;
        return new PaginaFotos(fotos.subList(desde, Math.min(desde + tamanio, fotos.size())), actual, totalPaginas, fotos.size());
    }

    // ---------------- armado ----------------

    private record Contexto(Map<Long, DonacionApi> donaciones, Map<Long, EntidadApi> entidades) {
    }

    private Contexto contexto(List<EntidadApi> entidades) {
        List<DonacionApi> lista = intentar("donaciones", donaciones::donaciones);
        return new Contexto(
                porId(lista, DonacionApi::id),
                porId(entidades, EntidadApi::id));
    }

    private static DonacionPublica aPublica(EntregaApi entrega, String fotoUrl, Contexto contexto) {
        DonacionApi donacion = entrega.idDonacionAsociada() == null ? null
                : contexto.donaciones().get(entrega.idDonacionAsociada().longValue());
        EntidadApi entidad = entrega.idEntidadBeneficiariaAsociada() == null ? null
                : contexto.entidades().get(entrega.idEntidadBeneficiariaAsociada().longValue());
        return new DonacionPublica(
                donacion != null ? donacion.descripcionItem() : null,
                donacion != null ? donacion.categoria() : null,
                donacion != null ? donacion.subcategoria() : null,
                donacion != null ? cantidad(donacion.cantidadAsignada()) : null,
                donacion != null ? donacion.unidadMedida() : null,
                entidad != null ? entidad.nombre() : null,
                entrega.fechaHoraEntrega(),
                fotoUrl);
    }

    private static boolean realizada(EntregaApi e) {
        return "ENTREGADA".equals(e.estadoEntrega()) && e.fechaHoraEntrega() != null;
    }

    private static String primeraFotoValida(EntregaApi e) {
        return e.fotos().stream().map(EntregaApi.FotoApi::url).filter(FuentePublicaApis::esUrlSegura).findFirst().orElse(null);
    }

    /** Solo http(s): una URL cargada por un usuario no puede usar otros esquemas (javascript:, data:, file:). */
    static boolean esUrlSegura(String url) {
        if (url == null) {
            return false;
        }
        String minusculas = url.trim().toLowerCase();
        return minusculas.startsWith("https://") || minusculas.startsWith("http://");
    }

    private static String cantidad(BigDecimal cantidad) {
        return cantidad == null ? null : cantidad.stripTrailingZeros().toPlainString();
    }

    private static <T> Integer contar(List<T> lista, java.util.function.Predicate<T> condicion) {
        return lista == null ? null : (int) lista.stream().filter(condicion).count();
    }

    private static <T> Map<Long, T> porId(List<T> lista, Function<T, Long> id) {
        if (lista == null) {
            return Map.of();
        }
        return lista.stream().filter(x -> id.apply(x) != null)
                .collect(Collectors.toMap(id, Function.identity(), (a, b) -> a));
    }

    /**
     * La landing no se cae si un servicio no responde: esa parte queda sin datos (null).
     * Siempre consulta con el token de servicio, aunque quien mira tenga sesión iniciada.
     */
    private static <T> List<T> intentar(String que, Supplier<List<T>> llamada) {
        try {
            List<T> resultado = CredencialesKeycloak.comoServicio(llamada);
            return resultado == null ? List.of() : resultado;
        } catch (BackendException e) {
            log.warn("Landing sin {}: {}", que, e.getMessage());
            return null;
        }
    }
}
