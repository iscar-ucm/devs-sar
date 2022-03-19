/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.optimizer;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import utils.JSONLoader;

/**
 *
 * @author jbbordon
 */
public class CntrlParams {

    private int numOfRuns;
    private double sequenceTime;
    private int stopIteration;
    private double stopTime;
    private double stopPD;
    private int exchangeIteration;
    private String sortingMethod;    
    private OptimizationVarType[] constraints;
    private OptimizationVarType[] paretos;
    private double[] paretosFactors;
    private boolean logIterations;

    /**
     *
     * @param controlParamJSON
     */
    public CntrlParams(JSONObject controlParamJSON) {
        numOfRuns = (int) (long) controlParamJSON.get("numOfRuns");
        sequenceTime = (double) controlParamJSON.get("subsequence");        
        // read stop criteria
        JSONObject stopCriteria = (JSONObject) controlParamJSON.get("stopCriteria");
        stopIteration = (int) (long) stopCriteria.get("iterations");
        stopTime = (double) stopCriteria.get("time");
        stopPD = (double) stopCriteria.get("pd");
        // read exchangeIteration
        exchangeIteration = (int) (long) controlParamJSON.get("exchangeIteration");
        // read sorting method
        sortingMethod = (String) controlParamJSON.get("sortingMethod");
        // read constraints
        JSONArray consArray = (JSONArray) controlParamJSON.get("constraints");
        constraints = new OptimizationVarType[consArray.size()];
        for (int i = 0; i < consArray.size(); i++) {
            constraints[i] = OptimizationVarType.valueOf((String) consArray.get(i));
        }
        // read paretos
        JSONArray paretosArray = (JSONArray) controlParamJSON.get("paretos");
        paretos = new OptimizationVarType[paretosArray.size()];
        for (int i = 0; i < paretosArray.size(); i++) {
            paretos[i] = OptimizationVarType.valueOf((String) paretosArray.get(i));
        }
        // read paretos factor from configuration file 
        JSONObject configJSON = JSONLoader.getCntrlParam();
        paretosFactors = new double[paretos.length];
        for (int i = 0; i < paretos.length; i++) {
            switch (paretos[i]) {
                case etd:
                    paretosFactors[i] = (double) configJSON.get("etd");
                    break;
                case pd:
                    paretosFactors[i] = (double) configJSON.get("pd");
                    break;
                case fuel:
                    paretosFactors[i] = (double) configJSON.get("fuel");
                    break;                   
                case heurist:
                    paretosFactors[i] = (double) configJSON.get("heurist");
                    break;
                case smooth:
                    paretosFactors[i] = (double) configJSON.get("smooth");
                    break;                    
            }
        }
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
     * @return the sequenceTime
     */
    public double getSequenceTime() {
        return sequenceTime;
    }

    /**
     * @param sequenceTime the sequenceTime to set
     */
    public void setSequenceTime(double sequenceTime) {
        this.sequenceTime = sequenceTime;
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
     * @return the exchangeIteration
     */
    public int getExchangeIteration() {
        return exchangeIteration;
    }

    /**
     * @param exchangeIteration the exchangeIteration to set
     */
    public void setExchangeIteration(int exchangeIteration) {
        this.exchangeIteration = exchangeIteration;
    }

    /**
     * @return the sortingMethod
     */
    public String getSortingMethod() {
        return sortingMethod;
    }

    /**
     * @param sortingMethod the sortingMethod to set
     */
    public void setSortingMethod(String sortingMethod) {
        this.sortingMethod = sortingMethod;
    }

    /**
     * @return the constraints
     */
    public OptimizationVarType[] getConstraints() {
        return constraints;
    }

    /**
     * @param constraints the constraints to set
     */
    public void setConstraints(OptimizationVarType[] constraints) {
        this.constraints = constraints;
    }

    /**
     * @return the paretos
     */
    public OptimizationVarType[] getParetos() {
        return paretos;
    }

    /**
     * @param paretos the paretos to set
     */
    public void setParetos(OptimizationVarType[] paretos) {
        this.paretos = paretos;
    }

    /**
     * @return the paretosFactors
     */
    public double[] getParetosFactors() {
        return paretosFactors;
    }

    /**
     * @param paretosFactors the paretosFactors to set
     */
    public void setParetosFactors(double[] paretosFactors) {
        this.paretosFactors = paretosFactors;
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
