/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.optimizer.operator.assigner;

import models.optimizer.comparator.ObjectiveComparator;
import java.util.ArrayList;
import java.util.Collections;
import models.optimizer.Solution;

/**
 *
 * @author juanbordonruiz
 */
public class NicheCount {

    protected int numberOfObjectives;
    public static final String propertyNicheCount = "nicheCount";

    public NicheCount(int numberOfObjectives) {
        this.numberOfObjectives = numberOfObjectives;
    }

    public ArrayList<Solution> execute(ArrayList<Solution> arg) {
        ArrayList<Solution> solutions = new ArrayList<>();
        solutions.addAll(arg);

        int size = solutions.size();
        if (size == 0) {
            return solutions;
        }

        if (size == 1) {
            solutions.get(0).getProperties().put(propertyNicheCount, 0);
            return solutions;
        } // if

        for (int i = 0; i < size; ++i) {
            solutions.get(i).getProperties().put(propertyNicheCount, 0);
        }

        double maxObjective;
        double minObjective;
        // TODO: Investigate how to compute sigmaShare in 3-objectives and more.
        double sigmaShare = 0.0;

        for (int i = 0; i < numberOfObjectives; ++i) {
            // Sort the population by objective i
            Collections.sort(solutions, new ObjectiveComparator(i));
            minObjective = solutions.get(0).getObjectives().get(i);
            maxObjective = solutions.get(size - 1).getObjectives().get(i);
            sigmaShare += (maxObjective - minObjective);
        }
        sigmaShare /= (size - 1);

        double distanceIJ;
        for (int i = 0; i < size; ++i) {
            Solution solI = solutions.get(i);
            for (int j = 0; j < size; ++j) {
                if (i == j) {
                    continue;
                }
                Solution solJ = solutions.get(j);
                distanceIJ = 0;
                for (int m = 0; m < numberOfObjectives; ++m) {
                    distanceIJ += Math.pow(solI.getObjectives().get(m) - solJ.getObjectives().get(m), 2);
                }
                distanceIJ = Math.sqrt(distanceIJ);
                if (distanceIJ < sigmaShare) {
                    solI.getProperties().put(propertyNicheCount, solI.getProperties().get(propertyNicheCount).intValue() + 1);
                }
            }
        }

        return solutions;
    }
}
