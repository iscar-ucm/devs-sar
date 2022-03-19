/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.spec2.evaluator;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import models.environment.Nfz;
import xdevs.core.modeling.Atomic;
import xdevs.core.modeling.Port;
import models.environment.SearchArea;
import models.optimizer.DecisionVar;
import models.optimizer.DecisionVarName;
import models.optimizer.DecisionVarType;
import models.uav.Uav;
import models.target.Target;
import models.uav.UavCntrlSignals;
import models.uav.UavState;

/**
 *
 * @author jbbordon
 */
public class EvaluatorFunction extends Atomic {

    private static final Logger LOGGER = Logger.getLogger(EvaluatorFunction.class.getName());

    // in Ports of the model
    public Port<SearchArea> efI1 = new Port<>("searchArea");
    public Port<Nfz[]> efI2 = new Port<>("nfzs");
    public Port<ArrayList<Uav>> efI3 = new Port<>("fsUavs");
    public Port<ArrayList<Target>> efI4 = new Port<>("targets");
    public ArrayList<Port> efI5 = new ArrayList<>(); // t Target from TM   

    // out Ports of the model);
    public ArrayList<Port> efO1 = new ArrayList<>(); // u Uav to UM
    public ArrayList<Port> efO2 = new ArrayList<>(); // t Target to TM
    public Port<ArrayList<Uav>> efO3 = new Port<>("evalUavs");
    public Port<ArrayList<Target>> efO4 = new Port<>("evalTargets");

    // internal data
    protected SearchArea searchArea;
    protected Nfz[] nfzs;
    protected ArrayList<Uav> uavs;
    protected ArrayList<Target> targets;
    protected double clock;
    protected int targetsRcvd, uavsRcvd;

    public EvaluatorFunction(String coupledName, int numUavs, int numTargets) {
        super(coupledName + " EF");
        // Ports of the Atomic model
        super.addInPort(efI1);
        super.addInPort(efI2);
        super.addInPort(efI3);
        super.addInPort(efI4);
        // u ports efO1 (one per uav in the scenario)
        for (int u = 1; u <= numUavs; ++u) {
            Port<Uav> uPortO1 = new Port<>("uav" + u + " toUM");
            efO1.add(uPortO1);
            super.addOutPort(uPortO1);
        }
        // t ports efI5 & efO2 (one per target in the scenario)   
        for (int t = 1; t <= numTargets; ++t) {
            Port<Target> tPortI4 = new Port<>("evalTarget" + t);
            efI5.add(tPortI4);
            super.addInPort(tPortI4);
            Port<Target> tPortO2 = new Port<>("target" + t + " toTM");
            efO2.add(tPortO2);
            super.addOutPort(tPortO2);
        }
        super.addOutPort(efO3);
        super.addOutPort(efO4);
    }

    @Override
    public void initialize() {
        searchArea = null;
        nfzs = null;
        uavs = new ArrayList<>();
        targets = new ArrayList<>();
        clock = 0.0;
        uavsRcvd = 0;
        targetsRcvd = 0;
        super.passivate();
    }

    @Override
    public void exit() {
        searchArea = null;
        nfzs = null;
        uavs = new ArrayList<>();
        targets = new ArrayList<>();
        clock = 0.0;
        uavsRcvd = 0;
        targetsRcvd = 0;
        super.passivate();
    }

    @Override
    public void deltint() {
        if (phaseIs("start")) {
            // wait for evaluated targets & uavs to arrive
            clock = Double.MAX_VALUE;
            super.holdIn("evaluating", clock);
        } else if (phaseIs("end")) {
            LOGGER.log(
                    Level.FINEST,
                    String.format(
                            "%1$s: EVALUATION ENDS",
                            this.getName()
                    )
            );
            exit();
        }
    }

    @Override
    public void deltext(double e) {
        if (phaseIs("passive")) {
            // model is waiting for input data to start simulation
            if (!efI1.isEmpty()) {
                searchArea = efI1.getSingleValue();
            }
            if (!efI2.isEmpty()) {
                nfzs = efI2.getSingleValue();
            }
            if (!efI3.isEmpty()) {
                uavs = efI3.getSingleValue();
            }
            if (!efI4.isEmpty()) {
                targets = efI4.getSingleValue();
            }
            if (searchArea != null && nfzs != null && uavs.size() > 0 && targets.size() > 0) {
                // start simulation as all data has been received
                super.holdIn("start", clock);
                LOGGER.log(
                        Level.FINEST,
                        String.format(
                                "%1$s: EVALUATION STARTS",
                                this.getName()
                        )
                );
            }
        } else if (phaseIs("evaluating")) {
            // check t TM in ports for new target evaluations received
            for (int t = 0; t < targets.size(); ++t) {
                if (!efI5.get(t).isEmpty()) {
                    // new target evaluation has arrived
                    targets.set(t, (Target) efI5.get(t).getSingleValue());
                    targetsRcvd++;
                }
            }
            // check if all target evaluations have been received
            if (targetsRcvd == targets.size()) {
                // final evaluations
                evaluateCollisions();
                evaluateNFZs();
                //evaluateSmoothness();
                evaluateFuelEmpties();
                evaluateTgts();
                // simulation shall be finished
                clock = 0.0;
                super.holdIn("end", clock);
            }
        }
    }

    @Override
    public void lambda() {
        if (phaseIs("start")) {
            // output init data to the u UM models
            for (int u = 0; u < uavs.size(); ++u) {
                efO1.get(u).addValue(uavs.get(u));
            }
            // output init data to the t TM models
            for (int t = 0; t < targets.size(); ++t) {
                efO2.get(t).addValue(targets.get(t));
            }
        } else if (phaseIs("end")) {
            // output evaluated targets and uav paths
            efO3.addValue(uavs);
            efO4.addValue(targets);
        }
    }

    /**
     * Evaluate feseability of each uav State in terms of possible uav
     * collisions and ground collisions.
     */
    private void evaluateCollisions() {

        if (uavs.size() > 1) {

            /* UAVS COLLISION CHECK */
            for (int u1 = 0; u1 < uavs.size(); ++u1) {

                // variable to hold uav1 path
                ArrayList<UavState> uav1Path = uavs.get(u1).getPath();

                for (int u2 = u1 + 1; u2 < uavs.size(); ++u2) {

                    //  variable to hold uav2 path
                    ArrayList<UavState> uav2Path = uavs.get(u2).getPath();

                    // loop uav1Path & uav2Path
                    int index1 = 0;
                    int index2 = 0;

                    while (index1 < uav1Path.size() && index2 < uav2Path.size()) {

                        // current states of the path
                        UavState uav1State = uav1Path.get(index1);
                        UavState uav2State = uav2Path.get(index2);

                        if (uav1State.getTime() == uav2State.getTime()) {
                            // time is the same check possible collision
                            double distance = Math.sqrt(
                                    Math.pow(uav2State.getX() - uav1State.getX(), 2.0)
                                    + Math.pow(uav2State.getY() - uav1State.getY(), 2.0)
                                    + Math.pow(uav2State.getHeight() - uav1State.getHeight(), 2.0)
                            );

                            if (distance <= uavs.get(u1).getMotionModel().getSafteyDist()) {
                                // collision flag should be set
                                uavs.get(u1).setTotalCollisions();
                            }
                            if (distance <= uavs.get(u2).getMotionModel().getSafteyDist()) {
                                // collision flag should be set
                                uavs.get(u2).setTotalCollisions();
                            }
                            // advace both uavPaths
                            index1++;
                            index2++;

                        } else if (uav1State.getTime() > uav2State.getTime()) {
                            // advance uav2Path as uav1State time is higher
                            index2++;

                        } else {
                            // advance uav1Path as uav2State time is higher
                            index1++;
                        }
                    }
                }
            }
        }
        /* HERE SHOULD BE A GROUND COLLISION CHECK */
    }

    /**
     * Evaluate feseability of each uav State in terms of possible NFZ
     * overflight.
     */
    private void evaluateNFZs() {

        for (int u = 0; u < uavs.size(); ++u) {

            // variable to hold uav path
            ArrayList<UavState> uavPath = uavs.get(u).getPath();

            // loop each state in the path
            for (int i = 0; i < uavPath.size(); ++i) {

                // retrieve current state
                UavState uavState = uavPath.get(i);

                // locate uav in the searchArea map
                int n = 0;
                boolean exit = false;
                int uavXCell
                        = (int) Math.floor(uavState.getX() / searchArea.getxScale());
                int uavYCell
                        = (int) Math.floor(uavState.getY() / searchArea.getyScale());

                // loop nfz array
                while (!exit && n < nfzs.length) {

                    // check for NFZ overflight
                    if (uavXCell == nfzs[n].getxRow() && uavYCell == nfzs[n].getyCol()) {
                        // nfz flag should be set
                        uavs.get(u).setTotalNFZs();
                        exit = true;
                    } else {
                        n++;
                    }
                }
            }
        }
    }

    /**
     * Evaluate feseability of each uav State in terms of possible fuel empty.
     */
    private void evaluateFuelEmpties() {

        for (int u = 0; u < uavs.size(); ++u) {

            // variable to hold uav path & and uav end fuel
            double endFuel = uavs.get(u).getEndFuel();
            ArrayList<UavState> uavPath = uavs.get(u).getPath();

            // loop each state in the path
            for (int i = 0; i < uavPath.size(); ++i) {

                // retrieve current state
                UavState uavState = uavPath.get(i);

                // check fuel
                if (uavState.getFuel() < endFuel) {
                    // fuel empty flag
                    uavs.get(u).setTotalFuelEmpties();
                }

            }
        }
    }

    /**
     * Evaluate heading actions smoothness of each uav
     */
    private void evaluateSmoothness() {

        for (int u = 0; u < uavs.size(); ++u) {

            // retrive u Uav heading decision type
            DecisionVarType headingType;
            DecisionVar[] decisionArray
                    = uavs.get(u).getMotionModel().getDecisionArray();
            int d = 0;
            while (decisionArray[d].getName() != DecisionVarName.heading) {
                d++;
            }
            headingType = decisionArray[d].getType();

            // variable to hold uav cntrlSignals
            ArrayList<UavCntrlSignals> cntrlSignals = uavs.get(u).getCntrlSignals();

            // variables to hold actual & prev heading cntrl
            double actCntrl, prevCntrl;

            if (headingType == DecisionVarType.absolute) {
                // get prev absolute value and do the angular difference to
                // convert the absolute signal to incremental
                prevCntrl = cntrlSignals.get(0).getcHeading()
                        - uavs.get(u).getInitState().getHeading();
                while (prevCntrl < -180.0) {
                    prevCntrl += 2 * 180.0;
                }
                while (prevCntrl > 180.0) {
                    prevCntrl -= 2 * 180.0;
                }
            } else {
                prevCntrl = cntrlSignals.get(0).getcHeading();
            }

            // loop each cntrlSignal in the path
            for (int i = 1; i < cntrlSignals.size(); ++i) {

                // retrive actual heading cntrl action
                actCntrl = cntrlSignals.get(i).getcHeading();

                if (headingType == DecisionVarType.absolute) {
                    // get prev absolute value and do the angular difference to
                    // convert the absolute signal to incremental
                    actCntrl -= cntrlSignals.get(i - 1).getcHeading();
                    while (actCntrl < -180.0) {
                        actCntrl += 2 * 180.0;
                    }
                    while (actCntrl > 180.0) {
                        actCntrl -= 2 * 180.0;
                    }
                }

                // calculate prevCntrl & actCntrl signs
                double actCntrlSign = Math.signum(actCntrl);
                double prevCntrlSign = Math.signum(prevCntrl);

                // check if there is a sign change between each cntrl signal,
                // which is interpretate as the uav changing the direction
                if (actCntrlSign + prevCntrlSign == 0.0) {
                    uavs.get(u).setSmoothValue(
                            uavs.get(u).getSmoothValue()
                            + Math.pow(
                                    Math.abs(actCntrl) + Math.abs(prevCntrl),
                                    2.0));
                }

                prevCntrl = actCntrl;
            }
        }
    }

    /**
     * Evaluate target dp, etd & heurist.
     */
    private void evaluateTgts() {
        // loop each target
        for (int t = 0; t < targets.size(); ++t) {
            targets.get(t).calculateTargetHeuristic(uavs);
        }
    }

}
