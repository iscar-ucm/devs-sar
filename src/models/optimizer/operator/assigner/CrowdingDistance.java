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
public class CrowdingDistance {

    private final int numberOfObjectives;
    public static final String propertyCrowdingDistance = "crowdingDistance";

    public CrowdingDistance(int numberOfObjectives) {
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
            solutions.get(0).getProperties().put(propertyCrowdingDistance, Double.POSITIVE_INFINITY);
            return solutions;
        } // if

        if (size == 2) {
            solutions.get(0).getProperties().put(propertyCrowdingDistance, Double.POSITIVE_INFINITY);
            solutions.get(1).getProperties().put(propertyCrowdingDistance, Double.POSITIVE_INFINITY);
            return solutions;
        } // if

        for (int i = 0; i < size; ++i) {
            solutions.get(i).getProperties().put(propertyCrowdingDistance, 0.0);
        }

        double objetiveMaxn;
        double objetiveMinn;
        double distance = 0.0;

        for (int i = 0; i < numberOfObjectives; ++i) {
            // Sort the population by objective i
            Collections.sort(solutions, new ObjectiveComparator(i));
            objetiveMinn = solutions.get(0).getResults().get(i);
            objetiveMaxn = solutions.get(size - 1).getResults().get(i);
            // check not extremes are equal, this means all values are the same for the objective
            if (!(objetiveMaxn - objetiveMinn == 0)) {
                //Set the crowding distance
                solutions.get(0).getProperties().put(propertyCrowdingDistance, Double.POSITIVE_INFINITY);
                solutions.get(size - 1).getProperties().put(propertyCrowdingDistance, Double.POSITIVE_INFINITY);

                for (int j = 1; j < size - 1; j++) {
                    distance = solutions.get(j + 1).getResults().get(i) - solutions.get(j - 1).getResults().get(i);
                    distance = distance / (objetiveMaxn - objetiveMinn);
                    distance += solutions.get(j).getProperties().get(propertyCrowdingDistance).doubleValue();
                    solutions.get(j).getProperties().put(propertyCrowdingDistance, distance);
                } // for
            }
        } // for

        return solutions;
    }
}
