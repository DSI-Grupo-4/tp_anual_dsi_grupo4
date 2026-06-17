package ar.edu.utn.frba.dds.donaciones.domain.personas;
import com.opencsv.*;
import java.io.*;
import java.util.*;

public class ImportadorCSV extends Importador{

    private FileReader archCSV=null;
    private CSVReader csvReader=null;

    public void importar(String rutaArchivo){
        try{
        archCSV = new FileReader(rutaArchivo);
        csvReader = new CSVReader(archCSV);
        String[] fila=null;
        while ((fila=csvReader.readNext())!=null){
            String[] datosDonanteT=fila;
            listaimportador.add(registrarDonante(datosDonanteT));
        }
        }
        finally{
            csvReader.Close();
        }
    }

    public Donante registrarDonante(String[] datoscsv){
        for(Donante donante: super.getdonantesRegistrados()){
            if (donante.personaAsociada.Medios.contains(datoscsv[4])){ //lo asocia con la fila donde se ubica mail, en este caso la quinta
                if (datoscsv[0] =="HUMANA")
                    {
                        donante.personaAsociada.DNI=datoscsv[2];
                        donante.personaAsociada.Nombre=datoscsv[3];
                        donante.personaAsociada.removeMedio(EMAIL,donante.medios.value);
                        donante.personaAsociada.addMedio(EMAIL,datoscsv[4]);
                        donante.personaAsociada.addMedio(TELEFONO,datoscsv[5]);
                        break;
                    }
                if (datoscsv[0]=="JURIDICA")
                    {
                        donante.personaAsociada.representante.DNI=datoscsv[2]; //solo permitimos un representante válido en caso de donadores
                        donante.personaAsociada.RazonSocial=datoscsv[2];
                        donante.personaAsociada.removeMedio(EMAIL,donante.medios.value);
                        donante.personaAsociada.addMedio(EMAIL,datoscsv[4]);
                        donante.personaAsociada.addMedio(TELEFONO,datoscsv[5]);
                        break;
                    }   
                }
                else
                nuevodonante=generarnuevoDonante(datoscsv);
            }    
        }

    public Donante generarnuevoDonante(String[] datos){
        Donante nuevodonante= new Donante();
        if (datos[0] =="HUMANA")
                {
                    nuevodonante.personaAsociada.DNI=datoscsv[2];
                    nuevodonante.personaAsociada.Nombre=datoscsv[3];
                    nuevodonante.personaAsociada.addMedio(EMAIL,datoscsv[4]);
                    nuevodonante.personaAsociada.addMedio(TELEFONO,datoscsv[5]);
                }
        if (datos[0]=="JURIDICA"){
                    nuevodonante.personaAsociada.representante.DNI=datoscsv[2]; //solo permitimos un representante válido en caso de donadores
                    nuevodonante.personaAsociada.RazonSocial=datoscsv[2];
                    nuevodonante.personaAsociada.addMedio(EMAIL,datoscsv[4]);
                    nuevodonante.personaAsociada.addMedio(TELEFONO,datoscsv[5]);   
                }
        return nuevoDonante;
        }
    
    public static void read(String ruta) {
        String archivoCSV=ruta;

        try (CSVReader csvReader = new CSVReader(new FileReader(archivoCSV))) {
            String[] fila;
            while ((fila = csvReader.readNext()) != null) {
                System.out.println("Columna 1: " + fila[0] + " | Columna 2: " + fila[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
