/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.spec2.flightSimulator.uav.sensor;

import xdevs.core.modeling.Atomic;
import xdevs.core.modeling.Port;
import models.sensor.motionModels.MotionModel;
import models.sensor.motionModels.DinamicModel;
import models.sensor.SensorState;
import models.sensor.SensorCntrlSignals;

/**
 *
 * @author jbbordon
 */
public class SensorMotion extends Atomic {

    // in Ports of the model
    public Port<MotionModel> smI1 = new Port<>("motionModel");
    public Port<SensorState> smI2 = new Port<>("initState");
    public Port<Double> smI3 = new Port<>("endTime");
    public Port<SensorCntrlSignals> smI4 = new Port<>("currentCntrlSignals");

    // out Ports of the model
    public Port<SensorState> smO1 = new Port<>("sensorState");

    // internal data
    protected DinamicModel motionModel;
    protected SensorState currentState;
    protected SensorCntrlSignals cntrlSignals;
    protected double clock, endTime;

    public SensorMotion(String coupledName, int index) {
        super(coupledName + " SMM" + index);
        // Ports of the Atomic model
        super.addInPort(smI1);
        super.addInPort(smI2);
        super.addInPort(smI3);
        super.addInPort(smI4);
        super.addOutPort(smO1);
    }

    @Override
    public void initialize() {
        motionModel = null;
        currentState = null;
        cntrlSignals = null;
        clock = 0.0;
        endTime = 0.0;
        super.passivate();
    }

    @Override
    public void exit() {
        motionModel = null;
        currentState = null;
        cntrlSignals = null;
        clock = 0.0;
        endTime = 0.0;
        super.passivate();
    }

    @Override
    public void deltint() {
        if (phaseIs("moving")) {
            if (currentState.getTime() + motionModel.getAt() > endTime) {
                // flight time has ended
                exit();
            } else {
                // wait until next motion model step
                clock = motionModel.getAt();
                super.holdIn("moving", clock);
            }
        }
    }

    @Override
    public void deltext(double e) {
        // model is waiting for input data to start simulation
        if (phaseIs("passive")) {
            if (!smI1.isEmpty()) {
                motionModel = (DinamicModel) smI1.getSingleValue();
            }
            if (!smI2.isEmpty()) {
                currentState = smI2.getSingleValue();
            }
            if (!smI3.isEmpty()) {
                endTime = smI3.getSingleValue();
            }
            // check if all required data has arrived
            if (motionModel != null
                    && currentState != null
                    && endTime != 0.0) {
                // init model & start simulation
                motionModel.initModel(currentState);
                if (currentState.getTime() + motionModel.getAt() > endTime) {
                    // flight time has ended
                    exit();
                } else {
                    // wait until next motion model step
                    clock = motionModel.getAt();
                    super.holdIn("moving", clock);
                }
            }
        } else if (phaseIs("moving")) {
            if (!smI4.isEmpty()) {
                // new sensor cntrlSignals have arrived
                cntrlSignals = smI4.getSingleValue();
                motionModel.setCntrlSignals(cntrlSignals);
            }
        }
    }

    @Override
    public void lambda() {
        if (phaseIs("moving")) {
            // step model to simulate the sensor new state
            currentState = motionModel.stepModel();
            smO1.addValue(currentState);
        }
    }

}
