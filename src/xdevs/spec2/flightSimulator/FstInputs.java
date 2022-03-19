/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.spec2.flightSimulator;

import java.util.ArrayList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import utils.CSVHandler;
import models.environment.SearchArea;
import models.environment.WindMatrix;
import models.sensor.motionModels.MotionModelType;
import models.uav.Uav;
import xdevs.core.modeling.Atomic;
import xdevs.core.modeling.Port;

/**
 *
 * @author bordon
 */
public class FstInputs extends Atomic {

    // in Ports of the model
    public Port<ArrayList<Uav>> tiI1 = new Port<>("simulatedUavs");

    // out Ports of the model
    public Port<SearchArea> tiO1 = new Port<>("zone");
    public Port<WindMatrix> tiO2 = new Port<>("windMatrix");
    public Port<ArrayList<Uav>> tiO3 = new Port<>("uavs");

    // internal data
    private SearchArea searchArea;
    private WindMatrix windMatrix;
    private ArrayList<Uav> scenarioUavs;
    private CSVHandler csvHandler;
    
    public FstInputs(JSONObject jsonRoot, CSVHandler csvHandler) {
        super("TestInputs");
        super.addInPort(tiI1);
        super.addOutPort(tiO1);
        super.addOutPort(tiO2);
        super.addOutPort(tiO3);      
        
        JSONObject zoneJS = (JSONObject) jsonRoot.get("zone");
        searchArea = new SearchArea(zoneJS);

        JSONObject windMatrixJS = (JSONObject) jsonRoot.get("windMatrix");
        windMatrix = new WindMatrix(windMatrixJS);

        // get json structures test structures
        JSONArray uavsArray = (JSONArray) jsonRoot.get("uavs");
        JSONArray cntrlArray = (JSONArray) jsonRoot.get("uavsActions");

        // read scenario uavs
        scenarioUavs = new ArrayList();
        for (int i = 0; i < uavsArray.size(); ++i) {

            // read iUAV
            JSONObject iUavJS = (JSONObject) uavsArray.get(i);
            scenarioUavs.add(new Uav(iUavJS, searchArea));

            // read iUav cntrl actions             
            scenarioUavs.get(i).setCntrlSignals(
                    csvHandler.loadUavCntrl((String) iUavJS.get("name")));

            // read iUav sensors actions
            JSONArray sensorArray = (JSONArray) iUavJS.get("sensors");
            for (int k = 0; k < scenarioUavs.get(i).getSensors().size(); ++k) {
                // read iUav K sensor
                JSONObject kSensor = (JSONObject) sensorArray.get(k);
                // read motion model
                JSONObject motionModelJS = (JSONObject) kSensor.get("motionModel");
                MotionModelType sensorMMType
                        = MotionModelType.valueOf((String) motionModelJS.get("type"));
                if (sensorMMType != MotionModelType.staticModel) {
                    // read actions
                    scenarioUavs.get(i).getSensors().get(k).setCntrlSignals(
                            csvHandler.loadSensorCntrl(
                                    (String) iUavJS.get("name"),
                                    (String) kSensor.get("name")));
                }
            }
        }
        this.csvHandler = csvHandler;
    }

    @Override
    public void initialize() {
        super.holdIn("start", 0.0);
    }

    @Override
    public void exit() {
        super.passivate();
    }

    @Override
    public void deltint() {
        if (phaseIs("start")) {
            super.holdIn("waiting", Double.MAX_VALUE);
        }
    }

    @Override
    public void deltext(double e) {
        if (phaseIs("waiting")) {
            if (!tiI1.isEmpty()) {
                scenarioUavs = tiI1.getSingleValue();
                for (int i = 0; i < scenarioUavs.size(); ++i) {
                    csvHandler.writeUav(scenarioUavs.get(i));
                }
                exit();
            }
        }
    }

    @Override
    public void lambda() {
        if (phaseIs("start")) {
            // output the init data to iUM
            tiO1.addValue(searchArea);
            tiO2.addValue(windMatrix);
            tiO3.addValue(scenarioUavs);
        }
    }

}
