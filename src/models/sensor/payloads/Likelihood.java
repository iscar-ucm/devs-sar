/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.sensor.payloads;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;

/**
 *
 * @author jbbordon
 */
public class Likelihood {

    private int xCells;
    private int yCells;
    private double xScale;
    private double yScale;
    private double time;
    private DMatrixRMaj matrix;

    /**
     * sensor likelihood is expresed as NDP matrix instead of DP
     *
     * @param xCells
     * @param yCells
     * @param xScale
     * @param yScale
     */
    public Likelihood(int xCells, int yCells, double xScale, double yScale) {
        this.xCells = xCells;
        this.yCells = yCells;
        this.xScale = xScale;
        this.yScale = yScale;
        matrix = new DMatrixRMaj(xCells, yCells);
        CommonOps_DDRM.add(matrix, 1.0);
        this.time = 0.0;
    }

    /**
     * sensor likelihood is expresed as NDP matrix instead of DP
     *
     * @param xCells
     * @param yCells
     * @param xScale
     * @param yScale
     * @param time
     */
    public Likelihood(int xCells, int yCells, double xScale, double yScale, double time) {
        this.xCells = xCells;
        this.yCells = yCells;
        this.xScale = xScale;
        this.yScale = yScale;
        matrix = new DMatrixRMaj(xCells, yCells);
        CommonOps_DDRM.add(matrix, 1.0);
        this.time = time;
    }

    /**
     * @return the xCells
     */
    public int getxCells() {
        return xCells;
    }

    /**
     * @param xCells the xCells to set
     */
    public void setxCells(int xCells) {
        this.xCells = xCells;
    }

    /**
     * @return the yCells
     */
    public int getyCells() {
        return yCells;
    }

    /**
     * @param yCells the yCells to set
     */
    public void setyCells(int yCells) {
        this.yCells = yCells;
    }

    /**
     * @return the xScale
     */
    public double getxScale() {
        return xScale;
    }

    /**
     * @param xScale the xScale to set
     */
    public void setxScale(double xScale) {
        this.xScale = xScale;
    }

    /**
     * @return the yScale
     */
    public double getyScale() {
        return yScale;
    }

    /**
     * @param yScale the yScale to set
     */
    public void setyScale(double yScale) {
        this.yScale = yScale;
    }

    /**
     * @return the time
     */
    public double getTime() {
        return time;
    }

    /**
     * @param time the time to set
     */
    public void setTime(double time) {
        this.time = time;
    }

    /**
     * @return the matrix
     */
    public DMatrixRMaj getMatrix() {
        return matrix;
    }

    /**
     * @param matrix to set
     */
    public void setMatrix(DMatrixRMaj matrix) {
        this.matrix = matrix;
    }

}
