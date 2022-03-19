/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.flightSimulator.uav.sensor;

import models.uav.UavState;
import models.sensor.Sensor;
import models.sensor.payloads.Likelihood;
import models.sensor.payloads.Payload;
import xdevs.core.modeling.Atomic;
import xdevs.core.modeling.Port;

/**
 *
 * @author jbbordon
 */
public abstract class SensorPayload extends Atomic {

    // in Ports of the model
    public Port<Sensor> spI1 = new Port<>("sensor"); // sensor  
    public Port<UavState> spI2 = new Port<>("uavState"); // current uav state

    // out Ports of the model
    public Port<Likelihood> spO1 = new Port<>("likelihood"); // sensor likelihood

    // internal data
    protected Sensor mySensor;
    protected Payload myPayload;
    protected UavState uavState;
    protected Likelihood myLikelihood;
    protected double clock, scenarioTime;

    /**
     *
     * @param coupledName
     * @param index
     */
    public SensorPayload(String coupledName, int index) {
        super(coupledName + " SP" + index);
        // Ports of the Atomic model
        super.addInPort(spI1);
        super.addInPort(spI2);
        super.addOutPort(spO1);
    }

    @Override
    public void initialize() {
        mySensor = null;
        myPayload = null;
        uavState = null;
        myLikelihood = null;
        clock = 0.0;
        scenarioTime = 0.0;
        super.passivate();
    }

    @Override
    public void exit() {
        mySensor = null;
        myPayload = null;        
        uavState = null;
        myLikelihood = null;
        clock = 0.0;
        scenarioTime = 0.0;
        super.passivate();
    }
    
}
