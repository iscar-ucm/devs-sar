/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.optimizer.operator.crossover;

import java.util.ArrayList;
import models.optimizer.Solution;
import models.sensor.Sensor;
import models.sensor.SensorCntrlSignals;
import models.sensor.SensorCntrlType;
import models.uav.Uav;
import models.uav.UavCntrlSignals;
import models.uav.UavCntrlType;

/**
 *
 * @author juanbordonruiz
 */
public class Binary extends CrossOver {

    public Binary(double crossOverF) {
        super("binary", crossOverF);
    }

    @Override
    public ArrayList<Solution> execute(Solution parentA, Solution parentB) {

        // create the offSping
        ArrayList<Solution> offSpring = new ArrayList<>();

        // create the childreen
        Solution childA = parentA.copy();
        Solution childB = parentB.copy();

        // for each uav crossover it with the corresponding parent
        for (int u = 0; u < childA.getUavs().size(); ++u) {

            // this is the case of a UAV that either the start time has not started
            // or it has reached its endtime at this point            
            if (!parentA.getUavs().get(u).getCntrlSignals().isEmpty()) {

                Uav aUav = childA.getUavs().get(u);
                Uav bUav = childB.getUavs().get(u);

                ArrayList<ArrayList<UavCntrlSignals>> uavChildSignals;

                if (aUav.getControlType() == UavCntrlType.cyclic) {
                    uavChildSignals
                            = crossOverCU(
                                    parentA.getUavs().get(u),
                                    parentB.getUavs().get(u));
                } else {
                    uavChildSignals
                            = crossOverAU(
                                    parentA.getUavs().get(u),
                                    parentB.getUavs().get(u));
                }

                // add the new uavChildSignals to the new childreen
                aUav.setCntrlSignals(uavChildSignals.get(0));
                bUav.setCntrlSignals(uavChildSignals.get(1));

                // for each sensor crossover it with the corresponding parent
                // if needed
                for (int s = 0; s < aUav.getSensors().size(); ++s) {

                    // static sensors do not have to be crossed over or a sensor
                    // that either the start time has not started or it has reached its endtime at this point   
                    if (!aUav.getSensors().get(s).isStatic()
                            && !parentA.getUavs().get(u).getSensors().get(s).getCntrlSignals().isEmpty()) {

                        ArrayList<ArrayList<SensorCntrlSignals>> sensorChildSignals;

                        if (aUav.getSensors().get(s).getControlType() == SensorCntrlType.cyclic) {
                            sensorChildSignals
                                    = crossOverCS(
                                            parentA.getUavs().get(u).getSensors().get(s),
                                            parentB.getUavs().get(u).getSensors().get(s));
                        } else {
                            sensorChildSignals
                                    = crossOverAS(
                                            parentA.getUavs().get(u).getSensors().get(s),
                                            parentB.getUavs().get(u).getSensors().get(s));
                        }

                        // add the new sensorChildSignals to the new childreen
                        aUav.getSensors().get(s).setCntrlSignals(sensorChildSignals.get(0));
                        bUav.getSensors().get(s).setCntrlSignals(sensorChildSignals.get(1));
                    }
                }
            }
        }
        // finally add the childreen to the offSpring
        offSpring.add(childA);
        offSpring.add(childB);

        return offSpring;
    }

    /**
     * This method crossover two cyclic uavs and generates the corresponding
     * childreen. This is achieved by going through all the parents signals and
     * randomly selecting parentA or parentB signals.
     *
     * @param parentA uav
     * @param parentB uav
     * @return the childreen resulting of the crososver method
     */
    private ArrayList<ArrayList<UavCntrlSignals>> crossOverCU(
            Uav parentA,
            Uav parentB) {

        // create the uavChildSignals
        ArrayList<ArrayList<UavCntrlSignals>> childreen
                = new ArrayList<>();

        // create the childreen
        ArrayList<UavCntrlSignals> firstChild = new ArrayList<>();
        ArrayList<UavCntrlSignals> secondChild = new ArrayList<>();

        // retrieve both parents uav cntrl signals
        ArrayList<UavCntrlSignals> cntrlA = parentA.getCntrlSignals();
        ArrayList<UavCntrlSignals> cntrlB = parentB.getCntrlSignals();

        // loop parents cntrl signals and recombine
        for (int c = 0; c < cntrlA.size(); ++c) {

            //  var to hold the new signals
            UavCntrlSignals newSignals;

            double randA = Math.random();

            if (randA < getCrossOverF()) {
                // select cntrlActions from A parent for the first child
                newSignals = cntrlA.get(c).clone();
                firstChild.add(newSignals);
                // select cntrlActions from B parent for the second child
                newSignals = cntrlB.get(c).clone();
                secondChild.add(newSignals);

            } else {
                // select cntrlActions from B parent for the first child
                newSignals = cntrlB.get(c).clone();
                firstChild.add(newSignals);
                // select cntrlActions from A parent for the second child
                newSignals = cntrlA.get(c).clone();
                secondChild.add(newSignals);
            }
        }
        // store uavChildSignals created and return it
        childreen.add(firstChild);
        childreen.add(secondChild);
        return childreen;
    }

    /**
     * This method crossover two acyclic uavs and generates the corresponding
     * childreen. This is achieved by going through all the parents signals and
     * randomly selecting parentA or parentB signals.
     *
     * @param parentA uav
     * @param parentB uav
     * @return the childreen resulting of the crososver method
     */
    private ArrayList<ArrayList<UavCntrlSignals>> crossOverAU(
            Uav parentA,
            Uav parentB) {

        // create the uavChildSignals
        ArrayList<ArrayList<UavCntrlSignals>> childreen
                = new ArrayList<>();

        // create the childreen
        ArrayList<UavCntrlSignals> firstChild = new ArrayList<>();
        ArrayList<UavCntrlSignals> secondChild = new ArrayList<>();

        // retrieve both parents uav cntrl signals
        ArrayList<UavCntrlSignals> cntrlA = parentA.getCntrlSignals();
        ArrayList<UavCntrlSignals> cntrlB = parentB.getCntrlSignals();

        if (cntrlA.size() <= cntrlB.size()) {
            // loop parents cntrl signals and recombine
            for (int c = 0; c < cntrlA.size(); ++c) {
                // var to hold the new signals
                UavCntrlSignals newSignals;
                double randA = Math.random();

                if (randA < getCrossOverF()) {
                    // select cntrlActions from A parent for the first child
                    newSignals = cntrlA.get(c).clone();
                    firstChild.add(newSignals);
                    // select cntrlActions from B parent for the second child
                    newSignals = cntrlB.get(c).clone();
                    secondChild.add(newSignals);

                } else {
                    // select cntrlActions from B parent for the first child
                    newSignals = cntrlB.get(c).clone();
                    firstChild.add(newSignals);
                    // select cntrlActions from A parent for the second child
                    newSignals = cntrlA.get(c).clone();
                    secondChild.add(newSignals);
                }
            }
            // add the rest of the signals
            for (int c = cntrlA.size(); c < cntrlB.size(); ++c) {
                //  var to hold the new signals
                UavCntrlSignals newSignals;
                // select cntrlActions from B parent
                newSignals = cntrlB.get(c).clone();
                firstChild.add(newSignals);
                secondChild.add(newSignals);
            }

        } else {

            // loop parents cntrl signals and recombine
            for (int c = 0; c < cntrlB.size(); ++c) {
                // var to hold the new signals
                UavCntrlSignals newSignals;
                double randA = Math.random();

                if (randA < getCrossOverF()) {
                    // select cntrlActions from A parent for the first child
                    newSignals = cntrlA.get(c).clone();
                    firstChild.add(newSignals);
                    // select cntrlActions from B parent for the second child
                    newSignals = cntrlB.get(c).clone();
                    secondChild.add(newSignals);

                } else {
                    // select cntrlActions from B parent for the first child
                    newSignals = cntrlB.get(c).clone();
                    firstChild.add(newSignals);
                    // select cntrlActions from A parent for the second child
                    newSignals = cntrlA.get(c).clone();
                    secondChild.add(newSignals);
                }
            }
            // add the rest of the signals
            for (int c = cntrlB.size(); c < cntrlA.size(); ++c) {
                //  var to hold the new signals
                UavCntrlSignals newSignals;
                // select cntrlActions from B parent
                newSignals = cntrlA.get(c).clone();
                firstChild.add(newSignals);
                secondChild.add(newSignals);
            }
        }
        // store uavChildSignals created and return it
        childreen.add(firstChild);
        childreen.add(secondChild);
        return childreen;
    }

    /**
     * This method crossover two cyclic sensors and generates the corresponding
     * childreen. This is achieved by going through all the parents signals and
     * randomly selecting parentA or parentB signals.
     *
     * @param parentA sensor
     * @param parentB sensor
     * @return the SensorCntrlSignals result of the crossover method
     */
    private ArrayList<ArrayList<SensorCntrlSignals>> crossOverCS(
            Sensor parentA,
            Sensor parentB) {

        // create the sensorChildSignals
        ArrayList<ArrayList<SensorCntrlSignals>> childreen
                = new ArrayList<>();

        // create the childreen
        ArrayList<SensorCntrlSignals> firstChild = new ArrayList<>();
        ArrayList<SensorCntrlSignals> secondChild = new ArrayList<>();

        // retrieve both parents sensor cntrl signals
        ArrayList<SensorCntrlSignals> cntrlA = parentA.getCntrlSignals();
        ArrayList<SensorCntrlSignals> cntrlB = parentB.getCntrlSignals();

        // loop parents cntrl signals and recombine
        for (int c = 0; c < cntrlA.size(); ++c) {

            //  var to hold the new signals
            SensorCntrlSignals newSignals;

            double randA = Math.random();

            if (randA < getCrossOverF()) {
                // select cntrlActions from A parent for the first child
                newSignals = cntrlA.get(c).clone();
                firstChild.add(newSignals);
                // select cntrlActions from B parent for the second child
                newSignals = cntrlB.get(c).clone();
                secondChild.add(newSignals);

            } else {
                // select cntrlActions from B parent for the first child
                newSignals = cntrlB.get(c).clone();
                firstChild.add(newSignals);
                // select cntrlActions from A parent for the second child
                newSignals = cntrlA.get(c).clone();
                secondChild.add(newSignals);
            }
        }
        // store sensorChildSignals created and return it
        childreen.add(firstChild);
        childreen.add(secondChild);
        return childreen;
    }

    /**
     * This method crossover two acyclic sensors and generates the corresponding
     * child. This is achieved by going through all the parents signals and
     * randomly selecting parentA or parentB signals.
     *
     * @param parentA sensor
     * @param parentB sensor
     * @return the SensorCntrlSignals result of the crossover method
     */
    private ArrayList<ArrayList<SensorCntrlSignals>> crossOverAS(
            Sensor parentA,
            Sensor parentB) {

        // create the sensorChildSignals
        ArrayList<ArrayList<SensorCntrlSignals>> childreen
                = new ArrayList<>();

        // create the childreen
        ArrayList<SensorCntrlSignals> firstChild = new ArrayList<>();
        ArrayList<SensorCntrlSignals> secondChild = new ArrayList<>();

        // retrieve both parents sensor cntrl signals
        ArrayList<SensorCntrlSignals> cntrlA = parentA.getCntrlSignals();
        ArrayList<SensorCntrlSignals> cntrlB = parentB.getCntrlSignals();

        if (cntrlA.size() <= cntrlB.size()) {
            // loop parents cntrl signals and recombine
            for (int c = 0; c < cntrlA.size(); ++c) {
                // var to hold the new signals
                SensorCntrlSignals newSignals;
                double randA = Math.random();

                if (randA < getCrossOverF()) {
                    // select cntrlActions from A parent for the first child
                    newSignals = cntrlA.get(c).clone();
                    firstChild.add(newSignals);
                    // select cntrlActions from B parent for the second child
                    newSignals = cntrlB.get(c).clone();
                    secondChild.add(newSignals);

                } else {
                    // select cntrlActions from B parent for the first child
                    newSignals = cntrlB.get(c).clone();
                    firstChild.add(newSignals);
                    // select cntrlActions from A parent for the second child
                    newSignals = cntrlA.get(c).clone();
                    secondChild.add(newSignals);
                }
            }
            // add the rest of the signals
            for (int c = cntrlA.size(); c < cntrlB.size(); ++c) {
                //  var to hold the new signals
                SensorCntrlSignals newSignals;
                // select cntrlActions from B parent
                newSignals = cntrlB.get(c).clone();
                firstChild.add(newSignals);
                secondChild.add(newSignals);
            }

        } else {

            // loop parents cntrl signals and recombine
            for (int c = 0; c < cntrlB.size(); ++c) {
                // var to hold the new signals
                SensorCntrlSignals newSignals;
                double randA = Math.random();

                if (randA < getCrossOverF()) {
                    // select cntrlActions from A parent for the first child
                    newSignals = cntrlA.get(c).clone();
                    firstChild.add(newSignals);
                    // select cntrlActions from B parent for the second child
                    newSignals = cntrlB.get(c).clone();
                    secondChild.add(newSignals);

                } else {
                    // select cntrlActions from B parent for the first child
                    newSignals = cntrlB.get(c).clone();
                    firstChild.add(newSignals);
                    // select cntrlActions from A parent for the second child
                    newSignals = cntrlA.get(c).clone();
                    secondChild.add(newSignals);
                }
            }
            // add the rest of the signals
            for (int c = cntrlB.size(); c < cntrlA.size(); ++c) {
                //  var to hold the new signals
                SensorCntrlSignals newSignals;
                // select cntrlActions from B parent
                newSignals = cntrlA.get(c).clone();
                firstChild.add(newSignals);
                secondChild.add(newSignals);
            }
        }
        // store sensorChildSignals created and return it
        childreen.add(firstChild);
        childreen.add(secondChild);
        return childreen;
    }
}
