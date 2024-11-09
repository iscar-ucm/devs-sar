/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.optimizer.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import models.optimizer.DecisionVar;
import models.optimizer.DecisionVarType;
import models.optimizer.Problem;
import models.optimizer.Solution;
import models.optimizer.comparator.PropertyComparator;
import models.optimizer.operator.assigner.FrontsExtractor;
import models.optimizer.comparator.SolutionDominance;
import models.optimizer.operator.assigner.CrowdingDistance;
import models.optimizer.operator.assigner.NicheCount;
import models.optimizer.operator.crossover.Binary;
import models.optimizer.operator.crossover.CrossOver;
import models.optimizer.operator.crossover.SinglePoint;
import models.optimizer.operator.migration.RandomRetrieve;
import models.optimizer.operator.migration.Migrate;
import models.sensor.Sensor;
import models.sensor.SensorCntrlSignals;
import models.sensor.SensorCntrlType;
import models.sensor.motionModels.DinamicModel;
import models.target.Target;
import models.uav.Uav;
import models.uav.UavCntrlSignals;
import models.uav.UavCntrlType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author Juan
 */
public class MODE extends Algorithm {

    // DE parameters
    private double[] mutationF;
    private double recombineF;

    // DE lists
    private ArrayList<Solution> population;
    private ArrayList<ArrayList<Solution>> fronts;

    // operators
    private SolutionDominance dominance;
    private Migrate retrieveOperator;
    private CrossOver crossOverOperator;

    // auxiliar data
    private Random rnd;
    private HashSet<Integer> alreadyChosen;    

    /**
     *
     * @param algorithmJSON
     */
    public MODE(JSONObject algorithmJSON) {
        super(algorithmJSON);
        // read DifferentialEvolution configuration parameters 
        retrieveOperator = new RandomRetrieve(3);
        JSONObject recombine = (JSONObject) algorithmJSON.get("recombine");
        recombineF = (double) recombine.get("factor");
        switch ((String) recombine.get("method")) {
            case "binary":
                crossOverOperator = new Binary(recombineF);
                break;
            case "singlePoint":
                crossOverOperator = new SinglePoint(recombineF);
                break;
        }
        JSONArray mutationJS = (JSONArray) algorithmJSON.get("mutation");
        mutationF = new double[mutationJS.size()];
        for (int i = 0; i < mutationJS.size(); ++i) {
            mutationF[i] = (double) mutationJS.get(i);
        }
    }

    /**
     * @return the mutation
     */
    public double[] getMutation() {
        return mutationF;
    }

    /**
     * @param mutation the mutation to set
     */
    public void setMutation(double[] mutation) {
        this.mutationF = mutation;
    }

    /**
     * @return the crossOver
     */
    public double getCrossOver() {
        return recombineF;
    }

    /**
     * @param crossOver the crossOver to set
     */
    public void setCrossOver(double crossOver) {
        this.recombineF = crossOver;
    }

    /**
     * This method retrieves the current set of solutions for the actual
     * iteration.
     *
     * @return the first front solution set.
     */
    @Override
    public ArrayList<Solution> getSolutions() {
        ArrayList<Solution> currentSet = new ArrayList<>();
        currentSet.addAll(population);
        return currentSet;
    }

    /**
     * This method retrieves the first front set of solutions for the actual
     * iteration.
     *
     * @return the first front solution set.
     */
    @Override
    public ArrayList<Solution> getFirstFront() {
        FrontsExtractor extractor = new FrontsExtractor(dominance);
        fronts = extractor.execute(population);
        return fronts.get(0);
    }

    /**
     * This method generates the first set of random solutions. The number of
     * solutions to be generated it's configured by numSol field.
     *
     * @param problems the current problem(s) state(s).
     * @return a ns ArrayList of Solution
     */
    @Override
    public ArrayList<Solution> initialize(ArrayList<Problem> problems) {

        // reset sequence data
        population = new ArrayList<>();
        fronts = new ArrayList<>();
        dominance = new SolutionDominance();
        firstIteration = true;
        rnd = new Random();
        alreadyChosen = new HashSet<>();

        // create a new set of solutions from scratch
        ArrayList<Solution> newSolutions = new ArrayList<>();

        for (int i = 0; i < ns; ++i) {

            // generate a new solution selecting in a random way the problem
            // definition from the actual problem pool
            int problemIdx = rnd.nextInt(problems.size());
            if (alreadyChosen.contains(problemIdx) && alreadyChosen.size() < problems.size()) {
                do {
                    problemIdx = rnd.nextInt(problems.size());
                } while (alreadyChosen.contains(problemIdx));
            }
            alreadyChosen.add(problemIdx);
            Solution iSol = problems.get(problemIdx).newSolution();

            // for each uav in the solution
            for (int u = 0; u < iSol.getUavs().size(); ++u) {

                // generate random uav cntrl actions
                if (iSol.getUavs().get(u).getControlType()
                        == UavCntrlType.cyclic) {
                    genCRUactions(iSol.getUavs().get(u));
                } else {
                    genARUactions(iSol.getUavs().get(u));
                }
                // retrieve iu uav sensor array
                ArrayList<Sensor> iuSensors
                        = iSol.getUavs().get(u).getSensors();

                // for each sensor in the uav
                for (int s = 0; s < iuSensors.size(); ++s) {
                    if (!iuSensors.get(s).isStatic()) {
                        // generate random sensor cntrl actions
                        if (iuSensors.get(s).getControlType()
                                == SensorCntrlType.cyclic) {
                            genCRSactions(iuSensors.get(s));
                        } else {
                            genARSactions(iuSensors.get(s));
                        }
                    }
                }
            }
            // add iSol to the whole set
            newSolutions.add(iSol);
        }
        return newSolutions;
    }

    /**
     * This method updates the algorithm with the results of the evaluation
     * process. MODE updates the algorithm by selecting the best members of the
     * current trial vectors and the previous population.
     *
     * @param evaluatedUavs the current uavs trialUavs evaluated.
     * @param evaluatedTgts the current targets trialUavs evaluated.
     */
    @Override
    public void evaluate(
            ArrayList<ArrayList<Uav>> evaluatedUavs,
            ArrayList<ArrayList<Target>> evaluatedTgts) {

        if (!isFirstIteration()) {
            // selection phase
            for (int i = 0; i < ns; i++) {
                Solution iTrialVector = new Solution(
                        evaluatedUavs.get(i),
                        evaluatedTgts.get(i),
                        objectives);
                int flag = dominance.compare(iTrialVector, population.get(i));
                if (flag < 0) // the new particle is better than the older one
                {
                    population.set(i, iTrialVector);
                }
            }

        } else {
            // flag to false and create initial lists
            for (int i = 0; i < ns; i++) {
                population.add(new Solution(
                        evaluatedUavs.get(i),
                        evaluatedTgts.get(i),
                        objectives));
            }
            firstIteration = false;
        }
    }

    /**
     * This method iterates the algorithm and returns a new set of solutions.
     * MODE iterates by doing 2 steps: mutation and recombination.
     *
     * @return a new set of trialVectors to be evaluated.
     */
    @Override
    public ArrayList<Solution> iterate() {

        // mutation phase
        ArrayList<Solution> noisyVectors = mutation();

        // recombination phase
        ArrayList<Solution> trialVectors = recombine(noisyVectors);

        // return
        return trialVectors;
    }

    private ArrayList<Solution> reduceTradeIn(ArrayList<Solution> pop) {

        // sort current generation in fronts
        FrontsExtractor extractor = new FrontsExtractor(dominance);
        ArrayList<ArrayList<Solution>> fronts = extractor.execute(pop);

        // combine all the fronts
        ArrayList<Solution> reducedPop = new ArrayList<>();
        ArrayList<Solution> front;
        int i = 0;
        while (reducedPop.size() < ns && i < fronts.size()) {
            front = fronts.get(i);
            if ((front.size() + reducedPop.size()) > ns) {
                // add members of the current front until max size is reached
                if (sortingMethod.startsWith("CROWDING_DISTANCE")) {
                    CrowdingDistance assigner = new CrowdingDistance(front.get(0).getResults().size());
                    assigner.execute(front);
                    Collections.sort(front, new PropertyComparator(CrowdingDistance.propertyCrowdingDistance));
                    for (int j = front.size() - 1; reducedPop.size() < ns; --j) {
                        reducedPop.add(front.get(j));
                    }

                } else if (sortingMethod.startsWith("NICHE_COUNT")) {
                    NicheCount assigner = new NicheCount(front.get(0).getResults().size());
                    assigner.execute(front);
                    Collections.sort(front, new PropertyComparator(NicheCount.propertyNicheCount));
                    for (int j = 0; reducedPop.size() < ns; ++j) {
                        reducedPop.add(front.get(j));
                    }
                }

            } else {
                // add all the members of the current front
                reducedPop.addAll(front);
            }
            i++;
        }
        return reducedPop;
    }    
    
    @Override
    public void tradeIn(ArrayList<Solution> solutions) {
        
        // reduce the tradeInSolutions to fit to local ns
        ArrayList<Solution> reducedTrade = new ArrayList<>();
        if (solutions.size() < ns) {
            reducedTrade = solutions;
        } else {
            reducedTrade = reduceTradeIn(solutions);
        }
        
        // selection phase
        for (int i = 0; i < reducedTrade.size(); i++) {
            int flag = dominance.compare(reducedTrade.get(i), population.get(i));
            if (flag < 0) // the new particle is better than the older one
            {
                population.set(i, reducedTrade.get(i));
            }
        }
    }

    /**
     *
     */
    private ArrayList<Solution> recombine(
            ArrayList<Solution> noisyVectors) {

        // create a new generation from scratch
        ArrayList<Solution> trialVectors = new ArrayList<>();

        // loop the selected solutions
        for (int i = 0; i < ns; ++i) {

            // retrieve the parents
            Solution parentA = noisyVectors.get(i);
            Solution parentB = population.get(i);

            // recomine condition
            double randA = rnd.nextDouble();
            if (randA < recombineF) {

                // croosover the parents to obtain two child
                ArrayList<Solution> childreen
                        = crossOverOperator.execute(parentA, parentB);

                // random selection of one child
                randA = rnd.nextDouble();
                if (randA < 0.5) {
                    // add first child
                    trialVectors.add(childreen.get(0));
                } else {
                    // add second child
                    trialVectors.add(childreen.get(1));
                }

            } else {
                // add the noisy vector as it is
                trialVectors.add(noisyVectors.get(i));
            }
        }
        return trialVectors;
    }

    /**
     *
     */
    private void mutateSensor(Sensor noisySensor, Sensor xaSensor, Sensor xbSensor, Sensor xcSensor) {

        // data used for mutating the cntrlSignals
        DinamicModel sensorMModel = (DinamicModel) noisySensor.getMotionModel();
        DecisionVar[] decision = sensorMModel.getDecisionArray();
        ArrayList<SensorCntrlSignals> sensorCntrl = new ArrayList<>();
        ArrayList<SensorCntrlSignals> xaCntrl = xaSensor.getCntrlSignals();
        ArrayList<SensorCntrlSignals> xbCntrl = xbSensor.getCntrlSignals();
        ArrayList<SensorCntrlSignals> xcCntrl = xcSensor.getCntrlSignals();

        // loop sensor cntrl signals
        for (int i = 0; i < xaCntrl.size(); ++i) {

            // create the i-Cntrl and set time
            SensorCntrlSignals iSensorCntrl = new SensorCntrlSignals();
            iSensorCntrl.setTime(xaCntrl.get(i).getTime());

            // for each decision variable
            for (int d = 0; d < decision.length; ++d) {

                // check if the variable should be optimized or not
                if (decision[d].getType() != DecisionVarType.noaction) {

                    double newValue, mutationValue;
                    switch (decision[d].getName()) {
                        case elevation:
                            mutationValue
                                    = (Math.random() * mutationF[0]) - mutationF[1];
                            newValue
                                    = xcCntrl.get(i).getcElevation()
                                    + (mutationValue * (xaCntrl.get(i).getcElevation() - xbCntrl.get(i).getcElevation()));
                            // make sure cntrl signal is in range
                            if (newValue > decision[d].getMaxValue()) {
                                newValue = decision[d].getMaxValue();
                            } else if (newValue < decision[d].getMinValue()) {
                                newValue = decision[d].getMinValue();
                            }
                            iSensorCntrl.setcElevation(newValue);
                            break;

                        case azimuth:
                            mutationValue
                                    = (Math.random() * mutationF[0]) - mutationF[1];
                            newValue
                                    = xcCntrl.get(i).getcAzimuth()
                                    + (mutationValue * (xaCntrl.get(i).getcAzimuth() - xbCntrl.get(i).getcAzimuth()));
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

                            iSensorCntrl.setcAzimuth(newValue);
                            break;
                    }
                }
            }
            // add the i-Cntrl to the list
            sensorCntrl.add(iSensorCntrl);
        }
        // set the new cntrl to the sensor
        noisySensor.setCntrlSignals(sensorCntrl);
    }

    /**
     *
     */
    private void mutateUav(Uav noisyUav, Uav xaUav, Uav xbUav, Uav xcUav) {

        // data used for mutating the cntrlSignals        
        DecisionVar[] decision = noisyUav.getMotionModel().getDecisionArray();
        ArrayList<UavCntrlSignals> noisyCntrl = new ArrayList<>();
        ArrayList<UavCntrlSignals> xaCntrl = xaUav.getCntrlSignals();
        ArrayList<UavCntrlSignals> xbCntrl = xbUav.getCntrlSignals();
        ArrayList<UavCntrlSignals> xcCntrl = xcUav.getCntrlSignals();

        // loop uav cntrlSignals
        for (int i = 0; i < xaCntrl.size(); ++i) {

            // create the i-Cntrl and set time
            UavCntrlSignals newCntrl = new UavCntrlSignals();
            newCntrl.setTime(xaCntrl.get(i).getTime());

            // for each decision variable
            for (int d = 0; d < decision.length; ++d) {

                double newValue, mutationValue;
                switch (decision[d].getName()) {
                    case elevation:
                        mutationValue
                                = (Math.random() * mutationF[0]) - mutationF[1];
                        newValue
                                = xcCntrl.get(i).getcElevation()
                                + (mutationValue * (xaCntrl.get(i).getcElevation() - xbCntrl.get(i).getcElevation()));
                        // make sure cntrl signal is in range
                        if (newValue > decision[d].getMaxValue()) {
                            newValue = decision[d].getMaxValue();
                        } else if (newValue < decision[d].getMinValue()) {
                            newValue = decision[d].getMinValue();
                        }
                        newCntrl.setcElevation(newValue);
                        break;

                    case speed:
                        mutationValue
                                = (Math.random() * mutationF[0]) - mutationF[1];
                        newValue
                                = xcCntrl.get(i).getcSpeed()
                                + (mutationValue * (xaCntrl.get(i).getcSpeed() - xbCntrl.get(i).getcSpeed()));
                        if (newValue > decision[d].getMaxValue()) {
                            newValue = decision[d].getMaxValue();
                        } else if (newValue < decision[d].getMinValue()) {
                            newValue = decision[d].getMinValue();
                        }
                        newCntrl.setcSpeed(newValue);
                        break;

                    case heading:
                        mutationValue
                                = (Math.random() * mutationF[0]) - mutationF[1];
                        newValue
                                = xcCntrl.get(i).getcHeading()
                                + (mutationValue * (xaCntrl.get(i).getcHeading() - xbCntrl.get(i).getcHeading()));
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

                        newCntrl.setcHeading(newValue);
                        break;
                }
            }
            // add the i-Cntrl to the list
            noisyCntrl.add(newCntrl);
        }
        // set the new cntrl to the Uav
        noisyUav.setCntrlSignals(noisyCntrl);
    }

    /**
     *
     */
    private ArrayList<Solution> mutation() {

        // Create the noisyVectors
        ArrayList<Solution> noisyVectors = new ArrayList<>();

        for (int i = 0; i < ns; ++i) {

            Solution noisyVector = population.get(i).copy();
            ArrayList<Solution> targetVectors = retrieveOperator.execute(population);

            // Target vectors are selected
            ArrayList<Uav> xa = targetVectors.get(0).getUavs();
            ArrayList<Uav> xb = targetVectors.get(1).getUavs();
            ArrayList<Uav> xc = targetVectors.get(2).getUavs();

            // mutate each UAV in the iNoisyVector
            for (int u = 0; u < noisyVector.getUavs().size(); u++) {

                // i-Noisy vector uav creation 
                Uav noisyUav = noisyVector.getUavs().get(u);

                // clone the cntrlSignals
                for (int c = 0; c < population.get(i).getUavs().get(u).getCntrlSignals().size(); ++c) {
                    noisyUav.getCntrlSignals().add(
                            population.get(i).getUavs().get(u).getCntrlSignals().get(c).clone());
                }

                // mutate uav cntrl
                mutateUav(
                        noisyUav,
                        xa.get(u),
                        xb.get(u),
                        xc.get(u));

                // loop uav sensors and mutate if needed
                for (int s = 0; s < noisyUav.getSensors().size(); ++s) {
                    if (!noisyUav.getSensors().get(s).isStatic()) {

                        // clone the cntrlSignals
                        for (int c = 0; c < population.get(i).getUavs().get(u).getSensors().get(s).getCntrlSignals().size(); ++c) {
                            noisyUav.getSensors().get(s).getCntrlSignals().add(
                                    population.get(i).getUavs().get(u).getSensors().get(s).getCntrlSignals().get(c).clone());
                        }

                        // mutate sensor cntrl actions
                        mutateSensor(
                                noisyUav.getSensors().get(s),
                                xa.get(u).getSensors().get(s),
                                xb.get(u).getSensors().get(s),
                                xc.get(u).getSensors().get(s));
                    }
                }
            }
            noisyVectors.add(noisyVector);
        }
        return noisyVectors;
    }
}
