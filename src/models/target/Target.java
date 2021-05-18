/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.target;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import java.util.ArrayList;
import models.environment.SearchArea;
import models.target.motionModels.MotionModel;
import models.target.motionModels.MotionModelType;
import models.target.motionModels.DinamicModel;
import models.target.motionModels.StaticModel;
import models.uav.Uav;
import models.uav.UavState;
import utils.CSVHandler;

/**
 *
 * @author jbbordon
 */
public class Target {

    private String name;
    private MotionModel motionModel;
    private int xCells;
    private int yCells;
    private double xScale;
    private double yScale;
    private double startTime;    
    private double endTime;
    private double endSequenceTime;
    private double dp;        // detection probability
    private double etd;       // expected time detection
    private double heuristic; // myopia criteria    
    private double missPD;  // total probability lost due to motion model predictions
    private boolean fullPath;
    private ArrayList<TargetState> path;

    /**
     * constructor 1 parameter JSON
     *
     * @param targetJSON
     * @param searchArea
     */
    public Target(JSONObject targetJSON, SearchArea searchArea) {
        // read target name       
        name = (String) targetJSON.get("name");
        // set target dimensions
        xCells = searchArea.getxCells();
        yCells = searchArea.getyCells();
        xScale = searchArea.getxScale();
        yScale = searchArea.getyScale();
        // read motion model
        JSONObject motionModelJS = (JSONObject) targetJSON.get("motionModel");
        MotionModelType targetMMType
                = MotionModelType.valueOf((String) motionModelJS.get("type"));
        switch (targetMMType) {
            case staticModel:
                motionModel = new StaticModel(motionModelJS);
                break;
            case homogeneous:
            case interpolation:
            case potential:
                motionModel = new DinamicModel(motionModelJS, xCells, yCells);
                break;
        }
        // read target initial State & init belief according init function
        TargetState initState;
        JSONObject jsInitState = (JSONObject) targetJSON.get("initState");
        String initType = (String) jsInitState.get("function");
        switch (initType) {
            case "load":
                initState
                        = new TargetState(
                                CSVHandler.loadTarget(name),
                                (double) jsInitState.get("time"));
                break;
            case "gaussian":
                JSONArray gaussiansJS = (JSONArray) jsInitState.get("gaussians");
                initState
                        = new TargetState(
                                initSeveralGaussians(gaussiansJS),
                                (double) jsInitState.get("time"));
                break;
            default:
                initState
                        = new TargetState(
                                (double) jsInitState.get("time"));
                break;
        }
        startTime = initState.getTime();     
        // init the path array
        fullPath = false;
        path = new ArrayList<>();
        path.add(initState);
        // read target endTime            
        endTime = (double) targetJSON.get("endTime");
        endSequenceTime = endTime;
        missPD = 0.0;
    }

    public Target(Target tgt) {
        // read target name       
        name = tgt.getName();
        // set target dimensions
        xCells = tgt.xCells;
        yCells = tgt.yCells;
        xScale = tgt.xScale;
        yScale = tgt.yScale;
        // read motion model
        MotionModelType targetMMType
                = tgt.getMotionModel().getType();
        switch (targetMMType) {
            case staticModel:
                motionModel = new StaticModel((StaticModel) tgt.getMotionModel());
                break;
            case homogeneous:
            case interpolation:
            case potential:
                motionModel = new DinamicModel((DinamicModel) tgt.getMotionModel());
                break;
        }
        // read target initial State & init belief
        DMatrixRMaj newBelief = new DMatrixRMaj(xCells, yCells);
        newBelief = tgt.getInitState().getBelief().copy();
        TargetState initState;
        initState = new TargetState(
                newBelief,
                tgt.getInitState().getTime()
        );
        startTime = tgt.getStartTime();         
        // init the path array
        fullPath = false;
        path = new ArrayList<>();
        path.add(initState);
        // read target endTime            
        endTime = tgt.getEndTime();
        endSequenceTime = tgt.getEndSequenceTime();
        dp = tgt.getDp();
        etd = tgt.getEtd();
        heuristic = tgt.getHeuristic();
        missPD = tgt.getMissPD();
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the motionModel
     */
    public MotionModel getMotionModel() {
        return motionModel;
    }

    /**
     * @param motionModel the motionModel to set
     */
    public void setMotionModel(MotionModel motionModel) {
        this.motionModel = motionModel;
    }

    /**
     * @return the initState
     */
    public TargetState getInitState() {
        return path.get(0);
    }

    /**
     * @param initState the initState to set
     */
    public void setInitState(TargetState initState) {
        path.set(0, initState);
    }

    /**
     * @return the startTime
     */
    public double getStartTime() {
        return startTime;
    }

    /**
     * @param startTime the startTime to set
     */
    public void setStartTime(double startTime) {
        this.startTime = startTime;
    }

    /**
     * @return the endTime
     */
    public double getEndTime() {
        return endTime;
    }

    /**
     * @param endTime
     */
    public void setEndTime(double endTime) {
        this.endTime = endTime;
    }

    /**
     * @return the endSequenceTime
     */
    public double getEndSequenceTime() {
        return endSequenceTime;
    }

    /**
     * @param endSequenceTime the endSequenceTime to set
     */
    public void setEndSequenceTime(double endSequenceTime) {
        this.endSequenceTime = endSequenceTime;
    }

    /**
     * @return the dp
     */
    public double getDp() {
        return dp;
    }

    /**
     * @param dp the dp to set
     */
    public void setDp(double dp) {
        this.dp = dp;
    }

    /**
     * @return the etd
     */
    public double getEtd() {
        return etd;
    }

    /**
     * @param etd the etd to set
     */
    public void setEtd(double etd) {
        this.etd = etd;
    }

    /**
     * @return the heuristic
     */
    public double getHeuristic() {
        return heuristic;
    }

    /**
     * @param heuristic the heuristic to set
     */
    public void setHeuristic(double heuristic) {
        this.heuristic = heuristic;
    }

    /**
     * @return the missPD
     */
    public double getMissPD() {
        return missPD;
    }

    /**
     * @param missPD the missPD to set
     */
    public void setMissPD(double missPD) {
        this.missPD = missPD;
    }

    /**
     * @return the fullPath
     */
    public boolean isFullPath() {
        return fullPath;
    }

    /**
     * @param fullPath the fullPath to set
     */
    public void setFullPath(boolean fullPath) {
        this.fullPath = fullPath;
    }

    /**
     * @return the path
     */
    public ArrayList<TargetState> getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(ArrayList<TargetState> path) {
        this.path = path;
    }

    /**
     * @return the target last state
     */
    public TargetState getFinalState() {
        return path.get(path.size() - 1);
    }

    /**
     * @param finalState the state to set
     */
    public void setFinalState(TargetState finalState) {
        path.set(path.size() - 1, finalState);
    }

    /**
     * This method calculates the non detection probability for the last state
     * of the path
     *
     */
    public void calculateTargetDp() {
        dp = 1.0 - CommonOps_DDRM.elementSum(getFinalState().getBelief()) - missPD;
    }

    /**
     * This method calculates the myopia heuristic given a uav solution.
     *
     * @param uavs the solution reached.
     */
    public void calculateTargetHeuristic(ArrayList<Uav> uavs) {

        // retrive target last state belief
        DMatrixRMaj lastBelief = getFinalState().getBelief();

        // variable to hold the heuristic map
        DMatrixRMaj tgtHeuristicMap = new DMatrixRMaj(xCells, yCells);
        CommonOps_DDRM.add(tgtHeuristicMap, 1.0);

        for (int u = 0; u < uavs.size(); ++u) {

            // variable to hold the heuristic map for each uav
            DMatrixRMaj uavHeuristicMap = new DMatrixRMaj(xCells, yCells);

            // retrieve u uav end state
            UavState endState = uavs.get(u).getFinalState();

            // locate uav in the target belief
            int uavXCell
                    = (int) Math.floor(endState.getY() / yScale);
            int uavYCell
                    = (int) Math.floor(endState.getX() / xScale);

            // calculate uav speed in terms of cells step until endTime
            double uavCellsByStep
                    = (uavs.get(u).getMotionModel().getAt() * endState.getAirSpeed())
                    / Math.sqrt(Math.pow(xScale, 2.0) + Math.pow(yScale, 2.0));
            double remSteps
                    = (uavs.get(u).getEndTime() - endState.getTime())
                    / uavs.get(u).getMotionModel().getAt();
            double reach
                    = remSteps * uavCellsByStep;
            double lambda
                    = Math.pow((1.0 - 0.95), (1.0 / reach));

            // loop each cell in the target belief
            for (int x = 0; x < xCells; ++x) {

                for (int y = 0; y < yCells; ++y) {

                    // calculate distance from uav end state to current cell
                    double cellDistance = Math.sqrt(
                            Math.pow(x - uavXCell, 2.0)
                            + Math.pow(y - uavYCell, 2.0)
                    );

                    // calculate angle difference from uav heading to current cell
                    double cellAngle
                            = Math.atan2(x - uavXCell, y - uavYCell) * 180 / Math.PI;
                    double angleDiff
                            = (endState.getHeading() - cellAngle) % 180.0;

                    // compute distance taking in count the angle difference
                    cellDistance += Math.abs(angleDiff * 10.0 / 180.0);

                    // finally calculate the cellValue
                    double cellValue
                            = 1.0 - (Math.pow(lambda, cellDistance));

                    uavHeuristicMap.set(x, y, cellValue);
                }
            }
            // compute iUav heuristic map 
            CommonOps_DDRM.elementMult(tgtHeuristicMap, uavHeuristicMap);
        }
        // finally compute target heuristic map with last target belief
        CommonOps_DDRM.elementMult(tgtHeuristicMap, lastBelief);
        heuristic = CommonOps_DDRM.elementSum(tgtHeuristicMap);
    }

    /**
     * This method concatenates tgtA path (not modified) with tgtB path
     * (modified).
     *
     * @param tgtA not modified.
     * @param tgtB modified.
     */
    public static void concatenateTgtSequence(Target tgtA, Target tgtB) {

        // get target paths 
        ArrayList<TargetState> tgtAPath
                = tgtA.getPath();
        ArrayList<TargetState> tgtBPath
                = tgtB.getPath();

        // concatenate tgtA path to tgtB path
        tgtAPath.remove(0);
        tgtBPath.addAll(tgtAPath);

        // store path in tgtB
        tgtB.setPath(tgtBPath);

        // copy dp, etd, heurist & missPD
        tgtB.setDp(tgtA.getDp());
        tgtB.setEtd(tgtA.getEtd());
        tgtB.setHeuristic(tgtA.getHeuristic());
        tgtB.setMissPD(tgtA.getMissPD());        
    }

    /**
     * This method copies the input ArrayListTarget and returns another list as
     * a copy.
     *
     * @param targets
     * @return a copy of the targets.
     */
    public static ArrayList<Target> copyTargetsArray(ArrayList<Target> targets) {

        // the copy to return
        ArrayList<Target> targetsCopy = new ArrayList<>();

        // for each target in the scenario
        for (int t = 0; t < targets.size(); ++t) {
            // copy the input target
            Target newTarget = new Target(targets.get(t));
            // add it target
            targetsCopy.add(newTarget);
        }

        return targetsCopy;
    }

    /**
     * This method creates a new Target population given the uavs to populate,
     * the number of individuals and the sequence number and time,
     *
     * @param targets
     * @param numSol
     * @param sequenceNum
     * @param sequenceTime
     * @return the target population for the sequence number and time.
     */
    public static ArrayList<ArrayList<Target>> populateNsTargets(
            ArrayList<Target> targets, int numSol, int sequenceNum, double sequenceTime) {

        // new population to return
        ArrayList<ArrayList<Target>> targetsPopulation = new ArrayList<>();

        // data to hold the scenario uavs that should be simulated in this sequence
        ArrayList<Target> tgtsToSimulate = new ArrayList<>();

        // sequence times
        double endSequenceTime = sequenceNum * sequenceTime;

        // for each target in the scenario
        for (int t = 0; t < targets.size(); ++t) {

            // make a copy of the scenario target
            Target newTarget = new Target(targets.get(t));

            // get scenario target initTime and endTime
            double tgtStartTime = targets.get(t).getFinalState().getTime();
            double tgtEndTime = targets.get(t).getEndTime();

            // check if target has to be simulated in this sequence
            if (tgtStartTime < endSequenceTime) {

                // set newTarget endTime
                if (endSequenceTime > tgtEndTime) {
                    // adjust time to not exceed scenario uav endTime
                    newTarget.setEndSequenceTime(tgtEndTime);

                } else {
                    newTarget.setEndSequenceTime(endSequenceTime);

                }

                // initial state for newTarget should be lastTgtState simulated
                TargetState lastTgtState = targets.get(t).getFinalState();
                newTarget.setInitState(lastTgtState);

                // add the target to tgtsToSimulate list
                tgtsToSimulate.add(newTarget);

            }
        }

        // finally loop numSol times to create the new population
        for (int i = 0; i < numSol; ++i) {
            targetsPopulation.add(copyTargetsArray(tgtsToSimulate));
        }

        return targetsPopulation;
    }

    private DMatrixRMaj initSeveralGaussians(JSONArray gaussiansJS) {

        DMatrixRMaj belief = new DMatrixRMaj(xCells, yCells);

        // loop gaussiansJSArray
        for (int i = 0; i < gaussiansJS.size(); ++i) {

            JSONObject iGaussian = (JSONObject) gaussiansJS.get(i);

            // iGaussian belief
            DMatrixRMaj guassianBelief
                    = initGaussian(iGaussian);

            // apply weight
            double weight = (double) iGaussian.get("weight");
            CommonOps_DDRM.scale(weight, guassianBelief);

            // add it to actual belief
            CommonOps_DDRM.addEquals(belief, guassianBelief);

        }

        // normalize belief
        double sum = CommonOps_DDRM.elementSum(belief);
        CommonOps_DDRM.divide(belief, sum);     

        //DMatrixVisualization.show(belief, "Target Init State");        
        return belief;

    }

    private DMatrixRMaj initGaussian(JSONObject gaussianJS) {

        DMatrixRMaj belief = new DMatrixRMaj(xCells, yCells);

        // read Gaussian (mu)
        JSONArray muJS = (JSONArray) gaussianJS.get("mu");
        DMatrixRMaj mu = new DMatrixRMaj(1, muJS.size());
        for (int i = 0; i < muJS.size(); ++i) {
            mu.set(i, (double) (long) muJS.get(i));
        }

        // read Gaussian (cov & invSigma)
        JSONArray covJS = (JSONArray) gaussianJS.get("cov");
        DMatrixRMaj cov = new DMatrixRMaj(2, 2);
        DMatrixRMaj invSigma = new DMatrixRMaj(2, 2);
        for (int row = 0; row < covJS.size(); ++row) {
            JSONArray covRow = (JSONArray) covJS.get(row);
            for (int col = 0; col < covRow.size(); ++col) {
                cov.set(row, col, (double) covRow.get(col));
            }
        }
        CommonOps_DDRM.invert(cov, invSigma);

        // [X, Y] = meshgrid(1:1:wcol, 1:1:wrow)
        DMatrixRMaj xMatrix = new DMatrixRMaj(xCells, yCells);
        DMatrixRMaj yMatrix = new DMatrixRMaj(xCells, yCells);
        for (int x = 0; x < xCells; ++x) {
            for (int y = 0; y < yCells; ++y) {
                xMatrix.set(x, y, y + 1);
                yMatrix.set(x, y, x + 1);
            }
        }

        // apply values to the belief
        DMatrixRMaj xVec = new DMatrixRMaj(1, 2);
        DMatrixRMaj xDif = new DMatrixRMaj(1, 2);
        DMatrixRMaj aux = new DMatrixRMaj(1, 2);
        DMatrixRMaj aux2 = new DMatrixRMaj(1, 1);
        for (int x = 0; x < xCells; ++x) {
            for (int y = 0; y < yCells; ++y) {
                xVec.set(0, xMatrix.get(x, y));
                xVec.set(1, yMatrix.get(x, y));
                CommonOps_DDRM.subtract(xVec, mu, xDif);
                CommonOps_DDRM.mult(xDif, invSigma, aux);
                CommonOps_DDRM.multTransB(aux, xDif, aux2);
                belief.set(x, y, Math.exp(-aux2.get(0) / 2.0));
            }
        }
        return belief;
    }

}
