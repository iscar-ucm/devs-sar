/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.optimizer;

/**
 *
 * @author juanbordonruiz
 */
import java.util.ArrayList;
import java.util.Comparator;
import models.sensor.Sensor;
import models.sensor.SensorState;
import models.sensor.motionModels.DinamicModel;
import models.target.Target;
import models.target.TargetState;
import models.uav.Uav;
import models.uav.UavState;

public class Problem {

    private ArrayList<Uav> uavsDef;
    private ArrayList<Target> tgtsDef;
    private int sequenceNum;
    private double sequenceTime;

    public Problem(
            ArrayList<Uav> uavs, ArrayList<Target> tgts,
            int sequenceNum, double sequenceTime) {

        // sequence time
        double endSequenceTime = sequenceNum * sequenceTime;

        // new uavs problem definition
        uavsDef = new ArrayList<>();

        // loop each sol UAV
        uavs.forEach(uav -> {

            // make a copy of the scenario uav
            Uav newUav = uav.copy();

            // get uav initTime and endTime
            double uavStartTime = uav.getFinalState().getTime()
                    + uav.getMotionModel().getAt();
            double uavEndTime = uav.getEndTime();

            // adjust uav seqEndTime if necessary
            if (uavStartTime < endSequenceTime && uavStartTime <= uavEndTime) {
                // set newUav endTime
                if (endSequenceTime > uavEndTime) {
                    // adjust time to not exceed uav endTime
                    newUav.setSeqEndTime(uavEndTime);
                } else {
                    newUav.setSeqEndTime(endSequenceTime);
                }
            } else {
                newUav.setSeqEndTime(endSequenceTime);
            }

            // initial state for newUav should be lastUavState simulated
            newUav.getPath().clear();
            UavState lastUavState = uav.getFinalState();
            newUav.getPath().add(lastUavState.clone());

            // add uav cntrlSignals to the previous list
            uav.getCntrlSignals().forEach(cntrlSignal -> {
                newUav.getPrevCntrlSignals().add(cntrlSignal.clone());
            });

            ArrayList<Sensor> sensorsDef = newUav.getSensors();
            ArrayList<Sensor> solSensors = uav.getSensors();

            // loop each sol Sensor
            for (int s = 0; s < sensorsDef.size(); ++s) {

                // get sensor initTime and endTime
                double sensorStartTime = solSensors.get(s).getFinalState().getTime();
                double sensorEndTime = solSensors.get(s).getEndTime();
                if (!sensorsDef.get(s).isStatic()) {
                    DinamicModel sensorMM = (DinamicModel) sensorsDef.get(s).getMotionModel();
                    sensorStartTime += sensorMM.getAt();
                }

                // adjust sensor seqEndTime if necessary
                if (sensorStartTime < endSequenceTime && sensorStartTime <= sensorEndTime) {
                    // set newSensor endTime
                    if (endSequenceTime > sensorEndTime) {
                        // adjust time to not exceed sensor endTime
                        sensorsDef.get(s).setSeqEndTime(sensorEndTime);

                    } else {
                        sensorsDef.get(s).setSeqEndTime(endSequenceTime);

                    }
                } else {
                    sensorsDef.get(s).setSeqEndTime(endSequenceTime);
                }

                if (!sensorsDef.get(s).isStatic()) {
                    // initial state for newSensor should be lastSensortate simulated
                    sensorsDef.get(s).getPath().clear();
                    SensorState lastSensorState = solSensors.get(s).getFinalState();
                    sensorsDef.get(s).getPath().add(lastSensorState.clone());

                    // add solution cntrlSignals to the previous list
                    for (int c = 0; c < solSensors.get(s).getCntrlSignals().size(); ++c) {
                        sensorsDef.get(s).getPrevCntrlSignals().add(
                                solSensors.get(s).getCntrlSignals().get(c).clone());
                    };

                } else {
                    // static sensors
                    sensorsDef.get(s).getPath().clear();
                    SensorState newState = new SensorState(lastUavState.getTime());
                    sensorsDef.get(s).getPath().add(newState);
                }
            }

            // finally add the newUav to the problem definition
            uavsDef.add(newUav);
        });

        // new tgts problem definition        
        tgtsDef = new ArrayList<>();

        // loop each sol Tgt        
        tgts.forEach(solTgt -> {

            // make a copy of the scenario target
            Target newTarget = solTgt.copy();

            // get target initTime and endTime
            double tgtStartTime = solTgt.getFinalState().getTime();
            double tgtEndTime = solTgt.getEndTime();

            // adjust tgt seqEndTime if necessary
            if (tgtStartTime < endSequenceTime) {

                // set newTarget endTime
                if (endSequenceTime > tgtEndTime) {
                    // adjust time to not exceed target endTime
                    newTarget.setEndSequenceTime(tgtEndTime);

                } else {
                    newTarget.setEndSequenceTime(endSequenceTime);

                }
            }
            // initial state for newTgt should be lastTgtState simulated
            newTarget.getPath().clear();
            TargetState lastTgtState = solTgt.getFinalState();
            newTarget.getPath().add(lastTgtState.clone());

            // finally add the newTarget to the problem definition
            tgtsDef.add(newTarget);
        });

        this.sequenceNum = sequenceNum;
        this.sequenceTime = sequenceTime;
    }

    public Problem(
            Solution sol, int sequenceNum, double sequenceTime) {

        // sequence time
        double endSequenceTime = sequenceNum * sequenceTime;

        // new uavs problem definition
        uavsDef = new ArrayList<>();

        // loop each sol UAV
        sol.getUavs().forEach(solUav -> {

            // make a copy of the solution uav
            Uav newUav = solUav.copy();

            // get uav initTime and endTime
            double uavStartTime = solUav.getFinalState().getTime()
                    + solUav.getMotionModel().getAt();
            double uavEndTime = solUav.getEndTime();

            // adjust uav seqEndTime if necessary
            if (uavStartTime < endSequenceTime && uavStartTime <= uavEndTime) {
                // set newUav endTime
                if (endSequenceTime > uavEndTime) {
                    // adjust time to not exceed uav endTime
                    newUav.setSeqEndTime(uavEndTime);
                } else {
                    newUav.setSeqEndTime(endSequenceTime);
                }
            }

            // initial state for newUav should be lastUavState simulated
            newUav.getPath().clear();
            UavState lastUavState = solUav.getFinalState();
            newUav.getPath().add(lastUavState.clone());

            // add solution cntrlSignals to the previous list
            solUav.getCntrlSignals().forEach(cntrlSignal -> {
                newUav.getPrevCntrlSignals().add(cntrlSignal.clone());
            });

            ArrayList<Sensor> sensorsDef = newUav.getSensors();
            ArrayList<Sensor> solSensors = solUav.getSensors();

            // loop each sol Sensor
            for (int s = 0; s < sensorsDef.size(); ++s) {

                // get sensor initTime and endTime
                double sensorStartTime = solSensors.get(s).getFinalState().getTime();
                double sensorEndTime = solSensors.get(s).getEndTime();
                if (!sensorsDef.get(s).isStatic()) {
                    DinamicModel sensorMM = (DinamicModel) sensorsDef.get(s).getMotionModel();
                    sensorStartTime += sensorMM.getAt();
                }

                // adjust sensor seqEndTime if necessary
                if (sensorStartTime < endSequenceTime && sensorStartTime <= sensorEndTime) {
                    // set newSensor endTime
                    if (endSequenceTime > sensorEndTime) {
                        // adjust time to not exceed sensor endTime
                        sensorsDef.get(s).setSeqEndTime(sensorEndTime);

                    } else {
                        sensorsDef.get(s).setSeqEndTime(endSequenceTime);

                    }
                }

                if (!sensorsDef.get(s).isStatic()) {
                    // initial state for newSensor should be lastSensortate simulated
                    sensorsDef.get(s).getPath().clear();
                    SensorState lastSensorState = solSensors.get(s).getFinalState();
                    sensorsDef.get(s).getPath().add(lastSensorState.clone());

                    // add solution cntrlSignals to the previous list
                    for (int c = 0; c < solSensors.get(s).getCntrlSignals().size(); ++c) {
                        sensorsDef.get(s).getPrevCntrlSignals().add(
                                solSensors.get(s).getCntrlSignals().get(c).clone());
                    }

                } else {
                    // static sensors
                    sensorsDef.get(s).getPath().clear();
                    SensorState newState = new SensorState(lastUavState.getTime());
                    sensorsDef.get(s).getPath().add(newState);
                }
            }

            // finally add the newUav to the problem definition
            uavsDef.add(newUav);
        });

        // new tgts problem definition        
        tgtsDef = new ArrayList<>();

        // loop each sol Tgt        
        sol.getTgts().forEach(solTgt -> {

            // make a copy of the sol target
            Target newTarget = solTgt.copy();

            // get target initTime and endTime
            double tgtStartTime = solTgt.getFinalState().getTime();
            double tgtEndTime = solTgt.getEndTime();

            // adjust tgt seqEndTime if necessary
            if (tgtStartTime < endSequenceTime) {

                // set newTarget endTime
                if (endSequenceTime > tgtEndTime) {
                    // adjust time to not exceed target endTime
                    newTarget.setEndSequenceTime(tgtEndTime);

                } else {
                    newTarget.setEndSequenceTime(endSequenceTime);

                }
            }
            // initial state for newTgt should be lastTgtState simulated
            newTarget.getPath().clear();
            TargetState lastTgtState = solTgt.getFinalState();
            newTarget.getPath().add(lastTgtState.clone());

            // finally add the newTarget to the problem definition
            tgtsDef.add(newTarget);
        });

        this.sequenceNum = sequenceNum;
        this.sequenceTime = sequenceTime;
    }

    /**
     * @return the sequenceNum
     */
    public int getSequenceNum() {
        return sequenceNum;
    }

    /**
     * @return the sequenceTime
     */
    public double getSequenceTime() {
        return sequenceTime;
    }

    /**
     * This method returns an empty solution according to the actual sequence
     * problem definition.
     *
     * @return an emptySolution
     */
    public Solution newSolution() {
        // create the empty Solution
        Solution emptySolution = new Solution(uavsDef, tgtsDef);
        return emptySolution;
    }

    public int compareTo(Problem problem, Comparator<Problem> comparator) {
        return comparator.compare(this, problem);
    }

    @SuppressWarnings("unchecked")
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        return buffer.toString();
    }
}
