/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.optimizer.operator.migration;

import java.util.ArrayList;
import models.optimizer.Solution;

/**
 *
 * @author juanbordonruiz
 */
public class RandomRetrieve extends Migrate {

    private final int nx;    
    
    public RandomRetrieve(int nx) {
        super("random");
        this.nx = nx;        
    }

    @Override
    public ArrayList<Solution> execute(ArrayList<Solution> solutions) {

        // the selected members to return
        ArrayList<Solution> selectedMembers = new ArrayList<>();

        // Select nx random solutions
        int k;
        while (selectedMembers.size() < nx) {
            k = rnd.nextInt(solutions.size());
            if (!alreadyChosen.contains(k)) {
                selectedMembers.add(solutions.get(k).clone());
            }
        }
        alreadyChosen.clear();

        return selectedMembers;
    }
}
