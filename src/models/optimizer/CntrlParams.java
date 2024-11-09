/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.optimizer;

import org.json.simple.JSONObject;

/**
 *
 * @author jbbordon
 */
public class CntrlParams {

    private int numOfRuns;
    private int stopIteration;
    private double stopTime;
    private double stopPD;
    private boolean logIterations;

    /**
     *
     * @param controlParamJSON
     */
    public CntrlParams(JSONObject controlParamJSON) {
        numOfRuns = (int) (long) controlParamJSON.get("numOfRuns");      
        // read stop criteria
        JSONObject stopCriteria = (JSONObject) controlParamJSON.get("stopCriteria");
        stopIteration = (int) (long) stopCriteria.get("iterations");
        stopTime = (double) stopCriteria.get("time");
        stopPD = (double) stopCriteria.get("pd");
        // read logIterations, this is only needed for debuggin
        logIterations = (boolean) controlParamJSON.get("logIterations");  
    }

    /**
     * @return the numOfRuns
     */
    public int getNumOfRuns() {
        return numOfRuns;
    }

    /**
     * @param numOfRuns the numOfRuns to set
     */
    public void setNumOfRuns(int numOfRuns) {
        this.numOfRuns = numOfRuns;
    }

    /**
     * @return the stopIteration
     */
    public int getStopIteration() {
        return stopIteration;
    }

    /**
     * @param stopIteration the stopIteration to set
     */
    public void setStopIteration(int stopIteration) {
        this.stopIteration = stopIteration;
    }

    /**
     * @return the stopTime
     */
    public double getStopTime() {
        return stopTime;
    }

    /**
     * @param stopTime the stopTime to set
     */
    public void setStopTime(double stopTime) {
        this.stopTime = stopTime;
    }

    /**
     * @return the stopPD
     */
    public double getStopPD() {
        return stopPD;
    }

    /**
     * @param stopPD the stopPD to set
     */
    public void setStopPD(double stopPD) {
        this.stopPD = stopPD;
    }

    /**
     * @return the logIterations
     */
    public boolean isLogIterations() {
        return logIterations;
    }

    /**
     * @param logIterations the logIterations to set
     */
    public void setLogIterations(boolean logIterations) {
        this.logIterations = logIterations;
    }
}
