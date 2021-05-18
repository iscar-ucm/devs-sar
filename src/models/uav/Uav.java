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
public class Uav implements Cloneable {

    private String name;
    private String type;
    private UavCntrlType controlType;
    private double controlAt;
    private MotionModel motionModel;
    private double startTime;
    private double endTime;
    private double seqEndTime;
    private double endFuel;
    private ArrayList<Sensor> sensors;
    private ArrayList<UavState> path;
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
        controlType = UavCntrlType.valueOf((String)  controlJS.get("controlType"));
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
        startTime = initState.getTime();
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
        // init the cntrlSignals array
        cntrlSignals = new ArrayList<>();
        // init other data
        totalNFZs = 0;
        totalCollisions = 0;
        totalFuelEmpties = 0;
        smoothValue = 0.0;
    }

    /**
     * Constructor. Copies the input Uav but with path and cntrlSignals empty.
     *
     * @param uav object to create a copy
     */
    public Uav(Uav uav) {
        // copy the given uav
        name = uav.getName();
        type = uav.getType();
        controlType = uav.getControlType();
        controlAt = uav.getControlAt();
        MotionModelType uavMMType
                = uav.getMotionModel().getType();
        // copy the motion model
        switch (uavMMType) {
            case rungekutta:
                motionModel = new RungeKutta((RungeKutta) uav.getMotionModel());
                break;
            case jsbsim:
                motionModel = new Jsbsim((Jsbsim) uav.getMotionModel());
                break;
        }
        // copy UAV initial State
        UavState initState = new UavState(uav.getInitState());
        startTime = uav.getStartTime();
        // add initState as the first state in the uav path
        path = new ArrayList<>();
        path.add(initState);
        // copy UAV final State        
        endFuel = uav.getEndFuel();
        endTime = uav.getEndTime();
        seqEndTime = uav.getSeqEndTime();
        // copy UAV sensors
        short index;
        sensors = new ArrayList<>();
        for (index = 0; index < uav.getSensors().size(); index++) {
            sensors.add(new Sensor(uav.getSensors().get(index)));
        }
        // init the cntrlSignals array
        cntrlSignals = new ArrayList<>();
        // init other data
        totalNFZs = 0;
        totalCollisions = 0;
        totalFuelEmpties = 0;
        smoothValue = 0.0;        
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
     * This method concatenates uavA path and cntrlSignals (not modified) with
     * uavB path and cntrlSignals (modified).
     *
     * @param uavA not modified.
     * @param uavB modified.
     */
    public static void concatenateUavSequence(Uav uavA, Uav uavB) {

        // get uav paths and cntrl lists
        ArrayList<UavState> uavAPath
                = uavA.getPath();
        ArrayList<UavCntrlSignals> uavACntrlSignals
                = uavA.getCntrlSignals();
        ArrayList<UavState> uavBPath
                = uavB.getPath();
        ArrayList<UavCntrlSignals> uavBCntrlSignals
                = uavB.getCntrlSignals();

        // concatenate sequence path to the whole solution
        uavAPath.remove(0);
        uavBPath.addAll(uavAPath);

        // concatenate sequence cntrlSignals to the whole solution
        uavBCntrlSignals.addAll(uavACntrlSignals);

        // store path and cntrlSignals in the final solution
        uavB.setPath(uavBPath);
        uavB.setCntrlSignals(uavBCntrlSignals);

        // loop each sensor in the solution
        for (int s = 0; s < uavA.getSensors().size(); ++s) {
            Sensor.concatenateSensorSequence(
                    uavA.getSensors().get(s),
                    uavB.getSensors().get(s),
                    uavBPath.get(uavBPath.size() - 1).getTime());
        }
    }

    /**
     * This method copies the input UavArrayList and returns another list as a
     * copy. Control signals are left empty.
     *
     * @param uavs
     * @return a copy of uavs.
     */
    public static ArrayList<Uav> copyUavsArray(ArrayList<Uav> uavs) {

        // the copy to return
        ArrayList<Uav> uavsCopy = new ArrayList<>();

        // for each uav in the array
        for (int u = 0; u < uavs.size(); ++u) {
            // copy the input uav
            Uav newUav = new Uav(uavs.get(u));
            uavsCopy.add(newUav);
        }

        return uavsCopy;
    }

    /**
     * This method return an array list with the uavs that have to be simulated
     * for the given input sequence.
     *
     * @param uavs the secenario uavs.
     * @param sequenceNum the actual sequence number.
     * @param sequenceTime the sequence time.
     *
     * @return an ArrayList with the uavs.
     */
    public static ArrayList<Uav> uavsToSimulate(
            ArrayList<Uav> uavs, int sequenceNum, double sequenceTime) {

        // data to hold the scenario uavs that should be simulated in this sequence
        ArrayList<Uav> simulationUavs = new ArrayList<>();

        // sequence times
        double endSequenceTime = sequenceNum * sequenceTime;

        for (int u = 0; u < uavs.size(); ++u) {

            // make a copy of the scenario uav
            Uav newUav = new Uav(uavs.get(u));

            // get scenario uav initTime and endTime
            double uavStartTime = uavs.get(u).getFinalState().getTime()
                    + uavs.get(u).getMotionModel().getAt();
            double uavEndTime = uavs.get(u).getEndTime();

            // check if uav has to be simulated in this sequence
            if (uavStartTime < endSequenceTime && uavStartTime <= uavEndTime) {

                // set newUav endTime
                if (endSequenceTime > uavEndTime) {
                    // adjust time to not exceed scenario uav endTime
                    newUav.setSeqEndTime(uavEndTime);

                } else {
                    newUav.setSeqEndTime(endSequenceTime);

                }

                // initial state for newUav should be lastUavState simulated
                UavState lastUavState = uavs.get(u).getFinalState();
                newUav.setInitState(lastUavState);

                // check sensors to be simulated
                newUav.setSensors(Sensor.sensorsToSimulate(
                        uavs.get(u).getSensors(), sequenceNum, sequenceTime));

                // add the uav to simulationUavs list
                simulationUavs.add(newUav);
            }
        }

        return simulationUavs;
    }

    /**
     * This method creates a new Uav population given the uavs to populate, the
     * number of individuals and the sequence number and time,
     *
     * @param uavs
     * @param numSol
     * @param sequenceNum
     * @param sequenceTime
     * @return the uav population for the sequence number and time.
     */
    public static ArrayList<ArrayList<Uav>> populateNsUavs(
            ArrayList<Uav> uavs, int numSol, int sequenceNum, double sequenceTime) {

        // new population to return
        ArrayList<ArrayList<Uav>> uavsPopulation = new ArrayList<>();

        // loop numSol times to create the new population
        for (int i = 0; i < numSol; ++i) {
            uavsPopulation.add(copyUavsArray(uavsToSimulate(uavs, sequenceNum, sequenceTime)));
        }

        return uavsPopulation;
    }

}
