/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.optimizer.operator.retrieve;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import models.optimizer.Solution;
import models.optimizer.comparator.SolutionDominance;

/**
 *
 * @author juanbordonruiz
 */
public abstract class Retrieve {

    protected final String type;
    protected SolutionDominance dominance;  
    protected HashSet<Integer> alreadyChosen;
    protected Random rnd;    
    
    public Retrieve(String type) {
        this.type = type;
        dominance = new SolutionDominance();
        alreadyChosen = new HashSet<>();
        rnd = new Random();        
    }    

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }  
    
    public abstract ArrayList<Solution> execute(
            ArrayList<Solution> arg, int numSol);
}
