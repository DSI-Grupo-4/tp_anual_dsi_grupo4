package ar.edu.utn.frba.dds.web.cuenta;

import ar.edu.utn.frba.dds.web.client.donaciones.AdminApi.Contacto;
import ar.edu.utn.frba.dds.web.client.donaciones.AdminApi.Humana;
import ar.edu.utn.frba.dds.web.client.donaciones.AdminApi.Juridica;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.Data;
import org.springframework.validation.Errors;

/**
 * Organización que dona. El alta administrativa admite un único representante:
 * en el autorregistro es la persona de la cuenta, con su correo verificado.
 */
@Data
public class AltaDonanteJuridicoForm {
    private String razonSocial = "";
    private String tipoOrganizacion = "EMPRESA";
    private String rubro = "";
    private String telefono = "";
    private String nombre = "";
    private String apellido = "";
    private String documento = "";

    public void validar(Errors errores) {
        AltaDonanteForm.obligatorio(errores, "razonSocial", razonSocial);
        if (!Set.of("ONG", "EMPRESA", "INSTITUCION", "GUBERNAMENTAL").contains(tipoOrganizacion)) {
            errores.rejectValue("tipoOrganizacion", "dato", "Elegí un tipo de organización.");
        }
        AltaDonanteForm.obligatorio(errores, "nombre", nombre);
        AltaDonanteForm.obligatorio(errores, "apellido", apellido);
        AltaDonanteForm.obligatorio(errores, "documento", documento);
        if (telefono != null && !telefono.isBlank() && telefono.trim().length() > 40) {
            errores.rejectValue("telefono", "dato", "El teléfono es demasiado largo.");
        }
    }

    public Juridica datos(String email) {
        List<Contacto> contactos = new ArrayList<>();
        contactos.add(new Contacto("EMAIL", email, true));
        if (telefono != null && !telefono.isBlank()) {
            contactos.add(new Contacto("TELEFONO", telefono.trim(), false));
        }
        var representante = new Humana(nombre.trim(), apellido.trim(), null, documento.trim(), null, null,
                List.of(new Contacto("EMAIL", email, true)));
        return new Juridica(razonSocial.trim(), tipoOrganizacion, rubro == null ? "" : rubro.trim(),
                contactos, List.of(representante));
    }
}
