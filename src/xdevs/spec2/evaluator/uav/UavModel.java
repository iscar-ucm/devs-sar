/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.spec2.evaluator.uav;

import java.util.ArrayList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import xdevs.core.modeling.Coupled;
import xdevs.core.modeling.Port;
import models.uav.Uav;
import models.sensor.motionModels.MotionModelType;
import models.sensor.payloads.Likelihood;
import xdevs.flightSimulator.uav.sensor.SensorPayload;
import xdevs.flightSimulator.uav.sensor.StaticSensorPayload;

/**
 *
 * @author jbbordon
 */
public class UavModel extends Coupled {

    // in Ports of the model
    public Port<Uav> uI1 = new Port<>("uav");

    // out Ports of the model
    public ArrayList<Port> uO1 = new ArrayList<>(); // iSensor Likelihood at time t

    public UavModel(String coupledName, int index, JSONObject uavJS) {
        super(coupledName + " UM" + index);

        // EIC & EOC of the model
        super.addInPort(uI1);

        // UC model creation
        JSONArray sensorJSArray = (JSONArray) uavJS.get("sensors");
        UavControl uc = new UavControl(this.getName(), sensorJSArray.size());
        super.addComponent(uc);

        // iSP model creation
        for (int i = 0; i < sensorJSArray.size(); ++i) {

            // s ports uO1 (one per sensor in the uav)
            Port<Likelihood> sPort1 = new Port<>("sensorLikelihood" + i);
            uO1.add(sPort1);
            super.addOutPort(sPort1);   
                     
            SensorPayload iSP;
            JSONObject sensorJS = (JSONObject) sensorJSArray.get(i);
            JSONObject motionModelJS = (JSONObject) sensorJS.get("motionModel");
            MotionModelType sensorMMType
                    = MotionModelType.valueOf((String) motionModelJS.get("type"));
            if (sensorMMType != MotionModelType.staticModel) 
            { // dinamic sensor
                iSP = new DinamicSensorPayload(this.getName(), i + 1);
                super.addComponent(iSP);
            } else {
              // static sensor
                iSP = new StaticSensorPayload(this.getName(), i + 1);
                super.addComponent(iSP);
            }

            // coupling of IC (UC & iSP)
            super.addCoupling(uc.ucO1.get(i), iSP.spI1);
            super.addCoupling(uc.ucO2, iSP.spI2);
            super.addCoupling(iSP.spO1, uO1.get(i));
        }

        // coupling of EIC & EOC
        super.addCoupling(uI1, uc.ucI1);
    }

}
