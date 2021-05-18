/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.evaluator.target;

import java.util.logging.Level;
import models.sensor.payloads.Likelihood;
import models.target.TargetState;
import org.ejml.dense.row.CommonOps_DDRM;

/**
 *
 * @author jbbordon
 */
public class StaticTargetControl extends TargetControl {

    public StaticTargetControl(String coupledName, int numUavs) {
        super(coupledName, numUavs);
    }

    @Override
    public void deltint() {
        if (phaseIs("evaluating")) {
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
                clock = endTime;
                super.holdIn("evaluating", clock);
                LOGGER.log(
                        Level.ALL,
                        String.format(
                                "%1$s: TARGET EVALUATION START",
                                this.getName()
                        )
                );                
            }

        } else if (phaseIs("evaluating") || phaseIs("end")) {
            // check u UM in ports for new sensor Likelihood updates
            for (int u = 0; u < tcI2.size(); ++u) {
                if (!tcI2.get(u).isEmpty()) {
                    // new sensor belief update has arrived
                    sensorLikelihoods.add((Likelihood) tcI2.get(u).getSingleValue());
                }
            }
            if (sensorLikelihoods.size() > 0) {
                scenarioTime = sensorLikelihoods.get(0).getTime();
                // perform sensorFusion              
                sensorFusion();

                // calculate elapsed time since last capture
                double elapsedTime = scenarioTime - prevTime;
                prevTime = scenarioTime;

                // calculate etd
                double etd = myTarget.getEtd();
                etd += CommonOps_DDRM.elementSum(targetBelief) * elapsedTime;
                myTarget.setEtd(etd);

                // check if current belief should be save it or not
                if (myTarget.isFullPath()) {
                    // this is only done for final solutions in order to save memory
                    currentState
                            = new TargetState(
                                    targetBelief.copy(),
                                    scenarioTime);
                    myTarget.getPath().add(currentState);
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
        if (phaseIs("end")) {
            // add the final state to the path if needed
            if (!myTarget.isFullPath()) {
                //scenarioTime = myTarget.getEndSequenceTime();
                // add the final state
                currentState
                        = new TargetState(
                                targetBelief.copy(),
                                scenarioTime);
                myTarget.getPath().add(currentState);
            }
            // target time has ended so output evaluated target                    
            tcO1.addValue(myTarget);
        }
    }

}
