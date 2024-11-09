/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.evaluator;

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
    public ArrayList<Port> tiI1 = new ArrayList<>(); // evaluated iUav
    public ArrayList<Port> tiI2 = new ArrayList<>(); // evaluated iTarget

    // out Ports of the model
    public Port<SearchArea> tiO1 = new Port<>("searchArea");
    public Port<Nfz[]> tiO2 = new Port<>("nfzs");
    public Port<WindMatrix> tiO3 = new Port<>("windsMap");
    public ArrayList<Port> tiO4 = new ArrayList<>(); // iUAV to simulate
    public ArrayList<Port> tiO5 = new ArrayList<>(); // iTarget to simulate

    // internal data
    private SearchArea searchArea;
    private Nfz[] nfzs;
    private WindMatrix windMatrix;
    private ArrayList<Uav> scenarioUavs;
    private ArrayList<Target> scenarioTargets;
    private int eUavsRcvd, eTgtsRcvd;
    private boolean fullPath;
    private CSVHandler csvHandler;

    public EvInputs(JSONObject jsonRoot, boolean fullPath, CSVHandler csvHandler) {
        super("Inputs");
        super.addOutPort(tiO1);
        super.addOutPort(tiO2);
        super.addOutPort(tiO3);

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

        // read scenario uavs
        scenarioUavs = new ArrayList();
        for (int i = 0; i < uavsArray.size(); ++i) {

            // i ports tiI1 & tiO4 (one per uav in the scenario)
            Port<Uav> iPortI1 = new Port("eUav" + i);
            tiI1.add(iPortI1);
            super.addInPort(iPortI1);
            Port<Uav> iPortO4 = new Port("uav" + i);
            tiO4.add(iPortO4);
            super.addOutPort(iPortO4);

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

            // i ports tiI2 & tiO5 (one per target in the scenario)
            Port<Target> iPortI2 = new Port("eTarget" + i);
            tiI2.add(iPortI2);
            super.addInPort(iPortI2);
            Port<Target> iPortO5 = new Port("target" + i);
            tiO5.add(iPortO5);
            super.addOutPort(iPortO5);

            // read iTarget
            JSONObject iTargetJS = (JSONObject) targetsArray.get(i);
            scenarioTargets.add(new Target(iTargetJS, searchArea));
            // set full path
            scenarioTargets.get(i).setFullPath(fullPath);
        }
        this.fullPath = fullPath;
        this.csvHandler = csvHandler;
    }

    @Override
    public void initialize() {
        eUavsRcvd = 0;
        eTgtsRcvd = 0;
        super.holdIn("start", 0.0);
    }

    @Override
    public void exit() {
        eUavsRcvd = 0;
        eTgtsRcvd = 0;
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
            for (int i = 0; i < tiI1.size(); ++i) {
                if (!tiI1.get(i).isEmpty()) {
                    ++eUavsRcvd;
                    scenarioUavs.set(i, (Uav) tiI1.get(i).getSingleValue());
                    csvHandler.writeUav(scenarioUavs.get(i));
                }
            }
            for (int i = 0; i < tiI2.size(); ++i) {
                if (!tiI2.get(i).isEmpty()) {
                    ++eTgtsRcvd;
                    scenarioTargets.set(i, (Target) tiI2.get(i).getSingleValue());
                    csvHandler.writeTarget(scenarioTargets.get(i), fullPath);
                }
            }
        }
        if (eUavsRcvd == scenarioUavs.size()
                && eTgtsRcvd == scenarioTargets.size()) {
            exit();
        }
    }

    @Override
    public void lambda() {
        if (phaseIs("start")) {
            // output the init data to iUM
            tiO1.addValue(searchArea);
            tiO2.addValue(nfzs);
            tiO3.addValue(windMatrix);
            for (int i = 0; i < scenarioUavs.size(); ++i) {
                tiO4.get(i).addValue(scenarioUavs.get(i));
            }
            for (int i = 0; i < scenarioTargets.size(); ++i) {
                tiO5.get(i).addValue(scenarioTargets.get(i));
            }
        }
    }

}
