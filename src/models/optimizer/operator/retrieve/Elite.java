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
public class Elite extends Retrieve {

    private final int eliteSize;

    public Elite(int eliteSize) {
        super("elite");
        this.eliteSize = eliteSize;
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

        // create the elite population
        ArrayList<Solution> elitePop = new ArrayList<>();
        ArrayList<Solution> front;
        int i = 0;
        while (elitePop.size() < eliteSize && i < fronts.size()) {
            front = fronts.get(i);
            if ((front.size() + elitePop.size()) > eliteSize) {
                // add members of the current front until max size is reached
                CrowdingDistance assigner = new CrowdingDistance(front.get(0).getObjectives().size());
                assigner.execute(front);
                Collections.sort(front, new PropertyComparator(CrowdingDistance.propertyCrowdingDistance));
                for (int j = front.size() - 1; elitePop.size() < eliteSize; --j) {
                    elitePop.add(front.get(j));
                }
            } else {
                // add all the members of the current front
                elitePop.addAll(front);
            }
            i++;
        }

        // select numSol members from elite population
        if (numSol >= eliteSize) {
            for (int j = 0; j < elitePop.size(); j++) {
                selectedMembers.add(elitePop.get(j).clone());
            }
        } else {
            int k;
            while (selectedMembers.size() < numSol) {
                k = rnd.nextInt(eliteSize);
                if (!alreadyChosen.contains(k)) {
                    selectedMembers.add(elitePop.get(k).clone());
                }
            }
            alreadyChosen.clear();
        }

        return selectedMembers;
    }
}
