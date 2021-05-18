/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import com.opencsv.CSVWriter;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.ejml.ops.MatrixIO;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import models.sensor.Sensor;
import models.sensor.SensorCntrlSignals;
import models.sensor.SensorState;
import models.uav.Uav;
import models.uav.UavState;
import models.target.Target;
import models.target.TargetState;
import models.uav.UavCntrlSignals;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;

/**
 *
 * @author Juan
 */
public class CSVHandler {

    private static final Logger LOGGER = Logger.getLogger(CSVHandler.class.getName());
    private static final String CSVPATH = "data" + File.separator + "csv" + File.separator;
    private static String loadPath;
    private static String devsPath;
    private static String scenarioPath;
    private static String uavsPath;
    private static String targetsPath;
    private static String algorythmPath;

    public static void configure(String devsSpec, String tool, String scenarioName) {
        // set path to load files
        loadPath = "data" + File.separator + "scenarios" + File.separator + tool
                + File.separator + scenarioName + File.separator;
        // scenario folder & file creation        
        devsPath = CSVPATH + devsSpec + File.separator + tool + File.separator;
        File devsFolder = new File(devsPath);
        devsFolder.mkdir();
        // scenario folder & file creation        
        scenarioPath = devsPath + scenarioName + File.separator;
        File scenarioFolder = new File(scenarioPath);
        scenarioFolder.mkdir();
        uavsPath = scenarioPath + "uavs" + File.separator;
        File uavsFolder = new File(uavsPath);
        uavsFolder.mkdir();
        targetsPath = scenarioPath + "targets" + File.separator;
        File targetsFolder = new File(targetsPath);
        targetsFolder.mkdir();
    }

    public static ArrayList<UavCntrlSignals> loadUavCntrl(String uavName) {

        // uav signals to return
        ArrayList<UavCntrlSignals> uavCntrlSignals = new ArrayList<>();
        try {
            // load csv File
            FileReader reader = new FileReader(loadPath + uavName + "Cntrl.csv");
            CsvToBean<UavCntrlSignals> csvToBean = new CsvToBeanBuilder(reader)
                    .withType(UavCntrlSignals.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            Iterator<UavCntrlSignals> csvIterator = csvToBean.iterator();

            while (csvIterator.hasNext()) {
                UavCntrlSignals iUavCntrl = csvIterator.next();
                uavCntrlSignals.add(iUavCntrl);
            }

        } catch (IOException ex) {
            Logger.getLogger(CSVHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

        return uavCntrlSignals;
    }

    public static void writeUav(Uav uav) {
        // uav folder & file creation
        String uavFolderPath = uavsPath + uav.getName() + File.separator;
        File uavFolder = new File(uavFolderPath);

        // delete previous files if exist        
        if (!uavFolder.exists()) {
            // create tgt folder
            uavFolder.mkdir();
        } else {
            // make sure folder is empty
            String[] entries = uavFolder.list();
            for (String s : entries) {
                File currentFile = new File(uavFolder.getPath(), s);
                currentFile.delete();
            }
        }

        String uavCsvPath = uavFolderPath + uav.getName() + "Path.csv";
        File uavPathFile = new File(uavCsvPath);
        String uavCsvData = uavFolderPath + uav.getName() + "Data.csv";
        File uavDataFile = new File(uavCsvData);
        String uavCsvCntrl = uavFolderPath + uav.getName() + "Cntrl.csv";
        File uavCntrlFile = new File(uavCsvCntrl);

        try {
            // create FileWriter object with file as parameter 
            FileWriter outputfile1 = new FileWriter(uavPathFile);
            FileWriter outputfile2 = new FileWriter(uavDataFile);
            FileWriter outputfile3 = new FileWriter(uavCntrlFile);

            // create CSVWriter object filewriter object as parameter 
            CSVWriter writer1 = new CSVWriter(outputfile1,
                    CSVWriter.DEFAULT_SEPARATOR,
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END);
            CSVWriter writer2 = new CSVWriter(outputfile2,
                    CSVWriter.DEFAULT_SEPARATOR,
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END);
            CSVWriter writer3 = new CSVWriter(outputfile3,
                    CSVWriter.DEFAULT_SEPARATOR,
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END);

            // create a List which contains String array 
            List<String[]> data1 = new ArrayList<>();
            List<String[]> data2 = new ArrayList<>();

            // first row with the names of the fields
            data1.add(new String[]{"x", "y", "z", "heading", "airspeed", "fuel", "time"});
            data2.add(new String[]{"nfzs", "collisions", "fuelEmpties"});

            // go through uav path
            for (int i = 0; i < uav.getPath().size(); ++i) {
                // convert i uavState to a csv row
                UavState iState = uav.getPath().get(i);
                String[] row = new String[7];
                row[0] = String.valueOf(iState.getX());
                row[1] = String.valueOf(iState.getY());
                row[2] = String.valueOf(iState.getHeight());
                row[3] = String.valueOf(iState.getHeading());
                row[4] = String.valueOf(iState.getAirSpeed());
                row[5] = String.valueOf(iState.getFuel());
                row[6] = String.valueOf(iState.getTime());
                //add the row to the data1
                data1.add(row);
            }

            // uav data
            data2.add(new String[]{
                String.valueOf(uav.getTotalNFZs()),
                String.valueOf(uav.getTotalCollisions()),
                String.valueOf(uav.getTotalFuelEmpties())
            });

            StatefulBeanToCsv<UavCntrlSignals> beanToCsv3 = new StatefulBeanToCsvBuilder(writer3)
                    .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                    .build();

            // wite the data1 to the writer
            writer1.writeAll(data1);
            writer2.writeAll(data2);
            beanToCsv3.write(uav.getCntrlSignals());

            // closing writer connection 
            writer1.close();
            writer2.close();
            writer3.close();

            // go through uav sensors
            for (int s = 0; s < uav.getSensors().size(); ++s) {
                if (!uav.getSensors().get(s).isStatic()) {
                    // only write csv file for dinamic sensors
                    writeSensor(uavFolderPath, uav.getSensors().get(s));
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block 
            LOGGER.severe(e.getLocalizedMessage());
        } catch (CsvDataTypeMismatchException | CsvRequiredFieldEmptyException ex) {
            Logger.getLogger(CSVHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static ArrayList<SensorCntrlSignals> loadSensorCntrl(String sensorName) {

        // uav signals to return
        ArrayList<SensorCntrlSignals> sensorCntrlSignals = new ArrayList<>();
        try {
            // load csv File
            FileReader reader = new FileReader(loadPath + sensorName + "Cntrl.csv");
            CsvToBean<SensorCntrlSignals> csvToBean = new CsvToBeanBuilder(reader)
                    .withType(SensorCntrlSignals.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            Iterator<SensorCntrlSignals> csvIterator = csvToBean.iterator();

            while (csvIterator.hasNext()) {
                SensorCntrlSignals iSensorCntrl = csvIterator.next();
                sensorCntrlSignals.add(iSensorCntrl);
            }

        } catch (IOException ex) {
            Logger.getLogger(CSVHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

        return sensorCntrlSignals;
    }

    public static void writeSensor(String uavFolderPath, Sensor sensor) {
        // sensor folder & file creation   	
        String sensorFolderPath = uavFolderPath + sensor.getName() + File.separator;
        File sensorFolder = new File(sensorFolderPath);

        // delete previous files if exist        
        if (!sensorFolder.exists()) {
            // create tgt folder
            sensorFolder.mkdir();
        } else {
            // make sure folder is empty
            String[] entries = sensorFolder.list();
            for (String s : entries) {
                File currentFile = new File(sensorFolder.getPath(), s);
                currentFile.delete();
            }
        }

        String sensorCsvPath = sensorFolderPath + sensor.getName() + "Path.csv";
        File sensorPathFile = new File(sensorCsvPath);
        String sensorCsvCntrl = sensorFolderPath + sensor.getName() + "Cntrl.csv";
        File sensorCntrlFile = new File(sensorCsvCntrl);

        try {
            // create FileWriter object with file as parameter 
            FileWriter outputfile1 = new FileWriter(sensorPathFile);
            FileWriter outputfile2 = new FileWriter(sensorCntrlFile);

            // create CSVWriter object filewriter object as parameter 
            CSVWriter writer1 = new CSVWriter(outputfile1,
                    CSVWriter.DEFAULT_SEPARATOR,
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END);
            CSVWriter writer2 = new CSVWriter(outputfile2,
                    CSVWriter.DEFAULT_SEPARATOR,
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END);

            // create a List which contains String array 
            List<String[]> data1 = new ArrayList<>();
            List<String[]> data2 = new ArrayList<>();

            // first row with the names of the fields
            data1.add(new String[]{"azimuth", "elevation", "time"});
            data2.add(new String[]{"cAzimuth", "cElevation", "time"});

            // go through sensor path
            for (int i = 0; i < sensor.getPath().size(); ++i) {
                // convert i uavState to a csv row
                SensorState iState = sensor.getPath().get(i);
                String[] row = new String[3];
                row[0] = String.valueOf(iState.getAzimuth());
                row[1] = String.valueOf(iState.getElevation());
                row[2] = String.valueOf(iState.getTime());
                //add the row to the data1
                data1.add(row);
            }

            // go through sensorCntrlSignals
            for (int i = 0; i < sensor.getCntrlSignals().size(); ++i) {
                // convert i uavState to a csv row
                SensorCntrlSignals iCntrl = sensor.getCntrlSignals().get(i);
                String[] row = new String[3];
                row[0] = String.valueOf(iCntrl.getcAzimuth());
                row[1] = String.valueOf(iCntrl.getcElevation());
                row[2] = String.valueOf(iCntrl.getTime());
                //add the row to the data1
                data2.add(row);
            }

            // wite the data
            writer1.writeAll(data1);
            writer2.writeAll(data2);

            // closing connection 
            writer1.close();
            writer2.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block 
            LOGGER.severe(e.getLocalizedMessage());
        }
    }

    public static DMatrixRMaj loadTarget(String targetName) {

        // target belief to return
        DMatrixRMaj targetBelief = new DMatrixRMaj();
        try {
            // load belief from file
            targetBelief = MatrixIO.loadCSV(loadPath + targetName + "State0.csv", true);

        } catch (IOException ex) {
            Logger.getLogger(CSVHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

        return targetBelief;
    }

    public static void writeTarget(Target target) {

        // target folder & file creation  	
        String targetFolderPath = targetsPath + target.getName() + File.separator;
        File targetFolder = new File(targetFolderPath);

        // delete previous files if exist
        if (!targetFolder.exists()) {
            // create tgt folder
            targetFolder.mkdir();
        } else {
            // make sure folder is empty
            String[] entries = targetFolder.list();
            for (String s : entries) {
                File currentFile = new File(targetFolder.getPath(), s);
                currentFile.delete();
            }
        }

        // write sequence data
        String targetCsvPath = targetFolderPath + target.getName() + "Path.csv";
        File targetFile = new File(targetCsvPath);

        try {

            // create FileWriter object with file as parameter 
            FileWriter outputfile = new FileWriter(targetFile);

            // create CSVWriter object filewriter object as parameter 
            CSVWriter writer = new CSVWriter(outputfile);

            // create a List which contains String array 
            List<String[]> data = new ArrayList<>();

            // first row with the names of the fields
            data.add(new String[]{"dp", "etd", "time"});

            // variables used to calculate etd, dp & heurist
            double dp, elapsedTime;
            double prevTime = target.getPath().get(0).getTime();
            double probLost = 0.0;
            double etd = 0.0;

            // go through target path
            for (int i = 0; i < target.getPath().size(); ++i) {

                TargetState iState = target.getPath().get(i);

                // save ibelief into csv file
                MatrixIO.saveDenseCSV(
                        iState.getBelief(),
                        targetFolderPath
                        + target.getName() + "State" + i + ".csv"
                );

                String[] row = new String[3];

                // probability lost accumulation
                probLost += iState.getcMissPd();
                // dp calculation
                dp = 1.0 - CommonOps_DDRM.elementSum(iState.getBelief()) - probLost;
                // etd calculation
                if (!iState.isPrediction()) {
                    elapsedTime = iState.getTime() - prevTime;
                    prevTime = iState.getTime();
                    etd += (CommonOps_DDRM.elementSum(iState.getBelief()) + probLost) * elapsedTime;
                }

                //add the row to the data1                
                row[0] = String.valueOf(dp);
                row[1] = String.valueOf(etd);
                row[2] = String.valueOf(iState.getTime());
                data.add(row);
            }

            // wite the data1 to the writer1
            writer.writeAll(data);

            // closing writer1 connection 
            writer.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block 
            LOGGER.severe(e.getLocalizedMessage());
        }

    }

    public static void writeTargetPath(Target target) {
        // target folder & file creation  	
        String targetFolderPath = targetsPath + target.getName() + File.separator;

        try {
            // go through target path
            for (int i = 0; i < target.getPath().size(); ++i) {
                TargetState iState = target.getPath().get(i);
                // check if the targetState has a belief to write
                if (iState.getBelief() != null) {
                    MatrixIO.saveDenseCSV(
                            iState.getBelief(),
                            targetFolderPath
                            + target.getName() + "State" + i + ".csv"
                    );
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block 
            LOGGER.severe(e.getLocalizedMessage());
        }

    }

    public static void writeProfile(String profile) {

        // profile file
        String profileCsvPath = scenarioPath + "profile.csv";
        File profileFile = new File(profileCsvPath);

        try {
            // create FileWriter object with file as parameter 
            FileWriter outputfile = new FileWriter(profileFile);

            // wite the data1 to the writer
            outputfile.write(profile);

            // closing writer connection 
            outputfile.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block 
            LOGGER.severe(e.getLocalizedMessage());
        }

    }

}
