package ar.edu.utn.frba.dds.web.cuenta;

import ar.edu.utn.frba.dds.web.client.donaciones.AdminApi.Contacto;
import ar.edu.utn.frba.dds.web.client.donaciones.AdminApi.Humana;
import java.util.List;
import lombok.Data;
import org.springframework.validation.Errors;

/** Persona que un representante autorizado suma a su entidad. Usa su propio correo. */
@Data
public class NuevoRepresentanteForm {
    private Long entidadId;
    private String nombre = "";
    private String apellido = "";
    private String documento = "";
    private String email = "";

    public void validar(Errors errores) {
        if (entidadId == null) {
            errores.rejectValue("entidadId", "dato", "Elegí la entidad.");
        }
        AltaDonanteForm.obligatorio(errores, "nombre", nombre);
        AltaDonanteForm.obligatorio(errores, "apellido", apellido);
        AltaDonanteForm.obligatorio(errores, "documento", documento);
        if (email == null || !email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {
            errores.rejectValue("email", "dato", "Ingresá el correo de la persona representante.");
        }
    }

    public Humana datos() {
        return new Humana(nombre.trim(), apellido.trim(), null, documento.trim(), null, null,
                List.of(new Contacto("EMAIL", email.trim(), true)));
    }
}
