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
public class SinglePoint extends CrossOver {

    public SinglePoint(double crossOverF) {
        super("singlePoint", crossOverF);
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
     * This method crossover two cyclic uavs and generates the corresponding two
     * childreen. This is achieved by selecting a random crossover point and
     * mixing the signals from their parents.
     *
     * @param parentA uav
     * @param parentB uav
     * @return the two uavCntrlSignals result of the crososver method
     */
    private ArrayList<ArrayList<UavCntrlSignals>> crossOverCU(
            Uav parentA,
            Uav parentB) {

        // create the uavChildSignals
        ArrayList<ArrayList<UavCntrlSignals>> childreen
                = new ArrayList<>();

        // retrieve both parents sensor cntrl signals
        ArrayList<UavCntrlSignals> cntrlA = parentA.getCntrlSignals();
        ArrayList<UavCntrlSignals> cntrlB = parentB.getCntrlSignals();

        // create the two uavChildSignals
        ArrayList<UavCntrlSignals> firstChild = new ArrayList<>();
        ArrayList<UavCntrlSignals> secondChild = new ArrayList<>();

        // select a crossover pointn that shoud be a time between the
        // first cntrl signal start time and the last cntrl signal                    
        double startTime
                = cntrlA.get(0).getTime();
        double endTime
                = cntrlA.get(cntrlA.size() - 1).getTime();
        double range
                = endTime - startTime;
        double crossOverPoint
                = (Math.random() * range) + startTime;

        // loop parents cntrl signals and apply the croosover
        for (int c = 0; c < cntrlA.size(); ++c) {

            //  var to hold the new signals
            UavCntrlSignals newSignals;

            if (cntrlA.get(c).getTime() <= crossOverPoint) {
                // select cntrlActions from A parent for the first child
                newSignals = cntrlA.get(c).clone();
                firstChild.add(newSignals);
                // do the opposite for the second child
                newSignals = cntrlB.get(c).clone();
                secondChild.add(newSignals);

            } else {
                // select cntrlActions from B parent for the first child
                newSignals = cntrlB.get(c).clone();
                firstChild.add(newSignals);
                // do the opposite for the first child
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
     * two childreen. This is achieved by selecting a random crossover point and
     * mixing the signals from their parents.
     *
     * @param parentA uav
     * @param parentB uav
     * @return the two uavCntrlSignals result of the crososver method
     */
    private ArrayList<ArrayList<UavCntrlSignals>> crossOverAU(
            Uav parentA,
            Uav parentB) {

        // create the uavChildSignals
        ArrayList<ArrayList<UavCntrlSignals>> childreen
                = new ArrayList<>();

        // retrieve both parents sensor cntrl signals
        ArrayList<UavCntrlSignals> cntrlA = parentA.getCntrlSignals();
        ArrayList<UavCntrlSignals> cntrlB = parentB.getCntrlSignals();

        // create the two uavChildSignals
        ArrayList<UavCntrlSignals> firstChild = new ArrayList<>();
        ArrayList<UavCntrlSignals> secondChild = new ArrayList<>();

        // selection of startTime
        double startTime;
        double aStartTime
                = cntrlA.get(0).getTime();
        double bStartTime
                = cntrlB.get(0).getTime();
        if (aStartTime < bStartTime) {
            startTime = aStartTime;
        } else {
            startTime = bStartTime;
        }

        // selection of endTime
        double endTime;
        double aEndTime
                = cntrlA.get(cntrlA.size() - 1).getTime();
        double bEndTime
                = cntrlB.get(cntrlB.size() - 1).getTime();
        if (aEndTime < bEndTime) {
            endTime = bEndTime;
        } else {
            endTime = aEndTime;
        }

        // select a crossover point that shoud be a time between the
        // first cntrl signal start time and the last cntrl signal   
        double range
                = endTime - startTime;
        double crossOverPoint
                = (Math.random() * range) + startTime;

        // loop parentA and retrive cntrlSignals for first child
        int index = 0;
        while (index < cntrlA.size()
                && cntrlA.get(index).getTime() <= crossOverPoint) {
            //  var to hold the new signals
            UavCntrlSignals newSignals;
            // select cntrlActions from A parent for the first child
            newSignals = cntrlA.get(index).clone();
            firstChild.add(newSignals);
            index++;
        }

        // loop parentB and retrive cntrlSignals for first child
        index = 0;
        while (index < cntrlB.size()) {
            if (cntrlB.get(index).getTime() > crossOverPoint) {
                //  var to hold the new signals
                UavCntrlSignals newSignals;
                // select cntrlActions from B parent for the first child
                newSignals = cntrlB.get(index).clone();
                firstChild.add(newSignals);
            }
            index++;
        }

        // loop parentB and do the opposite for second child
        index = 0;
        while (index < cntrlB.size()
                && cntrlB.get(index).getTime() <= crossOverPoint) {
            //  var to hold the new signals
            UavCntrlSignals newSignals;
            // select cntrlActions from A parent for the first child
            newSignals = cntrlB.get(index).clone();
            secondChild.add(newSignals);
            index++;
        }

        // loop parentA and retrive cntrlSignals for second child
        index = 0;
        while (index < cntrlA.size()) {
            if (cntrlA.get(index).getTime() > crossOverPoint) {
                //  var to hold the new signals
                UavCntrlSignals newSignals;
                // select cntrlActions from A parent for the first second
                newSignals = cntrlA.get(index).clone();
                secondChild.add(newSignals);
            }
            index++;
        }

        if (firstChild.isEmpty()) {
            // clone parentA cntrlSignals
            for (short i = 0; i < parentA.getCntrlSignals().size(); ++i) {
                firstChild.add(parentA.getCntrlSignals().get(i).clone());
            }
        } else {
            // make sure a cntrlAt gap is ensure in the crossover point
            if (firstChild.size() > 1) {
                // var to hold previous and actual signals
                UavCntrlSignals prevCntrl, iCntrl;
                prevCntrl = firstChild.get(0);
                // var to loop the child                
                index = 1;
                boolean found = false;
                while (index < firstChild.size() && !found) {
                    iCntrl = firstChild.get(index);
                    // check there is a cntrlAt gap between both signals
                    double gap = iCntrl.getTime() - prevCntrl.getTime();
                    if (gap < parentA.getControlAt()) {
                        // delete prev or iCntrl to ensure the gap
                        double aux = Math.random();
                        if (aux < 0.5) {
                            firstChild.remove(index - 1);
                        } else {
                            firstChild.remove(index);
                        }
                        found = true;
                    } else {
                        prevCntrl = iCntrl;
                        index++;
                    }
                }
            }
        }

        if (secondChild.isEmpty()) {
            // clone parentB cntrlSignals
            for (short i = 0; i < parentB.getCntrlSignals().size(); ++i) {
                secondChild.add(parentB.getCntrlSignals().get(i).clone());
            }
        } else {
            // make sure a cntrlAt gap is ensure in the crossover point
            if (secondChild.size() > 1) {
                // var to hold previous and actual signals
                UavCntrlSignals prevCntrl, iCntrl;
                prevCntrl = secondChild.get(0);
                // var to loop the child
                index = 1;
                boolean found = false;
                while (index < secondChild.size() && !found) {
                    iCntrl = secondChild.get(index);
                    // check there is a cntrlAt gap between both signals
                    double gap = iCntrl.getTime() - prevCntrl.getTime();
                    if (gap < parentA.getControlAt()) {
                        // delete prev or iCntrl to ensure the gap
                        double aux = Math.random();
                        if (aux < 0.5) {
                            secondChild.remove(index - 1);
                        } else {
                            secondChild.remove(index);
                        }
                        found = true;
                    } else {
                        prevCntrl = iCntrl;
                        index++;
                    }
                }
            }
        }

        // store uavChildSignals created and return it
        childreen.add(firstChild);
        childreen.add(secondChild);
        return childreen;
    }

    /**
     * This method crossover two cyclic sensors and generates the corresponding
     * two childreen. This is achieved by selecting a random crossover point and
     * mixing the signals from their parents.
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

        // retrieve both parents sensor cntrl signals
        ArrayList<SensorCntrlSignals> cntrlA = parentA.getCntrlSignals();
        ArrayList<SensorCntrlSignals> cntrlB = parentB.getCntrlSignals();

        // create the two uavChildSignals
        ArrayList<SensorCntrlSignals> firstChild = new ArrayList<>();
        ArrayList<SensorCntrlSignals> secondChild = new ArrayList<>();

        // select a crossover pointn that shoud be a time between the
        // first cntrl signal start time and the last cntrl signal                    
        double startTime
                = cntrlA.get(0).getTime();
        double endTime
                = cntrlA.get(cntrlA.size() - 1).getTime();
        double range
                = endTime - startTime;
        double crossOverPoint
                = (Math.random() * range) + startTime;

        // loop parents cntrl signals and apply the croosover
        for (int c = 0; c < cntrlA.size(); ++c) {

            //  var to hold the new signals
            SensorCntrlSignals newSignals;

            if (cntrlA.get(c).getTime() <= crossOverPoint) {
                // select cntrlActions from A parent for the first child
                newSignals = cntrlA.get(c).clone();
                firstChild.add(newSignals);
                // do the opposite for the second child
                newSignals = cntrlB.get(c).clone();
                secondChild.add(newSignals);

            } else {
                // select cntrlActions from B parent for the first child
                newSignals = cntrlB.get(c).clone();
                firstChild.add(newSignals);
                // do the opposite for the first child
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
     * This method crossover two acyclic sensors and generates the corresponding
     * two childreen. This is achieved by selecting a random crossover point and
     * mixing the signals from their parents.
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

        // retrieve both parents sensor cntrl signals
        ArrayList<SensorCntrlSignals> cntrlA = parentA.getCntrlSignals();
        ArrayList<SensorCntrlSignals> cntrlB = parentB.getCntrlSignals();

        // create the two sensorChildSignals
        ArrayList<SensorCntrlSignals> firstChild = new ArrayList<>();
        ArrayList<SensorCntrlSignals> secondChild = new ArrayList<>();

        // selection of startTime
        double startTime;
        double aStartTime
                = cntrlA.get(0).getTime();
        double bStartTime
                = cntrlB.get(0).getTime();
        if (aStartTime < bStartTime) {
            startTime = aStartTime;
        } else {
            startTime = bStartTime;
        }

        // selection of endTime
        double endTime;
        double aEndTime
                = cntrlA.get(cntrlA.size() - 1).getTime();
        double bEndTime
                = cntrlB.get(cntrlB.size() - 1).getTime();
        if (aEndTime < bEndTime) {
            endTime = bEndTime;
        } else {
            endTime = aEndTime;
        }

        // select a crossover point that shoud be a time between the
        // first cntrl signal start time and the last cntrl signal   
        double range
                = endTime - startTime;
        double crossOverPoint
                = (Math.random() * range) + startTime;

        // loop parentA and retrive cntrlSignals for first child
        int index = 0;
        while (index < cntrlA.size()
                && cntrlA.get(index).getTime() <= crossOverPoint) {
            //  var to hold the new signals
            SensorCntrlSignals newSignals;
            // select cntrlActions from A parent for the first child
            newSignals = cntrlA.get(index).clone();
            firstChild.add(newSignals);
            index++;
        }

        // loop parentB and retrive cntrlSignals for first child
        index = 0;
        while (index < cntrlB.size()) {
            if (cntrlB.get(index).getTime() > crossOverPoint) {
                //  var to hold the new signals
                SensorCntrlSignals newSignals;
                // select cntrlActions from B parent for the first child
                newSignals = cntrlB.get(index).clone();
                firstChild.add(newSignals);
            }
            index++;
        }

        // loop parentB and do the opposite for second child
        index = 0;
        while (index < cntrlB.size()
                && cntrlB.get(index).getTime() <= crossOverPoint) {
            //  var to hold the new signals
            SensorCntrlSignals newSignals;
            // select cntrlActions from A parent for the first child
            newSignals = cntrlB.get(index).clone();
            secondChild.add(newSignals);
            index++;
        }

        // loop parentA and retrive cntrlSignals for second child
        index = 0;
        while (index < cntrlA.size()) {
            if (cntrlA.get(index).getTime() > crossOverPoint) {
                //  var to hold the new signals
                SensorCntrlSignals newSignals;
                // select cntrlActions from A parent for the first second
                newSignals = cntrlA.get(index).clone();
                secondChild.add(newSignals);
            }
            index++;
        }

        if (firstChild.isEmpty()) {
            // clone parentA cntrlSignals
            for (short i = 0; i < parentA.getCntrlSignals().size(); ++i) {
                firstChild.add(parentA.getCntrlSignals().get(i).clone());
            }
        } else {
            // make sure a cntrlAt gap is ensure in the crossover point
            if (firstChild.size() > 1) {
                // var to hold previous and actual signals
                SensorCntrlSignals prevCntrl, iCntrl;
                prevCntrl = firstChild.get(0);
                // var to loop the child                
                index = 1;
                boolean found = false;
                while (index < firstChild.size() && !found) {
                    iCntrl = firstChild.get(index);
                    // check there is a cntrlAt gap between both signals
                    double gap = iCntrl.getTime() - prevCntrl.getTime();
                    if (gap < parentA.getControlAt()) {
                        // delete prev or iCntrl to ensure the gap
                        double aux = Math.random();
                        if (aux < 0.5) {
                            firstChild.remove(index - 1);
                        } else {
                            firstChild.remove(index);
                        }
                        found = true;
                    } else {
                        prevCntrl = iCntrl;
                        index++;
                    }
                }
            }
        }

        if (secondChild.isEmpty()) {
            // clone parentB cntrlSignals
            for (short i = 0; i < parentB.getCntrlSignals().size(); ++i) {
                secondChild.add(parentB.getCntrlSignals().get(i).clone());
            }
        } else {
            // make sure a cntrlAt gap is ensure in the crossover point
            if (secondChild.size() > 1) {
                // var to hold previous and actual signals
                SensorCntrlSignals prevCntrl, iCntrl;
                prevCntrl = secondChild.get(0);
                // var to loop the child
                index = 1;
                boolean found = false;
                while (index < secondChild.size() && !found) {
                    iCntrl = secondChild.get(index);
                    // check there is a cntrlAt gap between both signals
                    double gap = iCntrl.getTime() - prevCntrl.getTime();
                    if (gap < parentA.getControlAt()) {
                        // delete prev or iCntrl to ensure the gap
                        double aux = Math.random();
                        if (aux < 0.5) {
                            secondChild.remove(index - 1);
                        } else {
                            secondChild.remove(index);
                        }
                        found = true;
                    } else {
                        prevCntrl = iCntrl;
                        index++;
                    }
                }
            }
        }

        // store sensorChildSignals created and return it
        childreen.add(firstChild);
        childreen.add(secondChild);
        return childreen;
    }

}
