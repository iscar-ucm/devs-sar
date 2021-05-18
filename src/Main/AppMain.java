package Main;


import java.util.logging.Level;
import org.json.simple.JSONObject;
import utils.CSVHandler;
import utils.JSONLoader;
import xdevs.core.simulation.profile.CoordinatorProfile;
import xdevs.core.util.DevsLogger;
import xdevs.evaluator.EvTool1;
import xdevs.flightSimulator.FsTool1;
import xdevs.spec2.evaluator.EvTool2;
import xdevs.spec2.flightSimulator.FsTool2;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Juan
 */
public class AppMain {

    private static final String DEVSPEC = "spec1"; // spec2
    private static final String TOOL = "Evaluator"; //FlightSimulator
    private static final String SCENARIO = "ScenarioCloud_1UAVradar";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        CoordinatorProfile coordinator = null;

        // setup logger level
        DevsLogger.setup(Level.INFO);

        // configure csvHandler
        CSVHandler.configure(DEVSPEC, TOOL, SCENARIO);

        // read scenario
        JSONObject scenarioJS;
        scenarioJS = JSONLoader.getScenario(TOOL, SCENARIO);

        // create the selected tool
        switch (TOOL) {

            case "Evaluator":
                if ("spec1".equals(DEVSPEC)) {
                    // spec1
                    EvTool1 evTool = new EvTool1(scenarioJS);
                    coordinator = new CoordinatorProfile(evTool);
                } else {
                    // spec2
                    EvTool2 evTool = new EvTool2(scenarioJS);
                    coordinator = new CoordinatorProfile(evTool);
                }
                break;

            case "FlightSimulator":
                if ("spec1".equals(DEVSPEC)) {
                    // spec1
                    FsTool1 fsTool = new FsTool1(scenarioJS);
                    coordinator = new CoordinatorProfile(fsTool);
                } else {
                    // spec2
                    FsTool2 fsTool = new FsTool2(scenarioJS);
                    coordinator = new CoordinatorProfile(fsTool);
                }
                break;

            default:
                break;
        }

        // initialize and start simulation
        coordinator.initialize();
        DevsLogger.setup(Level.INFO);
        coordinator.simulate(Long.MAX_VALUE);
        CSVHandler.writeProfile(coordinator.toString());  
        coordinator.exit();
    }

}
