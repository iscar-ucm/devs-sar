/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.io.*;
import com.opencsv.*;
import com.opencsv.exceptions.CsvValidationException;

/**
 *
 * @author juanbordonruiz
 */

public class CsvIncrementalActions {
    
   public static void main(String[] args) {
       
      // ruta del archivo de entrada 
      String inputFilePath = "data" + File.separator + "scenarios" + File.separator + "Evaluator" +
              File.separator + "ver_s1_va" + File.separator + "uav_2Abs.csv";
      // ruta del archivo de salida
      String outputFilePath = "data" + File.separator + "scenarios" + File.separator + "Evaluator" +
              File.separator + "ver_s1_va" + File.separator + "uav_2Inc.csv";
      
      try {
         // Abrimos el archivo de entrada
         CSVReader reader = new CSVReader(new FileReader(inputFilePath));
         
         // Creamos el archivo de salida
         CSVWriter writer = new CSVWriter(new FileWriter(outputFilePath));
         
         // Leemos la primera fila (cabecera) del archivo de entrada
         String[] header = reader.readNext();
         writer.writeNext(header,false);
         
         // Procesamos cada línea del archivo de entrada
         String[] currentLine;
         float currentElevation = 0;
         float currentHeading = 0;
         float currentSpeed = 0;
         
         while ((currentLine = reader.readNext()) != null) {
             
            float elevation = Float.parseFloat(currentLine[0]);
            float heading = Float.parseFloat(currentLine[1]);
            float speed = Float.parseFloat(currentLine[2]);
            float time = Float.parseFloat(currentLine[3]);
            
            float deltaElevation = elevation - currentElevation;
            float deltaHeading = heading - currentHeading;
            float deltaSpeed = speed - currentSpeed;
            
            String[] outputLine = {
               String.valueOf(deltaElevation),
               String.valueOf(deltaHeading),
               String.valueOf(deltaSpeed),
               String.valueOf(time)
            };
            
            writer.writeNext(outputLine,false);
            
            currentElevation = elevation;
            currentHeading = heading;
            currentSpeed = speed;
         }
         
         // Cerramos los archivos
         reader.close();
         writer.close();
         
      } catch (CsvValidationException e) { 
         e.printStackTrace();         
      } catch (IOException e) { 
         e.printStackTrace();
      } catch (NumberFormatException e) {
         e.printStackTrace();
      }
   }
}

