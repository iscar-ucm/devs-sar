/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.flightSimulator.uav;

import java.util.ArrayList;
import models.environment.SearchArea;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import models.environment.WindMatrix;
import models.uav.Uav;
import models.sensor.payloads.Likelihood;
import xdevs.core.modeling.Coupled;
import xdevs.core.modeling.Port;
import xdevs.flightSimulator.uav.sensor.SensorModel;

/**
 *
 * @author jbbordon
 */
public class UavModel extends Coupled {

    // in Ports of the model
    public Port<SearchArea> uI1 = new Port<>("searchArea");    
    public Port<WindMatrix> uI2 = new Port<>("windMatrix");
    public Port<Uav> uI3 = new Port<>("uav");

    // out Ports of the model
    public ArrayList<Port> uO1 = new ArrayList<>(); // iSensor Likelihood at time t
    public Port<Uav> uO2 = new Port<>("fsUav");

    public UavModel(String coupledName, int index, JSONObject uavJS) {
        super(coupledName + " UM" + index);

        // EIC & EOC of the model
        super.addInPort(uI1);
        super.addInPort(uI2);
        super.addInPort(uI3);
        super.addOutPort(uO2);

        // UC & UM model creations
        JSONArray sensorJSArray = (JSONArray) uavJS.get("sensors");
        UavControl uc = new UavControl(this.getName(), sensorJSArray);
        super.addComponent(uc);
        UavMotion umm = new UavMotion(this.getName());
        super.addComponent(umm);

        // coupling of IC (UC & UM)                                          
        super.addCoupling(uc.ucO1, umm.umI1);
        super.addCoupling(uc.ucO2, umm.umI2);
        super.addCoupling(uc.ucO3, umm.umI3);
        super.addCoupling(uc.ucO4, umm.umI4);
        super.addCoupling(uc.ucO5, umm.umI5);
        super.addCoupling(umm.umO1, uc.ucI4);

        // iSM model creations
        for (int i = 0; i < sensorJSArray.size(); ++i) {
            // s ports uO1 (one per sensor in the uav)
            Port<Likelihood> sPort1 = new Port<>("sensorLikelihood" + i);
            uO1.add(sPort1);
            super.addOutPort(sPort1);
            
            JSONObject sensorJS = (JSONObject) sensorJSArray.get(i);
            SensorModel iSM = new SensorModel(getName(), i + 1, sensorJS);
            super.addComponent(iSM);
            // coupling of IC (UC & iSM)
            super.addCoupling(uc.ucO6.get(i), iSM.sI1);                     
            super.addCoupling(iSM.sO1, uO1.get(i));
            super.addCoupling(iSM.sO2, uc.ucI5.get(i));
            // coupling of IC (UMM & iSM)
            super.addCoupling(umm.umO1, iSM.sI2);            
        }

        // coupling of EIC
        super.addCoupling(uI1, uc.ucI1);        
        super.addCoupling(uI2, uc.ucI2);
        super.addCoupling(uI3, uc.ucI3);
        // coupling of EOC
        super.addCoupling(uc.ucO7, uO2);
    }

}
