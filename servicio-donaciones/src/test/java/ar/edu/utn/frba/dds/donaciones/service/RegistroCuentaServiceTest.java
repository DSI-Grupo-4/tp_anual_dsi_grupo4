package ar.edu.utn.frba.dds.donaciones.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.donaciones.domain.personas.Genero;
import ar.edu.utn.frba.dds.donaciones.domain.personas.TipoContacto;
import ar.edu.utn.frba.dds.donaciones.domain.personas.TipoOrganizacion;
import ar.edu.utn.frba.dds.donaciones.dto.CiudadDTO;
import ar.edu.utn.frba.dds.donaciones.dto.DireccionDTO;
import ar.edu.utn.frba.dds.donaciones.dto.DonanteDTO;
import ar.edu.utn.frba.dds.donaciones.dto.EntidadBeneficiariaDTO;
import ar.edu.utn.frba.dds.donaciones.dto.MedioContactoDTO;
import ar.edu.utn.frba.dds.donaciones.dto.PersonaHumanaDTO;
import ar.edu.utn.frba.dds.donaciones.dto.PersonaJuridicaDTO;
import ar.edu.utn.frba.dds.donaciones.dto.ProvinciaDTO;
import ar.edu.utn.frba.dds.donaciones.integracion.CuentasKeycloak;
import ar.edu.utn.frba.dds.donaciones.seguridad.PropiedadRecursos;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegistroCuentaServiceTest {

    @Mock private DonanteService donantes;
    @Mock private EntidadBeneficiariaService entidades;
    @Mock private PropiedadRecursos propiedad;
    @Mock private CuentasKeycloak cuentas;

    private RegistroCuentaService servicio;

    @BeforeEach
    void setUp() {
        servicio = new RegistroCuentaService(donantes, entidades, propiedad, cuentas);
    }

    @Test
    void elDonanteQuedaConElCorreoVerificadoYNoConOtro() {
        when(propiedad.donantesConEmail(eq(TipoContacto.EMAIL), eq("ana@correo.com"))).thenReturn(List.of());
        when(propiedad.donantesConRepresentanteEmail(eq(TipoContacto.EMAIL), eq("ana@correo.com"))).thenReturn(List.of());
        when(propiedad.donantesConDocumento("30111222")).thenReturn(List.of());
        when(donantes.crearDonanteHumano(any())).thenAnswer(inv -> {
            DonanteDTO dto = new DonanteDTO();
            dto.setId(4L);
            return dto;
        });

        PersonaHumanaDTO pedido = persona();
        MedioContactoDTO ajeno = contacto(TipoContacto.EMAIL, "otro@correo.com", true);
        MedioContactoDTO telefono = contacto(TipoContacto.TELEFONO, "111", false);
        pedido.setMediosContacto(List.of(ajeno, telefono));

        servicio.registrarDonante("usuario-1", "Ana@Correo.com", pedido);

        ArgumentCaptor<PersonaHumanaDTO> captor = ArgumentCaptor.forClass(PersonaHumanaDTO.class);
        verify(donantes).crearDonanteHumano(captor.capture());
        assertThat(captor.getValue().getMediosContacto())
                .extracting(MedioContactoDTO::getValor)
                .containsExactly("ana@correo.com", "111");
        verify(cuentas).vincularUsuario("usuario-1", "ana@correo.com");
    }

    @Test
    void noReclamaUnDonanteQueYaTieneEseCorreo() {
        when(propiedad.donantesConEmail(eq(TipoContacto.EMAIL), eq("ana@correo.com"))).thenReturn(List.of(3L));

        assertThatThrownBy(() -> servicio.registrarDonante("usuario-1", "ana@correo.com", persona()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Buscar mis datos");
        verify(donantes, never()).crearDonanteHumano(any());
    }

    @Test
    void laOrganizacionQuedaConUnSoloRepresentanteYElCorreoVerificado() {
        when(propiedad.donantesConEmail(any(), eq("ana@correo.com"))).thenReturn(List.of());
        when(propiedad.donantesConRepresentanteEmail(any(), eq("ana@correo.com"))).thenReturn(List.of());
        when(propiedad.donantesConRazonSocial("Acme SA")).thenReturn(List.of());
        when(donantes.crearDonanteJuridico(any())).thenAnswer(inv -> {
            DonanteDTO dto = new DonanteDTO();
            dto.setId(8L);
            return dto;
        });

        PersonaJuridicaDTO pedido = new PersonaJuridicaDTO();
        pedido.setRazonSocial("Acme SA");
        pedido.setTipo(TipoOrganizacion.EMPRESA);
        pedido.setMediosContacto(List.of(contacto(TipoContacto.EMAIL, "otro@correo.com", true)));
        PersonaHumanaDTO representante = persona();
        PersonaHumanaDTO ajeno = persona();
        ajeno.setDocumento("999");
        ajeno.setMediosContacto(List.of(contacto(TipoContacto.EMAIL, "ajeno@correo.com", true)));
        pedido.setRepresentantes(List.of(representante, ajeno));

        servicio.registrarDonanteJuridico("usuario-1", "Ana@Correo.com", pedido);

        ArgumentCaptor<PersonaJuridicaDTO> captor = ArgumentCaptor.forClass(PersonaJuridicaDTO.class);
        verify(donantes).crearDonanteJuridico(captor.capture());
        assertThat(captor.getValue().getRepresentantes()).hasSize(1);
        assertThat(captor.getValue().getRepresentantes().get(0).getMediosContacto())
                .extracting(MedioContactoDTO::getValor)
                .containsExactly("ana@correo.com");
        assertThat(captor.getValue().getMediosContacto())
                .extracting(MedioContactoDTO::getValor)
                .containsExactly("ana@correo.com");
        verify(cuentas).vincularUsuario("usuario-1", "ana@correo.com");
        verify(donantes, never()).crearDonanteHumano(any());
    }

    @Test
    void noReclamaUnaOrganizacionDonanteYaCargada() {
        when(propiedad.donantesConEmail(any(), any())).thenReturn(List.of());
        when(propiedad.donantesConRepresentanteEmail(any(), any())).thenReturn(List.of());
        when(propiedad.donantesConRazonSocial("Acme SA")).thenReturn(List.of(3L));
        PersonaJuridicaDTO pedido = new PersonaJuridicaDTO();
        pedido.setRazonSocial("Acme SA");
        pedido.setTipo(TipoOrganizacion.EMPRESA);
        pedido.setRepresentantes(List.of(persona()));

        assertThatThrownBy(() -> servicio.registrarDonanteJuridico("usuario-1", "ana@correo.com", pedido))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("razón social");
        verify(donantes, never()).crearDonanteJuridico(any());
    }

    @Test
    void noCreaOtroDonanteConElMismoDocumento() {
        when(propiedad.donantesConEmail(any(), any())).thenReturn(List.of());
        when(propiedad.donantesConRepresentanteEmail(any(), any())).thenReturn(List.of());
        when(propiedad.donantesConDocumento("30111222")).thenReturn(List.of(9L));

        assertThatThrownBy(() -> servicio.registrarDonante("usuario-1", "ana@correo.com", persona()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("documento");
        verify(donantes, never()).crearDonanteHumano(any());
    }

    @Test
    void noReclamaUnaEntidadExistenteNiLeCopiaOtroCorreo() {
        when(propiedad.entidadesConEmail(eq(TipoContacto.EMAIL), eq("ana@correo.com"))).thenReturn(List.of(2L));

        assertThatThrownBy(() -> servicio.registrarEntidad("usuario-1", "ana@correo.com", entidad()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No se puede reclamar");
        verify(entidades, never()).crear(any());
    }

    private static PersonaHumanaDTO persona() {
        PersonaHumanaDTO dto = new PersonaHumanaDTO();
        dto.setNombre("Ana");
        dto.setApellido("Perez");
        dto.setDocumento("30111222");
        dto.setEdad(30);
        dto.setGenero(Genero.FEMENINO);
        dto.setDireccion(direccion());
        return dto;
    }

    private static EntidadBeneficiariaDTO entidad() {
        PersonaJuridicaDTO juridica = new PersonaJuridicaDTO();
        juridica.setRazonSocial("Comedor Sonrisas");
        juridica.setTipo(TipoOrganizacion.ONG);
        juridica.setMediosContacto(List.of(contacto(TipoContacto.TELEFONO, "444", true)));
        juridica.setRepresentantes(List.of(persona()));
        EntidadBeneficiariaDTO dto = new EntidadBeneficiariaDTO();
        dto.setDescripcion("Comedor");
        dto.setDireccion(direccion());
        dto.setPersonaJuridica(juridica);
        return dto;
    }

    private static DireccionDTO direccion() {
        ProvinciaDTO provincia = new ProvinciaDTO();
        provincia.setNombre("Buenos Aires");
        CiudadDTO ciudad = new CiudadDTO();
        ciudad.setNombre("La Plata");
        ciudad.setProvincia(provincia);
        DireccionDTO direccion = new DireccionDTO();
        direccion.setCalle("7");
        direccion.setNumero("123");
        direccion.setCiudad(ciudad);
        return direccion;
    }

    private static MedioContactoDTO contacto(TipoContacto tipo, String valor, boolean preferido) {
        MedioContactoDTO dto = new MedioContactoDTO();
        dto.setTipo(tipo);
        dto.setValor(valor);
        dto.setEsPreferido(preferido);
        return dto;
    }
}
