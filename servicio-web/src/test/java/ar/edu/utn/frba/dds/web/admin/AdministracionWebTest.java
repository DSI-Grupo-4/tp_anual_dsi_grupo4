package ar.edu.utn.frba.dds.web.admin;
import static ar.edu.utn.frba.dds.web.client.donaciones.AdminApi.*;
import ar.edu.utn.frba.dds.web.client.donaciones.AdminDonacionesClient;
import ar.edu.utn.frba.dds.web.error.*;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest @AutoConfigureMockMvc
class AdministracionWebTest {
    @Autowired MockMvc mvc;
    @MockitoBean AdminDonacionesClient api;
    Donante donante;
    Donacion donacion;
    @BeforeEach void datos() {
        var contactos=List.of(new Contacto("EMAIL","ana@example.org",true));
        var dir=new Direccion("San Martín","120",new Ciudad("Córdoba",new Provincia("Córdoba")));
        donante=new Donante(1L,true,"HUMANA","Ana","Pérez",30,"30123456","FEMENINO",null,null,null,contactos,dir,List.of());
        var representante=new Humana("Juan","Pérez",null,"32123456",null,null,contactos);
        var entidad=new Entidad(2L,true,"Comedor comunitario",new Juridica("Comedor Sonrisas","ONG","Alimentos",contactos,List.of(representante)),dir);
        var item=new Item("Sillas usadas","MOBILIARIO","SILLA","UNIDAD",BigDecimal.valueOf(3),null,"USADO",null,15,2,1);
        donacion=new Donacion(3L,1L,"Sillas usadas",BigDecimal.valueOf(3),4L,"MOBILIARIO","SILLA","UNIDAD",List.of(item),"EN_DEPOSITO",null,null,java.time.LocalDateTime.of(2026,11,10,12,0));
        when(api.resumen()).thenReturn(new Resumen(1,1,1,0));
        when(api.donantes(anyMap())).thenReturn(new Pagina<>(List.of(donante),1,1,0));
        when(api.entidades(anyMap())).thenReturn(new Pagina<>(List.of(entidad),1,1,0));
        when(api.donaciones(anyMap())).thenReturn(new Pagina<>(List.of(donacion),1,1,0));
        when(api.donante(1)).thenReturn(donante); when(api.entidad(2)).thenReturn(entidad); when(api.donacion(3)).thenReturn(donacion);
        when(api.necesidades(2)).thenReturn(List.of(new Necesidad(1L,"EXTRAORDINARIA","Sillas para el comedor","SILLA","UNIDAD",BigDecimal.TEN,BigDecimal.ZERO,false)));
        when(api.historial(3)).thenReturn(List.of(new Historial("EN_DEPOSITO",java.time.LocalDateTime.of(2026,11,10,12,0),"Recibida")));
    }
    @ParameterizedTest @ValueSource(strings={"/admin","/admin/donantes","/admin/entidades","/admin/donantes/nuevo","/admin/donantes/nuevo?tipo=JURIDICA","/admin/entidades/nuevo","/admin/donantes/1","/admin/donantes/1?editar=true","/admin/entidades/2","/admin/entidades/2?editar=true","/admin/donantes/importar","/admin/donaciones","/admin/asignaciones","/admin/donaciones/nueva","/admin/donaciones/nueva?donanteId=1","/admin/donaciones/3","/admin/donaciones/3?editar=true","/admin/asignaciones/3"})
    void renderizaPantallasConDatosRealesDeContrato(String ruta) throws Exception {
        mvc.perform(get(ruta)).andExpect(status().isOk()).andExpect(content().string(containsString("<main id=\"contenido\"")))
                .andExpect(content().string(not(containsString("Sección en construcción"))));
    }
    @Test void formularioInvalidoConservaDatosYNoInvocaAlta() throws Exception {
        mvc.perform(post("/admin/donantes/nuevo").param("nombre","Nombre conservado").param("contactos[0].tipo","EMAIL").param("contactos[0].valor","incorrecto"))
                .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("form","apellido","contactos[0].valor"))
                .andExpect(content().string(containsString("Nombre conservado")));
        verify(api,never()).crearDonante(anyString(),any());
    }
    @Test void altaValidaRedirigeALaFicha() throws Exception {
        when(api.crearDonante(eq("HUMANA"),any())).thenReturn(donante);
        mvc.perform(post("/admin/donantes/nuevo").param("nombre","Ana").param("apellido","Pérez").param("edad","30").param("genero","FEMENINO")
                .param("documento","30123456").param("calle","San Martín").param("numero","120").param("ciudad","Córdoba").param("provincia","Córdoba")
                .param("contactos[0].tipo","EMAIL").param("contactos[0].valor","ana@example.org"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/admin/donantes/1"));
    }
    @Test void rechazosDelBackendSeMuestranSinBorrarElFormulario() throws Exception {
        when(api.crearDonacion(any())).thenThrow(new BackendRechazoException(ServicioBackend.DONACIONES,400,"SILLA pertenece a MOBILIARIO",Map.of()));
        mvc.perform(post("/admin/donaciones/nueva").param("donanteId","1").param("descripcion","Carga conservada")
                .param("items[0].descripcion","Sillas").param("items[0].categoria","ALIMENTOS").param("items[0].subcategoria","SILLA")
                .param("items[0].unidadMedida","UNIDAD").param("items[0].cantidad","3"))
                .andExpect(status().isOk()).andExpect(content().string(containsString("SILLA pertenece a MOBILIARIO")))
                .andExpect(content().string(containsString("Carga conservada")));
    }
    @Test void conservaTresListasYEscapaTextoDeCandidatas() throws Exception {
        var c=new Candidata(2L,"<script>alert(1)</script>","Comedor",2);
        mvc.perform(get("/admin/asignaciones/3").flashAttr("resultado",new Resultado(List.of(c),List.of(c),List.of(c))))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Compatibilidad con las necesidades")))
                .andExpect(content().string(containsString("Coincidencias de ambos algoritmos")))
                .andExpect(content().string(not(containsString("<script>alert(1)</script>"))));
    }
    @Test void consultarNoEjecutaYPostSiEjecutaLosAlgoritmos() throws Exception {
        mvc.perform(get("/admin/asignaciones/3")).andExpect(status().isOk()); verify(api,never()).ejecutar(anyLong());
        when(api.ejecutar(3)).thenReturn(new Resultado(List.of(),List.of(),List.of()));
        mvc.perform(post("/admin/asignaciones/3/ejecutar")).andExpect(redirectedUrl("/admin/asignaciones/3")); verify(api).ejecutar(3);
    }
    @Test void importacionValidaMuestraCantidadSinInventarDesglose() throws Exception {
        when(api.importar(any())).thenReturn(new Importacion(12000));
        mvc.perform(multipart("/admin/donantes/importar").file(new MockMultipartFile("archivo","donantes.csv","text/csv","cabecera\nregistro".getBytes())))
                .andExpect(redirectedUrl("/admin/donantes")).andExpect(flash().attributeExists("toasts"));
    }
    @Test void listadosVaciosOfrecenRecuperacion() throws Exception {
        when(api.donaciones(anyMap())).thenReturn(new Pagina<>(List.of(),0,0,0));
        mvc.perform(get("/admin/donaciones")).andExpect(status().isOk()).andExpect(content().string(containsString("No encontramos resultados")));
    }
}
