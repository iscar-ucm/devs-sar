/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.spec2.evaluator;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import models.environment.SearchArea;
import models.environment.Nfz;
import models.environment.WindMatrix;
import models.sensor.motionModels.MotionModelType;
import models.uav.Uav;
import models.target.Target;
import xdevs.core.modeling.Atomic;
import xdevs.core.modeling.Port;
import utils.CSVHandler;

/**
 *
 * @author bordon
 */
public class EvInputs extends Atomic {

    private static final Logger LOGGER = Logger.getLogger(EvInputs.class.getName());

    // in Ports of the model
    public Port<ArrayList<Uav>> tiI1 = new Port<>("evaluatedUavs");
    public Port<ArrayList<Target>> tiI2 = new Port<>("evaluatedTargets");

    // out Ports of the model
    public Port<SearchArea> tiO1 = new Port<>("searchArea");
    public Port<Nfz[]> tiO2 = new Port<>("nfzs");
    public Port<WindMatrix> tiO3 = new Port<>("windsMap");
    public Port<ArrayList<Uav>> tiO4 = new Port<>("uavs");
    public Port<ArrayList<Target>> tiO5 = new Port<>("targets");

    // internal data
    private SearchArea searchArea;
    private Nfz[] nfzs;
    private WindMatrix windMatrix;
    private ArrayList<Uav> scenarioUavs;
    private ArrayList<Target> scenarioTargets;
    private CSVHandler csvHandler;    

    public EvInputs(JSONObject jsonRoot, CSVHandler csvHandler) {
        super("Inputs");
        super.addInPort(tiI1);
        super.addInPort(tiI2);
        super.addOutPort(tiO1);
        super.addOutPort(tiO2);
        super.addOutPort(tiO3);
        super.addOutPort(tiO4);
        super.addOutPort(tiO5);    
        
        JSONObject searchAreaJS = (JSONObject) jsonRoot.get("zone");
        searchArea = new SearchArea(searchAreaJS);

        JSONArray nfzsArray = (JSONArray) jsonRoot.get("nfzs");
        nfzs = new Nfz[nfzsArray.size()];
        for (int i = 0; i < nfzsArray.size(); ++i) {
            Nfz iNfz = new Nfz((JSONObject) nfzsArray.get(i));
            nfzs[i] = iNfz;
        }

        JSONObject windMatrixJS = (JSONObject) jsonRoot.get("windMatrix");
        windMatrix = new WindMatrix(windMatrixJS);

        // get json structures test structures
        JSONArray targetsArray = (JSONArray) jsonRoot.get("targets");
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

        // read scenario targets
        scenarioTargets = new ArrayList();
        for (int i = 0; i < targetsArray.size(); ++i) {
            // read iTarget
            JSONObject iTargetJS = (JSONObject) targetsArray.get(i);
            scenarioTargets.add(new Target(iTargetJS, searchArea));
            // set full path
            scenarioTargets.get(i).setFullPath(true);
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
            LOGGER.log(
                    Level.INFO,
                    String.format(
                            "%1$s: EVALUTION STARTS",
                            this.getName()
                    )
            );
            super.holdIn("waiting", Double.MAX_VALUE);
        }
    }

    @Override
    public void deltext(double e) {
        if (phaseIs("waiting")) {
            LOGGER.log(
                    Level.INFO,
                    String.format(
                            "%1$s: EVALUTION ENDS, WRITING DATA",
                            this.getName()
                    )
            );
            if (!tiI1.isEmpty()) {
                scenarioUavs = tiI1.getSingleValue();
                for (int i = 0; i < scenarioUavs.size(); ++i) {
                    csvHandler.writeUav(scenarioUavs.get(i));
                }
            }
            if (!tiI2.isEmpty()) {
                scenarioTargets = tiI2.getSingleValue();
                for (int i = 0; i < scenarioTargets.size(); ++i) {
                    csvHandler.writeTarget(scenarioTargets.get(i));
                }
            }
        }
    }

    @Override
    public void lambda() {
        if (phaseIs("start")) {
            // output the init data to iUM
            tiO1.addValue(searchArea);
            tiO2.addValue(nfzs);
            tiO3.addValue(windMatrix);
            tiO4.addValue(scenarioUavs);
            tiO5.addValue(scenarioTargets);
        }
    }

}
