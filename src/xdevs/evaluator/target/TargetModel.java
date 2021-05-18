/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.evaluator.target;

import org.json.simple.JSONObject;
import java.util.ArrayList;
import xdevs.core.modeling.Coupled;
import xdevs.core.modeling.Port;
import models.target.Target;
import models.target.motionModels.MotionModelType;
import models.sensor.payloads.Likelihood;

/**
 *
 * @author jbbordon
 */
public class TargetModel extends Coupled {

    // in Ports of the model
    public Port<Target> tI1 = new Port<>("target");
    public ArrayList<Port> tI2 = new ArrayList<>(); // sensor likelihoods from UM models    

    // out Ports of the model
    public Port<Target> tO1 = new Port<>("evaluatedTarget");

    public TargetModel(String coupledName, int index, JSONObject targetJS, int totalSensors) {
        super(coupledName + " TM" + index);

        // EIC & EOC of the model
        super.addInPort(tI1);
        for (int s = 1; s <= totalSensors; ++s) {
            Port<Likelihood> tmPortI2 = new Port<>("sensorLikehood" + s);
            tI2.add(tmPortI2);
            super.addInPort(tmPortI2);
        }
        super.addOutPort(tO1);

        // read target motion model
        JSONObject motionModelJS = (JSONObject) targetJS.get("motionModel");
        MotionModelType targetMMType
                = MotionModelType.valueOf((String) motionModelJS.get("type"));

        TargetControl tc;
        if (targetMMType == MotionModelType.staticModel) {
            /**
             * * STATIC TARGETS **
             */
            tc = new StaticTargetControl(this.getName(), totalSensors);
            super.addComponent(tc);
        } else {
            /**
             * * DINAMIC TARGETS **
             */
            tc = new DinamicTargetControl(this.getName(), totalSensors);
            super.addComponent(tc);
            TargetMotion tmm = new TargetMotion(this.getName());
            super.addComponent(tmm);
            // coupling of IC (TC & TM)
            super.addCoupling(tc.tcO2, tmm.tmI1);
            super.addCoupling(tc.tcO3, tmm.tmI2);
            super.addCoupling(tc.tcO4, tmm.tmI3);
            super.addCoupling(tmm.tmO1, tc.tcI3);
        }
            // coupling of EIC & EOC
        super.addCoupling(tI1, tc.tcI1);
        for (int s = 0; s < totalSensors; ++s) {
        	super.addCoupling(tI2.get(s), tc.tcI2.get(s));
        }
        super.addCoupling(tc.tcO1, tO1);
    }

}
