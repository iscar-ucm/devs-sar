/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.optimizer.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.logging.Logger;
import models.optimizer.CntrlParams;
import models.optimizer.DecisionVar;
import models.optimizer.DecisionVarType;
import models.optimizer.Problem;
import models.optimizer.Solution;
import models.optimizer.operator.assigner.CrowdingDistance;
import models.optimizer.operator.assigner.FrontsExtractor;
import models.optimizer.operator.Mutation;
import models.optimizer.comparator.PropertyComparator;
import models.optimizer.comparator.SolutionDominance;
import static models.optimizer.operator.assigner.FrontsExtractor.propertyRank;
import models.optimizer.operator.assigner.NicheCount;
import models.sensor.Sensor;
import models.sensor.SensorCntrlSignals;
import models.sensor.motionModels.DinamicModel;
import models.target.Target;
import models.uav.Uav;
import models.uav.UavCntrlSignals;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author juanbordonruiz
 */
public class OMOPSO extends Algorithm {

    private static final Logger LOGGER = Logger.getLogger(OMOPSO.class.getName());

    // PSO parameters
    private double c1;
    private double c2;
    private double chi;
    private double w;
    private double topPartPercentage;
    private double[] mutationF;

    // PSO lists
    private ArrayList<Solution> swarm;
    private ArrayList<Solution> leaders;
    private ArrayList<Solution> personalBests;

    // Operators
    private SolutionDominance dominance;
    private final Mutation mutationOperator;

    // PSO particle speeds    
    private ArrayList<ArrayList<ArrayList<UavCntrlSignals>>> uavSpeeds;
    private ArrayList<ArrayList<ArrayList<ArrayList<SensorCntrlSignals>>>> sensorSpeeds;

    // PSO auxilar data
    private Random rnd;

    public OMOPSO(JSONObject algorithmJSON, CntrlParams cntrlParams) {
        super(algorithmJSON, cntrlParams);

        // read Particle Swarm configuration parameters
        c1 = (double) algorithmJSON.get("c1");
        c2 = (double) algorithmJSON.get("c2");
        chi = (double) algorithmJSON.get("chi");
        w = (double) algorithmJSON.get("w");
        topPartPercentage = (double) algorithmJSON.get("topPartPercentage");
        JSONArray mutationJS = (JSONArray) algorithmJSON.get("mutation");
        mutationF = new double[mutationJS.size()];
        for (int i = 0; i < mutationJS.size(); ++i) {
            mutationF[i] = (double) mutationJS.get(i);
        }
        mutationOperator = new Mutation(mutationF, cntrlParams);
    }

    /**
     * @return the c1
     */
    public double getC1() {
        return c1;
    }

    /**
     * @param c1 the c1 to set
     */
    public void setC1(double c1) {
        this.c1 = c1;
    }

    /**
     * @return the c2
     */
    public double getC2() {
        return c2;
    }

    /**
     * @param c2 the c2 to set
     */
    public void setC2(double c2) {
        this.c2 = c2;
    }

    /**
     * @return the chi
     */
    public double getChi() {
        return chi;
    }

    /**
     * @param chi the chi to set
     */
    public void setChi(double chi) {
        this.chi = chi;
    }

    /**
     * @return the w
     */
    public double getW() {
        return w;
    }

    /**
     * @param w the w to set
     */
    public void setW(double w) {
        this.w = w;
    }

    /**
     * @return the topPartPercentage
     */
    public double getTopPartPercentage() {
        return topPartPercentage;
    }

    /**
     * @param topPartPercentage the topPartPercentage to set
     */
    public void setTopPartPercentage(double topPartPercentage) {
        this.topPartPercentage = topPartPercentage;
    }

    /**
     * @return the mutationF
     */
    public double[] getMutationF() {
        return mutationF;
    }

    /**
     * @param mutationF the mutationF to set
     */
    public void setMutationF(double[] mutationF) {
        this.mutationF = mutationF;
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
        currentSet.addAll(personalBests);
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
        ArrayList<Solution> firstFront = new ArrayList<>();
        firstFront.addAll(leaders);
        return firstFront;
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
        swarm = new ArrayList<>();
        leaders = new ArrayList<>();
        dominance = new SolutionDominance();
        personalBests = new ArrayList<>();
        uavSpeeds = new ArrayList<>();
        sensorSpeeds = new ArrayList<>();
        rnd = new Random();
        firstIteration = true;

        // create a new set of solutions from scratch
        ArrayList<Solution> newSwarm = new ArrayList<>();

        for (int i = 0; i < ns; ++i) {

            Solution iSol = myProblem.newSolution();

            // for each uav in the solution
            for (int u = 0; u < iSol.getUavs().size(); ++u) {

                // generate random uav cntrl actions
                genCRUactions(iSol.getUavs().get(u));

                // retrieve iu uav sensor array
                ArrayList<Sensor> iuSensors
                        = iSol.getUavs().get(u).getSensors();

                // for each sensor in the uav
                for (int s = 0; s < iuSensors.size(); ++s) {

                    if (!iuSensors.get(s).isStatic()) {
                        // generate random sensor cntrl actions
                        genCRSactions(iuSensors.get(s));
                    }
                }
            }
            // add iSol to the whole set
            newSwarm.add(iSol);
        }

        // init speeds for the newSwarm
        initUavSpeed(newSwarm);
        initSensorSpeed(newSwarm);

        return newSwarm;
    }

    @Override
    public void evaluate(
            ArrayList<ArrayList<Uav>> evaluatedUavs,
            ArrayList<ArrayList<Target>> evaluatedTgts) {

        if (!isFirstIteration()) {

            // Update personal bests
            for (int i = 0; i < ns; i++) {
                Solution particle = new Solution(
                        evaluatedUavs.get(i),
                        evaluatedTgts.get(i),
                        cntrlParams);
                swarm.add(particle);
                int flag = dominance.compare(particle, personalBests.get(i));
                if (flag < 0) // the new particle is better than the older one
                {
                    personalBests.set(i, particle.clone());
                }
            }

            // Add particles to the external archive
            for (int i = 0; i < ns; i++) {
                Solution particle = swarm.get(i);
                leaders.add(particle.clone());
            }
            reduceExternalArchive();

        } else {
            // flag to false and create initial lists
            for (int i = 0; i < ns; i++) {
                Solution newParticle = new Solution(
                        evaluatedUavs.get(i),
                        evaluatedTgts.get(i),
                        cntrlParams);
                swarm.add(newParticle);
                leaders.add(newParticle.clone());
                personalBests.add(newParticle.clone());
            }
            firstIteration = false;
        }

    }

    @Override
    public ArrayList<Solution> iterate() {
        // compute speeds
        computeSpeed();
        // compute new positions
        ArrayList<Solution> newSwarm = computeNewPositions();
        // mutate new particles
        mutationOperator.execute(newSwarm);
        // reset swarn & return
        swarm.clear();
        return newSwarm;
    }

    private void reduceExternalArchive() {

        // reduce the external archive to the first front set of solutions
        FrontsExtractor extractor = new FrontsExtractor(dominance);
        extractor.reduceToNonDominated(leaders);
        for (int i = 0; i < leaders.size(); ++i) {
            leaders.get(i).getProperties().put(propertyRank, 1);
        }

        // apply sorting method to the first front
        if (cntrlParams.getSortingMethod().indexOf("CROWDING_DISTANCE") == 0) {
            CrowdingDistance assigner = new CrowdingDistance(leaders.get(0).getObjectives().size());
            assigner.execute(leaders);
            Collections.sort(leaders, new PropertyComparator(CrowdingDistance.propertyCrowdingDistance));

        } else if (cntrlParams.getSortingMethod().indexOf("NICHE_COUNT") == 0) {
            NicheCount assigner = new NicheCount(leaders.get(0).getObjectives().size());
            assigner.execute(leaders);
            Collections.sort(leaders, new PropertyComparator(NicheCount.propertyNicheCount));

        } else {
            LOGGER.severe("Sorting method not propertly defined: " + cntrlParams.getSortingMethod());
        }

        // make sure the external archive does not exceed the required size
        while (leaders.size() > ns) {
            if (cntrlParams.getSortingMethod().indexOf("CROWDING_DISTANCE") == 0) {
                leaders.remove(0);
            } else // NICHE_COUNT or PARETO_SORT
            {
                leaders.remove(leaders.size() - 1);
            }
        }
    }

    private void initSensorSpeed(ArrayList<Solution> newSwarm) {

        // reset sensor speed data
        sensorSpeeds = new ArrayList<>();

        // for each particle
        for (int i = 0; i < ns; ++i) {

            ArrayList<ArrayList<ArrayList<SensorCntrlSignals>>> iSensorSpeeds = new ArrayList<>();
            sensorSpeeds.add(iSensorSpeeds);

            // for each uav in the scenario
            for (int u = 0; u < newSwarm.get(i).getUavs().size(); ++u) {

                ArrayList<ArrayList<SensorCntrlSignals>> iuSensorSpeeds = new ArrayList<>();
                iSensorSpeeds.add(iuSensorSpeeds);

                // retrieve iu uav sensor array
                ArrayList<Sensor> iuSensors
                        = newSwarm.get(i).getUavs().get(u).getSensors();

                // for each sensor in the uav
                for (int s = 0; s < iuSensors.size(); ++s) {

                    if (!iuSensors.get(s).isStatic()) {

                        // init sensor speed
                        ArrayList<SensorCntrlSignals> iusSensorSpeeds = new ArrayList<>();
                        iuSensorSpeeds.add(iusSensorSpeeds);

                        for (int c = 0; c < iuSensors.get(s).getCntrlSignals().size(); c++) {

                            // data used to create the sensor speed
                            SensorCntrlSignals iuscSensorSpeeds = new SensorCntrlSignals();
                            DinamicModel sensorMModel = (DinamicModel) iuSensors.get(s).getMotionModel();
                            DecisionVar[] decision = sensorMModel.getDecisionArray();

                            // for each decision variable
                            for (int d = 0; d < decision.length; ++d) {

                                // check if the variable should be optimized or not
                                if (decision[d].getType() != DecisionVarType.noaction) {

                                    double newValue
                                            = (Math.random() * decision[d].getRange())
                                            + decision[d].getMinValue();

                                    switch (decision[d].getName()) {
                                        case elevation:
                                            iuscSensorSpeeds.setcElevation(newValue);
                                            break;

                                        case azimuth:
                                            iuscSensorSpeeds.setcAzimuth(newValue);
                                            break;
                                    }
                                }
                            }
                            iusSensorSpeeds.add(iuscSensorSpeeds);
                        }
                    }
                }
            }
        }
    } // initSensorSpeed

    private void initUavSpeed(ArrayList<Solution> newSwarm) {

        // reset uav speed data
        uavSpeeds = new ArrayList<>();

        // for each particle
        for (int i = 0; i < ns; ++i) {

            ArrayList<ArrayList<UavCntrlSignals>> iUavSpeeds = new ArrayList<>();
            uavSpeeds.add(iUavSpeeds);

            // for each uav in the scenario
            for (int u = 0; u < newSwarm.get(i).getUavs().size(); ++u) {

                ArrayList<UavCntrlSignals> iuUavSpeeds = new ArrayList<>();
                iUavSpeeds.add(iuUavSpeeds);

                for (int c = 0; c < newSwarm.get(i).getUavs().get(u).getCntrlSignals().size(); c++) {

                    // data used to create the uav speed                    
                    UavCntrlSignals iucUavSpeeds = new UavCntrlSignals();
                    DecisionVar[] decision
                            = newSwarm.get(i).getUavs().get(u).getMotionModel().getDecisionArray();

                    // for each decision variable
                    for (int d = 0; d < decision.length; ++d) {

                        // check if the variable should be optimized or not
                        if (decision[d].getType() != DecisionVarType.noaction) {

                            double newValue
                                    = (Math.random() * decision[d].getRange())
                                    + decision[d].getMinValue();
                            switch (decision[d].getName()) {
                                case elevation:
                                    iucUavSpeeds.setcElevation(newValue);
                                    break;
                                case speed:
                                    iucUavSpeeds.setcSpeed(newValue);
                                    break;
                                case heading:
                                    iucUavSpeeds.setcHeading(newValue);
                                    break;
                            }
                        }
                    }
                    iuUavSpeeds.add(iucUavSpeeds);
                }
            }
        }
    } // initUavSpeed        

    private void computeSensorSpeed(
            int i, int u, int s, Sensor particle, Sensor personalBest, Sensor globalBest) {

        // data used for computing the sensor speed
        DinamicModel sensorMModel = (DinamicModel) particle.getMotionModel();
        DecisionVar[] decision = sensorMModel.getDecisionArray();
        ArrayList<SensorCntrlSignals> particleCntrl = particle.getCntrlSignals();
        ArrayList<SensorCntrlSignals> personalBestCntrl = personalBest.getCntrlSignals();
        ArrayList<SensorCntrlSignals> globalBestCntrl = globalBest.getCntrlSignals();

        // loop sensor cntrl signals
        for (int c = 0; c < particleCntrl.size(); ++c) {

            double r1, r2, C1, C2, W;
            //Parameters for velocity equation
            r1 = rnd.nextDouble();
            r2 = rnd.nextDouble();
            C1 = Math.min(1.5, getC1())
                    + (Math.max(1.5, getC1()) - Math.min(1.5, getC1())) * rnd.nextDouble();
            C2 = Math.min(1.5, getC1())
                    + (Math.max(1.5, getC2()) - Math.min(1.5, getC2())) * rnd.nextDouble();
            W = Math.min(0.1, getW())
                    + (Math.max(0.1, getW()) - Math.min(0.1, getW())) * rnd.nextDouble();

            // for each decision variable
            for (int d = 0; d < decision.length; ++d) {

                // check if the variable should be optimized or not
                if (decision[d].getType() != DecisionVarType.noaction) {

                    double vPart, pBest, gBest, pSpeed;

                    switch (decision[d].getName()) {
                        case elevation:
                            vPart = particleCntrl.get(c).getcElevation();
                            pBest = personalBestCntrl.get(c).getcElevation();
                            gBest = globalBestCntrl.get(c).getcElevation();
                            pSpeed = W * sensorSpeeds.get(i).get(u).get(s).get(c).getcElevation()
                                    + C1 * r1 * (pBest - vPart)
                                    + C2 * r2 * (gBest - vPart);
                            sensorSpeeds.get(i).get(u).get(s).get(c).setcElevation(pSpeed);
                            break;

                        case azimuth:
                            vPart = particleCntrl.get(c).getcAzimuth();
                            pBest = personalBestCntrl.get(c).getcAzimuth();
                            gBest = globalBestCntrl.get(c).getcAzimuth();
                            pSpeed = W * sensorSpeeds.get(i).get(u).get(s).get(c).getcAzimuth()
                                    + C1 * r1 * (pBest - vPart)
                                    + C2 * r2 * (gBest - vPart);
                            sensorSpeeds.get(i).get(u).get(s).get(c).setcAzimuth(pSpeed);
                            break;
                    }
                }
            }
        }
    }

    private void computeUavSpeed(
            int i, int u, Uav particle, Uav personalBest, Uav globalBest) {

        // data used for computing the new cntrlSignals values     
        DecisionVar[] decision = particle.getMotionModel().getDecisionArray();
        ArrayList<UavCntrlSignals> particleCntrl = particle.getCntrlSignals();
        ArrayList<UavCntrlSignals> personalBestCntrl = personalBest.getCntrlSignals();
        ArrayList<UavCntrlSignals> globalBestCntrl = globalBest.getCntrlSignals();

        // loop uav cntrlSignals
        for (int c = 0; c < particleCntrl.size(); ++c) {

            double r1, r2, C1, C2, W;
            //Parameters for velocity equation
            r1 = rnd.nextDouble();
            r2 = rnd.nextDouble();
            C1 = Math.min(1.5, getC1())
                    + (Math.max(1.5, getC1()) - Math.min(1.5, getC1())) * rnd.nextDouble();
            C2 = Math.min(1.5, getC1())
                    + (Math.max(1.5, getC2()) - Math.min(1.5, getC2())) * rnd.nextDouble();
            W = Math.min(0.1, getW())
                    + (Math.max(0.1, getW()) - Math.min(0.1, getW())) * rnd.nextDouble();

            // for each decision variable
            for (int d = 0; d < decision.length; ++d) {

                // check if the variable should be optimized or not
                if (decision[d].getType() != DecisionVarType.noaction) {

                    double vPart, pBest, gBest, newSpeed;

                    switch (decision[d].getName()) {
                        case elevation:
                            vPart = particleCntrl.get(c).getcElevation();
                            pBest = personalBestCntrl.get(c).getcElevation();
                            gBest = globalBestCntrl.get(c).getcElevation();
                            newSpeed = W * uavSpeeds.get(i).get(u).get(c).getcElevation()
                                    + C1 * r1 * (pBest - vPart)
                                    + C2 * r2 * (gBest - vPart);
                            uavSpeeds.get(i).get(u).get(c).setcElevation(newSpeed);
                            break;

                        case speed:
                            vPart = particleCntrl.get(c).getcSpeed();
                            pBest = personalBestCntrl.get(c).getcSpeed();
                            gBest = globalBestCntrl.get(c).getcSpeed();
                            newSpeed = W * uavSpeeds.get(i).get(u).get(c).getcSpeed()
                                    + C1 * r1 * (pBest - vPart)
                                    + C2 * r2 * (gBest - vPart);
                            uavSpeeds.get(i).get(u).get(c).setcSpeed(newSpeed);
                            break;

                        case heading:
                            vPart = particleCntrl.get(c).getcHeading();
                            pBest = personalBestCntrl.get(c).getcHeading();
                            gBest = globalBestCntrl.get(c).getcHeading();
                            newSpeed = W * uavSpeeds.get(i).get(u).get(c).getcHeading()
                                    + C1 * r1 * (pBest - vPart)
                                    + C2 * r2 * (gBest - vPart);
                            uavSpeeds.get(i).get(u).get(c).setcHeading(newSpeed);
                            break;
                    }
                }
            }
        }

        // loop uav sensors and compute each sensor cntrl speed
        for (int s = 0; s < particle.getSensors().size(); ++s) {
            if (!particle.getSensors().get(s).isStatic()) {
                // mutate sensor cntrl actions
                computeSensorSpeed(
                        i, u, s,
                        particle.getSensors().get(s),
                        personalBest.getSensors().get(s),
                        globalBest.getSensors().get(s));
            }
        }
    }

    private void computeSpeed() {

        CrowdingDistance assigner = new CrowdingDistance(leaders.get(0).getObjectives().size());
        assigner.execute(leaders);

        Solution particle, personalBest, globalBest, one, two;

        for (int i = 0; i < ns; i++) {

            particle = swarm.get(i);
            personalBest = personalBests.get(i);

            //Select a global best_ for calculate the speed of particle i, bestGlobal
            int pos1 = rnd.nextInt(leaders.size());
            int pos2 = rnd.nextInt(leaders.size());
            one = leaders.get(pos1);
            two = leaders.get(pos2);

            PropertyComparator crowdingDistanceComparator
                    = new PropertyComparator(CrowdingDistance.propertyCrowdingDistance);
            if (crowdingDistanceComparator.compare(two, one) < 1) {
                globalBest = one;
            } else {
                globalBest = two;
            }

            // for each particle-uav
            for (int u = 0; u < particle.getUavs().size(); u++) {
                computeUavSpeed(
                        i, u,
                        particle.getUavs().get(u),
                        personalBest.getUavs().get(u),
                        globalBest.getUavs().get(u));
            }

        }

    } // computeSpeed    

    private void computeSensorNewPosition(int i, int u, int s, Sensor sensor) {
        // data used for computing the new cntrlSignals values
        DinamicModel sensorMModel = (DinamicModel) sensor.getMotionModel();
        DecisionVar[] decision = sensorMModel.getDecisionArray();
        ArrayList<SensorCntrlSignals> prevCntrl
                = swarm.get(i).getUavs().get(u).getSensors().get(s).getCntrlSignals();
        ArrayList<SensorCntrlSignals> newCntrl = new ArrayList<>();

        // loop sensor cntrl signals
        for (int c = 0; c < prevCntrl.size(); ++c) {
            // content will be overriden
            SensorCntrlSignals cntrlSignal = prevCntrl.get(c).clone();

            // for each decision variable
            for (int d = 0; d < decision.length; ++d) {

                // check if the variable should be optimized or not
                if (decision[d].getType() != DecisionVarType.noaction) {

                    double newValue;
                    switch (decision[d].getName()) {
                        case elevation:
                            newValue
                                    = prevCntrl.get(c).getcElevation()
                                    + chi * sensorSpeeds.get(i).get(u).get(s).get(c).getcElevation();
                            // make sure cntrl signal is in range
                            if (newValue > decision[d].getMaxValue()) {
                                newValue = decision[d].getMaxValue();
                            } else if (newValue < decision[d].getMinValue()) {
                                newValue = decision[d].getMinValue();
                            }
                            cntrlSignal.setcElevation(newValue);
                            break;

                        case azimuth:
                            newValue
                                    = prevCntrl.get(c).getcAzimuth()
                                    + chi * sensorSpeeds.get(i).get(u).get(s).get(c).getcAzimuth();
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
                            cntrlSignal.setcAzimuth(newValue);
                            break;
                    }
                }
            }
            newCntrl.add(cntrlSignal);
        }
        sensor.setCntrlSignals(newCntrl);
    }

    private void computeUavNewPosition(int i, int u, Uav uav) {
        // data used for computing the new cntrlSignals values     
        DecisionVar[] decision = uav.getMotionModel().getDecisionArray();
        ArrayList<UavCntrlSignals> prevCntrl
                = swarm.get(i).getUavs().get(u).getCntrlSignals();
        ArrayList<UavCntrlSignals> newCntrl = new ArrayList<>();

        // loop uav cntrlSignals
        for (int c = 0; c < prevCntrl.size(); ++c) {
            // content will be overriden
            UavCntrlSignals cntrlSignal = prevCntrl.get(c).clone();

            // for each decision variable
            for (int d = 0; d < decision.length; ++d) {

                double newValue;
                switch (decision[d].getName()) {
                    case elevation:
                        newValue
                                = prevCntrl.get(c).getcElevation()
                                + chi * uavSpeeds.get(i).get(u).get(c).getcElevation();
                        // make sure cntrl signal is in range
                        if (newValue > decision[d].getMaxValue()) {
                            newValue = decision[d].getMaxValue();
                        } else if (newValue < decision[d].getMinValue()) {
                            newValue = decision[d].getMinValue();
                        }
                        cntrlSignal.setcElevation(newValue);
                        break;

                    case speed:
                        newValue
                                = prevCntrl.get(c).getcSpeed()
                                + chi * uavSpeeds.get(i).get(u).get(c).getcSpeed();
                        if (newValue > decision[d].getMaxValue()) {
                            newValue = decision[d].getMaxValue();
                        } else if (newValue < decision[d].getMinValue()) {
                            newValue = decision[d].getMinValue();
                        }
                        cntrlSignal.setcSpeed(newValue);
                        break;

                    case heading:
                        newValue
                                = prevCntrl.get(c).getcHeading()
                                + chi * uavSpeeds.get(i).get(u).get(c).getcHeading();
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

                        cntrlSignal.setcHeading(newValue);
                        break;
                }
            }
            newCntrl.add(cntrlSignal);
        }
        uav.setCntrlSignals(newCntrl);

        // loop uav sensors and mutate if needed
        for (int s = 0; s < uav.getSensors().size(); ++s) {
            if (!uav.getSensors().get(s).isStatic()) {
                // mutate sensor cntrl actions
                computeSensorNewPosition(i, u, s,
                        uav.getSensors().get(s));
            }
        }
    }

    private ArrayList<Solution> computeNewPositions() {

        // Create the newSwarm
        ArrayList<Solution> newSwarm = new ArrayList<>();

        for (int i = 0; i < ns; i++) {
            // Create the i-particle
            Solution newParticle = swarm.get(i).copy();

            // loop each particle-uav
            for (int u = 0; u < newParticle.getUavs().size(); u++) {
                Uav newParticleUav = newParticle.getUavs().get(u);

                // compute the new position
                computeUavNewPosition(i, u, newParticleUav);
            }
            newSwarm.add(newParticle);
        }
        return newSwarm;
    }
}
