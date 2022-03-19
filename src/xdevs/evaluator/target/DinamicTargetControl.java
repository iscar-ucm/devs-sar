/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.evaluator.target;

import java.util.logging.Level;
import models.target.TargetState;
import models.sensor.payloads.Likelihood;
import org.ejml.dense.row.CommonOps_DDRM;

/**
 *
 * @author jbbordon
 */
public class DinamicTargetControl extends TargetControl {

    @Override
    public void initialize() {
        super.initialize();
    }

    @Override
    public void exit() {
        super.exit();
    }

    public DinamicTargetControl(String coupledName, int numUavs) {
        super(coupledName, numUavs);
    }

    @Override
    public void deltint() {
        if (phaseIs("start")) {
            clock = endTime;
            super.holdIn("evaluating", clock);

        } else if (phaseIs("updateMotionModel")) {
            // check if current belief should be save it or not            
            if (myTarget.isFullPath()) {
                myTarget.getPath().add(currentState);
            }
            // wait remaining time
            super.holdIn("evaluating", clock);

        } else if (phaseIs("evaluating")) {
            LOGGER.log(
                    Level.ALL,
                    String.format(
                            "%1$s: TARGET EVALUATION END",
                            this.getName()
                    )
            );
            super.holdIn("end", 1.0);

        } else if (phaseIs("end")) {
            exit();
        }
    }

    @Override
    public void deltext(double e) {
        if (phaseIs("passive")) {
            // model is waiting for input data to start simulation
            if (!tcI1.isEmpty()) {
                myTarget = tcI1.getSingleValue();
            }
            if (myTarget != null) {
                currentState = myTarget.getInitState();
                targetBelief = currentState.getBelief().copy();
                scenarioTime = myTarget.getInitState().getTime();
                prevTime = scenarioTime;
                endTime = myTarget.getEndSequenceTime();
                missPd = myTarget.getMissPD();
                etd = myTarget.getEtd();
                super.holdIn("start", clock);
                LOGGER.log(
                        Level.ALL,
                        String.format(
                                "%1$s: TARGET EVALUATION START",
                                this.getName()
                        )
                );
            }
        } else if (phaseIs("evaluating") || phaseIs("end")) {
            
            // check TMM port for a belief predictions
            if (!tcI3.isEmpty()) {
                // new motion model belief prediction has arrived
                currentState = tcI3.getSingleValue();
                // update missing PD due to motion model predictions
                missPd += currentState.getMissPd();
                targetBelief = currentState.getBelief().copy();
                scenarioTime = currentState.getTime();
                // calculate elapsed time since last capture
                double elapsedTime = scenarioTime - prevTime;
                prevTime = scenarioTime;
                // update etd
                etd += (CommonOps_DDRM.elementSum(targetBelief) + missPd) * elapsedTime;
                // check if current belief should be save it or not            
                if (myTarget.isFullPath()) {
                    myTarget.getPath().add(currentState);
                }
            }
            
            // check u UM in ports for new sensor Likelihood updates
            for (int u = 0; u < tcI2.size(); ++u) {
                if (!tcI2.get(u).isEmpty()) {
                    // new sensor belief update has arrived
                    sensorLikelihoods.add((Likelihood) tcI2.get(u).getSingleValue());
                }
            }
            if (sensorLikelihoods.size() > 0) {
                clock -= e;
                scenarioTime = sensorLikelihoods.get(0).getTime();
                // perform sensorFusion
                sensorFusion();
                // calculate elapsed time since last capture
                double elapsedTime = scenarioTime - prevTime;
                prevTime = scenarioTime;
                // update etd & dp
                etd += (CommonOps_DDRM.elementSum(targetBelief) + missPd) * elapsedTime;
                dp = 1.0 - CommonOps_DDRM.elementSum(targetBelief);
                currentState = new TargetState(targetBelief.copy(), scenarioTime, dp, etd);
                super.holdIn("updateMotionModel", 0.0);
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
        if (phaseIs("start")) {
            // output the init data to TMM
            tcO2.addValue(myTarget.getMotionModel());
            tcO3.addValue(myTarget.getInitState());
            tcO4.addValue(endTime);
        } else if (phaseIs("updateMotionModel")) {
            // output the new targetState as a result of uav sensors actions
            tcO3.addValue(currentState);
        } else if (phaseIs("end")) {
            // add the final state to the path if needed
            if (!myTarget.isFullPath()) {
                // add the final state
                currentState
                        = new TargetState(
                                targetBelief.copy(),
                                scenarioTime,
                                dp, etd);
                myTarget.getPath().add(currentState);
            } else {
                // make sure etd & dp are updated in the last state just in case
                // this is a prediction state
                if (currentState.isPrediction()) {
                    currentState.setPd(dp);
                    currentState.setEtd(etd);
                }
            }
                
            // target time has ended so output evaluated target                    
            tcO1.addValue(myTarget);
        }
    }

}
