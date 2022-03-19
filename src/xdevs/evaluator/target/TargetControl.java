/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.evaluator.target;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import java.util.ArrayList;
import java.util.logging.Logger;
import models.sensor.payloads.Likelihood;
import models.target.Target;
import models.target.TargetState;
import models.target.motionModels.MotionModel;
import xdevs.core.modeling.Atomic;
import xdevs.core.modeling.Port;

/**
 *
 * @author jbbordon
 */
public abstract class TargetControl extends Atomic {

    protected static final Logger LOGGER = Logger.getLogger(TargetControl.class.getName());

    // in Ports of the model
    public Port<Target> tcI1 = new Port<>("target");
    public ArrayList<Port> tcI2 = new ArrayList<>(); // uavs likelihood at time t
    public Port<TargetState> tcI3 = new Port<>("tgtState"); // belief update from motion model    

    // out Ports of the model
    public Port<Target> tcO1 = new Port<>("evalTarget");
    public Port<MotionModel> tcO2 = new Port<>("motionModel");
    public Port<TargetState> tcO3 = new Port<>("tgtState"); // belief state at time t to the motion model
    public Port<Double> tcO4 = new Port<>("endTime");

    // internal data
    protected Target myTarget;
    protected TargetState currentState;
    protected DMatrixRMaj targetBelief;
    protected ArrayList<Likelihood> sensorLikelihoods;
    protected double clock, prevTime, scenarioTime, endTime, etd, dp, missPd;

    public TargetControl(String coupledName, int numSensors) {
        super(coupledName + " TC");
        // Ports of the Atomic model
        super.addInPort(tcI1);
        // s ports tcI2 (one per uav in the scenario)
        for (int s = 0; s < numSensors; ++s) {
            Port<Likelihood> tcPortI2 = new Port<>("sensorLikelihood" + s);
            tcI2.add(tcPortI2);
            super.addInPort(tcPortI2);
        }
        super.addInPort(tcI3);
        super.addOutPort(tcO1);
        super.addOutPort(tcO2);
        super.addOutPort(tcO3);
        super.addOutPort(tcO4);
    }

    @Override
    public void initialize() {
        myTarget = null;
        currentState = null;
        targetBelief = null;
        sensorLikelihoods = new ArrayList<>();
        clock = 0.0;
        prevTime = 0.0;
        scenarioTime = 0.0;
        endTime = 0.0;
        etd = 0.0;
        dp = 0.0;
        missPd = 0.0;
        super.passivate();
    }

    @Override
    public void exit() {
        myTarget = null;
        currentState = null;
        targetBelief = null;
        sensorLikelihoods = new ArrayList<>();
        clock = 0.0;
        prevTime = 0.0;        
        scenarioTime = 0.0;
        endTime = 0.0;
        etd = 0.0;
        dp = 0.0;
        missPd = 0.0;        
        super.passivate();
    }

    protected void sensorFusion() {
        // loop through available uavs for a likelihood update
        for (int u = 0; u < sensorLikelihoods.size(); ++u) {
            // update targetBelief with the likelihood received
            CommonOps_DDRM.elementMult(targetBelief, sensorLikelihoods.get(u).getMatrix());
        }
        // reset uav likelihood for next iterations
        sensorLikelihoods.clear();
    }

}
