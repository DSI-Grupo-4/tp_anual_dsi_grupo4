package ar.edu.utn.frba.dds.donaciones.domain.personas;

import com.opencsv.CSVReader;
import java.io.FileReader;
import java.util.ArrayList;

public class ImportadorCSV extends Importador {

    public ImportadorCSV(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public void importar(String ruta) {
        listaDonantes = new ArrayList<>();
        try (CSVReader csvReader = new CSVReader(new FileReader(ruta))) {
            csvReader.readNext(); // saltar encabezado
            String[] fila;
            long id = 1L;
            while ((fila = csvReader.readNext()) != null) {
                Donante donante = crearDonante(id++, fila);
                if (donante != null) {
                    listaDonantes.add(donante);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Donante crearDonante(long id, String[] datos) {
        if (datos.length < 6) return null;

        String tipo = datos[0];
        String documento = datos[2];
        String nombreCompleto = datos[3];
        String email = datos[4];
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
        persona.agregarMedio(new MedioContacto(TipoContacto.TELEFONO, telefono, false));

        return new Donante(id, persona);
    }
}
