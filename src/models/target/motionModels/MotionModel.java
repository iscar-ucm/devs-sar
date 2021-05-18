/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.target.motionModels;

import org.json.simple.JSONObject;

/**
 *
 * @author jbbordon
 */
public abstract class MotionModel {

    protected MotionModelType type;

    /**
     *
     * @param motionModelJSON
     */
    public MotionModel(JSONObject motionModelJSON) {
        type = MotionModelType.valueOf(
                (String) motionModelJSON.get("type"));
    }

    public MotionModel(MotionModel motionModel) {
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
