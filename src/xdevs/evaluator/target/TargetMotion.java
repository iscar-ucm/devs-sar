/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.evaluator.target;

import java.util.logging.Logger;
import xdevs.core.modeling.Atomic;
import xdevs.core.modeling.Port;
import models.target.TargetState;
import models.target.motionModels.MotionModel;
import models.target.motionModels.DinamicModel;

/**
 *
 * @author jbbordon
 */
public class TargetMotion extends Atomic {

    private static final Logger LOGGER = Logger.getLogger(TargetModel.class.getName());

    // in Ports of the model
    public Port<MotionModel> tmI1 = new Port<>("motionModel");
    public Port<TargetState> tmI2 = new Port<>("targetState");
    public Port<Double> tmI3 = new Port<>("endTime");

    // out Ports of the model
    public Port<TargetState> tmO1 = new Port<>("targetState"); // belief state at time t

    // internal data
    protected DinamicModel motionModel;
    protected TargetState currentState;
    protected double clock, endTime;

    public TargetMotion(String coupledName) {
        super(coupledName + " TMM");
        // Ports of the Atomic model
        super.addInPort(tmI1);
        super.addInPort(tmI2);
        super.addInPort(tmI3);
        super.addOutPort(tmO1);
    }

    @Override
    public void initialize() {
        motionModel = null;
        currentState = null;
        endTime = 0.0;
        clock = 0.0;
        super.passivate();
    }

    @Override
    public void exit() {
        motionModel = null;
        currentState = null;
        endTime = 0.0;
        clock = 0.0;
        super.passivate();
    }

    @Override
    public void deltint() {
        if (phaseIs("predicting")) {
            if ((currentState.getTime() + motionModel.getAt()) > endTime) {
                // target time has ended
                exit();
            } else {
                // wait until next prediction time
                clock = motionModel.getAt();
                super.holdIn("predicting", clock);
            }
        }
    }

    @Override
    public void deltext(double e) {
        // model is waiting for input data to start simulation
        if (phaseIs("passive")) {
            if (!tmI1.isEmpty()) {
                motionModel = (DinamicModel) tmI1.getSingleValue();
            }
            if (!tmI2.isEmpty()) {
                currentState = tmI2.getSingleValue();
            }
            if (!tmI3.isEmpty()) {
                endTime = tmI3.getSingleValue();
            }
            // check if all required data has arrived
            if (motionModel != null
                    && currentState != null
                    && endTime != 0.0) {
                // init motion model 
                motionModel.initModel(currentState);
                if ((motionModel.getPredictionTime() + motionModel.getAt()) > endTime) {
                    // target time has ended
                    exit();
                } else {
                    // wait until next prediction time
                    clock = motionModel.getPredictionTime() + motionModel.getAt();
                    super.holdIn("predicting", clock);
                }
            }

        } else if (phaseIs("predicting")) {
            if (!tmI2.isEmpty()) {
                // new target state has arrived
                currentState = tmI2.getSingleValue();
                // update motion model with the new target belief received
                motionModel.setTargetState(currentState);
            }
        }
    }

    @Override
    public void lambda() {
        if (phaseIs("predicting")) {
            // step model to simulate the target new state
            currentState = motionModel.stepModel();
            // output new uav state available
            tmO1.addValue(currentState);
        }
    }
}
