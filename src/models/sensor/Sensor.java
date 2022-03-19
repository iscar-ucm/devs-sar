/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.sensor;

import org.json.simple.JSONObject;
import java.util.ArrayList;
import models.environment.SearchArea;
import models.sensor.motionModels.MotionModel;
import models.sensor.motionModels.MotionModelType;
import models.sensor.motionModels.RungeKutta;
import models.sensor.motionModels.StaticModel;
import models.sensor.payloads.Payload;
import models.sensor.payloads.Camera;
import models.sensor.payloads.Ideal;
import models.sensor.payloads.Footprint;
import models.sensor.payloads.PayloadType;
import models.sensor.payloads.Radar;

/**
 *
 * @author jbbordon
 */
public class Sensor {

    private String name;
    private String type;
    private SensorCntrlType controlType;
    private double controlAt;
    private MotionModel motionModel;
    private Payload payload;
    private double startTime;
    private double endTime;
    private double seqEndTime;
    private ArrayList<SensorState> path;
    private ArrayList<SensorCntrlSignals> prevCntrlSignals;
    private ArrayList<SensorCntrlSignals> cntrlSignals;

    /**
     *
     * @param sensorJSON
     * @param searchArea
     */
    public Sensor(JSONObject sensorJSON, SearchArea searchArea) {
        // read sensor name, type & capture rate        
        name = (String) sensorJSON.get("name");
        type = (String) sensorJSON.get("type");
        // read payload function
        JSONObject payloadJS = (JSONObject) sensorJSON.get("payload");
        PayloadType payloadType
                = PayloadType.valueOf((String) payloadJS.get("type"));
        switch (payloadType) {
            case camera:
                payload = new Camera(type, payloadJS, searchArea);
                break;
            case ideal:
                payload = new Ideal(type, payloadJS, searchArea);
                break;
            case footprint:
                payload = new Footprint(type, payloadJS, searchArea);
                break;
            case radar:
                payload = new Radar(type, payloadJS, searchArea);
                break;
        }
        // read motion model
        JSONObject motionModelJS = (JSONObject) sensorJSON.get("motionModel");
        MotionModelType sensorMMType
                = MotionModelType.valueOf((String) motionModelJS.get("type"));
        switch (sensorMMType) {
            case rungekutta:
                motionModel = new RungeKutta(type, motionModelJS);
                break;
            case staticModel:
                motionModel = new StaticModel(motionModelJS);
                break;
        }
        SensorState initState;
        JSONObject jInitState;
        if (sensorMMType == MotionModelType.staticModel) {
            // Static sensor
            // read sensor initial State
            jInitState = (JSONObject) sensorJSON.get("initState");
            initState = new SensorState(
                    (double) jInitState.get("time"));
        } else {
            // Dynamic sensor
            JSONObject controlJS = (JSONObject) sensorJSON.get("control");
            controlType = SensorCntrlType.valueOf((String) controlJS.get("controlType"));
            controlAt = (double) controlJS.get("controlAt");
            // read sensor initial State
            jInitState = (JSONObject) sensorJSON.get("initState");
            initState = new SensorState(
                    (double) jInitState.get("azimuth"),
                    (double) jInitState.get("elevation"),
                    (double) jInitState.get("time"));
        }
        startTime = initState.getTime();
        // read sensor end time
        endTime = (double) sensorJSON.get("endTime");
        seqEndTime = endTime;
        // add initState as the first state in the sensor path
        path = new ArrayList<>();
        path.add(initState);
        // init the cntrlSignals arrays        
        cntrlSignals = new ArrayList<>();
        prevCntrlSignals = new ArrayList<>();
    }

    /**
     * Constructor. Used to copy a Sensor.
     *
     */
    public Sensor(String name, String type, SensorCntrlType controlType, double controlAt,
            Payload payload, MotionModel motionModel, double startTime, SensorState initialState,
            double endTime, double seqEndTime, ArrayList<SensorCntrlSignals> prevCntrlSignals) {
        this.name = name;
        this.type = type;
        // init payload function
        switch (payload.getPayloadType()) {
            case camera:
                this.payload = new Camera((Camera) payload);
                break;
            case ideal:
                this.payload = new Ideal((Ideal) payload);
                break;
            case footprint:
                this.payload = new Footprint((Footprint) payload);
                break;
            case radar:
                this.payload = new Radar((Radar) payload);
                break;
        }
        switch (motionModel.getType()) {
            case rungekutta:
                this.motionModel = new RungeKutta((RungeKutta) motionModel);
                break;
            case staticModel:
                this.motionModel = new StaticModel((StaticModel) motionModel);
                break;
        }
        this.controlType = controlType;
        this.controlAt = controlAt;
        this.startTime = startTime;
        // init path
        this.path = new ArrayList<>();
        this.path.add(initialState.clone());
        this.endTime = endTime;
        this.seqEndTime = seqEndTime;
        // init cntrlSignals    
        this.cntrlSignals = new ArrayList<>();
        // clone prevCntrl        
        this.prevCntrlSignals = new ArrayList<>();
        for (short i = 0; i < prevCntrlSignals.size(); i++) {
            this.prevCntrlSignals.add(prevCntrlSignals.get(i).clone());
        }         
    }

    /**
     * Constructor. Used to clone a Sensor.
     *
     */
    public Sensor(String name, String type, SensorCntrlType controlType, double controlAt,
            Payload payload, MotionModel motionModel, double startTime, ArrayList<SensorState> path,
            double endTime, double seqEndTime, ArrayList<SensorCntrlSignals> prevCntrlSignals,
            ArrayList<SensorCntrlSignals> cntrlSignals) {
        this.name = name;
        this.type = type;
        // init payload function
        switch (payload.getPayloadType()) {
            case camera:
                this.payload = new Camera((Camera) payload);
                break;
            case ideal:
                this.payload = new Ideal((Ideal) payload);
                break;
            case footprint:
                this.payload = new Footprint((Footprint) payload);
                break;
            case radar:
                this.payload = new Radar((Radar) payload);
                break;
        }
        switch (motionModel.getType()) {
            case rungekutta:
                this.motionModel = new RungeKutta((RungeKutta) motionModel);
                break;
            case staticModel:
                this.motionModel = new StaticModel((StaticModel) motionModel);
                break;
        }
        this.controlType = controlType;
        this.controlAt = controlAt;
        this.startTime = startTime;
        // clone sensor path
        this.path = new ArrayList<>();
        for (short i = 0; i < path.size(); i++) {
            this.path.add(path.get(i).clone());
        }
        this.endTime = endTime;
        this.seqEndTime = seqEndTime;
        // clone Sensor cntrlSignals arrays  
        this.cntrlSignals = new ArrayList<>();
        for (short i = 0; i < cntrlSignals.size(); i++) {
            this.cntrlSignals.add(cntrlSignals.get(i).clone());
        }
        this.prevCntrlSignals = new ArrayList<>();
        for (short i = 0; i < prevCntrlSignals.size(); i++) {
            this.prevCntrlSignals.add(prevCntrlSignals.get(i).clone());
        }        
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
     * @return the controlType
     */
    public SensorCntrlType getControlType() {
        return controlType;
    }

    /**
     * @param controlType the controlType to set
     */
    public void setControlType(SensorCntrlType controlType) {
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
     * @return the payload
     */
    public Payload getPayload() {
        return payload;
    }

    /**
     * @param payload the payload to set
     */
    public void setPayload(Payload payload) {
        this.payload = payload;
    }

    /**
     * @return the startTime
     */
    public double getStartTime() {
        return startTime;
    }

    /**
     * @param startTime the startTime to set
     */
    public void setStartTime(double startTime) {
        this.startTime = startTime;
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
     * @return the initState
     */
    public SensorState getInitState() {
        return path.get(0);
    }

    /**
     * @param initState the initState to set
     */
    public void setInitState(SensorState initState) {
        path.set(0, initState);
    }

    /**
     * @return the finalState
     */
    public SensorState getFinalState() {
        return path.get(path.size() - 1);
    }

    /**
     * @param finalState the finalState to set
     */
    public void setFinalState(SensorState finalState) {
        path.set(path.size() - 1, finalState);
    }

    /**
     * @param path the path to set
     */
    public void setPath(ArrayList<SensorState> path) {
        this.path = path;
    }

    /**
     * @return the path
     */
    public ArrayList<SensorState> getPath() {
        return path;
    }

    /**
     * @return the prevCntrlSignals
     */
    public ArrayList<SensorCntrlSignals> getPrevCntrlSignals() {
        return prevCntrlSignals;
    }

    /**
     * @param cntrlSignals the cntrlSignals to set
     */
    public void setCntrlSignals(ArrayList<SensorCntrlSignals> cntrlSignals) {
        this.cntrlSignals = cntrlSignals;
    }

    /**
     * @return the cntrlSignals
     */
    public ArrayList<SensorCntrlSignals> getCntrlSignals() {
        return cntrlSignals;
    }

    /**
     * This method returns true if the sensor is static, false otherwise.
     *
     * @return if the sensor is static or not.
     */
    public boolean isStatic() {
        return getMotionModel().getType() == MotionModelType.staticModel;
    }

    /**
     * Returns a copy the Sensor with empty cntrlSignals and sensor path set to
     * the initial state.
     *
     * @return Sensor copy.
     */
    public Sensor copy() {
        return new Sensor(
                this.name,
                this.type,
                this.controlType,
                this.controlAt,
                this.payload,
                this.motionModel,
                this.startTime,
                this.getInitState(),
                this.endTime,
                this.seqEndTime,
                this.getPrevCntrlSignals());
    }

    @Override
    /**
     * Returns an exact copy of the object.
     */
    public Sensor clone() {
        return new Sensor(
                this.name,
                this.type,
                this.controlType,
                this.controlAt,
                this.payload,
                this.motionModel,
                this.startTime,
                this.getPath(),
                this.endTime,
                this.seqEndTime,
                this.getPrevCntrlSignals(),
                this.getCntrlSignals());
    }
}
