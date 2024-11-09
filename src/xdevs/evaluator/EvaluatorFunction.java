/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.evaluator;

import java.util.ArrayList;
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

    // in Ports of the model);
    public Port<SearchArea> efI1 = new Port<>("searchArea");
    public Port<Nfz[]> efI2 = new Port<>("nfzs");
    public ArrayList<Port> efI3 = new ArrayList<>(); // u Uav from FS UM
    public ArrayList<Port> efI4 = new ArrayList<>(); // t Target from E TM   

    // out Ports of the model);
    public ArrayList<Port> efO1 = new ArrayList<>(); // evalUavs
    public ArrayList<Port> efO2 = new ArrayList<>(); // evalTargets

    // internal data
    protected SearchArea searchArea;
    protected Nfz[] nfzs;
    protected ArrayList<Uav> uavs;
    protected ArrayList<Target> targets;
    protected double clock;
    
    public EvaluatorFunction(String coupledName, int numUavs, int numTargets) {
        super(coupledName + " EF");
        // Ports of the Atomic model
        super.addInPort(efI1);
        super.addInPort(efI2);

        // u ports efI3 & ecO2 (one per uav in the scenario)
        for (int u = 1; u <= numUavs; ++u) {
            Port<Uav> uPortI3 = new Port<>("evalUav" + u);
            efI3.add(uPortI3);
            super.addInPort(uPortI3);
            Port<Uav> uPortO1 = new Port<>("fsUav" + u);
            efO1.add(uPortO1);
            super.addOutPort(uPortO1);
        }
        // t ports efI4 & ecO3 (one per target in the scenario)   
        for (int t = 1; t <= numTargets; ++t) {
            Port<Target> tPortI4 = new Port<>("evalTarget" + t);
            efI4.add(tPortI4);
            super.addInPort(tPortI4);
            Port<Target> tPortO2 = new Port<>("evalTarget" + t);
            efO2.add(tPortO2);
            super.addOutPort(tPortO2);
        }
    }
    
    @Override
    public void initialize() {
        searchArea = null;
        nfzs = null;
        uavs = new ArrayList<>();
        targets = new ArrayList<>();
        clock = 0.0;
        super.passivate();
    }
    
    @Override
    public void exit() {
        searchArea = null;
        nfzs = null;
        uavs = new ArrayList<>();
        targets = new ArrayList<>();
        clock = 0.0;
        super.passivate();
    }
    
    @Override
    public void deltint() {
        if (phaseIs("outputData")) {
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
            // check u UM in ports for new uav evaluations received
            for (int u = 0; u < efI3.size(); ++u) {
                if (!efI3.get(u).isEmpty()) {
                    // new uav simulation has arrived
                    uavs.add((Uav) efI3.get(u).getSingleValue());
                }
            }
            // check t TM in ports for new target evaluations received
            for (int t = 0; t < efI4.size(); ++t) {
                if (!efI4.get(t).isEmpty()) {
                    // new target evaluation has arrived
                    targets.add((Target) efI4.get(t).getSingleValue());
                }
            }
            if (searchArea != null && nfzs != null
                    && targets.size() > 0 && uavs.size() > 0) {

                // final evaluations
                evaluateCollisions();
                evaluateNFZs();
                evaluateSmoothness();
                evaluateTgts();

                // output data
                super.holdIn("outputData", clock);
            }
        }
    }
    
    @Override
    public void lambda() {
        if (phaseIs("outputData")) {
            // output init data to the u UM models
            for (int u = 0; u < uavs.size(); ++u) {
                efO1.get(u).addValue(uavs.get(u));
            }
            // output init data to the t TM models
            for (int t = 0; t < targets.size(); ++t) {
                efO2.get(t).addValue(targets.get(t));
            }
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
                int uav1Coll = uav1Path.get(0).getCol();
                
                for (int u2 = 0; u2 < uavs.size(); ++u2) {
                    
                    if (u1 != u2) {

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
                                    // collision count should be incremented
                                    uav1Coll++;
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
                uav1Path.get(uav1Path.size() - 1).setCol(uav1Coll);
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
            int uavNFZs = uavPath.get(0).getNfzs();

            // loop each state in the path
            for (int i = 0; i < uavPath.size(); ++i) {

                // retrieve current state
                UavState uavState = uavPath.get(i);

                // locate uav in the searchArea map
                int n = 0;
                boolean exit = false;
                int uavXCell
                        = (int) Math.floor(uavState.getY() / searchArea.getyScale());
                int uavYCell
                        = (int) Math.floor(uavState.getX() / searchArea.getxScale());

                // loop nfz array
                while (!exit && n < nfzs.length) {

                    // check for NFZ overflight
                    if (uavXCell == nfzs[n].getxRow() && uavYCell == nfzs[n].getyCol()) {
                        // nfz flag should be set
                        uavNFZs++;
                        exit = true;
                    } else {
                        n++;
                    }
                }
                uavState.setNfzs(uavNFZs);
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

            // variable to loop uav cntrlSignals
            ArrayList<UavCntrlSignals> allSignals = new ArrayList();
            allSignals.addAll(uavs.get(u).getPrevCntrlSignals());
            allSignals.addAll(uavs.get(u).getCntrlSignals());

            // check uav has not started yet or has ended before this sequence
            if (!allSignals.isEmpty()) {
                
                double actCntrl, prevCntrl;
                double smoothValue = 0.0;
                
                if (headingType == DecisionVarType.absolute) {
                    prevCntrl = allSignals.get(0).getcHeading();
                } else {
                    prevCntrl = allSignals.get(0).getcHeading()
                            - uavs.get(u).getInitState().getHeading();
                }

                // loop each cntrlSignal in the path
                for (int i = 1; i < allSignals.size(); ++i) {

                    // retrive actual heading cntrl action
                    actCntrl = allSignals.get(i).getcHeading();
                    
                    if (headingType == DecisionVarType.absolute) {
                        // angular difference to convert the absolute signal to incremental
                        actCntrl -= allSignals.get(i - 1).getcHeading();
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
                        smoothValue
                                += Math.pow(
                                        Math.abs(actCntrl) + Math.abs(prevCntrl),
                                        2.0);
                    }

                    // set smooth value
                    allSignals.get(i).setSmooth(smoothValue);
                    // update previous value
                    prevCntrl = actCntrl;
                }
            }
        }
    }

    /**
     * Evaluate target dp, etd & heurist.
     */
    private void evaluateTgts() {
        // loop each target
        for (int t = 0; t < targets.size(); ++t) {
            targets.get(t).calculateMyopia(uavs);
        }
    }
    
}
