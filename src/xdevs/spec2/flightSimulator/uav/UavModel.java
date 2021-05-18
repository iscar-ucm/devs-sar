/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.spec2.flightSimulator.uav;

import models.environment.SearchArea;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import models.environment.WindMatrix;
import models.sensor.motionModels.MotionModelType;
import models.uav.Uav;
import xdevs.core.modeling.Coupled;
import xdevs.core.modeling.Port;
import xdevs.spec2.flightSimulator.uav.sensor.SensorModel;

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
    public Port<Uav> uO1 = new Port<>("fsUav");

    public UavModel(String coupledName, int index, JSONObject uavJS) {
        super(coupledName + " UM" + index);

        // EIC & EOC of the model
        super.addInPort(uI1);
        super.addInPort(uI2);
        super.addInPort(uI3);
        super.addOutPort(uO1);

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
            JSONObject sensorJS = (JSONObject) sensorJSArray.get(i);
            JSONObject motionModelJS = (JSONObject) sensorJS.get("motionModel");
            MotionModelType sensorMMType
                    = MotionModelType.valueOf((String) motionModelJS.get("type"));
            if (sensorMMType != MotionModelType.staticModel) {
                // ideal, footprint & radar do not have motion models
                SensorModel iSM = new SensorModel(getName(), i + 1);
                super.addComponent(iSM);
                // coupling of IC (UC & iSM)
                super.addCoupling(uc.ucO6.get(i), iSM.sI1);
                super.addCoupling(iSM.sO1, uc.ucI5.get(i));
            }
        }

        // coupling of EIC & EOC
        super.addCoupling(uI1, uc.ucI1);
        super.addCoupling(uI2, uc.ucI2);
        super.addCoupling(uI3, uc.ucI3);
        // coupling of EOC
        super.addCoupling(uc.ucO8, uO1);
    }

}
