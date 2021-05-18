/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.flightSimulator.uav;

import xdevs.core.modeling.Atomic;
import xdevs.core.modeling.Port;
import models.uav.motionModels.MotionModel;
import models.uav.UavCntrlSignals;
import models.uav.UavState;
import models.environment.Wind;

/**
 *
 * @author jbbordon
 */
public class UavMotion extends Atomic {

    // in Ports of the model
    public Port<MotionModel> umI1 = new Port<>("motionModel");
    public Port<UavState> umI2 = new Port<>("initState");
    public Port<Double> umI3 = new Port<>("endTime");
    public Port<UavCntrlSignals> umI4 = new Port<>("currentCntrlSignals");
    public Port<Wind> umI5 = new Port<>("wind");

    // out Ports of the model
    public Port<UavState> umO1 = new Port<>("uavState");

    // internal data        
    protected MotionModel motionModel;
    protected UavState currentState;
    protected UavCntrlSignals cntrlSignals;
    protected Wind wind;
    protected double clock, endTime;

    public UavMotion(String coupledName) {
        super(coupledName + " UMM");
        // Ports of the Atomic model
        super.addInPort(umI1);
        super.addInPort(umI2);
        super.addInPort(umI3);
        super.addInPort(umI4);
        super.addInPort(umI5);
        super.addOutPort(umO1);
    }

    @Override
    public void initialize() {
        motionModel = null;
        currentState = null;
        cntrlSignals = null;
        wind = null;
        endTime = 0.0;
        clock = 0.0;
        super.passivate();
    }

    @Override
    public void exit() {
        motionModel = null;
        currentState = null;
        cntrlSignals = null;
        wind = null;
        endTime = 0.0;
        clock = 0.0;
        super.passivate();
    }

    @Override
    public void deltint() {
        if (phaseIs("flying")) {
            if (currentState.getTime() + motionModel.getAt() > endTime) {
                // flight time has ended
                exit();
            } else {
                // wait until next motion model step
                clock = motionModel.getAt();
                super.holdIn("flying", clock);
            }
        }
    }

    @Override
    public void deltext(double e
    ) {
        // model is waiting for input data to start simulation
        if (phaseIs("passive")) {
            if (!umI1.isEmpty()) {
                motionModel = umI1.getSingleValue();
            }
            if (!umI2.isEmpty()) {
                currentState = umI2.getSingleValue();
            }
            if (!umI3.isEmpty()) {
                endTime = umI3.getSingleValue();
            }
            if (!umI5.isEmpty()) {
                wind = umI5.getSingleValue();
            }
            // check if all required data has arrived
            if (motionModel != null
                    && currentState != null
                    && endTime != 0.0
                    && wind != null) {
                // init model & start simulation
                motionModel.initModel(currentState);
                motionModel.setWind(wind);
                if (currentState.getTime() + motionModel.getAt() > endTime) {
                    // flight time has ended
                    exit();
                } else {
                    // wait until next motion model step
                    clock = currentState.getTime() + motionModel.getAt();
                    super.holdIn("flying", clock);
                }
            }
        } else if (phaseIs("flying")) {
            if (!umI4.isEmpty()) {
                // new uavCntrlSignals have arrived
                cntrlSignals = umI4.getSingleValue();
                motionModel.setCntrlSignals(cntrlSignals);
            }
            if (!umI5.isEmpty()) {
                // new wind has arrived
                wind = umI5.getSingleValue();
                motionModel.setWind(wind);
            }
        }
    }
    
    @Override
    public void lambda() {
        if (phaseIs("flying")) {
            // step model to simulate the new uav state
            currentState = motionModel.stepModel();
            umO1.addValue(currentState);
        }
    }

}
