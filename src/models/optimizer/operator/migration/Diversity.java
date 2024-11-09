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
import models.optimizer.operator.assigner.NicheCount;

/**
 *
 * @author juanbordonruiz
 */
public class Diversity extends Migrate {

    private final int nx;
    private final String sortingMethod;

    public Diversity(int nx, String sortingMethod) {
        super("diversity");
        this.nx = nx;
        this.sortingMethod = sortingMethod;
    }

    @Override
    public ArrayList<Solution> execute(ArrayList<Solution> arg) {

        ArrayList<Solution> solutions = new ArrayList<>();
        solutions.addAll(arg);

        // the selected members to return
        ArrayList<Solution> selectedMembers = new ArrayList<>();

        // sort the solutions by sorthing method
        if (sortingMethod.startsWith("CROWDING_DISTANCE")) {
            CrowdingDistance assigner = new CrowdingDistance(solutions.get(0).getResults().size());
            assigner.execute(solutions);
            Collections.sort(solutions, new PropertyComparator(CrowdingDistance.propertyCrowdingDistance));
            for (int i = solutions.size() - 1; i > (solutions.size() - nx); --i) {
                selectedMembers.add(solutions.get(i).clone());
            }

        } else if (sortingMethod.startsWith("NICHE_COUNT")) {
            NicheCount assigner = new NicheCount(solutions.get(0).getResults().size());
            assigner.execute(solutions);
            Collections.sort(solutions, new PropertyComparator(NicheCount.propertyNicheCount));
            // Select diverse solutions
            for (int i = 0; i < nx; i++) {
                selectedMembers.add(solutions.get(i).clone());
            }
        }
        return selectedMembers;
    }
}
