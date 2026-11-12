package ar.edu.utn.frba.dds.web.cuenta;

import ar.edu.utn.frba.dds.web.client.donaciones.AdminApi.Ciudad;
import ar.edu.utn.frba.dds.web.client.donaciones.AdminApi.Contacto;
import ar.edu.utn.frba.dds.web.client.donaciones.AdminApi.Direccion;
import ar.edu.utn.frba.dds.web.client.donaciones.AdminApi.Humana;
import ar.edu.utn.frba.dds.web.client.donaciones.AdminApi.Provincia;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.Data;
import org.springframework.validation.Errors;

/** Datos de un donante humano. El correo lo fija el controller con el de la cuenta. */
@Data
public class AltaDonanteForm {
    private String nombre = "";
    private String apellido = "";
    private String documento = "";
    private String genero = "";
    private Integer edad;
    private String calle = "";
    private String numero = "";
    private String ciudad = "";
    private String provincia = "";
    private String telefono = "";

    public void validar(Errors errores) {
        obligatorio(errores, "nombre", nombre);
        obligatorio(errores, "apellido", apellido);
        obligatorio(errores, "documento", documento);
        if (edad == null || edad < 0 || edad > 130) {
            errores.rejectValue("edad", "dato", "Ingresá una edad entre 0 y 130.");
        }
        if (!Set.of("MASCULINO", "FEMENINO", "X").contains(genero)) {
            errores.rejectValue("genero", "dato", "Elegí una opción.");
        }
        direccion(errores);
        if (telefono != null && !telefono.isBlank() && telefono.trim().length() > 40) {
            errores.rejectValue("telefono", "dato", "El teléfono es demasiado largo.");
        }
    }

    public Humana datos(String email) {
        List<Contacto> contactos = new ArrayList<>();
        contactos.add(new Contacto("EMAIL", email, true));
        if (telefono != null && !telefono.isBlank()) {
            contactos.add(new Contacto("TELEFONO", telefono.trim(), false));
        }
        return new Humana(nombre.trim(), apellido.trim(), edad, documento.trim(), genero,
                new Direccion(calle.trim(), numero.trim(), new Ciudad(ciudad.trim(), new Provincia(provincia.trim()))),
                contactos);
    }

    void direccion(Errors errores) {
        obligatorio(errores, "calle", calle);
        obligatorio(errores, "numero", numero);
        obligatorio(errores, "ciudad", ciudad);
        obligatorio(errores, "provincia", provincia);
    }

    static void obligatorio(Errors errores, String campo, String valor) {
        if (valor == null || valor.isBlank()) {
            errores.rejectValue(campo, "dato", "Completá este campo.");
        }
    }
}
