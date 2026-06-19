package ar.edu.utn.frba.dds.donaciones.domain.personas;

import com.opencsv.CSVReader;
import lombok.Getter;
import lombok.Setter;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class ImportadorCSV extends Importador {

    private CSVReader csvReader;
    private FileReader archivo;

    public ImportadorCSV(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public void realizarRuta() {
        tallaImportador = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(nombre))) {
            this.csvReader = reader;
            reader.readNext();
            List<String[]> filas = reader.readAll();
            for (String[] fila : filas) {
                Donante donante = generarDonante(Collections.singletonList(fila));
                if (donante != null) {
                    registrarDonante(Collections.singletonList(fila));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Donante generarDonante(List<String[]> filas) {
        if (filas.isEmpty()) return null;
        String[] datos = filas.get(0);
        if (datos.length < 6) return null;

        String tipo = datos[0];
        String nombreCompleto = datos[3];
        String email = datos[4];
        String telefono = datos[5];

        Persona persona;
        if ("HUMANA".equals(tipo)) {
            String[] partes = nombreCompleto.split(" ", 2);
            String pNombre = partes[0];
            String apellido = partes.length > 1 ? partes[1] : "";
            persona = new PersonaHumana(pNombre, apellido, TipoDoc.DNI, 0, null);
        } else if ("JURIDICA".equals(tipo)) {
            persona = new PersonaJuridica(nombreCompleto, null, null);
        } else {
            return null;
        }

        persona.addMedioContacto(TipoMedio.EMAIL, email);
        persona.addMedioContacto(TipoMedio.TELEFONO, telefono);

        return new Donante(persona);
    }

    public Donante registrarDonante(List<String[]> filas) {
        Donante donante = generarDonante(filas);
        if (donante != null) {
            tallaImportador.add(donante);
        }
        return donante;
    }
}
