package ar.edu.utn.frba.dds.donaciones.service;
import ar.edu.utn.frba.dds.donaciones.domain.personas.*;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.*;
import ar.edu.utn.frba.dds.donaciones.domain.categorias.*;
import ar.edu.utn.frba.dds.donaciones.dto.*;
import ar.edu.utn.frba.dds.donaciones.repository.*;
import jakarta.persistence.criteria.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@Transactional(readOnly = true)
public class ConsultaAdministracionService {
    private final DonanteRepository donantes;
    private final EntidadBeneficiariaRepository entidades;
    private final DonacionRepository donaciones;
    private final DonanteService ds;
    private final EntidadBeneficiariaService es;
    private final DonacionService os;
    public ConsultaAdministracionService(DonanteRepository d, EntidadBeneficiariaRepository e, DonacionRepository o,
            DonanteService ds, EntidadBeneficiariaService es, DonacionService os) {
        donantes=d; entidades=e; donaciones=o; this.ds=ds; this.es=es; this.os=os;
    }
    private Pageable pagina(int pagina, int cantidad) {
        if (pagina < 0 || cantidad < 1 || cantidad > 100) throw new IllegalArgumentException("Página inválida; tamaño entre 1 y 100");
        return PageRequest.of(pagina, cantidad, Sort.by("id").descending());
    }
    private String patron(String q) { return "%" + q.trim().toLowerCase(Locale.ROOT).replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%"; }
    public PaginaDTO<DonanteDTO> donantes(String q, Boolean activo, int page, int size) {
        Specification<Donante> filtro = (r, query, cb) -> {
            var condiciones = new ArrayList<Predicate>();
            if (activo != null) condiciones.add(cb.equal(r.get("activo"), activo));
            if (!q.isBlank()) {
                var sub = query.subquery(Long.class); var persona = sub.from(Persona.class);
                var h = cb.treat(persona, PersonaHumana.class); var j = cb.treat(persona, PersonaJuridica.class);
                var contactos = persona.join("mediosContacto", JoinType.LEFT);
                sub.select(persona.get("id")).where(cb.or(
                    cb.like(cb.lower(cb.concat(cb.concat(h.get("nombre"), " "), h.get("apellido"))), patron(q), '!'),
                    cb.like(cb.lower(h.get("documento")), patron(q), '!'),
                    cb.like(cb.lower(j.get("razonSocial")), patron(q), '!'),
                    cb.like(cb.lower(contactos.get("valor")), patron(q), '!')));
                condiciones.add(r.get("persona").get("id").in(sub));
            }
            return cb.and(condiciones.toArray(Predicate[]::new));
        };
        return PaginaDTO.desde(donantes.findAll(filtro, pagina(page,size)).map(ds::convertirADTO));
    }
    public PaginaDTO<EntidadBeneficiariaDTO> entidades(String q, Boolean activa, int page, int size) {
        Specification<EntidadBeneficiaria> filtro = (r, query, cb) -> {
            var condiciones = new ArrayList<Predicate>();
            if (activa != null) condiciones.add(cb.equal(r.get("activa"), activa));
            if (!q.isBlank()) condiciones.add(cb.or(cb.like(cb.lower(r.get("entidad").get("razonSocial")),patron(q),'!'),
                cb.like(cb.lower(r.get("descripcion")),patron(q),'!')));
            return cb.and(condiciones.toArray(Predicate[]::new));
        };
        return PaginaDTO.desde(entidades.findAll(filtro, pagina(page,size)).map(es::convertirADTO));
    }
    public PaginaDTO<DonacionDTO> donaciones(String q, EstadoTrack estado, Categoria categoria, Subcategoria subcategoria,
            Long donanteId, Long entidadId, int page, int size) {
        return donaciones(q,estado,categoria,subcategoria,donanteId,entidadId,null,null,page,size);
    }
    public PaginaDTO<DonacionDTO> donaciones(String q, EstadoTrack estado, Categoria categoria, Subcategoria subcategoria,
            Long donanteId, Long entidadId, java.time.LocalDate desde, java.time.LocalDate hasta, int page, int size) {
        if(desde!=null && hasta!=null && desde.isAfter(hasta)) throw new IllegalArgumentException("La fecha inicial no puede ser posterior a la final");
        Specification<Donacion> filtro = (r, query, cb) -> {
            var p = new ArrayList<Predicate>();
            if(desde!=null) p.add(cb.greaterThanOrEqualTo(r.get("fechaCreacion"),desde.atStartOfDay()));
            if(hasta!=null) p.add(cb.lessThan(r.get("fechaCreacion"),hasta.plusDays(1).atStartOfDay()));
            if (estado != null) p.add(cb.equal(r.get("estadoActual"),estado));
            if (donanteId != null) p.add(cb.equal(r.get("donante").get("id"),donanteId));
            if (entidadId != null) p.add(cb.equal(r.get("entidadBeneficiaria").get("id"),entidadId));
            if (!q.isBlank() || categoria != null || subcategoria != null) {
                var sub = query.subquery(Long.class); var item = sub.from(ItemDonado.class);
                var ip = new ArrayList<Predicate>();
                if (!q.isBlank()) ip.add(cb.like(cb.lower(item.get("descripcion")),patron(q),'!'));
                if (categoria != null) ip.add(cb.equal(item.get("categoria"),categoria));
                if (subcategoria != null) ip.add(cb.equal(item.get("subcategoria"),subcategoria));
                sub.select(item.get("donacion").get("id")).where(cb.and(ip.toArray(Predicate[]::new)));
                p.add(r.get("id").in(sub));
            }
            return cb.and(p.toArray(Predicate[]::new));
        };
        return PaginaDTO.desde(donaciones.findAll(filtro,pagina(page,size)).map(os::convertirADTO));
    }
    public Map<String,Long> resumen() {
        return Map.of("donantesActivos",donantes.count((r,q,cb)->cb.isTrue(r.get("activo"))),
            "entidadesActivas",entidades.count((r,q,cb)->cb.isTrue(r.get("activa"))),
            "enDeposito",donaciones.count((r,q,cb)->cb.equal(r.get("estadoActual"),EstadoTrack.EN_DEPOSITO)),
            "asignadas",donaciones.count((r,q,cb)->cb.equal(r.get("estadoActual"),EstadoTrack.ASIGNACION_REALIZADA)));
    }

    /** Últimos 30 días, ordenados por entrega reciente; proyección sin identidad ni contactos de donantes. */
    public List<DonacionDestacadaDTO> destacadas(int limite) {
        if(limite<1 || limite>20) throw new IllegalArgumentException("El límite debe estar entre 1 y 20");
        var ahora=java.time.LocalDateTime.now();
        return donaciones.destacadas(EstadoTrack.ENTREGADA,ahora.minusDays(30),ahora,PageRequest.of(0,limite));
    }
}
