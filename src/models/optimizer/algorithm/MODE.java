/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.optimizer.algorithm;

import java.util.ArrayList;
import java.util.Random;
import models.optimizer.CntrlParams;
import models.optimizer.DecisionVar;
import models.optimizer.DecisionVarType;
import models.optimizer.Problem;
import models.optimizer.Solution;
import models.optimizer.operator.assigner.FrontsExtractor;
import models.optimizer.comparator.SolutionDominance;
import models.optimizer.operator.crossover.Binary;
import models.optimizer.operator.crossover.CrossOver;
import models.optimizer.operator.crossover.SinglePoint;
import models.optimizer.operator.retrieve.Elite;
import models.optimizer.operator.retrieve.FirstFront;
import models.optimizer.operator.retrieve.RandomRetrieve;
import models.optimizer.operator.retrieve.Retrieve;
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
    private Retrieve retrieveOperator;
    private CrossOver crossOverOperator;

    // auxiliar data
    private Random rnd;

    /**
     *
     * @param algorithmJSON
     * @param cntrlParams
     */
    public MODE(JSONObject algorithmJSON, CntrlParams cntrlParams) {
        super(algorithmJSON, cntrlParams);
        // read DifferentialEvolution configuration parameters 
        JSONObject selectionJS = (JSONObject) algorithmJSON.get("retrieve");
        switch ((String) selectionJS.get("method")) {
            case "elite":
                int eliteSize = (int) (long) selectionJS.get("size");
                retrieveOperator = new Elite(eliteSize);
                break;
            case "firstFront":
                retrieveOperator = new FirstFront();
                break;
            case "random":
                retrieveOperator = new RandomRetrieve();
                break;
        }
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
     * @param myProblem the current problem state.
     * @return a ns ArrayList of Solution
     */
    @Override
    public ArrayList<Solution> initialize(Problem myProblem) {

        // reset sequence data
        population = new ArrayList<>();
        fronts = new ArrayList<>();
        dominance = new SolutionDominance();
        firstIteration = true;
        rnd = new Random();

        // create a new set of solutions from scratch
        ArrayList<Solution> newSolutions = new ArrayList<>();

        for (int i = 0; i < ns; ++i) {

            Solution iSol = myProblem.newSolution();

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
                        cntrlParams);
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
                        cntrlParams));
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

            // croosover the parents to obtain one child
            ArrayList<Solution> childreen
                    = crossOverOperator.execute(parentA, parentB);

            // finally add the resulting child to the trialVectors
            double randA = rnd.nextDouble();
            if (randA < recombineF) {
                // add first child
                trialVectors.add(childreen.get(0));
            } else {
                // add second child
                trialVectors.add(childreen.get(1));
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
                            } else {
                                if (newValue > decision[d].getMaxValue()) {
                                    newValue = decision[d].getMaxValue();
                                } else if (newValue < decision[d].getMinValue()) {
                                    newValue = decision[d].getMinValue();
                                }
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
                        } else {
                            if (newValue > decision[d].getMaxValue()) {
                                newValue = decision[d].getMaxValue();
                            } else if (newValue < decision[d].getMinValue()) {
                                newValue = decision[d].getMinValue();
                            }
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
            ArrayList<Solution> targetVectors = retrieveOperator.execute(population, 3);

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
