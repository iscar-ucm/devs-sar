/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.sensor;

import org.json.simple.JSONObject;
import java.util.ArrayList;
import models.environment.SearchArea;
import models.sensor.motionModels.DinamicModel;
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
        // init the cntrlSignals array        
        cntrlSignals = new ArrayList<>();
    }

    /**
     *
     * @param sensor makes a sensor copy but leaving the cntrlSignal arraylist
     * and the path empty
     */
    public Sensor(Sensor sensor) {
        // copy sensor name, type & capture rate        
        name = sensor.getName();
        type = sensor.getType();
        // init payload function
        switch (sensor.getPayload().getPayloadType()) {
            case camera:
                payload = new Camera((Camera) sensor.getPayload());
                break;
            case ideal:
                payload = new Ideal((Ideal) sensor.getPayload());
                break;
            case footprint:
                payload = new Footprint((Footprint) sensor.getPayload());
                break;
            case radar:
                payload = new Radar((Radar) sensor.getPayload());
                break;
        }
        // read motion model
        MotionModelType sensorMMType
                = sensor.getMotionModel().getType();
        switch (sensorMMType) {
            case rungekutta:
                motionModel = new RungeKutta((RungeKutta) sensor.getMotionModel());
                break;
            case staticModel:
                motionModel = new StaticModel((StaticModel) sensor.getMotionModel());
                break;
        }
        SensorState initState;
        if (sensorMMType == MotionModelType.staticModel) {
            // Static sensor
            // copy sensor initial State
            initState = new SensorState(
                    sensor.getInitState().getTime());
        } else { // ideal, footprint & radar
            // Dynamic sensor
            controlType = sensor.getControlType();
            controlAt = sensor.getControlAt();
            // copy sensor initial State
            initState = new SensorState(sensor.getInitState());
        }
        startTime = sensor.getStartTime();
        // copy sensor final State
        endTime = sensor.getEndTime();
        seqEndTime = sensor.getSeqEndTime();
        // add initState as the first state in the sensor path
        path = new ArrayList<>();
        path.add(initState);
        // init the cntrlSignals array        
        cntrlSignals = new ArrayList<>();
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
     * This method concatenates sensorA path and cntrlSignals (not modified)
     * with sensorB path and cntrlSignals (modified).
     *
     * @param sensorA not modified.
     * @param sensorB modified.
     * @param uavTime last uavState time.
     */
    public static void concatenateSensorSequence(
            Sensor sensorA, Sensor sensorB, double uavTime) {

        // get sensor paths
        ArrayList<SensorState> sensorAPath
                = sensorA.getPath();
        ArrayList<SensorState> sensorBPath
                = sensorB.getPath();

        if (!sensorA.isStatic()) {
            // dinamic sensors
            // get sensor cntrl lists
            ArrayList<SensorCntrlSignals> sensorACntrlSignals
                    = sensorA.getCntrlSignals();
            ArrayList<SensorCntrlSignals> sensorBCntrlSignals
                    = sensorB.getCntrlSignals();

            // concatenate sequence path to the whole solution
            sensorAPath.remove(0);
            sensorBPath.addAll(sensorAPath);

            // concatenate sequence cntrlSignals to the whole solution
            sensorBCntrlSignals.addAll(sensorACntrlSignals);

            // store path and cntrlSignals in the final solution
            sensorB.setPath(sensorBPath);
            sensorB.setCntrlSignals(sensorBCntrlSignals);

        } else {
            // static sensors
            SensorState newState = new SensorState(uavTime);
            sensorBPath.add(newState);
            sensorB.setPath(sensorBPath);
        }
    }

    /**
     * This method return an array list with the uav sensors that have to be
     * simulated for the given input sequence.
     *
     * @param sensors the uav sensors.
     * @param sequenceNum the actual sequence number.
     * @param sequenceTime the sequence time.
     *
     * @return an ArrayList with the uav sensors
     */
    public static ArrayList<Sensor> sensorsToSimulate(
            ArrayList<Sensor> sensors, int sequenceNum, double sequenceTime) {

        // data to hold the uav sensors that should be simulated in this sequence
        ArrayList<Sensor> sensorsToSimulate = new ArrayList<>();

        // sequence times
        double endSequenceTime = sequenceNum * sequenceTime;

        // for each sensor in the uav
        for (int s = 0; s < sensors.size(); ++s) {

            // copy the input sensor
            Sensor newSensor = new Sensor(sensors.get(s));

            // get scenario sensor initTime and endTime
            double sensorStartTime = sensors.get(s).getFinalState().getTime();
            double sensorEndTime = sensors.get(s).getEndTime();

            if (!sensors.get(s).isStatic()) {
                DinamicModel sensorMM = (DinamicModel) sensors.get(s).getMotionModel();
                sensorStartTime += sensorMM.getAt();
            }

            // check if sensor has to be simulated in this sequence
            if (sensorStartTime < endSequenceTime && sensorStartTime <= sensorEndTime) {

                // set newSensor endTime
                if (endSequenceTime > sensorEndTime) {
                    // adjust time to not exceed scenario sensor endTime
                    newSensor.setSeqEndTime(sensorEndTime);

                } else {
                    newSensor.setSeqEndTime(endSequenceTime);

                }

                // initial state for newSensor should be last simulated state
                SensorState lastSesnsorState
                        = sensors.get(s).getFinalState();
                newSensor.setInitState(lastSesnsorState);

                // add newSensor to sensorsToSimulate list
                sensorsToSimulate.add(newSensor);
            }
        }
        return sensorsToSimulate;
    }

}
