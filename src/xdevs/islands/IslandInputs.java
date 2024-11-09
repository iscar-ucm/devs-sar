/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.islands;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import models.optimizer.Solution;
import models.optimizer.algorithm.Algorithm;
import models.planner.Scenario;
import org.json.simple.JSONObject;
import utils.CSVHandler;
import xdevs.core.modeling.Atomic;
import xdevs.core.modeling.Port;

/**
 *
 * @author Juan
 */
public class IslandInputs extends Atomic {

    private static final Logger LOGGER = Logger.getLogger(IslandInputs.class.getName());

    // in Ports of the model
    public Port<ArrayList<Solution>> tiI1 = new Port<>("finalSolutions"); // set of final solutions

    // out Ports of the model
    public Port<Scenario> tiO1 = new Port<>("scenario");

    // internal data
    private JSONObject scenarioJSON;
    private Scenario myScenario;
    private Algorithm myAlgorithm;
    private ArrayList<Solution> finalSolutions;    
    private int currentAlgorithm, currentRun, oIndex;
    private double startTime, endTime;    
    private CSVHandler csvHandler;

    public IslandInputs(JSONObject jsonRoot, int oIndex, CSVHandler csvHandler) {
        super("OP" + oIndex);
        super.addInPort(tiI1);
        super.addOutPort(tiO1);
        scenarioJSON = jsonRoot;
        this.oIndex = oIndex;
        this.csvHandler = csvHandler;
    }

    @Override
    public void initialize() {
        currentRun = 1;
        currentAlgorithm = oIndex - 1;
        finalSolutions = new ArrayList<>();        
        myScenario = new Scenario(scenarioJSON);
        csvHandler.setOptimizerPath(
                oIndex, myScenario.getalgorithms().get(currentAlgorithm).getType().toString());
        // reduce scenario algorithms to the current one
        myAlgorithm = myScenario.getalgorithms().get(currentAlgorithm);
        myScenario.getalgorithms().clear();
        myScenario.getalgorithms().add(myAlgorithm);
        startTime = System.currentTimeMillis();
        endTime = 0.0;        
        super.holdIn("run", 0.0);
    }

    @Override
    public void exit() {
        startTime = 0.0;
        endTime = 0.0;         
        super.passivate();
    }

    @Override
    public void deltint() {
        if (phaseIs("run")) {
            super.holdIn("waiting", Double.MAX_VALUE);
        } else if (phaseIs("end")) {
            exit();
        }
    }

    @Override
    public void deltext(double e) {
        if (phaseIs("waiting")) {
            if (!tiI1.isEmpty()) {
                finalSolutions = tiI1.getSingleValue();
            }
            endTime = (System.currentTimeMillis() - startTime) / 1000.0;           
            // write current run results
            csvHandler.writeRun(
                    currentRun,
                    endTime,
                    myAlgorithm.getObjectives(),
                    finalSolutions);

            LOGGER.log(Level.INFO,
                    String.format("##### %1$s: RUN %2$s   ENDS #####",
                            this.getName(),
                            currentRun
                    )
            );

            // check if another run is needed
            if (currentRun < myScenario.getParams().getNumOfRuns()) {
                currentRun++;
                myScenario = new Scenario(scenarioJSON);
                finalSolutions = new ArrayList<>();                
                // reduce scenario algorithms to the current one
                myAlgorithm = myScenario.getalgorithms().get(currentAlgorithm);
                myScenario.getalgorithms().clear();
                myScenario.getalgorithms().add(myAlgorithm);
                startTime = System.currentTimeMillis();
                endTime = 0.0;                
                super.holdIn("run", 0.0);
            } else {
                csvHandler.writeOpResults();
                super.holdIn("end", 0.0);
            }
        }
    }

    @Override
    public void lambda() {
        if (phaseIs("run")) {
            LOGGER.log(Level.INFO,
                    String.format("##### %1$s: RUN %2$s STARTS #####",
                            this.getName(),
                            currentRun
                    )
            );
            // output the init data to the planner
            tiO1.addValue(myScenario);
        }
    }

}
