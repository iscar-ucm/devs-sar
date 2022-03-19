/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.optimizer;

import org.json.simple.JSONObject;

/**
 *
 * @author Juan
 */
public class DecisionVar {

    private DecisionVarName name;
    private DecisionVarType type;
    private double minValue;
    private double maxValue;
    private double range;

    /**
     *
     * @param decisionJSON
     */
    public DecisionVar(JSONObject decisionJSON) {
        name = DecisionVarName.valueOf((String) decisionJSON.get("name"));
        type = DecisionVarType.valueOf((String) decisionJSON.get("type"));
        minValue = (double) decisionJSON.get("min");
        maxValue = (double) decisionJSON.get("max");
        if (minValue < 0 && maxValue < 0) {
            range = minValue - maxValue;
        } else if (minValue > 0 && maxValue > 0)  {
            range = maxValue - minValue;
        } else {
            range = Math.abs(minValue) + Math.abs(maxValue);
        }
    }

    /**
     * @return the name
     */
    public DecisionVarName getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(DecisionVarName name) {
        this.name = name;
    }

    /**
     * @return the type
     */
    public DecisionVarType getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(DecisionVarType type) {
        this.type = type;
    }

    /**
     * @return the minValue
     */
    public double getMinValue() {
        return minValue;
    }

    /**
     * @param minValue the minValue to set
     */
    public void setMinValue(double minValue) {
        this.minValue = minValue;
    }

    /**
     * @return the maxValue
     */
    public double getMaxValue() {
        return maxValue;
    }

    /**
     * @param maxValue the maxValue to set
     */
    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }

    /**
     * @return the range
     */
    public double getRange() {
        return range;
    }

}
