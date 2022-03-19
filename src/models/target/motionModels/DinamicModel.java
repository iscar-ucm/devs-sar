/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.target.motionModels;

import models.target.TargetState;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import utils.CommonOperations;

/**
 *
 * @author Juan
 */
public class DinamicModel extends MotionModel {

    // motion model update period
    private double at;
    // motion model transition matrix
    private DMatrixRMaj transitionMatrix;
    // flag to avoid probability escape from the searchArea borders
    private boolean nonEscape;
    // prediction time
    private double predictionTime;
    // target current state variable
    protected TargetState targetState;

    /**
     *
     * @param motionModelJSON
     * @param xCells belief number of rows
     * @param yCells belief number of cols
     */
    public DinamicModel(JSONObject motionModelJSON, int xCells, int yCells) {
        super(motionModelJSON);
        at = (double) motionModelJSON.get("at");
        nonEscape = (boolean) motionModelJSON.get("nonEscape");
        // read points
        JSONArray pointsArray = (JSONArray) motionModelJSON.get("points");
        Point[] points = new Point[pointsArray.size()];
        for (int i = 0; i < pointsArray.size(); ++i) {
            Point iPoint = new Point((JSONObject) pointsArray.get(i));
            points[i] = iPoint;
        }
        switch (type) {
            case staticModel:
                break;
            case homogeneous:
                if (nonEscape) {
                    transitionMatrix = transHomogeneousNonEscape(points, xCells, yCells);
                } else {
                    transitionMatrix = transHomogeneous(points, xCells, yCells);
                }
                break;
            case interpolation:
                transitionMatrix = transInterpolation(points, xCells, yCells);
                break;
            case potential:
                if (nonEscape) {
                    transitionMatrix = transPotentialNonEscape(points, xCells, yCells);
                } else {
                    transitionMatrix = transPotential(points, xCells, yCells);
                }
                break;
        }
    }

    public DinamicModel(DinamicModel motionModel) {
        super(motionModel);
        at = motionModel.getAt();
        transitionMatrix = motionModel.getTransitionMatrix().copy();
    }

    /**
     * @return the at
     */
    public double getAt() {
        return at;
    }

    /**
     * @param at the at to set
     */
    public void setAt(double at) {
        this.at = at;
    }

    /**
     * @return the transitionMatrix
     */
    public DMatrixRMaj getTransitionMatrix() {
        return transitionMatrix;
    }

    /**
     * @param transitionMatrix the transitionMatrix to set
     */
    public void setTransitionMatrix(DMatrixRMaj transitionMatrix) {
        this.transitionMatrix = transitionMatrix;
    }

    /**
     * @return the nonEscape
     */
    public boolean isNonEscape() {
        return nonEscape;
    }

    /**
     * @param nonEscape the nonEscape to set
     */
    public void setNonEscape(boolean nonEscape) {
        this.nonEscape = nonEscape;
    }

    /**
     * @return the predictionTime
     */
    public double getPredictionTime() {
        return predictionTime;
    }

    /**
     * @param targetState the targetState to set
     */
    public void setTargetState(TargetState targetState) {
        this.targetState = targetState;
    }

    public void initModel(TargetState initState) {
        // init internal variables
        targetState = initState;
        if (CommonOperations.isMultiple(initState.getTime(), at)) {
            predictionTime = initState.getTime();
        } else {
            predictionTime = initState.getTime() - (initState.getTime() % at);
        }
    }

    public TargetState stepModel() {

        // variables used to store the current and new target beliefs
        DMatrixRMaj targetBelief
                = targetState.getBelief();
        DMatrixRMaj newBelief
                = new DMatrixRMaj(
                        targetBelief.getNumRows(), targetBelief.getNumCols());

        // loop each cell of the target belief
        for (int x = 0; x < targetBelief.getNumRows(); ++x) {

            for (int y = 0; y < targetBelief.getNumCols(); ++y) {

                // *************** COMPUTE CURRENT CELL TRANSITION ********** //       
                // auxiliar variables                
                double cellProb = 0.0;
                int cellIndex;

                // input from C : no move                
                cellIndex = (x * targetBelief.getNumCols()) + y;
                // compute probability of no moving
                cellProb
                        += transitionMatrix.get(cellIndex, Point.CENTER)
                        * targetBelief.get(cellIndex);

                // input from N : (N  => S)
                if (x < (targetBelief.getNumRows() - 1)) {
                    // index of the N cell
                    cellIndex = ((x + 1) * targetBelief.getNumCols()) + y;
                    // compute probability of N  => S
                    cellProb
                            += transitionMatrix.get(cellIndex, Point.SOUTH)
                            * targetBelief.get(cellIndex);
                }

                // input from NE: (NE => SW)
                if (x < (targetBelief.getNumRows() - 1) && y < (targetBelief.getNumCols() - 1)) {
                    // index of the NE cell
                    cellIndex = ((x + 1) * targetBelief.getNumCols()) + (y + 1);
                    // compute probability of NE => SW
                    cellProb
                            += transitionMatrix.get(cellIndex, Point.SOUTHWEST)
                            * targetBelief.get(cellIndex);
                }

                // input from E : (E  => W)
                if (y < (targetBelief.getNumCols() - 1)) {
                    // index of the E cell
                    cellIndex = (x * targetBelief.getNumCols()) + (y + 1);
                    // compute probability of E => W
                    cellProb
                            += transitionMatrix.get(cellIndex, Point.WEST)
                            * targetBelief.get(cellIndex);
                }

                // input from SE: (SE => NW)
                if (x > 0 && y < (targetBelief.getNumCols() - 1)) {
                    // index of the SE cell
                    cellIndex = ((x - 1) * targetBelief.getNumCols()) + (y + 1);
                    // compute probability of SE => NW
                    cellProb
                            += transitionMatrix.get(cellIndex, Point.NORTHWEST)
                            * targetBelief.get(cellIndex);
                }

                // input from S : (S  => N)
                if (x > 0) {
                    // index of the N cell
                    cellIndex = ((x - 1) * targetBelief.getNumCols()) + y;
                    // compute probability of S => N
                    cellProb
                            += transitionMatrix.get(cellIndex, Point.NORTH)
                            * targetBelief.get(cellIndex);
                }

                // input from SW: (SW => NE)
                if (x > 0 && y > 0) {
                    // index of the SW cell
                    cellIndex = ((x - 1) * targetBelief.getNumCols()) + (y - 1);
                    // compute probability of SE => NW
                    cellProb
                            += transitionMatrix.get(cellIndex, Point.NORTHEAST)
                            * targetBelief.get(cellIndex);
                }

                // input from W : (W  => E)
                if (y > 0) {
                    // index of the W cell
                    cellIndex = (x * targetBelief.getNumCols()) + (y - 1);
                    // compute probability of W => E
                    cellProb
                            += transitionMatrix.get(cellIndex, Point.EAST)
                            * targetBelief.get(cellIndex);
                }

                // input from NW: (NW => SE)
                if (x < (targetBelief.getNumRows() - 1) && y > 0) {
                    // index of the NW cell
                    cellIndex = ((x + 1) * targetBelief.getNumCols()) + (y - 1);
                    // compute probability of NW => SE
                    cellProb
                            += transitionMatrix.get(cellIndex, Point.SOUTHEAST)
                            * targetBelief.get(cellIndex);
                }

                // finally set current cell probability into the newBelief
                newBelief.set(x, y, cellProb);
            }
        }

        // check nonEscape flag and create the new state
        predictionTime += at;
        if (!isNonEscape()) {
            // compute probability lost
            double prevProb = CommonOps_DDRM.elementSum(targetBelief);
            double newProb = CommonOps_DDRM.elementSum(newBelief);
            targetState = new TargetState(
                    newBelief,
                    getPredictionTime(),
                    prevProb - newProb);
        } else {
            targetState = new TargetState(
                    newBelief,
                    getPredictionTime(),
                    0.0);
        }

        return targetState;

    }

    public void resetModel() {
    }

    private DMatrixRMaj transHomogeneousNonEscape(Point[] points, int xCells, int yCells) {
        // the transition matrix to return
        DMatrixRMaj tMatrix
                = new DMatrixRMaj(xCells * yCells, points[0].getCardinalActions().length);

        // loop each cell in the target belief
        for (int x = 0; x < xCells; ++x) {

            for (int y = 0; y < yCells; ++y) {

                // auxiliar variables
                int cellIndex = (x * yCells) + y;

                // compute point actions
                double action;
                double sum = 0.0;

                // action C (no move)             
                action = points[0].getCardinalActions()[Point.CENTER];
                tMatrix.add(cellIndex, Point.CENTER, action);
                sum += action;

                // action moving N
                if (x < (xCells - 1)) {
                    action = points[0].getCardinalActions()[Point.NORTH];
                    tMatrix.add(cellIndex, Point.NORTH, action);
                    sum += action;
                }

                // action moving NE
                if (x < (xCells - 1) && y < (yCells - 1)) {
                    action = points[0].getCardinalActions()[Point.NORTHEAST];
                    tMatrix.add(cellIndex, Point.NORTHEAST, action);
                    sum += action;
                }

                // action moving E
                if (y < (yCells - 1)) {
                    action = points[0].getCardinalActions()[Point.EAST];
                    tMatrix.add(cellIndex, Point.EAST, action);
                    sum += action;
                }

                // action moving SE
                if (x > 0 && y < (yCells - 1)) {
                    action = points[0].getCardinalActions()[Point.SOUTHEAST];
                    tMatrix.add(cellIndex, Point.SOUTHEAST, action);
                    sum += action;
                }

                // action moving S
                if (x > 0) {
                    action = points[0].getCardinalActions()[Point.SOUTH];
                    tMatrix.add(cellIndex, Point.SOUTH, action);
                    sum += action;
                }

                // action moving SW
                if (x > 0 && y > 0) {
                    action = points[0].getCardinalActions()[Point.SOUTHWEST];
                    tMatrix.add(cellIndex, Point.SOUTHWEST, action);
                    sum += action;
                }

                // action moving W
                if (y > 0) {
                    action = points[0].getCardinalActions()[Point.WEST];
                    tMatrix.add(cellIndex, Point.WEST, action);
                    sum += action;
                }

                // action moving NW
                if (x < (xCells - 1) && y > 0) {
                    action = points[0].getCardinalActions()[Point.NORTHWEST];
                    tMatrix.add(cellIndex, Point.NORTHWEST, action);
                    sum += action;
                }

                // check sum is not equal 0, this can happen when we are at
                // the borders of the search area and target moving directions
                // are not possible (in example x = xCells - 1 && target moving
                // directions N, NE or NW).
                if (sum == 0) {
                    // moving direction is not possible so stay in center
                    tMatrix.set(cellIndex, Point.CENTER, 1.0);
                }
            }
        }
        // return the transitionMatrix
        return tMatrix;
    }

    private DMatrixRMaj transHomogeneous(Point[] points, int xCells, int yCells) {
        // the transition matrix to return
        DMatrixRMaj tMatrix
                = new DMatrixRMaj(xCells * yCells, points[0].getCardinalActions().length);

        // loop each cell in the target belief
        for (int x = 0; x < xCells; ++x) {

            for (int y = 0; y < yCells; ++y) {

                // auxiliar variables
                int cellIndex = (x * yCells) + y;

                // loop each action of the given point and set corresponding
                // matrix action value
                for (int a = 0; a < points[0].getCardinalActions().length; a++) {
                    tMatrix.set(cellIndex, a, points[0].getCardinalActions()[a]);
                }
            }
        }
        // return the transitionMatrix
        return tMatrix;
    }

    private DMatrixRMaj transInterpolation(Point[] points, int xCells, int yCells) {
        // the transition matrix to return        
        DMatrixRMaj tMatrix
                = new DMatrixRMaj(xCells * yCells, points[0].getCardinalActions().length);
        return tMatrix;
    }

    private DMatrixRMaj transPotentialNonEscape(Point[] points, int xCells, int yCells) {
        // the transition matrix to return
        DMatrixRMaj tMatrix
                = new DMatrixRMaj(xCells * yCells, points[0].getCardinalActions().length);

        // loop each cell in the target belief
        for (int x = 0; x < xCells; ++x) {
            for (int y = 0; y < yCells; ++y) {

                // auxiliar variables
                double sum = 0.0;
                int cellIndex = (x * yCells) + y;

                // loop each point given                
                for (Point point : points) {

                    // calculate distance from the currentCell to the currentPoint
                    double distance
                            = Math.sqrt(
                                    Math.pow(x - point.getxCell(), 2.0)
                                    + Math.pow(y - point.getyCell(), 2.0));
                    if (distance == 0) {
                        // current cell is the actual point
                        distance = 1;
                    }

                    // compute point actions
                    double action;

                    // action C (no move)             
                    action = point.getCardinalActions()[Point.CENTER] / distance;
                    tMatrix.add(cellIndex, Point.CENTER, action);
                    sum += action;

                    // action moving N
                    if (x < (xCells - 1)) {
                        action = point.getCardinalActions()[Point.NORTH] / distance;
                        tMatrix.add(cellIndex, Point.NORTH, action);
                        sum += action;
                    }

                    // action moving NE
                    if (x < (xCells - 1) && y < (yCells - 1)) {
                        action = point.getCardinalActions()[Point.NORTHEAST] / distance;
                        tMatrix.add(cellIndex, Point.NORTHEAST, action);
                        sum += action;
                    }

                    // action moving E
                    if (y < (yCells - 1)) {
                        action = point.getCardinalActions()[Point.EAST] / distance;
                        tMatrix.add(cellIndex, Point.EAST, action);
                        sum += action;
                    }

                    // action moving SE
                    if (x > 0 && y < (yCells - 1)) {
                        action = point.getCardinalActions()[Point.SOUTHEAST] / distance;
                        tMatrix.add(cellIndex, Point.SOUTHEAST, action);
                        sum += action;
                    }

                    // action moving S
                    if (x > 0) {
                        action = point.getCardinalActions()[Point.SOUTH] / distance;
                        tMatrix.add(cellIndex, Point.SOUTH, action);
                        sum += action;
                    }

                    // action moving SW
                    if (x > 0 && y > 0) {
                        action = point.getCardinalActions()[Point.SOUTHWEST] / distance;
                        tMatrix.add(cellIndex, Point.SOUTHWEST, action);
                        sum += action;
                    }

                    // action moving W
                    if (y > 0) {
                        action = point.getCardinalActions()[Point.WEST] / distance;
                        tMatrix.add(cellIndex, Point.WEST, action);
                        sum += action;
                    }

                    // action moving NW
                    if (x < (xCells - 1) && y > 0) {
                        action = point.getCardinalActions()[Point.NORTHWEST] / distance;
                        tMatrix.add(cellIndex, Point.NORTHWEST, action);
                        sum += action;
                    }

                }

                // check sum is not equal 0, this can happen when we are at
                // the borders of the search area and target moving directions
                // are not possible (in example x = xCells - 1 && target moving
                // directions N, NE or NW).
                if (sum == 0) {

                    // moving direction is not possible so stay in center
                    tMatrix.set(cellIndex, Point.CENTER, 1.0);

                } else {

                    // normalize to a probability of 1
                    for (int a = 0; a < points[0].getCardinalActions().length; a++) {
                        double value = tMatrix.get(cellIndex, a);
                        value = value / sum;
                        tMatrix.set(cellIndex, a, value);
                    }
                }
            }
        }
        // return the transitionMatrix
        return tMatrix;
    }

    private DMatrixRMaj transPotential(Point[] points, int xCells, int yCells) {
        // the transition matrix to return
        DMatrixRMaj tMatrix
                = new DMatrixRMaj(xCells * yCells, points[0].getCardinalActions().length);

        // loop each cell in the target belief
        for (int x = 0; x < xCells; ++x) {
            for (int y = 0; y < yCells; ++y) {

                // auxiliar variables
                double sum = 0.0;
                int cellIndex = (x * yCells) + y;

                // loop each point given                
                for (Point point : points) {

                    // calculate distance from the currentCell to the currentPoint
                    double distance
                            = Math.sqrt(
                                    Math.pow(x - point.getxCell(), 2.0)
                                    + Math.pow(y - point.getyCell(), 2.0));
                    if (distance == 0) {
                        // current cell is the actual point
                        distance = 1;
                    }

                    // loop each action of the current point and set corresponding
                    // matrix action value
                    for (int a = 0; a < point.getCardinalActions().length; a++) {
                        double action = point.getCardinalActions()[a] / distance;
                        tMatrix.add(cellIndex, a, action);
                        sum += action;
                    }

                }

                // normalize to a probability of 1
                for (int a = 0; a < points[0].getCardinalActions().length; a++) {
                    double value = tMatrix.get(cellIndex, a);
                    value = value / sum;
                    tMatrix.set(cellIndex, a, value);
                }
            }
        }
        // return the transitionMatrix
        return tMatrix;
    }

}
