package ar.edu.utn.frba.dds.web.cuenta;

import ar.edu.utn.frba.dds.web.client.donaciones.AdminApi.Ciudad;
import ar.edu.utn.frba.dds.web.client.donaciones.AdminApi.Contacto;
import ar.edu.utn.frba.dds.web.client.donaciones.AdminApi.Direccion;
import ar.edu.utn.frba.dds.web.client.donaciones.AdminApi.Entidad;
import ar.edu.utn.frba.dds.web.client.donaciones.AdminApi.Humana;
import ar.edu.utn.frba.dds.web.client.donaciones.AdminApi.Juridica;
import ar.edu.utn.frba.dds.web.client.donaciones.AdminApi.Provincia;
import java.util.List;
import java.util.Set;
import lombok.Data;
import org.springframework.validation.Errors;

/**
 * Entidad nueva. Quien completa el formulario es el primer representante
 * y su correo es el de la cuenta, no un correo que elija en el formulario.
 */
@Data
public class AltaEntidadForm {
    private String razonSocial = "";
    private String tipoOrganizacion = "ONG";
    private String rubro = "";
    private String descripcion = "";
    private String calle = "";
    private String numero = "";
    private String ciudad = "";
    private String provincia = "";
    private String telefono = "";
    private String nombre = "";
    private String apellido = "";
    private String documento = "";

    public void validar(Errors errores) {
        AltaDonanteForm.obligatorio(errores, "razonSocial", razonSocial);
        if (!Set.of("ONG", "EMPRESA", "INSTITUCION", "GUBERNAMENTAL").contains(tipoOrganizacion)) {
            errores.rejectValue("tipoOrganizacion", "dato", "Elegí un tipo de organización.");
        }
        AltaDonanteForm.obligatorio(errores, "descripcion", descripcion);
        AltaDonanteForm.obligatorio(errores, "calle", calle);
        AltaDonanteForm.obligatorio(errores, "numero", numero);
        AltaDonanteForm.obligatorio(errores, "ciudad", ciudad);
        AltaDonanteForm.obligatorio(errores, "provincia", provincia);
        AltaDonanteForm.obligatorio(errores, "telefono", telefono);
        AltaDonanteForm.obligatorio(errores, "nombre", nombre);
        AltaDonanteForm.obligatorio(errores, "apellido", apellido);
        AltaDonanteForm.obligatorio(errores, "documento", documento);
    }

    public Entidad datos(String email) {
        var direccion = new Direccion(calle.trim(), numero.trim(), new Ciudad(ciudad.trim(), new Provincia(provincia.trim())));
        var representante = new Humana(nombre.trim(), apellido.trim(), null, documento.trim(), null, null,
                List.of(new Contacto("EMAIL", email, true)));
        var juridica = new Juridica(razonSocial.trim(), tipoOrganizacion, rubro == null ? "" : rubro.trim(),
                List.of(new Contacto("EMAIL", email, true), new Contacto("TELEFONO", telefono.trim(), false)),
                List.of(representante));
        return new Entidad(null, null, descripcion.trim(), juridica, direccion);
    }
}
