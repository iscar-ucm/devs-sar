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
        startTime = (double) jsInitState.get("time");
        // init the path array
        fullPath = false;
        path = new ArrayList<>();
        path.add(initState);
        // read target endTime            
        endTime = (double) targetJSON.get("endTime");
        endSequenceTime = endTime;
    }

    /**
     * Constructor. Used to copy a Target.
     *
     */
    public Target(String name, MotionModel motionModel, int xCells, int yCells,
            double xScale, double yScale, double startTime, double endTime,
            double endSequenceTime, TargetState initState) {
        this.name = name;
        this.xCells = xCells;
        this.yCells = yCells;
        this.xScale = xScale;
        this.yScale = yScale;
        // read motion model
        MotionModelType targetMMType
                = motionModel.getType();
        switch (targetMMType) {
            case staticModel:
                this.motionModel = new StaticModel((StaticModel) motionModel);
                break;
            case homogeneous:
            case interpolation:
            case potential:
                this.motionModel = new DinamicModel((DinamicModel) motionModel);
                break;
        }
        this.startTime = startTime;
        fullPath = false;
        this.path = new ArrayList<>();
        this.path.add(initState.clone());
        this.endTime = endTime;
        this.endSequenceTime = endSequenceTime;
    }

    /**
     * Constructor. Used to clone a Target.
     *
     */
    public Target(String name, MotionModel motionModel, int xCells, int yCells,
            double xScale, double yScale, double startTime, double endTime,
            double endSequenceTime, boolean fullPath, ArrayList<TargetState> path) {
        this.name = name;
        this.xCells = xCells;
        this.yCells = yCells;
        this.xScale = xScale;
        this.yScale = yScale;
        // read motion model
        MotionModelType targetMMType
                = motionModel.getType();
        switch (targetMMType) {
            case staticModel:
                this.motionModel = new StaticModel((StaticModel) motionModel);
                break;
            case homogeneous:
            case interpolation:
            case potential:
                this.motionModel = new DinamicModel((DinamicModel) motionModel);
                break;
        }
        this.startTime = startTime;
        this.fullPath = false;
        // clone Target path        
        this.path = new ArrayList<>();
        for (short i = 0; i < path.size(); i++) {
            this.path.add(path.get(i).clone());
        }
        this.endTime = endTime;
        this.endSequenceTime = endSequenceTime;
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
    public double getPd() {
        return getFinalState().getPd();
    }

    /**
     * @return the etd
     */
    public double getEtd() {
        return getFinalState().getEtd();
    }

    /**
     * @return the heuristic
     */
    public double getMyo() {
        return getFinalState().getMyo();
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
     * This method calculates the myopia criteria given a uav solution.
     *
     * @param uavs the solution reached.
     */
    public void calculateMyopia(ArrayList<Uav> uavs) {

        if (endSequenceTime == endTime) {

            // myopia criteria shouldn't be evaluated in the last sequence
            getFinalState().setMyo(0.0);

        } else {

            // retrive target last state belief
            DMatrixRMaj lastBelief = getFinalState().getBelief().copy();

            // variable to hold the heuristic map
            DMatrixRMaj myoMap = new DMatrixRMaj(xCells, yCells);
            CommonOps_DDRM.add(myoMap, 1.0);

            for (int u = 0; u < uavs.size(); ++u) {

                // this checks if the UAV was simulated this sequence or not
                if (!uavs.get(u).getCntrlSignals().isEmpty()) {

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
                    CommonOps_DDRM.elementMult(myoMap, uavHeuristicMap);
                }
            }
            // finally compute target heuristic map with last target belief
            CommonOps_DDRM.elementMult(myoMap, lastBelief);
            getFinalState().setMyo(CommonOps_DDRM.elementSum(myoMap));
        }
    }

    /**
     * Returns a copy the target with empty path.
     *
     * @return Target copy.
     */
    public Target copy() {
        return new Target(
                this.name,
                this.motionModel,
                this.xCells,
                this.yCells,
                this.xScale,
                this.yScale,
                this.startTime,
                this.endTime,
                this.endSequenceTime,
                this.path.get(0));
    }

    @Override
    /**
     * Returns an exact clone of the object.
     *
     * @return Target clone.
     */
    public Target clone() {
        return new Target(
                this.name,
                this.motionModel,
                this.xCells,
                this.yCells,
                this.xScale,
                this.yScale,
                this.startTime,
                this.endTime,
                this.endSequenceTime,
                this.fullPath,
                this.path);
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
        // normalize belief
        double sum = CommonOps_DDRM.elementSum(belief);
        CommonOps_DDRM.divide(belief, sum);
        return belief;
    }

}
