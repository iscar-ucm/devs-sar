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
import models.optimizer.Problem;
import models.optimizer.Solution;
import models.optimizer.comparator.ArrayDominance;
import models.optimizer.operator.crossover.SinglePoint;
import models.optimizer.operator.assigner.FrontsExtractor;
import models.optimizer.operator.Mutation;
import models.optimizer.comparator.PropertyComparator;
import models.optimizer.comparator.SolutionDominance;
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
public class SPEA2 extends Algorithm {

    // NSGAII lists      
    private ArrayList<Solution> population;
    private ArrayList<Solution> archive;
    private ArrayList<ArrayList<Solution>> fronts;

    // NSGAII parameters    
    private double crossOverF;
    private double[] mutationF;

    // Operators
    private SolutionDominance dominance;
    private Selection selectionOperator;
    private CrossOver crossOverOperator;
    private final Mutation mutationOperator;

    // NSGAII auxilar data
    private int K;
    private Random rnd;
    private HashSet<Integer> alreadyChosen;    

    /**
     *
     * @param algorithmJSON
     */
    public SPEA2(JSONObject algorithmJSON) {
        super(algorithmJSON);

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
        mutationOperator = new Mutation(mutationF);
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
        currentSet.addAll(archive);
        return archive;
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
        fronts = extractor.execute(archive);
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
        archive = new ArrayList<>();
        fronts = new ArrayList<>();
        dominance = new SolutionDominance();
        K = (int) Math.sqrt(ns + ns);
        rnd = new Random();
        alreadyChosen = new HashSet<>();        
        firstIteration = true;

        // create a new set of solutions from scratch
        ArrayList<Solution> newSolutions = new ArrayList<>();

        for (int i = 0; i < ns; ++i) {

            // select randomly a scenario definition from the problem pool
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

            // set population to the offSpring
            population.clear();
            for (int i = 0; i < ns; i++) {
                Solution child = new Solution(
                        evaluatedUavs.get(i),
                        evaluatedTgts.get(i),
                        objectives);
                population.add(child);
            }

            // create the union between the archive and the offSpring
            ArrayList<Solution> union = new ArrayList<>();
            union.addAll(population);
            union.addAll(archive);
            assignFitness(union);

            // reduce the archive
            ArrayList<Solution> unionReduced = reduceByFitness(union);
            if (unionReduced.size() < ns) // External archive size
            {
                expand(unionReduced, union, ns - unionReduced.size());
            } else if (unionReduced.size() > ns) {
                unionReduced = reduce(unionReduced, ns);
            }
            archive = unionReduced;

        } else {
            // flag to false and create initial generation
            for (int i = 0; i < ns; i++) {
                archive.add(new Solution(
                        evaluatedUavs.get(i),
                        evaluatedTgts.get(i),
                        objectives));
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

    @Override
    public void tradeIn(ArrayList<Solution> solutions) {
        // create the union between the archive and the tradeIn solutions
        ArrayList<Solution> union = new ArrayList<>();
        union.addAll(archive);
        solutions.forEach(solution -> {
            union.add(solution.clone());
        });
        assignFitness(union);

        // reduce the archive
        ArrayList<Solution> unionReduced = reduceByFitness(union);
        if (unionReduced.size() < ns) // External archive size
        {
            expand(unionReduced, union, ns - unionReduced.size());
        } else if (unionReduced.size() > ns) {
            unionReduced = reduce(unionReduced, ns);
        }
        archive = unionReduced;
    }

    public void assignFitness(ArrayList<Solution> solutions) {
        int i, j, popSize = solutions.size();
        int strength[] = new int[popSize];
        int raw[] = new int[popSize];
        double density[] = new double[popSize];
        double sigma;
        Solution solI;
        int compare;
        double fitness;

        for (i = 0; i < popSize; ++i) {
            strength[i] = 0;
            raw[i] = 0;
            density[i] = 0;
        }

        // Assigns strength
        for (i = 0; i < popSize; ++i) {
            solI = solutions.get(i);
            for (j = 0; j < popSize; ++j) {
                if (i == j) {
                    continue;
                }
                compare = dominance.compare(solI, solutions.get(j));
                if (compare < 0) {
                    strength[i]++;
                }
            }
        }

        // Assigns raw fitness
        for (i = 0; i < popSize; ++i) {
            solI = solutions.get(i);
            for (j = 0; j < popSize; ++j) {
                if (i == j) {
                    continue;
                }
                compare = dominance.compare(solI, solutions.get(j));
                if (compare == 1 || compare == 2) {
                    raw[i] += strength[j];
                }
            }
        }

        // Assigns density
        for (i = 0; i < popSize; ++i) {
            sigma = computeSigma(i, solutions);
            density[i] = 1 / (sigma + 2);
            fitness = raw[i] + density[i];
            solutions.get(i).getProperties().put("fitness", fitness);
        }
    }

    private double computeSigma(int i, ArrayList<Solution> solutions) {
        return computeSigmas(i, solutions).get(K);
    }

    public double euclideanDistance(Solution sol1, Solution sol2) {
        int nObjs = Math.min(sol1.getResults().size(), sol2.getResults().size());

        double sum = 0;
        for (int i = 0; i < nObjs; i++) {
            sum += ((sol1.getResults().get(i) - sol2.getResults().get(i)) * (sol1.getResults().get(i) - sol2.getResults().get(i)));
        }
        return Math.sqrt(sum);
    }

    private ArrayList<Double> computeSigmas(int i, ArrayList<Solution> solutions) {
        int popSize = solutions.size();
        int j;
        double distance;
        ArrayList<Double> distancesToI = new ArrayList<Double>();
        Solution solI = solutions.get(i);
        for (j = 0; j < popSize; j++) {
            distance = euclideanDistance(solI, solutions.get(j));
            distancesToI.add(distance);
        }
        Collections.sort(distancesToI);
        return distancesToI;
    }

    public ArrayList<Solution> reduceByFitness(ArrayList<Solution> pop) {
        ArrayList<Solution> result = new ArrayList<Solution>();
        Solution indI;
        for (int i = 0; i < pop.size(); ++i) {
            indI = pop.get(i);
            if (indI.getProperties().get("fitness").doubleValue() < 1) {
                result.add(indI);
            }
        }
        return result;
    }

    public void expand(ArrayList<Solution> pop, ArrayList<Solution> all, int nElems) {
        int i = 0, count = 0, allSize = all.size();
        Solution indI;
        Collections.sort(all, new PropertyComparator("fitness"));
        for (i = 0; i < allSize; ++i) {
            indI = all.get(i);
            if (indI.getProperties().get("fitness").doubleValue() >= 1) {
                pop.add(indI);
                count++;
                if (count == nElems) {
                    break;
                }
            }
        }
    }

    public ArrayList<Solution> reduce(ArrayList<Solution> pop, int maxSize) {
        int i, min;
        ArrayList<ArrayList<Double>> allSigmas = new ArrayList<ArrayList<Double>>();
        HashSet<Integer> erased = new HashSet<Integer>();
        int toErase = pop.size() - maxSize;
        ArrayDominance comparator = new ArrayDominance();

        for (i = 0; i < pop.size(); i++) {
            allSigmas.add(computeSigmas(i, pop));
        }

        while (erased.size() < toErase) {
            min = 0;
            while (erased.contains(min)) {
                min++;
            }
            for (i = 0; i < pop.size(); i++) {
                if (i == min || erased.contains(i)) {
                    continue;
                }
                if (comparator.compare(allSigmas.get(i), allSigmas.get(min)) == -1) {
                    min = i;
                }
            }
            erased.add(min);
        }

        ArrayList<Solution> result = new ArrayList<>();
        for (i = 0; i < pop.size(); i++) {
            if (!erased.contains(i)) {
                result.add(pop.get(i));
            }
        }

        return result;
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
            selectedMembers.add(selectionOperator.execute(archive));
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
