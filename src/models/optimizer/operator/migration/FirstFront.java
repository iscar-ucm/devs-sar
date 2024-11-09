/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.optimizer.operator.migration;

import java.util.ArrayList;
import java.util.Collections;
import models.optimizer.Solution;
import models.optimizer.comparator.PropertyComparator;
import models.optimizer.operator.assigner.CrowdingDistance;
import models.optimizer.operator.assigner.FrontsExtractor;

/**
 *
 * @author juanbordonruiz
 */
public class FirstFront extends Migrate {

    public FirstFront() {
        super("firstFront");
    }

    @Override
    public ArrayList<Solution> execute(ArrayList<Solution> arg) {

        ArrayList<Solution> solutions = new ArrayList<>();
        solutions.addAll(arg);

        // the selected members to return
        ArrayList<Solution> selectedMembers = new ArrayList<>();

        // sort the solutions by fronts
        FrontsExtractor extractor = new FrontsExtractor(dominance);
        ArrayList<ArrayList<Solution>> fronts = extractor.execute(solutions);

        // create the first front population
        ArrayList<Solution> firstFront;
        firstFront = fronts.get(0);

        // Select first front solutions
        for (int i = 0; i < firstFront.size(); i++) {
            selectedMembers.add(firstFront.get(i).clone());
        }
        return selectedMembers;
    }
}
