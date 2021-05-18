/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.uav.motionModels;

import models.environment.Wind;
import models.optimizer.DecisionVar;
import models.uav.UavCntrlSignals;
import models.uav.UavState;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import utils.JSONLoader;

/**
 *
 * @author jbbordon
 */
public abstract class MotionModel {
    
    protected final static double G = 9.8; // earth gravity

    protected MotionModelType type;
    protected double at;
    // uav decision array
    private DecisionVar[] decisionArray;
    // uav current state variable
    protected UavState uavState;
    // uav current cntrlSignals variable
    protected UavCntrlSignals cntrlSignals;
    // current wind
    protected Wind wind;
    // uav type specific parameters
    protected double vMax;        // maximum level speed (m/s)
    protected double vMin;        // minimum level speed, stall speed (m/s)
    protected double gammaMax;    // maximum climb angle (degrees)
    protected double phiMax;      // maximum bank angle (degrees)      
    protected double srMax;       // Maximum slew rate of climb
    protected double hMax;        // uav ceiling (meters)
    protected double tauH;        // time constant for hdot (sec)
    protected double tauVa;       // time constant for Vadot (sec)    
    protected double tauRoll;     // time constant for phi (sec)    
    protected double rangeMax;    // maximun range (meters)
    protected double tMax;        // maximum time of flight at nominal spee (sec)
    protected double vhMax;       // maximum rate of climb (m/s)
    protected double nMax;        // maximun load factor
    protected double jiMax;       // maximum track angle speed (rad/seg)
    protected double rMin;        // minimum radius of turn (meters)
    protected double kFuel;       // distance/time/speed 
    protected double safteyDist;  // distance in meters to avoid collisions

    /**
     *
     * @param uavType
     * @param motionModelJSON
     */
    public MotionModel(String uavType, JSONObject motionModelJSON) {
        type = MotionModelType.valueOf(
                (String) motionModelJSON.get("type"));
        at = (double) motionModelJSON.get("at");
        JSONArray decisionJS = (JSONArray) motionModelJSON.get("decision");
        decisionArray = new DecisionVar[decisionJS.size()];
        for (int i = 0; i < decisionJS.size(); ++i) {
            JSONObject varJSON = (JSONObject) decisionJS.get(i);
            DecisionVar decisionVar = new DecisionVar(varJSON);
            decisionArray[i] = decisionVar;
        }
        cntrlSignals = new UavCntrlSignals();
        wind = new Wind();
        // read uav type specific flight parameters
        JSONObject uavParameters = JSONLoader.getUavParameters(uavType);
        vMax = (double) uavParameters.get("VMAX");
        vMin = (double) uavParameters.get("VMIN");
        gammaMax = (double) uavParameters.get("GAMMAMAX");
        phiMax = (double) uavParameters.get("PHIMAX");
        srMax = (double) uavParameters.get("SRMAX");
        hMax = (double) uavParameters.get("HMAX");
        tauVa = (double) uavParameters.get("TAUVA");
        tauH = (double) uavParameters.get("TAUH");
        tauRoll = (double) uavParameters.get("TAUROLL");
        rangeMax = (double) uavParameters.get("RANGEMAX");
        tMax = (double) uavParameters.get("TMAX");
        safteyDist = (double) uavParameters.get("SAFTEYDIST");        
        vhMax = vMin * Math.tan(Math.PI * gammaMax / 180.0);
        nMax = 1.0 / Math.cos(Math.PI * phiMax / 180.0); // no se usa
        jiMax = G * Math.tan(Math.PI * phiMax / 180.0) / vMin;
        rMin = vMin * vMin / G / Math.tan(Math.PI * phiMax / 180.0); // no se usa
        kFuel = rangeMax / tMax / 45.0;           
    }

    /**
     *
     * @param motionModel
     */
    public MotionModel(MotionModel motionModel) {
        // copy the given Motion Model
        type = motionModel.getType();
        at = motionModel.getAt();
        decisionArray = motionModel.getDecisionArray();
        cntrlSignals = new UavCntrlSignals();
        wind = new Wind();
        vMax = motionModel.getvMax();
        vMin = motionModel.getvMin();
        gammaMax = motionModel.getGammaMax();
        phiMax = motionModel.getPhiMax();
        srMax = motionModel.getSrMax();
        hMax = motionModel.gethMax();
        tauVa = motionModel.getTauVa();
        tauH = motionModel.getTauH();
        tauRoll = motionModel.getTauRoll();
        rangeMax = motionModel.getRangeMax();
        tMax = motionModel.gettMax();
        safteyDist = motionModel.getSafteyDist();
        vhMax = motionModel.getVhMax();
        nMax = motionModel.getnMax();
        jiMax = motionModel.getJiMax();
        rMin = motionModel.getrMin();
        kFuel = motionModel.getkFuel();
    }

    /**
     * @return the type
     */
    public MotionModelType getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(MotionModelType type) {
        this.type = type;
    }

    /**
     * @return the at
     */
    public double getAt() {
        return at;
    }

    /**
     * @param at the at to set
     */
    public void setAt(double at) {
        this.at = at;
    }

    /**
     * @return the decisionArray
     */
    public DecisionVar[] getDecisionArray() {
        return decisionArray;
    }

    /**
     * @param decisionArray the decisionArray to set
     */
    public void setDecisionArray(DecisionVar[] decisionArray) {
        this.decisionArray = decisionArray;
    }

    /**
     * @param wind the wind to set
     */
    public void setWind(Wind wind) {
        this.wind = wind;
    }

    /**
     * @return the vMax
     */
    public double getvMax() {
        return vMax;
    }

    /**
     * @return the vMin
     */
    public double getvMin() {
        return vMin;
    }

    /**
     * @return the gammaMax
     */
    public double getGammaMax() {
        return gammaMax;
    }

    /**
     * @return the phiMax
     */
    public double getPhiMax() {
        return phiMax;
    }

    /**
     * @return the srMax
     */
    public double getSrMax() {
        return srMax;
    }

    /**
     * @return the hMax
     */
    public double gethMax() {
        return hMax;
    }

    /**
     * @return the tauH
     */
    public double getTauH() {
        return tauH;
    }

    /**
     * @return the tauVa
     */
    public double getTauVa() {
        return tauVa;
    }

    /**
     * @return the tauRoll
     */
    public double getTauRoll() {
        return tauRoll;
    }

    /**
     * @return the rangeMax
     */
    public double getRangeMax() {
        return rangeMax;
    }

    /**
     * @return the tMax
     */
    public double gettMax() {
        return tMax;
    }

    /**
     * @return the vhMax
     */
    public double getVhMax() {
        return vhMax;
    }

    /**
     * @return the nMax
     */
    public double getnMax() {
        return nMax;
    }

    /**
     * @return the jiMax
     */
    public double getJiMax() {
        return jiMax;
    }

    /**
     * @return the rMin
     */
    public double getrMin() {
        return rMin;
    }

    /**
     * @return the kFuel
     */
    public double getkFuel() {
        return kFuel;
    }

    /**
     * @return the safteyDist
     */
    public double getSafteyDist() {
        return safteyDist;
    }  
    
    /**
     * @param cntrlSignals the UavCntrlSignals to set
     */
    public void setCntrlSignals(UavCntrlSignals cntrlSignals) {
        // for each decision variable
        for (int i = 0; i < getDecisionArray().length; ++i) {
            switch (getDecisionArray()[i].getName()) {
                case heading: // Yaw control
                    switch (getDecisionArray()[i].getType()) {
                        case rate:
                        case absolute:
                        case noaction:
                            this.cntrlSignals.setcHeading(cntrlSignals.getcHeading());
                            break;
                        case increment:
                            double currentHeading = this.cntrlSignals.getcHeading();
                            currentHeading += cntrlSignals.getcHeading();
                            // make sure heading is in range
                            if (Math.abs(currentHeading) > 180) {
                                if (currentHeading > 180) {
                                    currentHeading -= 360;
                                } else {
                                    currentHeading += 360;
                                }
                            }
                            this.cntrlSignals.setcHeading(currentHeading);
                            break;
                    }
                    break;
                case elevation: // Elevation control
                    switch (getDecisionArray()[i].getType()) {
                        case rate:
                        case absolute:
                        case noaction:
                            this.cntrlSignals.setcElevation(cntrlSignals.getcElevation());
                            break;
                        case increment:
                            double currentElevation = this.cntrlSignals.getcElevation();
                            currentElevation += cntrlSignals.getcElevation();
                            this.cntrlSignals.setcElevation(currentElevation);
                            break;
                    }
                    break;
                case speed:
                    switch (getDecisionArray()[i].getType()) {
                        case rate:
                        case absolute:
                        case noaction:
                            this.cntrlSignals.setcSpeed(cntrlSignals.getcSpeed());
                            break;
                        case increment:
                            double currentSpeed = this.cntrlSignals.getcSpeed();
                            currentSpeed += cntrlSignals.getcSpeed();
                            this.cntrlSignals.setcSpeed(currentSpeed);
                            break;
                    }
                    break;
            }
        }
    }

    public abstract void initModel(UavState initState);

    public abstract UavState stepModel();

    public abstract void resetModel();

}
