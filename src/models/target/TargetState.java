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
    private boolean prediction; // flag to identify if this is a prediction state
    private double time;
    private double pd;        // detection probability
    private double etd;       // expected time detection
    private double heuristic; // myopia criteria    
    private double missPd;    // probability lost due to a motion model prediction    

    /**
     *
     * Used to clone a state.
     * 
     * @param belief
     * @param prediction
     * @param time
     * @param pd
     * @param etd
     * @param heuristic
     * @param missPd
     */
    public TargetState(DMatrixRMaj belief, boolean prediction, double time,
            double pd, double etd, double heuristic, double missPd) {
        this.belief = belief.copy();
        this.prediction = prediction;
        this.time = time;
        this.pd = pd;
        this.etd = etd;
        this.heuristic = heuristic;
        this.missPd = missPd;
    }

    /**
     *
     * Used to create new states as a result of a sensor action.
     * 
     * @param belief
     * @param time
     * @param pd
     * @param etd
     */
    public TargetState(DMatrixRMaj belief, double time, double pd, double etd) {
        this.belief = belief;
        prediction = false;
        this.time = time;
        this.pd = pd;
        this.etd = etd;
        heuristic = 0.0;
        missPd = 0.0;
    }    
    
    /**
     * Used to create predicted states.
     *
     * @param belief
     * @param time
     */
    public TargetState(DMatrixRMaj belief, double time, double missPd) {
        this.belief = belief;
        prediction = true;
        this.time = time;
        pd = 0.0;
        etd = 0.0;
        heuristic = 1.0;
        this.missPd = missPd;
    }

    /**
     * Used to create the initial scenario target state.
     * 
     * @param time
     */
    public TargetState(DMatrixRMaj belief, double time) {
        this.belief = belief;
        prediction = false;
        this.time = time;
        pd = 0.0;
        etd = 0.0;
        heuristic = 1.0;
        missPd = 0.0;
    }    
    
    /**
     *
     * @param time
     */
    public TargetState(double time) {
        this.belief = null;
        prediction = false;
        this.time = time;
        pd = 0.0;
        etd = 0.0;
        heuristic = 1.0;
        missPd = 0.0;
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

    /**
     * @return the pd
     */
    public double getPd() {
        return pd;
    }

    /**
     * @param pd the pd to set
     */
    public void setPd(double pd) {
        this.pd = pd;
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
     * @return the missPd
     */
    public double getMissPd() {
        return missPd;
    }

    /**
     * @param missPd
     */
    public void setMissPd(double missPd) {
        this.missPd = missPd;
    }

    @Override
    public TargetState clone() {
        return new TargetState(this.belief, this.prediction, this.time, this.pd,
                this.etd, this.heuristic, this.missPd);
    }
}
