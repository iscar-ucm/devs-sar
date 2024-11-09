/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.optimizer.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import models.optimizer.DecisionVar;
import models.optimizer.DecisionVarType;
import models.optimizer.Objectives;
import models.optimizer.Problem;
import models.optimizer.Solution;
import models.optimizer.operator.migration.Diversity;
import models.optimizer.operator.migration.FirstFront;
import models.optimizer.operator.migration.RandomRetrieve;
import models.optimizer.operator.migration.Migrate;
import models.sensor.Sensor;
import models.sensor.SensorCntrlSignals;
import models.sensor.motionModels.DinamicModel;
import models.target.Target;
import models.uav.Uav;
import models.uav.UavCntrlSignals;
import org.json.simple.JSONObject;

/**
 *
 * @author jbbordon
 */
public abstract class Algorithm {

    private static final Logger LOGGER = Logger.getLogger(Algorithm.class.getName());

    // General algorithm characteristics
    protected AlgorithmType type;
    protected Objectives objectives;
    protected int ns; // number of solutions to be generated per iteration
    protected int nf; // number of final solutions to be output
    private double sequenceTime;    
    protected String sortingMethod;      
    protected boolean firstIteration;

    // migration method
    protected int ni; // number of iterations to execute the migration process     
    protected Migrate migrationOperator;

    /**
     *
     * @param algorithmJSON
     */
    public Algorithm(JSONObject algorithmJSON) {
        type = AlgorithmType.valueOf((String) algorithmJSON.get("type"));
        ns = (int) (long) algorithmJSON.get("ns");
        nf = (int) (long) algorithmJSON.get("nf");
        // read sequenceTime
        sequenceTime = (double) algorithmJSON.get("sequenceTime");  
        // read optimization objetives
        objectives = new Objectives((JSONObject) algorithmJSON.get("objectives"));
        // read sorting method
        sortingMethod = (String) algorithmJSON.get("sortingMethod");        
        // read exchange solution method
        JSONObject migrationJS = (JSONObject) algorithmJSON.get("migration");
        MigrationType exchangeType
                = MigrationType.valueOf((String) migrationJS.get("type"));
        int nx;
        switch (exchangeType) {
            case firstFront:
                ni = (int) (long) migrationJS.get("ni");             
                migrationOperator = new FirstFront();
                break;
            case diversity:  
                ni = (int) (long) migrationJS.get("ni");                
                nx = (int) (long) migrationJS.get("nx");
                migrationOperator = new Diversity(nx, sortingMethod);
                break;
            case random:
                ni = (int) (long) migrationJS.get("ni");                
                nx = (int) (long) migrationJS.get("nx");                
                migrationOperator = new RandomRetrieve(nx);
                break;
            case none:
                ni = 0;
                break;
        }

        firstIteration = true;
    }

    /**
     * @return the type
     */
    public AlgorithmType getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(AlgorithmType type) {
        this.type = type;
    }

    /**
     * @return the ns
     */
    public int getNs() {
        return ns;
    }

    /**
     * @param ns the ns to set
     */
    public void setNs(int ns) {
        this.ns = ns;
    }

    /**
     * @return the nf
     */
    public int getNf() {
        return nf;
    }

    /**
     * @param nf the nf to set
     */
    public void setNf(int nf) {
        this.nf = nf;
    }

    /**
     * @return the sequenceTime
     */
    public double getSequenceTime() {
        return sequenceTime;
    }

    /**
     * @param sequenceTime the sequenceTime to set
     */
    public void setSequenceTime(double sequenceTime) {
        this.sequenceTime = sequenceTime;
    }

    /**
     * @return the objectives
     */
    public Objectives getObjectives() {
        return objectives;
    }

    /**
     * @param objectives the objectives to set
     */
    public void setObjectives(Objectives objectives) {
        this.objectives = objectives;
    }

    /**
     * @return the sortingMethod
     */
    public String getSortingMethod() {
        return sortingMethod;
    }

    /**
     * @param sortingMethod the sortingMethod to set
     */
    public void setSortingMethod(String sortingMethod) {
        this.sortingMethod = sortingMethod;
    }

    /**
     * @return the firstIteration
     */
    public boolean isFirstIteration() {
        return firstIteration;
    }

    /**
     * @param firstIteration the firstIteration to set
     */
    public void setFirstIteration(boolean firstIteration) {
        this.firstIteration = firstIteration;
    }    

    /**
     * @return the ni
     */
    public int getNi() {
        return ni;
    }

    /**
     * @param ni the ni to set
     */
    public void setNi(int ni) {
        this.ni = ni;
    }
    
    /**
     * @return the migrationOperator
     */
    public Migrate getMigrationOperator() {
        return migrationOperator;
    }

    /**
     * @param migrationOperator the migrationOperator to set
     */
    public void setMigrationOperator(Migrate migrationOperator) {
        this.migrationOperator = migrationOperator;
    }

    /**
     * This method retrieves the current set of solutions for the actual
     * iteration.
     *
     * @return the first front solution set.
     */
    public abstract ArrayList<Solution> getSolutions();

    /**
     * This method retrieves the first front set of solutions for the actual
     * iteration.
     *
     * @return the first front solution set.
     */
    public abstract ArrayList<Solution> getFirstFront();

    /**
     * This method generates the first set of random solutions. The method it's
     * abstract and should be overrided by the specific algorithm.
     *
     * @param problems the current problem(s) state(s).
     *
     * @return a new set of solutions to be evaluated.
     */
    public abstract ArrayList<Solution> initialize(ArrayList<Problem> problems);

    /**
     * This method updates the algorithm with the results of the evaluation
     * process. The method it's abstract and should be overrided by the specific
     * algorithm.
     *
     * @param evaluatedUavs the current set of uavs evaluated.
     * @param evaluatedTgts the current set of targets evaluated.
     */
    public abstract void evaluate(
            ArrayList<ArrayList<Uav>> evaluatedUavs,
            ArrayList<ArrayList<Target>> evaluatedTgts);

    /**
     * This method iterates the algorithm and returns a new set of solutions.
     * The method it's abstract and should be overrided by the specific
     * algorithm.
     *
     * @return the new set of solutions to be evaluated.
     */
    public abstract ArrayList<Solution> iterate();

    /**
     * This method receives a set of intermediate solutions from another
     * algorithm and incorporate them into the iterative process.
     *
     * @param solutions
     */
    public abstract void tradeIn(ArrayList<Solution> solutions);

    /**
     * This method retrives a set of intermediate solutions to exchange them
     * with another algorithm.
     *
     * @return the solutions to exchange.
     */
    public ArrayList<Solution> tradeOut() {
        ArrayList<Solution> currentSet, outSolutions;
        currentSet = getSolutions();
        outSolutions = getMigrationOperator().execute(currentSet);
        return outSolutions;
    }

    /**
     * This method generates for the given uav the UavCntrlSignals arraylist in
     * a cyclic random way.
     *
     * @param uav the uav to generate the cntrlSignals arraylist.
     */
    protected void genCRUactions(Uav uav) {

        // data used for generating the cntrl signalas
        DecisionVar[] decision = uav.getMotionModel().getDecisionArray();
        double[] cntrlValues = new double[decision.length];
        double controlAt = uav.getControlAt();
        double initTime = uav.getInitState().getTime();
        double endTime = uav.getSeqEndTime();
        // set init time according last control time
        if (!uav.getPrevCntrlSignals().isEmpty()) {
            double prevCntrlTime
                    = uav.getPrevCntrlSignals().get(
                            uav.getPrevCntrlSignals().size() - 1).getTime();
            if ((prevCntrlTime + controlAt) > initTime) {
                initTime
                        = prevCntrlTime + controlAt;
            }
        }
        double currentTime = initTime;

        // loop uav flight time generating uav cntrl actions
        while (currentTime < endTime) {

            for (int i = 0; i < decision.length; ++i) {
                // check if the variable should be optimized or not
                if (decision[i].getType() != DecisionVarType.noaction) {
                    cntrlValues[i]
                            = (Math.random() * decision[i].getRange())
                            + decision[i].getMinValue();
                } else {
                    cntrlValues[i] = 0.0;
                }
            }

            // new cntrl actions are generated each uav at period
            UavCntrlSignals cntrlSignals = new UavCntrlSignals(
                    cntrlValues[0],
                    cntrlValues[1],
                    cntrlValues[2],
                    currentTime
            );

            // add the cntrlSignals to the uav
            uav.getCntrlSignals().add(cntrlSignals);
            currentTime += controlAt;
        }
    }

    /**
     * This method generates for the given uav the UavCntrlSignals arraylist in
     * an acyclic random way.
     *
     * @param uav the uav to generate the cntrlSignals arraylist.
     */
    protected void genARUactions(Uav uav) {

        // data used for generating the cntrl signalas
        DecisionVar[] decision = uav.getMotionModel().getDecisionArray();
        double[] cntrlValues = new double[decision.length];
        double controlAt = uav.getControlAt();
        double initTime = uav.getInitState().getTime();
        double endTime = uav.getSeqEndTime();
        // set init time according last control time
        if (!uav.getPrevCntrlSignals().isEmpty()) {
            double prevCntrlTime
                    = uav.getPrevCntrlSignals().get(
                            uav.getPrevCntrlSignals().size() - 1).getTime();
            if ((prevCntrlTime + controlAt) > initTime) {
                initTime
                        = prevCntrlTime + controlAt;
            }
        }
        double seqTime = endTime - initTime;
        int maxActions = (int) Math.floor((seqTime / controlAt));

        // generate a random number of control actions
        int cntrlActions = (int) Math.ceil(Math.random() * maxActions);

        // generate a control action at a random time
        for (int i = 0; i < cntrlActions; ++i) {

            for (int d = 0; d < decision.length; ++d) {
                // check if the variable should be optimized or not
                if (decision[d].getType() != DecisionVarType.noaction) {
                    cntrlValues[d]
                            = (Math.random() * decision[d].getRange())
                            + decision[d].getMinValue();
                } else {
                    cntrlValues[d] = 0.0;
                }
            }

            // generate the new control action at a random time
            double remTime = (seqTime - 0.5) - (cntrlActions - 1) * controlAt;
            double randCntrlTime
                    = (Math.random() * remTime) + initTime;
            // make sure randCntrlTime is in 0.5 steps
            randCntrlTime = Math.round(randCntrlTime * 2) / 2.0;

            UavCntrlSignals cntrlSignals = new UavCntrlSignals(
                    cntrlValues[0],
                    cntrlValues[1],
                    cntrlValues[2],
                    randCntrlTime
            );

            // add the cntrlSignals to the uav
            uav.getCntrlSignals().add(cntrlSignals);
        }

        // sort the list by time
        Collections.sort(uav.getCntrlSignals(), (UavCntrlSignals o1, UavCntrlSignals o2) -> {
            if (o1.getTime() == o2.getTime()) {
                return 0;
            }
            return o1.getTime() < o2.getTime() ? -1 : 1;
        });

        // loop the generated list and make sure each cntrlSignal it's spaced at least controlAt
        for (int i = 1; i < uav.getCntrlSignals().size(); ++i) {
            double cntrlTime = uav.getCntrlSignals().get(i).getTime();
            double newTime = cntrlTime + controlAt * i;
            uav.getCntrlSignals().get(i).setTime(newTime);
        }
    }

    /**
     * This method generates for the given sensor the SensorCntrlSignals
     * arraylist in a random way.
     *
     * @param sensor the sensor to generate the cntrlSignals arraylist.
     */
    protected void genCRSactions(Sensor sensor) {

        // data used for generating the cntrl signalas
        DinamicModel sensorMModel = (DinamicModel) sensor.getMotionModel();
        DecisionVar[] decision = sensorMModel.getDecisionArray();
        double[] cntrlValues = new double[decision.length];
        double controlAt = sensor.getControlAt();
        double initTime = sensor.getInitState().getTime();
        // set init time according last control time
        if (!sensor.getPrevCntrlSignals().isEmpty()) {
            double prevCntrlTime
                    = sensor.getPrevCntrlSignals().get(
                            sensor.getPrevCntrlSignals().size() - 1).getTime();
            if ((prevCntrlTime + controlAt) > initTime) {
                initTime
                        = prevCntrlTime + controlAt;
            }
        }
        double endTime = sensor.getSeqEndTime();
        double currentTime = initTime;

        // loop uav flight time generating uav cntrl actions
        while (currentTime < endTime) {

            for (int i = 0; i < decision.length; ++i) {
                // check if the variable should be optimized or not
                if (decision[i].getType() != DecisionVarType.noaction) {
                    cntrlValues[i]
                            = (Math.random() * decision[i].getRange())
                            + decision[i].getMinValue();
                } else {
                    cntrlValues[i] = 0.0;
                }
            }

            // new cntrl actions are generated each uav at period
            SensorCntrlSignals cntrlSignals = new SensorCntrlSignals(
                    cntrlValues[0],
                    cntrlValues[1],
                    currentTime
            );

            // add the cntrlSignals to the uav
            sensor.getCntrlSignals().add(cntrlSignals);
            currentTime += controlAt;
        }
    }

    /**
     * This method generates for the given uav the SensorCntrlSignals arraylist
     * in an acyclic random way.
     *
     * @param sensor the sensor to generate the cntrlSignals arraylist.
     */
    protected void genARSactions(Sensor sensor) {

        // data used for generating the cntrl signalas
        DinamicModel sensorMModel = (DinamicModel) sensor.getMotionModel();
        DecisionVar[] decision = sensorMModel.getDecisionArray();
        double[] cntrlValues = new double[decision.length];
        double controlAt = sensor.getControlAt();
        double initTime = sensor.getInitState().getTime();
        // set init time according last control time
        if (!sensor.getPrevCntrlSignals().isEmpty()) {
            double prevCntrlTime
                    = sensor.getPrevCntrlSignals().get(
                            sensor.getPrevCntrlSignals().size() - 1).getTime();
            if ((prevCntrlTime + controlAt) > initTime) {
                initTime
                        = prevCntrlTime + controlAt;
            }
        }
        double endTime = sensor.getSeqEndTime();
        double seqTime = endTime - initTime;
        int maxActions = (int) Math.floor((seqTime / controlAt));

        // generate a random number of control actions
        int cntrlActions = (int) Math.ceil(Math.random() * maxActions);

        // generate a control action at a random time
        for (int i = 0; i < cntrlActions; ++i) {

            for (int d = 0; d < decision.length; ++d) {
                // check if the variable should be optimized or not
                if (decision[d].getType() != DecisionVarType.noaction) {
                    cntrlValues[d]
                            = (Math.random() * decision[d].getRange())
                            + decision[d].getMinValue();
                } else {
                    cntrlValues[d] = 0.0;
                }
            }

            // generate the new control action at a random time
            double remTime = (seqTime - 0.5) - (cntrlActions - 1) * controlAt;
            double randCntrlTime
                    = (Math.random() * remTime) + initTime;
            // make sure randCntrlTime is in 0.5 steps
            randCntrlTime = Math.round(randCntrlTime * 2) / 2.0;

            SensorCntrlSignals cntrlSignals = new SensorCntrlSignals(
                    cntrlValues[0],
                    cntrlValues[1],
                    randCntrlTime
            );

            // add the cntrlSignals to the sensor
            sensor.getCntrlSignals().add(cntrlSignals);
        }

        // sort the list by time
        Collections.sort(sensor.getCntrlSignals(), (SensorCntrlSignals o1, SensorCntrlSignals o2) -> {
            if (o1.getTime() == o2.getTime()) {
                return 0;
            }
            return o1.getTime() < o2.getTime() ? -1 : 1;
        });

        // loop the generated list and make sure each cntrlSignal it's spaced at least controlAt
        for (int i = 1; i < sensor.getCntrlSignals().size(); ++i) {
            double cntrlTime = sensor.getCntrlSignals().get(i).getTime();
            double newTime = cntrlTime + controlAt * i;
            sensor.getCntrlSignals().get(i).setTime(newTime);
        }
    }

    /**
     * This method logs in the console the best solution reached in the current
     * iteration.
     *
     * @param bestTgtSolution solution reached.
     */
    protected void logBestIteration(ArrayList<Target> bestTgtSolution) {

        // loop each target in the solution
        for (int t = 0; t < bestTgtSolution.size(); ++t) {

            // log ETD
            double bestETD
                    = bestTgtSolution.get(t).getEtd();

            LOGGER.log(Level.FINE,
                    String.format("Tgt%1$s Best ETD: %2$s",
                            t + 1,
                            bestETD
                    )
            );

            // log PD
            double bestDP
                    = bestTgtSolution.get(t).getPd();

            LOGGER.log(Level.FINE,
                    String.format("Tgt%1$s Best DP : %2$s",
                            t + 1,
                            bestDP
                    )
            );

            // log Heurist
            double bestHeurist
                    = bestTgtSolution.get(t).getMyo();

            LOGGER.log(Level.FINE,
                    String.format("Tgt%1$s Best Heurist : %2$s",
                            t + 1,
                            bestHeurist
                    )
            );
        }
    }
}
