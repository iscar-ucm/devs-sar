/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.flightSimulator.uav.sensor;

import models.sensor.SensorState;
import utils.CommonOperations;
import xdevs.core.modeling.Port;

/**
 *
 * @author jbbordon
 */
public class DinamicSensorPayload extends SensorPayload {

    // in Ports of the model
    public Port<SensorState> spI3 = new Port<>("sensorState"); // current sensorState

    // internal data
    private SensorState sensorState;

    /**
     *
     * @param coupledName
     * @param index
     */
    public DinamicSensorPayload(String coupledName, int index) {
        super(coupledName, index);
        super.addInPort(spI3);
    }

    @Override
    public void initialize() {
        sensorState = null;
        super.initialize();
    }

    @Override
    public void exit() {
        sensorState = null;
        super.exit();
    }

    @Override
    public void deltint() {
        if (phaseIs("start")) {
            // wait infinity
            clock = Double.MAX_VALUE;
            super.holdIn("waiting", clock);

        } else if (phaseIs("capture")) {
            if ((scenarioTime + myPayload.getCaptureAt())
                    > mySensor.getSeqEndTime()) {
                // sensor time has ended
                exit();
            } else {
                // wait infinity
                super.holdIn("waiting", clock);
            }
        }
    }

    @Override
    public void deltext(double e) {
        if (phaseIs("passive")) {
            // model is waiting for input data to start simulation
            if (!spI1.isEmpty()) {
                mySensor = spI1.getSingleValue();
                myPayload = mySensor.getPayload();
            }
            if (myPayload != null) {
                // start simulation
                clock = mySensor.getInitState().getTime();
                // wait until sensor startTime
                super.holdIn("start", clock);
            }

        } else if (phaseIs("waiting")) {
            // check spI2 port for new uavState
            if (!spI2.isEmpty()) {
                // add the newState received from UMM model
                uavState = spI2.getSingleValue();
                myPayload.setUavState(uavState);
                scenarioTime = uavState.getTime();
            }
            if (!spI3.isEmpty()) {
                // add the newState received from SMM model                
                sensorState = spI3.getSingleValue();
                myPayload.setSensorState(sensorState);
                scenarioTime = sensorState.getTime();
            }
            // check if sensor has to measure
            if (sensorState != null && uavState != null) {
                if (CommonOperations.isEqual(sensorState.getTime(), uavState.getTime())) {
                    double elapsedTime = scenarioTime - mySensor.getStartTime();
                    if (CommonOperations.isMultiple(elapsedTime, myPayload.getCaptureAt())) {
                        // output sensorLikelihood
                        myLikelihood = myPayload.evaluate();
                        myLikelihood.setTime(scenarioTime);
                        super.holdIn("capture", 0.0);
                    }
                }
            }
        }
    }

    @Override
    public void deltcon() {
        deltext(0);
        deltint();
    }

    @Override
    public void lambda() {
        if (phaseIs("capture")) {
            // output sensor likelihood
            spO1.addValue(myLikelihood);
        }
    }

}
