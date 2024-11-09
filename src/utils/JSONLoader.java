/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author jbbordon
 */
public class JSONLoader {

    private static final Logger LOGGER = Logger.getLogger(JSONLoader.class.getName());

    private static JSONObject loadJSON(String jsonFilePath) throws FileNotFoundException, IOException, ParseException {
        JSONParser parser = new JSONParser();
        JSONObject jsonRoot = (JSONObject) parser.parse(new FileReader(jsonFilePath));
        return jsonRoot;
    }

    /**
     * @param tool
     * @param scenario
     * @return the scenario json
     */
    public static JSONObject getScenario(String tool, String scenario) {
        String jsonFilePath = "data" + File.separator + "scenarios" + File.separator
                + tool + File.separator + scenario + File.separator + scenario + ".json";
        JSONObject scenarioJson = null;
        try {
            scenarioJson = JSONLoader.loadJSON(jsonFilePath);
        } catch (IOException | ParseException ee) {
            LOGGER.severe(ee.getLocalizedMessage());
        }
        return scenarioJson;
    }

    /**
     * @param uavType
     * @return the json
     */
    public static JSONObject getUavParameters(String uavType) {
        String jsonFilePath = "data" + File.separator + "uavs" + File.separator
                + uavType + ".json";
        JSONObject uavParameters = null;
        try {
            uavParameters = JSONLoader.loadJSON(jsonFilePath);
        } catch (IOException | ParseException ee) {
            LOGGER.severe(ee.getLocalizedMessage());
        }
        return uavParameters;
    }

    public static JSONObject getSensorParameters(String sensorType) {
        String jsonFilePath = "data" + File.separator + "sensors" + File.separator
                + sensorType + ".json";
        JSONObject sensorParameters = null;
        try {
            sensorParameters = JSONLoader.loadJSON(jsonFilePath);
        } catch (IOException | ParseException ee) {
            LOGGER.severe(ee.getLocalizedMessage());
        }
        return sensorParameters;
    }

    public static JSONObject getRungeKuttaParam() {
        String jsonFilePath = "data" + File.separator + "motionmodels"
                + File.separator + "rungekutta.json";
        JSONObject rungeKuttaParam = null;
        try {
            rungeKuttaParam = JSONLoader.loadJSON(jsonFilePath);
        } catch (IOException | ParseException ee) {
            LOGGER.severe(ee.getLocalizedMessage());
        }
        return rungeKuttaParam;
    }

    public static JSONObject getCntrlParam(String factorsFile) {
        String jsonFilePath = "data" + File.separator + "algorithms"
                + File.separator + factorsFile + ".json";
        JSONObject paretosJS = null;
        try {
            paretosJS = JSONLoader.loadJSON(jsonFilePath);
        } catch (IOException | ParseException ee) {
            LOGGER.severe(ee.getLocalizedMessage());
        }
        return paretosJS;
    }

}
