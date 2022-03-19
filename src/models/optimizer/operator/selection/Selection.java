/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.optimizer.operator.selection;

import java.util.ArrayList;
import models.optimizer.Solution;
import models.optimizer.comparator.SolutionDominance;

/**
 *
 * @author juanbordonruiz
 */
public abstract class Selection {

    protected final String type;
    protected SolutionDominance dominance;    
    
    public Selection(String type) {
        this.type = type;
        dominance = new SolutionDominance();        
    }    

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }  
    
    public abstract Solution execute(ArrayList<Solution> solutions);
}
