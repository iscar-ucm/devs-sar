/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.uav;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.util.ArrayList;
import models.environment.SearchArea;
import models.environment.Geographic;
import models.environment.Cartesian;
import models.uav.motionModels.MotionModel;
import models.uav.motionModels.MotionModelType;
import models.uav.motionModels.Jsbsim;
import models.uav.motionModels.RungeKutta;
import models.sensor.Sensor;

/**
 *
 * @author jbbordon
 */
public class Uav {

    private String name;
    private String type;
    private UavCntrlType controlType;
    private double controlAt;
    private MotionModel motionModel;
    private double endTime;
    private double seqEndTime;
    private double endFuel;
    private ArrayList<Sensor> sensors;
    private ArrayList<UavState> path;
    private ArrayList<UavCntrlSignals> prevCntrlSignals;
    private ArrayList<UavCntrlSignals> cntrlSignals;
    private int totalNFZs;
    private int totalCollisions;
    private int totalFuelEmpties;
    private double smoothValue;

    /**
     * constructor 1 parameter JSON
     *
     * @param uavJSON
     * @param searchArea
     */
    public Uav(JSONObject uavJSON, SearchArea searchArea) {
        // read uav name & type
        name = (String) uavJSON.get("name");
        type = (String) uavJSON.get("type");
        // read control
        JSONObject controlJS = (JSONObject) uavJSON.get("control");
        controlType = UavCntrlType.valueOf((String) controlJS.get("controlType"));
        controlAt = (double) controlJS.get("controlAt");
        // read motion model
        JSONObject motionModelJS = (JSONObject) uavJSON.get("motionModel");
        MotionModelType uavMMType
                = MotionModelType.valueOf((String) motionModelJS.get("type"));
        switch (uavMMType) {
            case rungekutta:
                motionModel = new RungeKutta(type, motionModelJS);
                break;
            case jsbsim:
                motionModel = new Jsbsim(type, motionModelJS);
                break;
        }
        // read UAV initial State
        JSONObject jsInitState = (JSONObject) uavJSON.get("initState");
        Geographic uavInitGeoPos = new Geographic(
                (double) jsInitState.get("lat"),
                (double) jsInitState.get("lng")
        );
        UavState initState = new UavState(
                uavInitGeoPos,
                (double) jsInitState.get("alt"),
                (double) jsInitState.get("heading"),
                (double) jsInitState.get("speed"),
                (double) jsInitState.get("fuel"),
                (double) jsInitState.get("time"));
        // convert geographic to cartesian 
        Cartesian uavInitXYPos = Geographic.toCartesian(
                uavInitGeoPos,
                searchArea.getGeoPos(),
                searchArea.getAreaBearing()
        );
        initState.setXyPos(uavInitXYPos);
        // add initState as the first state in the uav path
        path = new ArrayList<>();
        path.add(initState);
        // read UAV final State
        JSONObject jsFinalState = (JSONObject) uavJSON.get("finalState");
        endFuel = (double) jsFinalState.get("fuel");
        endTime = (double) jsFinalState.get("time");
        seqEndTime = endTime;
        // read UAV sensors
        JSONArray sensorArray = (JSONArray) uavJSON.get("sensors");
        short index;
        JSONObject sensorJS;
        sensors = new ArrayList<>();
        for (index = 0; index < sensorArray.size(); index++) {
            sensorJS = (JSONObject) sensorArray.get(index);
            sensors.add(new Sensor(sensorJS, searchArea));
        }
        // init the cntrlSignals arrays
        cntrlSignals = new ArrayList<>();
        prevCntrlSignals = new ArrayList<>();
        // init other data
        totalNFZs = 0;
        totalCollisions = 0;
        totalFuelEmpties = 0;
        smoothValue = 0.0;
    }

    /**
     * Constructor. Used to copy a UAV.
     *
     */
    public Uav(String name, String type, UavCntrlType controlType, double controlAt,
            MotionModel motionModel, UavState initState, double endTime,
            double seqEndTime, double endFuel, ArrayList<Sensor> sensors, 
            ArrayList<UavCntrlSignals> prevCntrlSignals, int totalNFZs, int totalCollisions,
            int totalFuelEmpties, double smoothValue) {
        this.name = name;
        this.type = type;
        this.controlType = controlType;
        this.controlAt = controlAt;
        switch (motionModel.getType()) {
            case rungekutta:
                this.motionModel = new RungeKutta((RungeKutta) motionModel);
                break;
            case jsbsim:
                this.motionModel = new Jsbsim((Jsbsim) motionModel);
                break;
        }
        // init path
        this.path = new ArrayList<>();
        this.path.add(initState.clone());
        this.endFuel = endFuel;
        this.endTime = endTime;
        this.seqEndTime = seqEndTime;
        // copy UAV sensors
        this.sensors = new ArrayList<>();
        for (short i = 0; i < sensors.size(); i++) {
            this.sensors.add(sensors.get(i).copy());
        }
        // init cntrlSignals
        this.cntrlSignals = new ArrayList<>();
        // clone prevCntrl
        this.prevCntrlSignals = new ArrayList<>();
        for (short i = 0; i < prevCntrlSignals.size(); i++) {
            this.prevCntrlSignals.add(prevCntrlSignals.get(i).clone());
        }        
        this.totalNFZs = totalNFZs;
        this.totalCollisions = totalCollisions;
        this.totalFuelEmpties = totalFuelEmpties;
        this.smoothValue = smoothValue;
    }

    /**
     * Constructor. Used to clone a UAV.
     *
     */
    public Uav(String name, String type, UavCntrlType controlType, double controlAt,
            MotionModel motionModel, ArrayList<UavState> path, double endTime,
            double seqEndTime, double endFuel, ArrayList<Sensor> sensors,
            ArrayList<UavCntrlSignals> prevCntrlSignals, ArrayList<UavCntrlSignals> cntrlSignals,
            int totalNFZs, int totalCollisions, int totalFuelEmpties, double smoothValue) {
        this.name = name;
        this.type = type;
        this.controlType = controlType;
        this.controlAt = controlAt;
        switch (motionModel.getType()) {
            case rungekutta:
                this.motionModel = new RungeKutta((RungeKutta) motionModel);
                break;
            case jsbsim:
                this.motionModel = new Jsbsim((Jsbsim) motionModel);
                break;
        }
        // clone UAV path
        this.path = new ArrayList<>();
        for (short i = 0; i < path.size(); i++) {
            this.path.add(path.get(i).clone());
        }
        this.endFuel = endFuel;
        this.endTime = endTime;
        this.seqEndTime = seqEndTime;
        // clone UAV sensors
        this.sensors = new ArrayList<>();
        for (short i = 0; i < sensors.size(); i++) {
            this.sensors.add(sensors.get(i).clone());
        }
        // clone UAV cntrlSignals arrays   
        this.cntrlSignals = new ArrayList<>();
        for (short i = 0; i < cntrlSignals.size(); i++) {
            this.cntrlSignals.add(cntrlSignals.get(i).clone());
        }
        this.prevCntrlSignals = new ArrayList<>();
        for (short i = 0; i < prevCntrlSignals.size(); i++) {
            this.prevCntrlSignals.add(prevCntrlSignals.get(i).clone());
        }        
        this.totalNFZs = totalNFZs;
        this.totalCollisions = totalCollisions;
        this.totalFuelEmpties = totalFuelEmpties;
        this.smoothValue = smoothValue;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the controlType
     */
    public UavCntrlType getControlType() {
        return controlType;
    }

    /**
     * @param controlType the controlType to set
     */
    public void setControlType(UavCntrlType controlType) {
        this.controlType = controlType;
    }

    /**
     * @return the controlAt
     */
    public double getControlAt() {
        return controlAt;
    }

    /**
     * @param controlAt the controlAt to set
     */
    public void setControlAt(double controlAt) {
        this.controlAt = controlAt;
    }

    /**
     * @return the motionModel
     */
    public MotionModel getMotionModel() {
        return motionModel;
    }

    /**
     * @param motionModel the motionModel to set
     */
    public void setMotionModel(MotionModel motionModel) {
        this.motionModel = motionModel;
    }

    /**
     * @return the endTime
     */
    public double getEndTime() {
        return endTime;
    }

    /**
     * @param endTime the endTime to set
     */
    public void setEndTime(double endTime) {
        this.endTime = endTime;
    }

    /**
     * @return the seqEndTime
     */
    public double getSeqEndTime() {
        return seqEndTime;
    }

    /**
     * @param seqEndTime the seqEndTime to set
     */
    public void setSeqEndTime(double seqEndTime) {
        this.seqEndTime = seqEndTime;
    }

    /**
     * @return the endFuel
     */
    public double getEndFuel() {
        return endFuel;
    }

    /**
     * @param endFuel the endFuel to set
     */
    public void setEndFuel(double endFuel) {
        this.endFuel = endFuel;
    }

    /**
     * @return the initState
     */
    public UavState getInitState() {
        return path.get(0);
    }

    /**
     * @param initState the initState to set
     */
    public void setInitState(UavState initState) {
        path.set(0, initState);
    }

    /**
     * @return the finalState
     */
    public UavState getFinalState() {
        return path.get(path.size() - 1);
    }

    /**
     * @param finalState the finalState to set
     */
    public void setFinalState(UavState finalState) {
        path.set(path.size() - 1, finalState);
    }

    /**
     * @return the sensors
     */
    public ArrayList<Sensor> getSensors() {
        return sensors;
    }

    /**
     * @param sensors the sensors to set
     */
    public void setSensors(ArrayList<Sensor> sensors) {
        this.sensors = sensors;
    }

    /**
     * @return the path
     */
    public ArrayList<UavState> getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(ArrayList<UavState> path) {
        this.path = path;
    }

    /**
     * @return the prevCntrlSignals
     */
    public ArrayList<UavCntrlSignals> getPrevCntrlSignals() {
        return prevCntrlSignals;
    }
    
    /**
     * @return the cntrlSignals
     */
    public ArrayList<UavCntrlSignals> getCntrlSignals() {
        return cntrlSignals;
    }

    /**
     * @param cntrlSignals the cntrlSignals to set
     */
    public void setCntrlSignals(ArrayList<UavCntrlSignals> cntrlSignals) {
        this.cntrlSignals = cntrlSignals;
    }

    /**
     * @return the totalNFZs
     */
    public int getTotalNFZs() {
        return totalNFZs;
    }

    /**
     *
     */
    public void setTotalNFZs() {
        ++totalNFZs;
    }

    /**
     * @return the totalCollisions
     */
    public int getTotalCollisions() {
        return totalCollisions;
    }

    /**
     *
     */
    public void setTotalCollisions() {
        ++totalCollisions;
    }

    /**
     * @return the totalFuelEmpties
     */
    public int getTotalFuelEmpties() {
        return totalFuelEmpties;
    }

    /**
     *
     */
    public void setTotalFuelEmpties() {
        ++totalFuelEmpties;
    }

    /**
     * @return the smoothValue
     */
    public double getSmoothValue() {
        return smoothValue;
    }

    /**
     * @param smoothValue the smoothValue to set
     */
    public void setSmoothValue(double smoothValue) {
        this.smoothValue = smoothValue;
    }

    /**
     * Returns a copy of the UAV with empty cntrlSignals and path set to the
     * initial state.The same philosophy aplies to the UAV onboard sensors.
     *
     * @return Uav copy.
     */
    public Uav copy() {
        return new Uav(
                this.name,
                this.type,
                this.controlType,
                this.controlAt,
                this.motionModel,
                this.getInitState(),
                this.endTime,
                this.seqEndTime,
                this.endFuel,
                this.getSensors(),
                this.getPrevCntrlSignals(),
                this.totalNFZs,
                this.totalCollisions,
                this.totalFuelEmpties,
                this.smoothValue);
    }

    @Override
    /**
     * Returns an exact clone of the object.The same philosophy aplies to the
     * UAV onboard sensors.
     *
     * @return Uav clone.
     */
    public Uav clone() {
        return new Uav(
                this.name,
                this.type,
                this.controlType,
                this.controlAt,
                this.motionModel,
                this.getPath(),
                this.endTime,
                this.seqEndTime,
                this.endFuel,
                this.getSensors(),
                this.getPrevCntrlSignals(),
                this.getCntrlSignals(),
                this.totalNFZs,
                this.totalCollisions,
                this.totalFuelEmpties,
                this.smoothValue);
    }
}
