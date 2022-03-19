/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.optimizer.operator.retrieve;

import java.util.ArrayList;
import models.optimizer.Solution;

/**
 *
 * @author juanbordonruiz
 */
public class RandomRetrieve extends Retrieve {

    public RandomRetrieve() {
        super("random");
    }

    @Override
    public ArrayList<Solution> execute(
            ArrayList<Solution> solutions, int numSol) {

        // the selected members to return
        ArrayList<Solution> selectedMembers = new ArrayList<>();

        // select randomly numSol members
        int k;
        while (selectedMembers.size() < numSol) {
            k = rnd.nextInt(solutions.size());
            if (!alreadyChosen.contains(k)) {
                selectedMembers.add(solutions.get(k).clone());
            }
        }
        alreadyChosen.clear();

        return selectedMembers;
    }
}
