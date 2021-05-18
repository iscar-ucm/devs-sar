/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.target.motionModels;

import org.json.simple.JSONObject;

/**
 *
 * @author Juan
 */
public class StaticModel extends MotionModel {

    /**
     *
     * @param motionModelJSON
     */
    public StaticModel(JSONObject motionModelJSON) {
        super(motionModelJSON);
    }

    public StaticModel(StaticModel motionModel) {
        super(motionModel);
    }      
    
}
