package ar.edu.utn.frba.dds.donaciones.service;

import ar.edu.utn.frba.dds.donaciones.domain.personas.*;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.*;
import ar.edu.utn.frba.dds.donaciones.dto.*;
import ar.edu.utn.frba.dds.donaciones.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.mock.web.MockMultipartFile;
import java.nio.charset.StandardCharsets;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DataJpaTest(showSql=false,properties={"spring.jpa.hibernate.ddl-auto=create-drop","spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"})
class AdministracionPersistenciaTest {
    @Autowired DonanteRepository donantes;
    @Autowired EntidadBeneficiariaRepository entidades;
    @Autowired DonacionRepository donaciones;
    @Autowired EntityManager em;
    DonanteService ds;
    EntidadBeneficiariaService es;
    ConsultaAdministracionService consulta;
    ObjectMapper json=new ObjectMapper().findAndRegisterModules();
    @BeforeEach void preparar() {
        ds=new DonanteService(new GestorDonantes(donantes));
        es=new EntidadBeneficiariaService(entidades,mock(GestorDonaciones.class),donaciones);
        consulta=new ConsultaAdministracionService(donantes,entidades,donaciones,ds,es,mock(DonacionService.class));
    }
    private PersonaHumanaDTO humana() throws Exception {
        return json.readValue("""
            {"nombre":"Ana","apellido":"Pérez","edad":30,"documento":"30123456","genero":"FEMENINO",
             "direccion":{"calle":"San Martín","numero":"123","ciudad":{"nombre":"Córdoba","provincia":{"nombre":"Córdoba"}}},
             "mediosContacto":[{"tipo":"EMAIL","valor":"ana@example.org","esPreferido":true}]}
            """,PersonaHumanaDTO.class);
    }
    @Test void guardaDireccionYBuscaPorNombreDocumentoCorreoConPaginacion() throws Exception {
        var creado=ds.crearDonanteHumano(humana()); em.flush(); em.clear();
        assertThat(ds.buscarPorId(creado.getId()).getDireccion().getCiudad().getProvincia().getNombre()).isEqualTo("Córdoba");
        for(String q:java.util.List.of("Ana Pérez","30123456","ana@example.org"))
            assertThat(consulta.donantes(q,true,0,20).content()).extracting(DonanteDTO::getId).containsExactly(creado.getId());
        assertThat(consulta.donantes("no existe",true,0,20).content()).isEmpty();
        assertThatThrownBy(()->consulta.donantes("",true,0,101)).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void editarDonanteConservandoElEmailReutilizaElContactoPersistido() throws Exception {
        var creado=ds.crearDonanteHumano(humana()); em.flush(); em.clear();
        var contactoOriginal=donantes.findById(creado.getId()).orElseThrow()
                .getPersona().getMediosContacto().get(0).getId();

        var cambios=humana();
        cambios.setNombre("Ana actualizada");
        ds.actualizarHumano(creado.getId(),cambios); em.flush(); em.clear();

        var actualizado=donantes.findById(creado.getId()).orElseThrow();
        assertThat(((PersonaHumana) actualizado.getPersona()).getNombre()).isEqualTo("Ana actualizada");
        assertThat(actualizado.getPersona().getMediosContacto()).singleElement()
                .satisfies(contacto -> assertThat(contacto.getId()).isEqualTo(contactoOriginal));
    }
    @Test void editarEntidadPersisteYConservaDatosNoEditadosDelRepresentante() throws Exception {
        var p=new PersonaJuridicaDTO(); p.setRazonSocial("Comedor Sonrisas"); p.setTipo(TipoOrganizacion.ONG);
        p.setMediosContacto(humana().getMediosContacto()); p.setRepresentantes(java.util.List.of(humana()));
        var dto=new EntidadBeneficiariaDTO(); dto.setPersonaJuridica(p); dto.setDescripcion("Comedor");
        var creado=es.crear(dto); em.flush(); em.clear();
        var contactoEntidadOriginal=entidades.findById(creado.getId()).orElseThrow()
                .getEntidad().getMediosContacto().get(0).getId();
        // Un representante histórico puede tener teléfono preferido y todavía no tener correo.
        var historico=entidades.findById(creado.getId()).orElseThrow().getEntidad().getRepresentantes().get(0);
        historico.setMediosContacto(java.util.List.of(new MedioContacto(TipoContacto.TELEFONO,"3515551234",true)));
        em.flush(); em.clear();
        dto.setDescripcion("Comedor actualizado"); p.setRazonSocial("Sonrisas actualizada");
        var r=humana(); r.setEdad(null); r.setDireccion(null); r.setGenero(null); r.setNombre("Ana María"); p.setRepresentantes(java.util.List.of(r));
        es.actualizar(creado.getId(),dto); em.flush(); em.clear();
        var leido=es.obtenerPorId(creado.getId());
        assertThat(leido.getDescripcion()).isEqualTo("Comedor actualizado");
        assertThat(entidades.findById(creado.getId()).orElseThrow().getEntidad().getMediosContacto())
                .singleElement().satisfies(contacto ->
                        assertThat(contacto.getId()).isEqualTo(contactoEntidadOriginal));
        assertThat(leido.getPersonaJuridica().getRepresentantes()).singleElement().satisfies(rep->{
            assertThat(rep.getNombre()).isEqualTo("Ana María"); assertThat(rep.getEdad()).isEqualTo(30);
            assertThat(rep.getDireccion().getCalle()).isEqualTo("San Martín");
            assertThat(rep.getMediosContacto()).filteredOn(c->Boolean.TRUE.equals(c.getEsPreferido()))
                    .singleElement().satisfies(c->assertThat(c.getTipo()).isEqualTo(TipoContacto.TELEFONO));
            assertThat(rep.getMediosContacto()).filteredOn(c->c.getTipo()==TipoContacto.EMAIL).hasSize(1);
        });
        assertThat(consulta.entidades("actualizada",true,0,20).content()).hasSize(1);
    }
    @Test void buscaJuridicaYNoDuplicaFilasPorSusContactos() throws Exception {
        var dto=new PersonaJuridicaDTO(); dto.setRazonSocial("Empresa Solidaria"); dto.setTipo(TipoOrganizacion.EMPRESA);
        var email=humana().getMediosContacto().get(0); email.setValor("empresa@example.org"); dto.setMediosContacto(java.util.List.of(email));
        var d=ds.crearDonanteJuridico(dto); em.flush(); em.clear();
        assertThat(consulta.donantes("Solidaria",null,0,1).content()).extracting(DonanteDTO::getId).containsExactly(d.getId());
        assertThat(consulta.donantes("empresa@example.org",null,0,1).totalElements()).isEqualTo(1);
    }
    @Test void importaMasDeDiezMilFilasSinServiciosExternos() {
        var csv=new StringBuilder("TipoPersona,TipoDoc,Documento,Nombre,Email,Telefono\n");
        for(int i=0;i<10001;i++) csv.append("HUMANA,DNI,").append(30000000+i).append(",Donante Prueba,donante").append(i).append("@example.org,\n");
        var archivo=new MockMultipartFile("archivo","historico.csv","text/csv",csv.toString().getBytes(StandardCharsets.UTF_8));
        assertThat(ds.importarCSV(archivo).getCantidadImportados()).isEqualTo(10001);
        em.flush(); em.clear();
        assertThat(consulta.donantes("",true,0,20).content()).hasSize(20);
        assertThat(consulta.donantes("",true,0,20).totalElements()).isEqualTo(10001);
    }

    @Test void filtrosDeBienFechaYDestacadasPublicasUsanDatosPersistidos() throws Exception {
        var donor=donantes.findById(ds.crearDonanteHumano(humana()).getId()).orElseThrow();
        var p=new PersonaJuridica("Comedor Público",TipoOrganizacion.ONG,"Comedor",new java.util.ArrayList<>());
        var e=entidades.save(new EntidadBeneficiaria(null,p,"Destino"));
        var carga=new SolicitudDonacion("Descripción privada no publicable"); carga.setDonante(donor);
        carga.agregarItem(new ItemDonado(null,"Descripción de bien",ar.edu.utn.frba.dds.donaciones.domain.categorias.Categoria.MOBILIARIO,
            ar.edu.utn.frba.dds.donaciones.domain.categorias.Subcategoria.SILLA,ar.edu.utn.frba.dds.donaciones.domain.categorias.UnidadMedida.UNIDAD,
            java.math.BigDecimal.valueOf(3),null,null,new ar.edu.utn.frba.dds.donaciones.domain.categorias.ConEstado(ar.edu.utn.frba.dds.donaciones.domain.categorias.Condicion.USADO),15,2,1));
        em.persist(carga);
        var donacion=carga.segmentar().get(0); donacion.setEntidadBeneficiaria(e);
        for(var estado:java.util.List.of(EstadoTrack.ASIGNACION_REALIZADA,EstadoTrack.LISTA_PARA_ENTREGAR,EstadoTrack.EN_TRASLADO,EstadoTrack.ENTREGADA))
            donacion.cambiarEstado(estado,"Prueba");
        donaciones.saveAndFlush(donacion);
        var lista=consulta.destacadas(6);
        assertThat(lista).singleElement().satisfies(d->{
            assertThat(d.entidad()).isEqualTo("Comedor Público"); assertThat(d.cantidad()).isEqualByComparingTo("3");
        });
        String publico=json.writeValueAsString(lista);
        assertThat(publico).doesNotContain("ana@example.org","30123456","donanteId","Descripción privada","Descripción de bien");
        var hoy=java.time.LocalDate.now();
        assertThat(consulta.donaciones("Descripción",EstadoTrack.ENTREGADA,null,null,donor.getId(),e.getId(),hoy,hoy,0,20).totalElements()).isEqualTo(1);
        assertThat(consulta.donaciones("",null,null,null,null,null,hoy.plusDays(1),null,0,20).totalElements()).isZero();
        assertThatThrownBy(()->consulta.donaciones("",null,null,null,null,null,hoy,hoy.minusDays(1),0,20)).isInstanceOf(IllegalArgumentException.class);
        em.createQuery("update CambioEstado c set c.fechaCambio = :fecha where c.estadoNuevo = :estado")
            .setParameter("fecha",java.time.LocalDateTime.now().minusDays(31)).setParameter("estado",EstadoTrack.ENTREGADA).executeUpdate();
        assertThat(consulta.destacadas(6)).isEmpty();
    }
}
