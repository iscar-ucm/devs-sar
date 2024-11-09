/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.optimizer.operator;

import java.util.ArrayList;
import java.util.Random;
import models.optimizer.DecisionVar;
import models.optimizer.DecisionVarType;
import models.optimizer.Solution;
import models.sensor.Sensor;
import models.sensor.SensorCntrlSignals;
import models.sensor.motionModels.DinamicModel;
import models.uav.Uav;
import models.uav.UavCntrlSignals;

/**
 *
 * @author juanbordonruiz
 */
public class Mutation {

    private final double[] mutationF;

    public Mutation(double[] mutationF) {
        this.mutationF = mutationF;
    }

    public void execute(ArrayList<Solution> solutions) {
        // loop solutions
        for (int i = 0; i < solutions.size(); ++i) {
            ArrayList<Uav> newSolution = solutions.get(i).getUavs();
            // loop uavs
            for (int u = 0; u < newSolution.size(); ++u) {
                mutateUav(newSolution.get(u));
            }
        }
    }

    /**
     * This method mutates the UavCntrlSignals arraylist of the input uav and of
     * all the sensors in the uav.
     *
     * @param uav the uav to mutate its control signals
     */
    private void mutateUav(Uav uav) {

        // data used for mutating the cntrl signalas        
        DecisionVar[] decision = uav.getMotionModel().getDecisionArray();
        ArrayList<UavCntrlSignals> uavCntrl = uav.getCntrlSignals();

        // loop uav cntrl signals
        for (int i = 0; i < uavCntrl.size(); ++i) {

            // randon mutation need
            int applyHigherMutation = 0;
            if (Math.random()
                    < (1
                    / ((uav.getFinalState().getTime() - uav.getInitState().getTime())
                    / uavCntrl.size()))) {
                applyHigherMutation = 1;
            }

            // for each decision variable
            for (int d = 0; d < decision.length; ++d) {

                // check if the variable should be optimized or not
                if (decision[d].getType() != DecisionVarType.noaction) {

                    // gaussian random generator
                    Random randGenerator = new Random();

                    // apply lower factor to cntrlSignal
                    double lowerMutation
                            = randGenerator.nextGaussian()
                            * mutationF[1]
                            * decision[d].getRange();

                    // apply highr factor to cntrlSignal if needed
                    double higherMutation
                            = randGenerator.nextGaussian()
                            * mutationF[0]
                            * decision[d].getRange()
                            * applyHigherMutation;

                    double newValue;
                    switch (decision[d].getName()) {
                        case elevation:
                            newValue
                                    = uavCntrl.get(i).getcElevation()
                                    + lowerMutation + higherMutation;
                            // make sure cntrl signal is in range
                            if (newValue > decision[d].getMaxValue()) {
                                newValue = decision[d].getMaxValue();
                            } else if (newValue < decision[d].getMinValue()) {
                                newValue = decision[d].getMinValue();
                            }
                            uavCntrl.get(i).setcElevation(newValue);
                            break;

                        case speed:
                            newValue
                                    = uavCntrl.get(i).getcSpeed()
                                    + lowerMutation + higherMutation;
                            if (newValue > decision[d].getMaxValue()) {
                                newValue = decision[d].getMaxValue();
                            } else if (newValue < decision[d].getMinValue()) {
                                newValue = decision[d].getMinValue();
                            }
                            uavCntrl.get(i).setcSpeed(newValue);
                            break;

                        case heading:
                            newValue
                                    = uavCntrl.get(i).getcHeading()
                                    + lowerMutation + higherMutation;
                            // make sure cntrl signal is in range
                            if (Math.abs(newValue) > 180) {
                                if (newValue > 180) {
                                    newValue -= 360;
                                } else {
                                    newValue += 360;
                                }
                            }
                            if (newValue > decision[d].getMaxValue()) {
                                newValue = decision[d].getMaxValue();
                            } else if (newValue < decision[d].getMinValue()) {
                                newValue = decision[d].getMinValue();
                            }

                            uavCntrl.get(i).setcHeading(newValue);
                            break;
                    }
                }
            }
        }

        // loop uav sensors and mutate if needed
        for (int s = 0; s < uav.getSensors().size(); ++s) {
            if (!uav.getSensors().get(s).isStatic()) {
                // mutate sensor cntrl actions
                mutateSensor(uav.getSensors().get(s));
            }
        }
    }

    /**
     * This method mutates the SensorCntrlSignals arraylist of the input sensor.
     *
     * @param sensor the sensor to mutate its cntrlsignals
     */
    private void mutateSensor(Sensor sensor) {

        // data used for mutating the cntrl signalas
        DinamicModel sensorMModel = (DinamicModel) sensor.getMotionModel();
        DecisionVar[] decision = sensorMModel.getDecisionArray();
        ArrayList<SensorCntrlSignals> sensorCntrl = sensor.getCntrlSignals();

        // loop sensor cntrl signals
        for (int i = 0; i < sensorCntrl.size(); ++i) {

            // randon mutation need
            int applyHigherMutation = 0;
            if (Math.random()
                    < (1
                    / ((sensor.getFinalState().getTime() - sensor.getInitState().getTime())
                    / sensorCntrl.size()))) {
                applyHigherMutation = 1;
            }

            // for each decision variable
            for (int d = 0; d < decision.length; ++d) {

                // check if the variable should be optimized or not
                if (decision[d].getType() != DecisionVarType.noaction) {

                    // gaussian random generator
                    Random randGenerator = new Random();

                    // apply lower factor to cntrlSignal
                    double lowerMutation;
                    lowerMutation
                            = randGenerator.nextGaussian()
                            * mutationF[1]
                            * decision[d].getRange();

                    // apply highr factor to cntrlSignal if needed
                    double higherMutation
                            = randGenerator.nextGaussian()
                            * mutationF[0]
                            * decision[d].getRange()
                            * applyHigherMutation;

                    double newValue;
                    switch (decision[d].getName()) {
                        case elevation:
                            newValue
                                    = sensorCntrl.get(i).getcElevation()
                                    + lowerMutation + higherMutation;
                            // make sure cntrl signal is in range
                            if (newValue > decision[d].getMaxValue()) {
                                newValue = decision[d].getMaxValue();
                            } else if (newValue < decision[d].getMinValue()) {
                                newValue = decision[d].getMinValue();
                            }
                            sensorCntrl.get(i).setcElevation(newValue);
                            break;

                        case azimuth:
                            newValue
                                    = sensorCntrl.get(i).getcAzimuth()
                                    + lowerMutation + higherMutation;
                            // make sure cntrl signal is in range
                            if (Math.abs(newValue) > 180) {
                                if (newValue > 180) {
                                    newValue -= 360;
                                } else {
                                    newValue += 360;
                                }
                            }
                            if (newValue > decision[d].getMaxValue()) {
                                newValue = decision[d].getMaxValue();
                            } else if (newValue < decision[d].getMinValue()) {
                                newValue = decision[d].getMinValue();
                            }

                            sensorCntrl.get(i).setcAzimuth(newValue);
                            break;
                    }
                }
            }
        }
    }
}
