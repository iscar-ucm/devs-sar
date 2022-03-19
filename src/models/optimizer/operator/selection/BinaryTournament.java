/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.optimizer.operator.selection;

import java.util.ArrayList;
import models.optimizer.Solution;

/**
 *
 * @author juanbordonruiz
 */
public class BinaryTournament extends Selection {

    public BinaryTournament() {
        super("binaryTournament");
    }

    @Override
    public Solution execute(ArrayList<Solution> solutions) {

        // the member to return
        Solution selectedMember;

        // select 2 randon members
        int randA = (int) Math.ceil((Math.random() * solutions.size()) - 1);
        int randB = (int) Math.ceil((Math.random() * solutions.size()) - 1);

        Solution solA = solutions.get(randA);
        Solution solB = solutions.get(randB);

        // dominance compare
        int flag = dominance.compare(solA, solB);
        if (flag < 0) {
            // solA dominates B
            selectedMember = solA;
        } else {
            // either B dominates A or there is no dominace
            selectedMember = solB;
        }

        return selectedMember;
    }
}
