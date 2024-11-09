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
    private double myo;       // myopia criteria    

    /**
     *
     * Used to clone a state.
     * 
     * @param belief
     * @param prediction
     * @param time
     * @param pd
     * @param etd
     * @param myo
     */
    public TargetState(DMatrixRMaj belief, boolean prediction, double time,
            double pd, double etd, double myo) {
        this.belief = belief.copy();
        this.prediction = prediction;
        this.time = time;
        this.pd = pd;
        this.etd = etd;
        this.myo = myo;
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
        myo = 1.0;
    }    
    
    /**
     * Used to create predicted states.
     *
     * @param belief
     * @param time
     */
    public TargetState(DMatrixRMaj belief, boolean prediction, double time) {
        this.belief = belief;
        this.prediction = prediction;
        this.time = time;
        pd = 0.0;
        etd = 0.0;
        myo = 1.0;
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
        myo = 1.0;
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
        myo = 1.0;
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
     * @return the myo
     */
    public double getMyo() {
        return myo;
    }

    /**
     * @param myo the myo to set
     */
    public void setMyo(double myo) {
        this.myo = myo;
    }

    @Override
    public TargetState clone() {
        return new TargetState(this.belief, this.prediction, this.time, this.pd,
                this.etd, this.myo);
    }
}
