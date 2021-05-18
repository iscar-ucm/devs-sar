/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.spec2.evaluator.uav;

import models.sensor.SensorState;
import utils.CommonOperations;
import xdevs.flightSimulator.uav.sensor.SensorPayload;

/**
 *
 * @author jbbordon
 */
public class DinamicSensorPayload extends SensorPayload {

    // internal data
    protected SensorState sensorState;
    protected int currentStateIdx;

    /**
     *
     * @param coupledName
     * @param index
     */
    public DinamicSensorPayload(String coupledName, int index) {
        super(coupledName, index);
    }

    @Override
    public void initialize() {
        sensorState = null;
        currentStateIdx = 0;
        super.initialize();
    }

    @Override
    public void exit() {
        sensorState = null;
        currentStateIdx = 0;
        super.exit();
    }

    @Override
    public void deltint() {
        if (phaseIs("start")) {
            // wait until next sensor state
            sensorState = nextSensorState();
            scenarioTime = sensorState.getTime();
            currentStateIdx++;
            clock = nextSensorUpdate() - scenarioTime;
            super.holdIn("moving", clock);
        } else if (phaseIs("moving")) {
            // update the sensor state
            sensorState = nextSensorState();
            myPayload.setSensorState(sensorState);
            scenarioTime = sensorState.getTime();

            // check if sensor has to measure
            if (sensorState != null && uavState != null) {
                if (CommonOperations.isEqual(sensorState.getTime(), uavState.getTime())) {
                    double elapsedTime = scenarioTime - mySensor.getStartTime();
                    if (CommonOperations.isMultiple(elapsedTime, myPayload.getCaptureAt())) {
                        // output sensorLikelihood
                        myLikelihood = myPayload.evaluate();
                        myLikelihood.setTime(scenarioTime);
                        super.holdIn("capture", 0.0);
                    } else {
                        // update state pointer
                        currentStateIdx++;
                        if (currentStateIdx < mySensor.getPath().size()) {
                            // wait until next sensor state 
                            clock = nextSensorUpdate() - scenarioTime;
                            super.holdIn("moving", clock);
                        } else {
                            // all sensor states have been processed,
                            exit();
                        }
                    }
                }
            }

        } else if (phaseIs("capture")) {
            // update state pointer
            currentStateIdx++;
            if (currentStateIdx < mySensor.getPath().size()) {
                // wait until next sensor state 
                clock = nextSensorUpdate() - scenarioTime;
                super.holdIn("moving", clock);
            } else {
                // all sensor states have been processed,
                exit();
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
                clock = nextSensorUpdate();
                super.holdIn("start", clock);
            }

        } else if (phaseIs("moving")) {
            // check spI2 port for a new uavState
            if (!spI2.isEmpty()) {
                uavState = spI2.getSingleValue();
                myPayload.setUavState(uavState);
                scenarioTime = uavState.getTime();
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

    private SensorState nextSensorState() {
        return mySensor.getPath().get(currentStateIdx);
    }

    protected double nextSensorUpdate() {
        return mySensor.getPath().get(currentStateIdx).getTime();
    }

}
