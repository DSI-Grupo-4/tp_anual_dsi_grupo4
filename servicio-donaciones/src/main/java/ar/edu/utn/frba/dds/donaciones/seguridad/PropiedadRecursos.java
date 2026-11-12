package ar.edu.utn.frba.dds.donaciones.seguridad;

import ar.edu.utn.frba.dds.donaciones.domain.personas.Donante;
import ar.edu.utn.frba.dds.donaciones.domain.personas.TipoContacto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/**
 * Consultas de solo lectura para decidir a quién pertenece un recurso:
 * las reglas por id de la URL (SeguridadApiConfig) y la vinculación de
 * cuentas por email (CuentasKeycloak). Devuelven ids, no entidades, para
 * no depender de la sesión JPA del request.
 */
public interface PropiedadRecursos extends Repository<Donante, Long> {

    @Query("select d.donante.id from Donacion d where d.id = :id")
    Optional<Long> donanteDeDonacion(@Param("id") Long donacionId);

    @Query("select d.entidadBeneficiaria.id from Donacion d where d.id = :id")
    Optional<Long> entidadDeDonacion(@Param("id") Long donacionId);

    @Query("select s.donante.id from SolicitudDonacion s where s.id = :id")
    Optional<Long> donanteDeSolicitud(@Param("id") Long solicitudId);

    @Query("select distinct d.id from Donante d join d.persona p join p.mediosContacto m"
            + " where m.tipo = :tipo and lower(m.valor) = :email")
    List<Long> donantesConEmail(@Param("tipo") TipoContacto tipo, @Param("email") String email);

    @Query("select distinct d.id from Donante d, PersonaJuridica pj join pj.representantes r join r.mediosContacto m"
            + " where d.persona.id = pj.id and m.tipo = :tipo and lower(m.valor) = :email")
    List<Long> donantesConRepresentanteEmail(@Param("tipo") TipoContacto tipo, @Param("email") String email);

    @Query("select distinct e.id from EntidadBeneficiaria e join e.entidad pj join pj.mediosContacto m"
            + " where m.tipo = :tipo and lower(m.valor) = :email")
    List<Long> entidadesConEmail(@Param("tipo") TipoContacto tipo, @Param("email") String email);

    @Query("select distinct e.id from EntidadBeneficiaria e join e.entidad pj join pj.representantes r join r.mediosContacto m"
            + " where m.tipo = :tipo and lower(m.valor) = :email")
    List<Long> entidadesConRepresentanteEmail(@Param("tipo") TipoContacto tipo, @Param("email") String email);

    @Query("select d.id from Donante d")
    List<Long> idsDonantes();

    @Query("select e.id from EntidadBeneficiaria e")
    List<Long> idsEntidades();
}
