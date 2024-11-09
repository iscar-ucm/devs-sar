package Main;

import java.util.Arrays;
import java.util.logging.Level;
import org.json.simple.JSONObject;
import utils.CSVHandler;
import utils.JSONLoader;
import xdevs.core.simulation.profile.CoordinatorProfile;
import xdevs.core.util.DevsLogger;
import xdevs.evaluator.EvTool1;
import xdevs.flightSimulator.FsTool1;
import xdevs.islands.Islands;
import xdevs.optimizer.OpTool1;
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

    private static String DEVSPEC = "spec1"; // spec2
    private static String TOOL = "Optimizer";  //Optimizer //Evaluator //FlightSimulator // Islands
    private static String SCENARIO = "";
    private static boolean FULLPATH = true;
    private static final String USAGE_MESSAGE = "Usage: java Main.AppMain TOOL [DEVSPEC] SCENARIO\n"
            + "Possible values:\n"
            + "TOOL: Optimizer, Evaluator, FlightSimulator, Islands\n"
            + "DEVSPEC: spec1, spec2\n"
            + "If TOOL is Optimizer or Islands, DEVSPEC is always spec1\n"
            + "If TOOL is Evaluator, ntp option can be used to not recording the whole target path";

    public static void main(String[] args) {
        if (!validateArgs(args)) {
            System.out.println(USAGE_MESSAGE);
            return;
        }

        CoordinatorProfile coordinator = null;
        DevsLogger.setup(Level.INFO);
        CSVHandler csvHandler = new CSVHandler(DEVSPEC, TOOL, SCENARIO);
        JSONObject scenarioJS = JSONLoader.getScenario(TOOL, SCENARIO);

        switch (TOOL) {
            case "Islands":
                Islands islandModel = new Islands(scenarioJS, DEVSPEC, TOOL, SCENARIO);
                coordinator = new CoordinatorProfile(islandModel);
                break;
            case "Optimizer":
                OpTool1 opTool = new OpTool1(scenarioJS, csvHandler);
                coordinator = new CoordinatorProfile(opTool);
                break;
            case "Evaluator":
                if ("spec1".equals(DEVSPEC)) {
                    // spec1
                    EvTool1 evTool = new EvTool1(scenarioJS, FULLPATH, csvHandler);
                    coordinator = new CoordinatorProfile(evTool);
                } else {
                    // spec2
                    EvTool2 evTool = new EvTool2(scenarioJS, FULLPATH, csvHandler);
                    coordinator = new CoordinatorProfile(evTool);
                }
                break;
            case "FlightSimulator":
                if ("spec1".equals(DEVSPEC)) {
                    // spec1
                    FsTool1 fsTool = new FsTool1(scenarioJS, csvHandler);
                    coordinator = new CoordinatorProfile(fsTool);
                } else {
                    // spec2
                    FsTool2 fsTool = new FsTool2(scenarioJS, csvHandler);
                    coordinator = new CoordinatorProfile(fsTool);
                }
                break;
            default:
                break;
        }

        if (coordinator != null) {
            coordinator.initialize();
            DevsLogger.setup(Level.INFO);
            coordinator.simulate(Long.MAX_VALUE);
            csvHandler.writeProfile(coordinator.toString());
            coordinator.exit();
        }
    }

    private static boolean validateArgs(String[] args) {
        if (args.length < 2 || args.length > 4) {
            return false; // Verify there are at least 2 arguments and no more than 4
        }

        TOOL = args[0];
        if (!Arrays.asList("Optimizer", "Evaluator", "FlightSimulator", "Islands").contains(TOOL)) {
            return false; // TOOL is not a valid value
        }

        if ("Optimizer".equals(TOOL) || "Islands".equals(TOOL)) {
            if (args.length == 2) {
                // No need to provide DEVS spec in this case
                DEVSPEC = "spec1";
                SCENARIO = args[args.length - 1];
                return true;
            } else {
                return false;
            }
        }

        if ("Evaluator".equals(TOOL)) {
            switch (args.length) {
                case 4:
                    // TOOL is Evaluator and should have 4 arguments
                    if (!"ntp".equals(args[3])) {
                        return false; // The fourth argument should be "ntp"
                    }
                    DEVSPEC = args[1];
                    SCENARIO = args[2];
                    FULLPATH = false;
                    return true;
                case 3:
                    // TOOL is Evaluator and should have 3 arguments
                    DEVSPEC = args[1];
                    SCENARIO = args[args.length - 1];
                    return true;
                default:
                    return false;
            }
        }

        if ("FlightSimulator".equals(TOOL)) {
            if (args.length == 3) {
                // TOOL is FlightSimulator and should have 3 arguments
                DEVSPEC = args[1];
                SCENARIO = args[args.length - 1];
                return true;
            } else {
                return false;
            }
        }

        return true; // Arguments validated correctly
    }
}
