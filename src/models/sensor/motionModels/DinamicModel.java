/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.sensor.motionModels;

import models.optimizer.DecisionVar;
import models.sensor.SensorCntrlSignals;
import models.sensor.SensorState;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author Juan
 */
public abstract class DinamicModel extends MotionModel {

    protected double at;        
    // for each cntrl signal interpretation 0 speed 1 increment 2 absolute
    private DecisionVar[] decisionArray;
    // sensor current cntrl signals
    protected SensorCntrlSignals cntrlSignals;

    public DinamicModel(JSONObject motionModelJSON) {
        super(motionModelJSON);
        at = (double) motionModelJSON.get("at");               
        // read decisionArray
        JSONArray decisionJS = (JSONArray) motionModelJSON.get("decision");
        decisionArray = new DecisionVar[decisionJS.size()];
        for (int i = 0; i < decisionJS.size(); ++i) {
            JSONObject varJSON = (JSONObject) decisionJS.get(i);
            DecisionVar decisionVar = new DecisionVar(varJSON);
            decisionArray[i] = decisionVar;
        }
        cntrlSignals = new SensorCntrlSignals();
    }
    
    /**
     *
     * @param motionModel
     */
    public DinamicModel(DinamicModel motionModel) {
        super(motionModel); 
        // copy the given Motion Model
        at = motionModel.getAt();         
        decisionArray = motionModel.getDecisionArray();
        cntrlSignals = new SensorCntrlSignals();
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
     *
     * @param cntrlSignals the SensorCntrlSignals to set
     */
    public void setCntrlSignals(SensorCntrlSignals cntrlSignals) {
        // for each decision variable
        for (int i = 0; i < getDecisionArray().length; ++i) {
            switch (getDecisionArray()[i].getName()) {
                case azimuth: // Yaw control
                    switch (getDecisionArray()[i].getType()) {
                        case rate:
                        case absolute:
                        case noaction:
                            this.cntrlSignals.setcAzimuth(cntrlSignals.getcAzimuth());
                            break;
                        case increment:
                            double currentAzimuth = this.cntrlSignals.getcAzimuth();
                            currentAzimuth += cntrlSignals.getcAzimuth();
                            // make sure heading is in range
                            if (Math.abs(currentAzimuth) > 180) {
                                if (currentAzimuth > 180) {
                                    currentAzimuth -= 360;                                    
                                } else {
                                    currentAzimuth += 360;
                                }
                            }                                
                            this.cntrlSignals.setcAzimuth(currentAzimuth);
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
                            // make sure heading is in range
                            if (Math.abs(currentElevation) > 180) {
                                if (currentElevation > 180) {
                                    currentElevation -= 360;                                    
                                } else {
                                    currentElevation += 360;
                                }
                            }                              
                            this.cntrlSignals.setcElevation(currentElevation);
                            break;
                    }
                    break;
            }
        }
    }   
 
    public abstract void initModel(SensorState initState);

    public abstract SensorState stepModel();

    public abstract void resetModel();
    
}
