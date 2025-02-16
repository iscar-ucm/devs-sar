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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import models.optimizer.Objectives;
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
    private String solPath;
    private String uavsPath;
    private String targetsPath;
    private List<String[]> iterationsData; // each iteration firstFront
    private List<String[]> runsData; // run firstFront

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

        } catch (IOException e) {
            // Auto-generated catch block 
            LOGGER.severe(e.getLocalizedMessage());
        }
    }

    public void writeRun(
            int run,
            double eTime,
            Objectives objectives,
            ArrayList<Solution> runSolutions) {

        // run folder creation
        if (run < 10) {
            runPath = optimizerPath + '0' + run + File.separator;
        } else {
            runPath = optimizerPath + run + File.separator;
        }
        File runFolder = new File(runPath);
        runFolder.mkdir();

        // for each solution
        int iSol = 1;
        for (short i = 0; i < runSolutions.size(); ++i) {
            Solution currentSol = runSolutions.get(i);
            // folders creation
            if (iSol < 10) {
                solPath = runPath + '0' + iSol + File.separator;
            } else {
                solPath = runPath + iSol + File.separator;
            }

            File solFolder = new File(solPath);
            solFolder.mkdir();

            uavsPath = solPath + "uavs" + File.separator;
            File uavsFolder = new File(uavsPath);
            uavsFolder.mkdir();

            // write uavs results
            currentSol.getUavs().forEach(solUav -> {
                writeUavCntrl(solUav);
            });

            // run log fields
            int fixFields = 3;
            int numFields = fixFields
                    + objectives.getConstraints().length
                    + objectives.getParetos().length;

            if (runsData.isEmpty()) {
                // first row with the names of the fields
                String[] firstRow = new String[numFields];
                firstRow[0] = "run";
                firstRow[1] = "sol";
                firstRow[2] = "time";
                // names of the paretos
                int p;
                for (p = 0; p < objectives.getParetos().length; ++p) {
                    firstRow[fixFields + p] = objectives.getParetos()[p].toString();
                }
                // names of the constraints
                for (int c = 0; c < objectives.getConstraints().length; ++c) {
                    firstRow[fixFields + p + c] = objectives.getConstraints()[c].toString();
                }
                runsData.add(firstRow);                
            }

            // fix fields
            String[] row = new String[numFields];
            row[0] = String.valueOf(run);
            row[1] = String.valueOf(iSol);
            BigDecimal roundedTime = new BigDecimal(eTime);
            roundedTime = roundedTime.setScale(2, RoundingMode.HALF_UP);
            row[2] = String.valueOf(roundedTime);
            // paretos values
            int p = 0;
            for (p = 0; p < objectives.getParetos().length; ++p) {
                if ("pd".equals(objectives.getParetos()[p].toString())) {
                    BigDecimal n1 = new BigDecimal("1.0");
                    BigDecimal nDP = BigDecimal.valueOf(currentSol.getParetos().get(0, p));
                    BigDecimal pd = n1.subtract(nDP);
                    row[fixFields + p]
                            = String.valueOf(pd);
                } else {
                    row[fixFields + p]
                            = String.valueOf(currentSol.getParetos().get(0, p));
                }
            }
            // constraints values
            for (int c = 0; c < currentSol.getConstraints().numCols; ++c) {
                row[fixFields + p + c]
                        = String.valueOf(currentSol.getConstraints().get(0, c));
            }
            // add the iteration row
            runsData.add(row);
            iSol++;
        }

        // write current run iterations files
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

    public void writeFirstFront(
            int sequence,
            int iteration,
            double eTime,
            Objectives objectives,
            ArrayList<Solution> firstFront) {

        // iteration log fields
        int fixFields = 4;
        int numFields = fixFields
                + objectives.getConstraints().length
                + objectives.getParetos().length;

        if (iterationsData.isEmpty()) {

            // first row with the names of the fields
            String[] firstRow = new String[numFields];
            firstRow[0] = "seq";
            firstRow[1] = "iteration";
            firstRow[2] = "sol";
            firstRow[3] = "time";
            // names of the paretos
            int p;
            for (p = 0; p < objectives.getParetos().length; ++p) {
                firstRow[fixFields + p] = objectives.getParetos()[p].toString();
            }
            // names of the constraints
            for (int c = 0; c < objectives.getConstraints().length; ++c) {
                firstRow[fixFields + p + c] = objectives.getConstraints()[c].toString();
            }
            iterationsData.add(firstRow);
        }     
        
        final int[] frontPos = {1};
        firstFront.forEach(iSol -> {

            String[] row = new String[numFields];
            // fix fields
            row[0] = String.valueOf(sequence);
            row[1] = String.valueOf(iteration);
            row[2] = String.valueOf(frontPos[0]);
            BigDecimal roundedTime = new BigDecimal(eTime);
            roundedTime = roundedTime.setScale(2, RoundingMode.HALF_UP);
            row[3] = String.valueOf(roundedTime);
            // paretos values
            int p = 0;
            for (p = 0; p < objectives.getParetos().length; ++p) {
                if ("pd".equals(objectives.getParetos()[p].toString())) {
                    BigDecimal n1 = new BigDecimal("1.0");
                    BigDecimal nDP = BigDecimal.valueOf(iSol.getParetos().get(0, p));
                    BigDecimal pd = n1.subtract(nDP);
                    row[fixFields + p]
                            = String.valueOf(pd);
                } else {
                    row[fixFields + p]
                            = String.valueOf(iSol.getParetos().get(0, p));
                }
            }
            // constraints values
            for (int c = 0; c < iSol.getConstraints().numCols; ++c) {
                row[fixFields + p + c]
                        = String.valueOf(iSol.getConstraints().get(0, c));
            }
            // add the iteration row
            iterationsData.add(row);
            frontPos[0]++;
        });       
    }

    public void writeTarget(Target target, boolean fullPath) {

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
            //data.add(new String[]{"dp", "etd", "miss", "time"});

            // variables used to calculate etd, pd & myo
            double pd, elapsedTime;
            double prevTime = target.getPath().get(0).getTime();
            double missPd = 0.0;
            double prevProb = 1.0;
            double newProb;
            double etd = 0.0;

            // go through target path
            for (int i = 0; i < target.getPath().size(); ++i) {

                TargetState iState = target.getPath().get(i);

                if (fullPath || i == 0 || i == 1) {
                    // save ibelief into csv file
                    MatrixIO.saveDenseCSV(
                            iState.getBelief(),
                            targetFolderPath
                            + target.getName() + "State" + i + ".csv"
                    );
                }

                String[] row = new String[3];
                //String[] row = new String[4];

                // probability map sum
                newProb = CommonOps_DDRM.elementSum(iState.getBelief());
                if (!iState.isPrediction()) {
                    // etd calculation
                    elapsedTime = iState.getTime() - prevTime;
                    prevTime = iState.getTime();
                    etd += (CommonOps_DDRM.elementSum(iState.getBelief()) + missPd) * elapsedTime;
                } else {
                    missPd += Math.max(prevProb - newProb, 0.0);
                }
                // pd calculation
                pd = 1.0 - newProb - missPd;
                prevProb = newProb;

                //add the row to the data                
                row[0] = String.valueOf(pd);
                row[1] = String.valueOf(etd);
                //row[2] = String.valueOf(missPd);   
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
            data2.add(new String[]{"nfzs", "collisions"});

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
                String.valueOf(uav.getNFZs()),
                String.valueOf(uav.getCol())
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
