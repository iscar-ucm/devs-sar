/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.optimizer.operator.retrieve;

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
public class FirstFront extends Retrieve {

    public FirstFront() {
        super("firstFront");
    }

    @Override
    public ArrayList<Solution> execute(
            ArrayList<Solution> arg, int numSol) {

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

        // sort first front
        CrowdingDistance assigner = new CrowdingDistance(firstFront.get(0).getObjectives().size());
        assigner.execute(firstFront);
        Collections.sort(firstFront, new PropertyComparator(CrowdingDistance.propertyCrowdingDistance));

        // select numSol members from first front population
        int i = firstFront.size() - 1;
        while (selectedMembers.size() < numSol) {
            while (selectedMembers.size() < numSol && i >= 0) {
                selectedMembers.add(firstFront.get(i).clone());
                i--;
            }
            i = firstFront.size() - 1;
        }

        return selectedMembers;
    }
}
