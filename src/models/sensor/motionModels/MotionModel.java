/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.sensor.motionModels;

import models.sensor.SensorState;
import org.json.simple.JSONObject;

/**
 *
 * @author jbbordon
 */
public abstract class MotionModel {
   
    protected MotionModelType type;
    // sensor current state variable
    protected SensorState currentState;
    
    /**
     *
     * @param motionModelJSON
     */
    public MotionModel(JSONObject motionModelJSON) {
        type = MotionModelType.valueOf(
                (String) motionModelJSON.get("type")); 
    }

    /**
     *
     * @param motionModel
     */
    public MotionModel(MotionModel motionModel) {
        // copy the given Motion Model
        type = motionModel.getType();       
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

}
