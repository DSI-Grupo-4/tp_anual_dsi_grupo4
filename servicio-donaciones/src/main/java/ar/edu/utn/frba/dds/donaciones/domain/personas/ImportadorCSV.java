package ar.edu.utn.frba.dds.donaciones.domain.personas;
import com.opencsv.*;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
public class ImportadorCSV {
    FileReader archCSV=null;
    CSVReader csvReader=null;

    public void importar(String rutaArchivo){
        try{
        archCSV = new FileReader(rutaArchivo);
        csvReader = new CSVReader(archCSV);
        String[] fila=null;
        while ((fila=csvReader.readNext())!=null){
                System.out.println(fila[0]
              + " | " + fila[1]
              + " |  " + fila[2]);
        }
        }
        finally{
            csvReader.Close();
        }
    }

    public registrarDonante(String email){

    }
}
