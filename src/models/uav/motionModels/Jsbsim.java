/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.uav.motionModels;

import models.uav.UavState;
import org.json.simple.JSONObject;

/**
 *
 * @author jbbordon
 */
public class Jsbsim extends MotionModel {

    /**
     *
     * @param uavType
     * @param motionModelJSON
     */
    public Jsbsim(String uavType, JSONObject motionModelJSON) {
        super(uavType, motionModelJSON);
    }

    /**
     *
     * @param motionModel
     */
    public Jsbsim(Jsbsim motionModel) {
        super(motionModel);
    }    

    @Override
    public void initModel(UavState initState) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.         
    }

    @Override
    public UavState stepModel() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void resetModel() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
