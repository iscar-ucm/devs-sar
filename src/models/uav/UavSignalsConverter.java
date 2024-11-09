/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.uav;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author juanbordonruiz
 */
public class UavSignalsConverter {

    public ArrayList<UavCntrlSignals> absToInc(ArrayList<UavCntrlSignals> absoluteSignals) {

        ArrayList<UavCntrlSignals> incrementalSignals = new ArrayList<>();

        UavCntrlSignals currentSignals;
        double currentElevation = 0;
        double currentHeading = 0;
        double currentSpeed = 0;

        for (short i = 0; i < absoluteSignals.size(); i++) {

            currentSignals = absoluteSignals.get(i);
            double elevation = currentSignals.getcElevation();
            double heading = currentSignals.getcHeading();
            double speed = currentSignals.getcSpeed();
            double time = currentSignals.getTime();

            UavCntrlSignals deltaSignals = new UavCntrlSignals(
                    elevation - currentElevation,
                    heading - currentHeading,
                    speed - currentSpeed,
                    time);

            currentElevation = elevation;
            currentHeading = heading;
            currentSpeed = speed;

            incrementalSignals.add(deltaSignals);

        }

        return incrementalSignals;
    }
    
    public ArrayList<UavCntrlSignals> incToAbs(ArrayList<UavCntrlSignals> incrementalSignals) {

        ArrayList<UavCntrlSignals> absoluteSignals = new ArrayList<>();

        double currentElevation = 0;
        double currentHeading = 0;
        double currentSpeed = 0;

        for (short i = 0; i < incrementalSignals.size(); i++) {

            UavCntrlSignals currentSignals = incrementalSignals.get(i);

            double deltaElevation = currentSignals.getcElevation();
            double deltaHeading = currentSignals.getcHeading();
            double deltaSpeed = currentSignals.getcSpeed();
            double time = currentSignals.getTime();

            double elevation = currentElevation + deltaElevation;
            double heading = currentHeading + deltaHeading;
            double speed = currentSpeed + deltaSpeed;

            UavCntrlSignals absoluteSignal = new UavCntrlSignals(elevation, heading, speed, time);

            currentElevation = elevation;
            currentHeading = heading;
            currentSpeed = speed;

            absoluteSignals.add(absoluteSignal);
        }

        return absoluteSignals;
    }

    public static void main(String[] args) {

        // ruta del archivo de entrada 
        String inputFilePath = "data" + File.separator + "scenarios" + File.separator + "Evaluator"
                + File.separator + "TesisEvaluador01_S_A" + File.separator + "uav_1Abs.csv";
        // ruta del archivo de salida
        String outputFilePath = "data" + File.separator + "scenarios" + File.separator + "Evaluator"
                + File.separator + "TesisEvaluador01_S_A" + File.separator + "uav_1Inc.csv";

        try {
            // Abrimos el archivo de entrada
            CSVReader reader = new CSVReader(new FileReader(inputFilePath));

            // Creamos el archivo de salida
            CSVWriter writer = new CSVWriter(new FileWriter(outputFilePath));

            // Leemos la primera fila (cabecera) del archivo de entrada
            String[] header = reader.readNext();
            writer.writeNext(header, false);

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

                writer.writeNext(outputLine, false);

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
