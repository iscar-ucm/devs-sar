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
 * @author juanbordonruiz
 */
public class Objectives {
    
    private OptimizationVarType[] constraints;
    private OptimizationVarType[] paretos;
    private double[] paretosFactors;
    
    public Objectives(JSONObject objectivesJSON) {
        // read constraints
        JSONArray consArray = (JSONArray) objectivesJSON.get("constraints");
        constraints = new OptimizationVarType[consArray.size()];
        for (int i = 0; i < consArray.size(); i++) {
            constraints[i] = OptimizationVarType.valueOf((String) consArray.get(i));
        }
        // read paretos
        JSONArray paretosArray = (JSONArray) objectivesJSON.get("paretos");
        paretos = new OptimizationVarType[paretosArray.size()];
        for (int i = 0; i < paretosArray.size(); i++) {
            paretos[i] = OptimizationVarType.valueOf((String) paretosArray.get(i));
        }
        // read paretos factor from configuration file 
        JSONObject configJSON = JSONLoader.getCntrlParam((String) objectivesJSON.get("factors"));
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
                case myo:
                    paretosFactors[i] = (double) configJSON.get("myo");
                    break;
                case smooth:
                    paretosFactors[i] = (double) configJSON.get("smooth");
                    break;                    
            }
        }
    }    

    /**
     * @return the constraints
     */
    public OptimizationVarType[] getConstraints() {
        return constraints;
    }

    /**
     * @return the paretos
     */
    public OptimizationVarType[] getParetos() {
        return paretos;
    }

    /**
     * @return the paretosFactors
     */
    public double[] getParetosFactors() {
        return paretosFactors;
    }
}
