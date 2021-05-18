/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.sensor.motionModels;

import org.json.simple.JSONObject;

/**
 * NOTE: For static models sensor control at should be equal to to uav notion
 * model at.
 *
 * @author jbbordon
 */
public class StaticModel extends MotionModel {

    /**
     *
     * @param motionModelJSON
     */
    public StaticModel(JSONObject motionModelJSON) {
        super(motionModelJSON);
    }

    /**
     *
     * @param motionModel
     */
    public StaticModel(StaticModel motionModel) {
        super(motionModel);
    }        

}
