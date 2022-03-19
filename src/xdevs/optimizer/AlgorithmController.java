/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.optimizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import xdevs.core.modeling.Atomic;
import xdevs.core.modeling.Port;
import models.environment.SearchArea;
import models.environment.Nfz;
import models.environment.WindMatrix;
import models.optimizer.CntrlParams;
import models.optimizer.Problem;
import models.optimizer.Solution;
import models.target.Target;
import models.uav.Uav;
import models.planner.Scenario;
import models.optimizer.algorithm.Algorithm;
import models.optimizer.comparator.ParetoSort;
import models.optimizer.comparator.PropertyComparator;
import models.optimizer.operator.assigner.CrowdingDistance;
import models.optimizer.operator.assigner.NicheCount;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import utils.CSVHandler;

/**
 *
 * @author Juan
 */
public class AlgorithmController extends Atomic {

    private static final Logger LOGGER = Logger.getLogger(AlgorithmController.class.getName());

    // in Ports of the model
    public Port<Scenario> acI1 = new Port<>("scenario");
    public ArrayList<ArrayList<Port>> acI2 = new ArrayList<>(); // evaluatedUavs for each solution
    public ArrayList<ArrayList<Port>> acI3 = new ArrayList<>(); // evaluatedTargets for each solution

    // out Ports of the model
    public Port<SearchArea> acO1 = new Port<>("searchArea");
    public Port<Nfz[]> acO2 = new Port<>("nfzs");
    public Port<WindMatrix> acO3 = new Port<>("windsMap");
    public ArrayList<ArrayList<Port>> acO4 = new ArrayList<>(); // one port for each solution proposed
    public ArrayList<ArrayList<Port>> acO5 = new ArrayList<>(); // one port for each solution proposed
    public Port<Solution> acO6 = new Port<>("finalSolutions"); // final set of solutions found after the iteration process

    // atomic models states/phases
    private static final String PASSIVE = "passive";  // not active
    private static final String EVALUATE = "evaluate"; // algorythm iterations & output of uav solutions for evaluation    
    private static final String WAITING = "waiting";  // waiting for uav flight simulations and target evaluations  
    private static final String END = "end";  // reset data and model closure   

    // internal data
    private final int numSol;
    private int membersRcvd, seqIteration, sequence;
    private Scenario myScenario;
    private Problem myProblem;
    private Algorithm myAlgorithm;
    private CntrlParams myCntrlParams;
    private double clock, startTime, currentTime;
    private ArrayList<ArrayList<Uav>> evaluatedUavs;
    private ArrayList<ArrayList<Target>> evaluatedTargets;
    private ArrayList<Solution> newSolutions, firstFront;
    private Solution finalSol;
    private final CSVHandler csvHandler;

    public AlgorithmController(
            String coupledName, int aIndex, JSONObject jsonRoot, CSVHandler csvHandler) {
        super(coupledName + " AC");
        // ports of the atomic model
        super.addInPort(acI1);

        // variables to be used in the model creation process
        JSONArray targetsArray = (JSONArray) jsonRoot.get("targets");
        JSONArray uavsArray = (JSONArray) jsonRoot.get("uavs");
        JSONArray algorithmsArray = (JSONArray) jsonRoot.get("algorithms");
        JSONObject algorithmJS = (JSONObject) algorithmsArray.get(aIndex - 1);
        numSol = (int) (long) algorithmJS.get("ns");

        for (int i = 1; i <= numSol; ++i) {

            // i ports acI2 & acO4 (one per solution proposed in each algorithm iteration)
            ArrayList<Port> iPortI2 = new ArrayList<>();
            ArrayList<Port> iPortO4 = new ArrayList<>();
            for (int u = 1; u <= uavsArray.size(); ++u) {
                Port<Uav> iuPortI2 = new Port<>("ns" + i + "eUav" + u);
                iPortI2.add(iuPortI2);
                super.addInPort(iuPortI2);
                Port<Uav> iuPortO4 = new Port<>("ns" + i + "uav" + u);
                iPortO4.add(iuPortO4);
                super.addOutPort(iuPortO4);
            }
            acI2.add(iPortI2);
            acO4.add(iPortO4);

            // i ports acI3 & acO5 (one per solution proposed in each algorithm iteration)
            ArrayList<Port> iPortI3 = new ArrayList<>();
            ArrayList<Port> iPortO5 = new ArrayList<>();
            for (int t = 1; t <= targetsArray.size(); ++t) {
                Port<Target> iuPortI3 = new Port<>("ns" + i + "eTarget" + t);
                iPortI3.add(iuPortI3);
                super.addInPort(iuPortI3);
                Port<Target> iuPortO5 = new Port<>("ns" + i + "target" + t);
                iPortO5.add(iuPortO5);
                super.addOutPort(iuPortO5);
            }
            acI3.add(iPortI3);
            acO5.add(iPortO5);
        }

        super.addOutPort(acO1);
        super.addOutPort(acO2);
        super.addOutPort(acO3);
        super.addOutPort(acO6);
        this.csvHandler = csvHandler;
    }

    @Override
    public void initialize() {
        myScenario = null;
        myProblem = null;
        myAlgorithm = null;
        myCntrlParams = null;
        evaluatedUavs = new ArrayList<>();
        evaluatedTargets = new ArrayList<>();
        for (int i = 0; i < numSol; ++i) {
            ArrayList<Uav> iUavs = new ArrayList<>();
            evaluatedUavs.add(iUavs);
            ArrayList<Target> iTgts = new ArrayList<>();
            evaluatedTargets.add(iTgts);
        }
        newSolutions = new ArrayList<>();
        firstFront = new ArrayList<>();
        finalSol = null;
        sequence = 1;
        seqIteration = 0;
        membersRcvd = 0;
        clock = 0.0;
        startTime = 0.0;
        currentTime = 0.0;
        super.passivate();
    }

    @Override
    public void exit() {
        myScenario = null;
        myProblem = null;
        myAlgorithm = null;
        myCntrlParams = null;
        evaluatedUavs = new ArrayList<>();
        evaluatedTargets = new ArrayList<>();
        for (int i = 0; i < numSol; ++i) {
            ArrayList<Uav> iUavs = new ArrayList<>();
            evaluatedUavs.add(iUavs);
            ArrayList<Target> iTgts = new ArrayList<>();
            evaluatedTargets.add(iTgts);
        }
        newSolutions = new ArrayList<>();
        firstFront = new ArrayList<>();
        finalSol = null;
        sequence = 1;
        seqIteration = 0;
        membersRcvd = 0;
        clock = 0.0;
        startTime = 0.0;
        currentTime = 0.0;
        super.passivate();
    }

    @Override
    public void deltint() {
        if (phaseIs(EVALUATE)) {
            // reset iteration data
            evaluatedUavs = new ArrayList<>();
            evaluatedTargets = new ArrayList<>();
            for (int i = 0; i < numSol; ++i) {
                evaluatedUavs.add(new ArrayList<>());
                evaluatedTargets.add(new ArrayList<>());
            }
            clock = Double.MAX_VALUE;
            super.holdIn(WAITING, clock);

        } else if (phaseIs(END)) {
            exit();
        }
    }

    @Override
    public void deltext(double d
    ) {

        if (phaseIs(PASSIVE)) {

            // model is waiting for input data to start simulation   
            if (!acI1.isEmpty()) {

                // read initial data
                myScenario = acI1.getSingleValue();
                myAlgorithm = myScenario.getalgorithms().get(0);
                myCntrlParams = myScenario.getParams();
                myProblem = new Problem(
                        myScenario.getUavs(), myScenario.getTargets(),
                        sequence, myCntrlParams.getSequenceTime());

                // start the optimization process 
                startTime = System.nanoTime();
                newSolutions = myAlgorithm.initialize(myProblem);
                super.holdIn(EVALUATE, clock);

                LOGGER.log(Level.INFO,
                        String.format("##### %1$s: Optimization sequence %2$s starts #####",
                                this.getName(),
                                sequence
                        )
                );
            }

        } else if (phaseIs(WAITING)) {

            // check in ports for new evaluations
            for (int i = 0; i < numSol; ++i) {
                for (int u = 0; u < acI2.get(i).size(); ++u) {
                    if (!acI2.get(i).get(u).isEmpty()) {
                        evaluatedUavs.get(i).add(
                                (Uav) acI2.get(i).get(u).getSingleValue());
                    }
                }
                for (int t = 0; t < acI3.get(i).size(); ++t) {
                    if (!acI3.get(i).get(t).isEmpty()) {
                        evaluatedTargets.get(i).add(
                                (Target) acI3.get(i).get(t).getSingleValue());
                    }
                }
                if (evaluatedUavs.get(i).size() > 0
                        && evaluatedTargets.get(i).size() > 0) {
                    membersRcvd++;
                }
            }

            // check if all evaluations have been received
            if (membersRcvd == numSol) {

                // reset flag
                membersRcvd = 0;

                // update the algorithm with the evaluation results
                myAlgorithm.evaluate(evaluatedUavs, evaluatedTargets);

                // retrieve current first front
                firstFront = myAlgorithm.getFirstFront();

                // check if iteration data should be logged or not     
                if (myAlgorithm.getCntrlParams().isLogIterations()
                        && seqIteration != 0) {
                    csvHandler.writeIteration(sequence, seqIteration, firstFront);
                }

                // check end sequence criteria
                if (!endSequence()) {

                    // ************** SEQUENCE ITERATIONS *********************//                   
                    seqIteration++;

                    // iterate the algorithm
                    newSolutions = myAlgorithm.iterate();
                    clock = 0.0;
                    super.holdIn(EVALUATE, clock);
                    LOGGER.log(Level.FINE,
                            String.format("***** %1$s: Iteration%2$s starts             *****",
                                    this.getName(),
                                    seqIteration
                            )
                    );

                } else {

                    // **************** SEQUENCE ENDS *************************//
                    if (!endOptimization()) {
                        sequence++;
                        myProblem = null;
                        // sort the first front & retrieve best solution so far
                        ParetoSort comparator = new ParetoSort();
                        Collections.sort(firstFront, comparator);
                        myProblem
                                = new Problem(firstFront.get(0), sequence, myCntrlParams.getSequenceTime());
                        logSolution(firstFront.get(0).getTgts(), 1);

                        // **************** NEW SEQUENCE STARTS ***************//
                        // init sequence data & iterate again
                        startTime = System.nanoTime();
                        seqIteration = 0;
                        clock = 0.0;
                        newSolutions = myAlgorithm.initialize(myProblem);
                        super.holdIn(EVALUATE, clock);
                        LOGGER.log(Level.INFO,
                                String.format("##### %1$s: Optimization sequence %2$s starts #####",
                                        this.getName(),
                                        sequence
                                )
                        );

                    } else {

                        // ******************** ALGORYTHM ENDS ****************//
                        // sort the first front & retrieve nf solutions to output them
                        if (myCntrlParams.getSortingMethod().indexOf("CROWDING_DISTANCE") == 0) {
                            CrowdingDistance assigner = new CrowdingDistance(firstFront.get(0).getObjectives().size());
                            assigner.execute(firstFront);
                            Collections.sort(firstFront, new PropertyComparator(CrowdingDistance.propertyCrowdingDistance));
                            finalSol = firstFront.get(0);
                            logSolution(firstFront.get(0).getTgts(), 1);

                        } else if (myCntrlParams.getSortingMethod().indexOf("NICHE_COUNT") == 0) {
                            NicheCount assigner = new NicheCount(firstFront.get(0).getObjectives().size());
                            assigner.execute(firstFront);
                            Collections.sort(firstFront, new PropertyComparator(NicheCount.propertyNicheCount));
                            finalSol = firstFront.get(0);
                            logSolution(firstFront.get(0).getTgts(), 1);

                        }
                        clock = 0.0;
                        super.holdIn(END, clock);
                    }
                }
            }
        }
    }

    @Override
    public void lambda() {
        if (phaseIs(EVALUATE)) {
            // output iteration data to the FS and EV models             
            acO1.addValue(myScenario.getSearchArea());
            acO2.addValue(myScenario.getNfzs());
            acO3.addValue(myScenario.getWindMatrix());
            // init tgt data for the current sequence
            for (int i = 0; i < numSol; ++i) {
                for (int u = 0; u < newSolutions.get(i).getUavs().size(); ++u) {
                    acO4.get(i).get(u).addValue(newSolutions.get(i).getUavs().get(u));
                }
                for (int t = 0; t < newSolutions.get(i).getTgts().size(); ++t) {
                    acO5.get(i).get(t).addValue(newSolutions.get(i).getTgts().get(t));
                }
            }

        } else if (phaseIs(END)) {
            acO6.addValue(finalSol);
        }
    }

    private boolean checkIterations() {
        return seqIteration >= myCntrlParams.getStopIteration();
    }

    private boolean checkComputeTime() {
        currentTime = System.nanoTime() - startTime;
        double aux = (myCntrlParams.getStopTime() * 1000000000.0) - currentTime;
        return aux < 0.0;
    }

    private boolean checkDp() {
        // de momento solo suponemos un target
        boolean detected = false;
        int i = 0;
        while (!detected && i < firstFront.size()) {
            detected
                    = firstFront.get(i).getTgts().get(0).getDp()
                    >= myCntrlParams.getStopPD();
            ++i;
        }
        return detected;
    }

    private boolean endSequence() {
        return checkIterations() || checkComputeTime() || checkDp();
    }

    private boolean endOptimization() {
        boolean newSequence = false;
        // check if end DP criteria has been reached or not
        if (!checkDp()) {
            // check if there are more sequence to optimize
            if (firstFront.get(0).getTgts().get(0).getEndTime()
                    > sequence * myCntrlParams.getSequenceTime()) {
                // there is at least one sequence missing
                newSequence = true;
            }
        }
        return !newSequence;
    }

    private void logSolution(ArrayList<Target> tgtSolutions, int i) {

        LOGGER.log(Level.INFO,
                String.format("---- Solution %1$s                            ----",
                        i
                )
        );

        tgtSolutions.forEach(tgtSolution -> {
            // log heurist
            double bestHeurist
                    = tgtSolution.getHeuristic();

            LOGGER.log(Level.INFO,
                    String.format("%1$s: Tgt Best Heurist : %2$s",
                            this.getName(),
                            bestHeurist
                    )
            );

            // log ETD
            double bestETD
                    = tgtSolution.getEtd();

            LOGGER.log(Level.INFO,
                    String.format("%1$s: Tgt Best ETD     : %2$s",
                            this.getName(),
                            bestETD
                    )
            );

            // log PD
            double bestDP
                    = tgtSolution.getDp();

            LOGGER.log(Level.INFO,
                    String.format("%1$s: Tgt Best DP      : %2$s",
                            this.getName(),
                            bestDP
                    )
            );
        });
    }
}
