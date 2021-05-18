/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.flightSimulator.uav.sensor;

import models.sensor.Sensor;
import models.sensor.motionModels.MotionModelType;
import models.sensor.payloads.Likelihood;
import models.uav.UavState;
import org.json.simple.JSONObject;
import xdevs.core.modeling.Coupled;
import xdevs.core.modeling.Port;

/**
 *
 * @author jbbordon
 */
public class SensorModel extends Coupled {

    // in Ports of the model
    public Port<Sensor> sI1 = new Port<>("sensor");
    public Port<UavState> sI2 = new Port<>("uavState");

    // out Ports of the model
    public Port<Likelihood> sO1 = new Port<>("sensorLikelihood");
    public Port<Sensor> sO2 = new Port<>("fsSensor");

    public SensorModel(String coupledName, int index, JSONObject sensorJS) {
        super(coupledName + " SM" + index);

        // EIC & EOC of the model
        super.addInPort(sI1);       
        super.addInPort(sI2);
        super.addOutPort(sO1);
        super.addOutPort(sO2);

        JSONObject motionModelJS = (JSONObject) sensorJS.get("motionModel");
        MotionModelType sensorMMType
                = MotionModelType.valueOf((String) motionModelJS.get("type"));

        // SP, SC & SM model creations
        if (sensorMMType != MotionModelType.staticModel) {
            
            // dinamic sensors have a SMM model    
            SensorControl sc = new SensorControl(this.getName(), index);
            super.addComponent(sc);
            SensorMotion sm = new SensorMotion(this.getName(), index);
            super.addComponent(sm);
            DinamicSensorPayload sp = new DinamicSensorPayload(this.getName(), index);
            super.addComponent(sp);            

            // coupling of IC (SCM & SMM)                                          
            super.addCoupling(sc.scO1, sm.smI1);
            super.addCoupling(sc.scO2, sm.smI2);
            super.addCoupling(sc.scO3, sm.smI3);
            super.addCoupling(sc.scO4, sm.smI4);
            super.addCoupling(sm.smO1, sc.scI2);
            super.addCoupling(sm.smO1, sp.spI3);

            // coupling of EIC & EOC
            super.addCoupling(sI1, sc.scI1);
            super.addCoupling(sI2, sp.spI2);            
            super.addCoupling(sI1, sp.spI1);                       
            super.addCoupling(sc.scO5, sO2);
            super.addCoupling(sp.spO1, sO1);            

        } else {     
            // static sensors do not have motion model
            StaticSensorPayload sp = new StaticSensorPayload(this.getName(), index);
            super.addComponent(sp); 
            
            // coupling of EIC & EOC
            super.addCoupling(sI1, sp.spI1);
            super.addCoupling(sI2, sp.spI2);            
            super.addCoupling(sp.spO1, sO1);               
        }
    }

}
