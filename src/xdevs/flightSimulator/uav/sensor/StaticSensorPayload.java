/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.flightSimulator.uav.sensor;

import utils.CommonOperations;

/**
 *
 * @author jbbordon
 */
public class StaticSensorPayload extends SensorPayload {

    /**
     *
     * @param coupledName
     * @param index
     */
    public StaticSensorPayload(String coupledName, int index) {
        super(coupledName, index);
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
                scenarioTime = uavState.getTime();
                myPayload.setUavState(uavState);
                // check if sensor has to measure
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
