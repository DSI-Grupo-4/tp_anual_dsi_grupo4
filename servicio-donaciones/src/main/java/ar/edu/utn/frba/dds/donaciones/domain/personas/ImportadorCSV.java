package ar.edu.utn.frba.dds.donaciones.domain.personas;

import com.opencsv.CSVReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ImportadorCSV extends Importador {

    public ImportadorCSV(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public void importar(InputStream inputStream) {

        personasImportadas = new ArrayList<>();

        try (CSVReader csvReader =
                     new CSVReader(new InputStreamReader(inputStream, java.nio.charset.StandardCharsets.UTF_8))) {

            csvReader.readNext();

            String[] fila;

            while ((fila = csvReader.readNext()) != null) {

                Persona persona = crearPersona(fila);

                if (persona != null) {
                    personasImportadas.add(persona);
                }
            }

        } catch (Exception e) {
            personasImportadas.clear();
            throw new IllegalArgumentException("CSV inválido: " + e.getMessage(), e);
        }
    }

    private Persona crearPersona(String[] datos) {
        if (datos.length < 6) throw new IllegalArgumentException("Cada fila requiere seis columnas");

        String tipo = datos[0];
        String documento = datos[2];
        String nombreCompleto = datos[3];
        String email = datos[4].trim();
        if (!email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) throw new IllegalArgumentException("EMAIL obligatorio o inválido: " + email);
        String telefono = datos[5];

        Persona persona;
        if ("HUMANA".equals(tipo)) {
            String[] partes = nombreCompleto.split(" ", 2);
            String nombre = partes[0];
            String apellido = partes.length > 1 ? partes[1] : "";
            persona = new PersonaHumana(nombre, apellido, null, documento, null);
        } else if ("JURIDICA".equals(tipo)) {
            persona = new PersonaJuridica(nombreCompleto, null, null, null);
        } else {
            return null;
        }

        persona.agregarMedio(new MedioContacto(TipoContacto.EMAIL, email, true));
        if (!telefono.isBlank()) persona.agregarMedio(new MedioContacto(TipoContacto.TELEFONO, telefono.trim(), false));

        return persona;
    }
}
