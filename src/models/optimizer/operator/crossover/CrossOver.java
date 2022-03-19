/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.optimizer.operator.crossover;

import java.util.ArrayList;
import models.optimizer.Solution;

/**
 *
 * @author juanbordonruiz
 */
public abstract class CrossOver {
    
    private final String type;
    private final double crossOverF;

    public CrossOver(String type, double crossOverF) {
        this.type = type;
        this.crossOverF = crossOverF;
    }    

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @return the crossOverF
     */
    public double getCrossOverF() {
        return crossOverF;
    }
    
    public abstract ArrayList<Solution> execute(Solution parentA, Solution parentB);
}
