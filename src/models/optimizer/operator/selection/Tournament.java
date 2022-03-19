/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.optimizer.operator.selection;

import java.util.ArrayList;
import java.util.Collections;
import models.optimizer.Solution;
import models.optimizer.comparator.SolutionDominance;

/**
 *
 * @author juanbordonruiz
 */
public class Tournament extends Selection {

    private final int tournamentSize;

    public Tournament(int tournamentSize) {
        super("tournament");
        this.tournamentSize = tournamentSize;
    }

    @Override
    public Solution execute(ArrayList<Solution> solutions) {

        // the selected member to return
        Solution selectedMember;

        // var to hold the tournament members
        ArrayList<Solution> tournamentSet = new ArrayList<>();

        // create the set
        for (int j = 0; j < tournamentSize; ++j) {
            tournamentSet.add(solutions.get((int) Math.ceil((Math.random() * solutions.size()) - 1)));
        }

        // sort the set by dominance
        SolutionDominance comparator = new SolutionDominance();
        Collections.sort(tournamentSet, comparator);

        // add it to selectedMembers
        selectedMember = tournamentSet.get(0);

        return selectedMember;
    }
}
