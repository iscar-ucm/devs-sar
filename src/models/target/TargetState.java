/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.target;

import org.ejml.data.DMatrixRMaj;

/**
 *
 * @author jbbordon
 */
public class TargetState {

    private DMatrixRMaj belief;
    private double cMissPd;
    private boolean prediction;
    private double time;

    /**
     *
     * @param belief
     * @param stMissPd
     * @param prediction
     * @param time
     */
    public TargetState(DMatrixRMaj belief, double stMissPd, boolean prediction, double time) {
        this.belief = belief;
        this.cMissPd = stMissPd;
        this.prediction = prediction;
        this.time = time;
    }    
    
    /**v
     *
     * @param belief
     * @param time
     */
    public TargetState(DMatrixRMaj belief, double time) {
        this.belief = belief;
        cMissPd = 0.0;
        prediction = false;
        this.time = time;
    }

    /**
     *
     * @param time
     */
    public TargetState(double time) {
        this.belief = null;
        cMissPd = 0.0;
        prediction = false;        
        this.time = time;
    }

    /**
     * @return the belief
     */
    public DMatrixRMaj getBelief() {
        return belief;
    }

    /**
     * @param belief the belief to set
     */
    public void setBelief(DMatrixRMaj belief) {
        this.belief = belief;
    }

    /**
     * @return the cMissPd
     */
    public double getcMissPd() {
        return cMissPd;
    }

    /**
     * @param cMissPd the cMissPd to set
     */
    public void setcMissPd(double cMissPd) {
        this.cMissPd = cMissPd;
    }

    /**
     * @return the prediction
     */
    public boolean isPrediction() {
        return prediction;
    }

    /**
     * @param prediction the prediction to set
     */
    public void setPrediction(boolean prediction) {
        this.prediction = prediction;
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

}
