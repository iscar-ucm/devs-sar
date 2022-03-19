/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.optimizer.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import models.optimizer.CntrlParams;
import models.optimizer.Problem;
import models.optimizer.Solution;
import models.optimizer.operator.crossover.SinglePoint;
import models.optimizer.operator.assigner.CrowdingDistance;
import models.optimizer.operator.assigner.FrontsExtractor;
import models.optimizer.operator.Mutation;
import models.optimizer.comparator.PropertyComparator;
import models.optimizer.comparator.SolutionDominance;
import models.optimizer.operator.assigner.NicheCount;
import models.optimizer.operator.crossover.Binary;
import models.optimizer.operator.crossover.CrossOver;
import models.optimizer.operator.selection.BinaryTournament;
import models.optimizer.operator.selection.Selection;
import models.optimizer.operator.selection.Tournament;
import models.sensor.Sensor;
import models.sensor.SensorCntrlType;
import models.target.Target;
import models.uav.Uav;
import models.uav.UavCntrlType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author juanbordonruiz
 */
public class NSGA2 extends Algorithm {

    // NSGAII lists      
    private ArrayList<Solution> population;
    private ArrayList<ArrayList<Solution>> fronts;

    // NSGAII parameters    
    private double crossOverF;
    private double[] mutationF;

    // Operators
    private SolutionDominance dominance;
    private Selection selectionOperator;
    private CrossOver crossOverOperator;
    private final Mutation mutationOperator;

    // auxiliar data
    private Random rnd;

    /**
     *
     * @param algorithmJSON
     * @param cntrlParams
     */
    public NSGA2(JSONObject algorithmJSON, CntrlParams cntrlParams) {
        super(algorithmJSON, cntrlParams);

        // read Genetic configuration parameters
        JSONObject selectionJS = (JSONObject) algorithmJSON.get("selection");
        switch ((String) selectionJS.get("method")) {
            case "binaryTournament":
                selectionOperator = new BinaryTournament();
                break;
            case "tournament":
                int tournamentSize = (int) (long) selectionJS.get("size");
                selectionOperator = new Tournament(tournamentSize);
                break;
        }
        JSONObject crossOverJS = (JSONObject) algorithmJSON.get("crossover");
        crossOverF = (double) crossOverJS.get("factor");
        switch ((String) crossOverJS.get("method")) {
            case "binary":
                crossOverOperator = new Binary(crossOverF);
                break;
            case "singlePoint":
                crossOverOperator = new SinglePoint(crossOverF);
                break;
        }
        JSONArray mutationJS = (JSONArray) algorithmJSON.get("mutation");
        mutationF = new double[mutationJS.size()];
        for (int i = 0; i < mutationJS.size(); ++i) {
            mutationF[i] = (double) mutationJS.get(i);
        }
        mutationOperator = new Mutation(mutationF, cntrlParams);
    }

    /**
     * @return the crossOver
     */
    public double getCrossOver() {
        return crossOverF;
    }

    /**
     * @param crossOver the crossOver to set
     */
    public void setCrossOver(double crossOver) {
        this.crossOverF = crossOver;
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
     * sequence.
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
     * /**
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
        rnd = new Random();
        firstIteration = true;

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
     * process. NSGAII updates the algorithm by combining the offspring
     * generation with the previous one.
     *
     * @param evaluatedUavs the offspring uav generation evaluated.
     * @param evaluatedTgts the offspring tgt generation evaluated.
     */
    @Override
    public void evaluate(
            ArrayList<ArrayList<Uav>> evaluatedUavs,
            ArrayList<ArrayList<Target>> evaluatedTgts) {

        if (!isFirstIteration()) {
            // Create the union of previous and offSpringPop generations
            ArrayList<Solution> mixedPop = new ArrayList<>();
            mixedPop.addAll(population);
            for (int i = 0; i < ns; i++) {
                Solution child = new Solution(
                        evaluatedUavs.get(i),
                        evaluatedTgts.get(i),
                        cntrlParams);
                mixedPop.add(child);
            }
            // reduce the union
            population = reduce(mixedPop);

        } else {
            // flag to false and create initial generation
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
     * MOGA iterates by doing 3 steps: selection, crossover and mutation.
     *
     * @return the new generation to be evaluated.
     */
    @Override
    public ArrayList<Solution> iterate() {

        // selection phase
        ArrayList<Solution> selectedMembers = selection();

        // crossover phase
        ArrayList<Solution> offSpring = crossOver(selectedMembers);

        // mutation phase
        mutationOperator.execute(offSpring);

        // return
        return offSpring;

    }

    /**
     * This method reduce the union between the offspring & the previous
     * generation.
     *
     */
    private ArrayList<Solution> reduce(ArrayList<Solution> pop) {

        // sort current generation in fronts
        FrontsExtractor extractor = new FrontsExtractor(dominance);
        fronts = extractor.execute(pop);

        // combine all the fronts
        ArrayList<Solution> reducedPop = new ArrayList<>();
        ArrayList<Solution> front;
        int i = 0;
        while (reducedPop.size() < ns && i < fronts.size()) {
            front = fronts.get(i);
            if ((front.size() + reducedPop.size()) > ns) {
                // add members of the current front until max size is reached
                if (cntrlParams.getSortingMethod().indexOf("CROWDING_DISTANCE") == 0) {
                    CrowdingDistance assigner = new CrowdingDistance(front.get(0).getObjectives().size());
                    assigner.execute(front);
                    Collections.sort(front, new PropertyComparator(CrowdingDistance.propertyCrowdingDistance));
                    for (int j = front.size() - 1; reducedPop.size() < ns; --j) {
                        reducedPop.add(front.get(j));
                    }

                } else if (cntrlParams.getSortingMethod().indexOf("NICHE_COUNT") == 0) {
                    NicheCount assigner = new NicheCount(front.get(0).getObjectives().size());
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

    /**
     * This method returns ns members from the current generation by comparing a
     * random pairs. The result of the selecting evaluation is done by the
     * fitness value of each member, in terms of constraints and paretos.
     *
     */
    private ArrayList<Solution> selection() {

        // the current generation selected members to return
        ArrayList<Solution> selectedMembers = new ArrayList<>();

        // select numsol members to be crossed over for creating next generation
        for (int i = 0; i < ns; ++i) {
            selectedMembers.add(selectionOperator.execute(population));
        }
        return selectedMembers;
    }

    private ArrayList<Solution> crossOver(ArrayList<Solution> selectedMembers) {

        // create a new set of solutions from scratch
        ArrayList<Solution> offSpringPop = new ArrayList<>();

        // loop the input solutions
        for (int i = 0; i < selectedMembers.size() / 2; ++i) {

            // select the pair to be crossed over and the resulting uavChildSignals
            Solution parentA = selectedMembers.get(i);
            Solution parentB = selectedMembers.get((selectedMembers.size() - i) - 1);

            // generate a random number and check if the pair should be crossed
            // over or not by comparing to the crossover factor
            double randA = Math.random();

            if (randA <= crossOverF) { // members should be crossed over

                ArrayList<Solution> childreen
                        = crossOverOperator.execute(parentA, parentB);

                // add the childreen to the offSpringPop
                offSpringPop.add(childreen.get(0));
                offSpringPop.add(childreen.get(1));

            } else { // each child should be an exact copy of the corresponding parent    

                // create the childreen
                Solution childA = parentA.copy();
                Solution childB = parentB.copy();

                // clone each parent cntrlSignals
                for (int u = 0; u < parentA.getUavs().size(); ++u) {
                    // clone parentA signals into childA
                    for (int c = 0; c < parentA.getUavs().get(u).getCntrlSignals().size(); ++c) {
                        childA.getUavs().get(u).getCntrlSignals().add(
                                parentA.getUavs().get(u).getCntrlSignals().get(c).clone());
                    }
                    // clone parentB signals into childB                    
                    for (int c = 0; c < parentB.getUavs().get(u).getCntrlSignals().size(); ++c) {
                        childB.getUavs().get(u).getCntrlSignals().add(
                                parentB.getUavs().get(u).getCntrlSignals().get(c).clone());
                    }

                    // do the same for non-static sensors
                    for (int s = 0; s < parentA.getUavs().get(u).getSensors().size(); ++s) {
                        if (!parentA.getUavs().get(u).getSensors().get(s).isStatic()) {
                            // clone parentA signals into childA
                            for (int c = 0; c < parentA.getUavs().get(u).getSensors().get(s).getCntrlSignals().size(); ++c) {
                                childA.getUavs().get(u).getSensors().get(s).getCntrlSignals().add(
                                        parentA.getUavs().get(u).getSensors().get(s).getCntrlSignals().get(c).clone());
                            }
                            // clone parentB signals into childB                    
                            for (int c = 0; c < parentB.getUavs().get(u).getSensors().get(s).getCntrlSignals().size(); ++c) {
                                childB.getUavs().get(u).getSensors().get(s).getCntrlSignals().add(
                                        parentB.getUavs().get(u).getSensors().get(s).getCntrlSignals().get(c).clone());
                            }
                        }
                    }
                }

                // add the childreen to the offSpringPop
                offSpringPop.add(childA);
                offSpringPop.add(childB);
            }
        }
        return offSpringPop;
    }
}
