/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.spec2.flightSimulator.uav.sensor;

import xdevs.core.modeling.Coupled;
import xdevs.core.modeling.Port;

import models.sensor.Sensor;

/**
 *
 * @author jbbordon
 */
public class SensorModel extends Coupled {

    // in Ports of the model
    public Port<Sensor> sI1 = new Port<>("sensor");

    // out Ports of the model
    public Port<Sensor> sO1 = new Port<>("fsSensor");

    public SensorModel(String coupledName, int index) {
        super(coupledName + " SM" + index);

        // EIC & EOC of the model
        super.addInPort(sI1);
        super.addOutPort(sO1);

        // SCM & SMM model creations
        SensorControl sc = new SensorControl(this.getName(), index);
        super.addComponent(sc);
        SensorMotion sm = new SensorMotion(this.getName(), index);
        super.addComponent(sm);

        // coupling of IC (SCM & SMM)                                          
        super.addCoupling(sc.scO1, sm.smI1);
        super.addCoupling(sc.scO2, sm.smI2);
        super.addCoupling(sc.scO3, sm.smI3);
        super.addCoupling(sc.scO4, sm.smI4);
        super.addCoupling(sm.smO1, sc.scI2);

        // coupling of EIC & EOC
        super.addCoupling(sI1, sc.scI1);
        super.addCoupling(sc.scO5, sO1);
    }

}
