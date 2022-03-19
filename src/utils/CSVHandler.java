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
import models.optimizer.Solution;
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
    private static String loadTarget;
    private final String loadPath;
    private final String writePath;
    private String optimizerPath;
    private String runPath;
    private String uavsPath;
    private String targetsPath;
    private List<String[]> iterationsData;
    private List<String[]> runsData;

    public CSVHandler(String devsSpec, String tool, String scenarioName) {
        // set path to load files
        loadPath = "data" + File.separator + "scenarios" + File.separator + tool
                + File.separator + scenarioName + File.separator;
        loadTarget = loadPath;
        // create the folder to write results     
        writePath = CSVPATH + devsSpec + File.separator + tool + File.separator
                + scenarioName + File.separator;
        File scenarioFolder = new File(writePath);
        if (!scenarioFolder.exists()) {
            // create scenario folder 
            scenarioFolder.mkdir();
        } else {
            // delete possible previous files
            deleteFiles(scenarioFolder);
        }
        if (!tool.equals("Optimizer") && !tool.equals("Islands")) {
            // folders creation  
            uavsPath = writePath + "uavs" + File.separator;
            File uavsFolder = new File(uavsPath);
            uavsFolder.mkdir();
            targetsPath = writePath + "targets" + File.separator;
            File targetsFolder = new File(targetsPath);
            targetsFolder.mkdir();
        } else {
            // init data
            iterationsData = new ArrayList<>();
            runsData = new ArrayList<>();
            // first row with the names of the fields
            runsData.add(new String[]{"run", "sol", "dp", "etd", "heurist", "smoothness", "nfzs", "collisions"});
        }
    }

    public void setOptimizerPath(int oIndex, String algorithm) {
        optimizerPath = writePath + "Op" + oIndex + algorithm.toUpperCase() + File.separator;
        File optimizerFolder = new File(optimizerPath);
        optimizerFolder.mkdir();
    }

    public void writeProfile(String profile) {

        // profile file
        String profileCsvPath = writePath + "profile.csv";
        File profileFile = new File(profileCsvPath);

        try {
            // create FileWriter object with file as parameter 
            FileWriter outputfile = new FileWriter(profileFile);

            // wite the data to the writer
            outputfile.write(profile);

            // closing writer connection 
            outputfile.close();

        } catch (IOException e) {
            // Auto-generated catch block 
            LOGGER.severe(e.getLocalizedMessage());
        }

    }

    public void writeOpResults() {
        // optimizer file
        String runsFilePath = this.optimizerPath + "runs.csv";
        File runsFile = new File(runsFilePath);

        try {
            // create FileWriter object with file as parameter 
            FileWriter outputfile = new FileWriter(runsFile);

            // create CSVWriter object filewriter object as parameter 
            CSVWriter writer = new CSVWriter(outputfile);

            // wite the data to the writer
            writer.writeAll(runsData);

            // closing writer connection 
            writer.close();

            // reset runsData for next optimizer            
            runsData = new ArrayList<>();
            runsData.add(new String[]{"run", "sol", "dp", "etd", "heurist", "smoothness", "nfzs", "collisions"});

        } catch (IOException e) {
            // Auto-generated catch block 
            LOGGER.severe(e.getLocalizedMessage());
        }
    }

    public void writeRun(int run, Solution runSolution) {
        // folders creation
        if (run < 10) {
            runPath = optimizerPath + '0' + run + File.separator;
        } else {
            runPath = optimizerPath + run + File.separator;
        }
        File runFolder = new File(runPath);
        runFolder.mkdir();

        Solution currentSol = runSolution.clone();

        uavsPath = runPath + "uavs" + File.separator;
        File uavsFolder = new File(uavsPath);
        uavsFolder.mkdir();

        // write uavs results
        currentSol.getUavs().forEach(solUav -> {
            writeUavCntrl(solUav);
        });

        // write target results
        currentSol.getTgts().forEach(solTgt -> {
            // writeTarget(solTgt);
            // write current run objectives into optimizer file
            // AHORA MISMO SOLO SUPONEMOS UN TARGET
            String[] row = new String[8];
            row[0] = String.valueOf(run);
            row[1] = currentSol.getId().toString();
            row[2] = String.valueOf(solTgt.getDp());
            row[3] = String.valueOf(solTgt.getEtd());
            row[4] = String.valueOf(solTgt.getHeuristic());
            // go through uavs
            int totalNFZs = 0;
            int totalCollisions = 0;
            double totalSmoothness = 0.0;
            for (int u = 0; u < currentSol.getUavs().size(); ++u) {
                totalNFZs += currentSol.getUavs().get(u).getTotalNFZs();
                totalCollisions += currentSol.getUavs().get(u).getTotalCollisions();
                totalSmoothness += currentSol.getUavs().get(u).getSmoothValue();
            }
            row[5] = String.valueOf(totalSmoothness);
            row[5] = String.valueOf(totalNFZs);
            row[6] = String.valueOf(totalCollisions);
            runsData.add(row);
        });

        // write current run iterations file
        String iterationsCsvPath = runPath + "iterations.csv";
        File iterationsFile = new File(iterationsCsvPath);

        try {
            // create FileWriter object with file as parameter 
            FileWriter outputfile = new FileWriter(iterationsFile);

            // create CSVWriter object filewriter object as parameter 
            CSVWriter writer = new CSVWriter(outputfile);

            // wite the data to the writer
            writer.writeAll(iterationsData);

            // closing writer connection 
            writer.close();

            // reset iterationsData for next run
            iterationsData = new ArrayList<>();

        } catch (IOException e) {
            // Auto-generated catch block 
            LOGGER.severe(e.getLocalizedMessage());
        }
    }

    public void writeIteration(
            int sequence,
            int iteration,
            ArrayList<Solution> firstFront) {

        if (iterationsData.isEmpty()) {
            // first row with the names of the fields
            iterationsData.add(new String[]{"seq", "iteration", "sol", "dp", "etd", "heurist", "smoothess", "nfzs", "collisions"});
        }

        for (int i = 0; i < firstFront.size(); ++i) {

            Solution iSol = firstFront.get(i);

            // AHORA MISMO SOLO SUPONEMOS UN TARGET
            String[] row = new String[9];
            row[0] = String.valueOf(sequence);
            row[1] = String.valueOf(iteration);
            row[2] = String.valueOf(i);
            row[3] = String.valueOf(iSol.getTgts().get(0).getDp());
            row[4] = String.valueOf(iSol.getTgts().get(0).getEtd());
            row[5] = String.valueOf(iSol.getTgts().get(0).getHeuristic());

            // go through uavs
            int totalNFZs = 0;
            int totalCollisions = 0;
            double totalSmoothness = 0.0;
            for (int u = 0; u < iSol.getUavs().size(); ++u) {
                totalNFZs += iSol.getUavs().get(u).getTotalNFZs();
                totalCollisions += iSol.getUavs().get(u).getTotalCollisions();
                totalSmoothness += iSol.getUavs().get(u).getSmoothValue();
            }
            row[6] = String.valueOf(totalSmoothness);
            row[7] = String.valueOf(totalNFZs);
            row[8] = String.valueOf(totalCollisions);

            iterationsData.add(row);
        }
    }

    public void writeTarget(Target target) {

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
                probLost += iState.getMissPd();
                // dp calculation
                dp = 1.0 - CommonOps_DDRM.elementSum(iState.getBelief()) - probLost;
                // etd calculation
                if (!iState.isPrediction()) {
                    elapsedTime = iState.getTime() - prevTime;
                    prevTime = iState.getTime();
                    etd += (CommonOps_DDRM.elementSum(iState.getBelief()) + probLost) * elapsedTime;
                }

                //add the row to the data                
                row[0] = String.valueOf(dp);
                row[1] = String.valueOf(etd);
                row[2] = String.valueOf(iState.getTime());
                data.add(row);
            }

            // wite the data to the writer
            writer.writeAll(data);

            // closing writer connection 
            writer.close();

        } catch (IOException e) {
            // Auto-generated catch block 
            LOGGER.severe(e.getLocalizedMessage());
        }

    }

    public void writeUav(Uav uav) {
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

            StatefulBeanToCsv<UavCntrlSignals> beanToCsv = new StatefulBeanToCsvBuilder(writer3)
                    .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                    .build();

            // create a List which contains String array 
            List<String[]> data1 = new ArrayList<>();
            List<String[]> data2 = new ArrayList<>();

            // first row with the names of the fields
            data1.add(new String[]{"x", "y", "z", "heading", "airspeed", "fuel", "time"});
            data2.add(new String[]{"nfzs", "collisions", "fuelEmpties"});

            // go through uav path
            for (int i = 0; i < uav.getPath().size(); ++i) {
                // convert u uavState to a csv row
                UavState iState = uav.getPath().get(i);
                String[] row = new String[7];
                row[0] = String.valueOf(iState.getX());
                row[1] = String.valueOf(iState.getY());
                row[2] = String.valueOf(iState.getHeight());
                row[3] = String.valueOf(iState.getHeading());
                row[4] = String.valueOf(iState.getAirSpeed());
                row[5] = String.valueOf(iState.getFuel());
                row[6] = String.valueOf(iState.getTime());
                //add the row to the data
                data1.add(row);
            }

            // uav data
            data2.add(new String[]{
                String.valueOf(uav.getTotalNFZs()),
                String.valueOf(uav.getTotalCollisions()),
                String.valueOf(uav.getTotalFuelEmpties())
            });

            // wite the data to the writer
            writer1.writeAll(data1);
            writer2.writeAll(data2);
            beanToCsv.write(uav.getPrevCntrlSignals());
            beanToCsv.write(uav.getCntrlSignals());

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
            // Auto-generated catch block 
            LOGGER.severe(e.getLocalizedMessage());
        } catch (CsvDataTypeMismatchException | CsvRequiredFieldEmptyException ex) {
            Logger.getLogger(CSVHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void writeUavCntrl(Uav uav) {
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

        String uavCsvCntrl = uavFolderPath + uav.getName() + "Cntrl.csv";
        File uavCntrlFile = new File(uavCsvCntrl);

        try {
            // create FileWriter object with file as parameter 
            FileWriter outputfile = new FileWriter(uavCntrlFile);

            // create CSVWriter object filewriter object as parameter 
            CSVWriter writer = new CSVWriter(outputfile,
                    CSVWriter.DEFAULT_SEPARATOR,
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END);

            StatefulBeanToCsv<UavCntrlSignals> beanToCsv = new StatefulBeanToCsvBuilder(writer)
                    .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                    .build();

            // wite the data to the writer
            beanToCsv.write(uav.getPrevCntrlSignals());
            beanToCsv.write(uav.getCntrlSignals());

            // closing writer connection 
            writer.close();

            // go through uav sensors
            for (int s = 0; s < uav.getSensors().size(); ++s) {
                if (!uav.getSensors().get(s).isStatic()) {
                    // only write csv file for dinamic sensors
                    writeSensorCntrl(uavFolderPath, uav.getSensors().get(s));
                }
            }
        } catch (IOException e) {
            // Auto-generated catch block 
            LOGGER.severe(e.getLocalizedMessage());
        } catch (CsvDataTypeMismatchException | CsvRequiredFieldEmptyException ex) {
            Logger.getLogger(CSVHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void writeSensor(String uavFolderPath, Sensor sensor) {
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

            StatefulBeanToCsv<SensorCntrlSignals> beanToCsv = new StatefulBeanToCsvBuilder(writer2)
                    .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                    .build();

            // create a List which contains String array 
            List<String[]> data = new ArrayList<>();

            // first row with the names of the fields
            data.add(new String[]{"azimuth", "elevation", "time"});

            // go through sensor path
            for (int i = 0; i < sensor.getPath().size(); ++i) {
                // convert i sensorState to a csv row
                SensorState iState = sensor.getPath().get(i);
                String[] row = new String[3];
                row[0] = String.valueOf(iState.getAzimuth());
                row[1] = String.valueOf(iState.getElevation());
                row[2] = String.valueOf(iState.getTime());
                //add the row to the data
                data.add(row);
            }

            // wite the data to the writer
            writer1.writeAll(data);
            beanToCsv.write(sensor.getPrevCntrlSignals());
            beanToCsv.write(sensor.getCntrlSignals());

            // closing writer connection 
            writer1.close();
            writer2.close();

        } catch (IOException e) {
            // Auto-generated catch block 
            LOGGER.severe(e.getLocalizedMessage());
        } catch (CsvDataTypeMismatchException | CsvRequiredFieldEmptyException ex) {
            Logger.getLogger(CSVHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void writeSensorCntrl(String uavFolderPath, Sensor sensor) {
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

        String sensorCsvCntrl = sensorFolderPath + sensor.getName() + "Cntrl.csv";
        File sensorCntrlFile = new File(sensorCsvCntrl);

        try {
            // create FileWriter object with file as parameter 
            FileWriter outputfile = new FileWriter(sensorCntrlFile);

            // create CSVWriter object filewriter object as parameter 
            CSVWriter writer = new CSVWriter(outputfile,
                    CSVWriter.DEFAULT_SEPARATOR,
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END);

            StatefulBeanToCsv<SensorCntrlSignals> beanToCsv = new StatefulBeanToCsvBuilder(writer)
                    .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                    .build();

            // wite the data
            beanToCsv.write(sensor.getPrevCntrlSignals());
            beanToCsv.write(sensor.getCntrlSignals());

            // closing connection 
            writer.close();

        } catch (IOException e) {
            // Auto-generated catch block 
            LOGGER.severe(e.getLocalizedMessage());
        } catch (CsvDataTypeMismatchException | CsvRequiredFieldEmptyException ex) {
            Logger.getLogger(CSVHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ArrayList<UavCntrlSignals> loadUavCntrl(String uavName) {

        String uavCntrl = loadPath + "uavs" + File.separator + uavName + File.separator;

        // uav signals to return
        ArrayList<UavCntrlSignals> uavCntrlSignals = new ArrayList<>();
        try {
            // load csv File
            FileReader reader = new FileReader(uavCntrl + uavName + "Cntrl.csv");
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

    public ArrayList<SensorCntrlSignals> loadSensorCntrl(
            String uavName, String sensorName) {

        String sensorCntrl = loadPath + "uavs" + File.separator + uavName
                + File.separator + sensorName + File.separator;

        // uav signals to return
        ArrayList<SensorCntrlSignals> sensorCntrlSignals = new ArrayList<>();
        try {
            // load csv File
            FileReader reader = new FileReader(sensorCntrl + sensorName + "Cntrl.csv");
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

    public static DMatrixRMaj loadTarget(String targetName) {

        // target belief to return
        DMatrixRMaj targetBelief = new DMatrixRMaj();
        String targetLoadPath = loadTarget + "targets" + File.separator + targetName
                + File.separator;

        try {
            // load belief from file
            targetBelief = MatrixIO.loadCSV(targetLoadPath + targetName + "State0.csv", true);

        } catch (IOException ex) {
            Logger.getLogger(CSVHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

        return targetBelief;
    }

    private void deleteFiles(File dirPath) {
        File filesList[] = dirPath.listFiles();
        for (File file : filesList) {
            if (file.isFile()) {
                file.delete();
            } else {
                deleteFiles(file);
                file.delete();
            }
        }
    }
}
