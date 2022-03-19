/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.optimizer.operator.assigner;

import java.util.ArrayList;
import java.util.Comparator;
import models.optimizer.Solution;

/**
 *
 * @author juanbordonruiz
 */
public class FrontsExtractor {

    protected Comparator<Solution> comparator;
    public static final String propertyN = "n";
    public static final String propertyRank = "rank";
    public static final String propertyIndexS = "indexS";

    public FrontsExtractor(Comparator<Solution> comparator) {
        this.comparator = comparator;
    }

    public ArrayList<ArrayList<Solution>> execute(ArrayList<Solution> arg) {
        ArrayList<Solution> solutions = new ArrayList<>();
        solutions.addAll(arg);

        ArrayList<ArrayList<Solution>> fronts = new ArrayList<>();
        ArrayList<Solution> rest = reduceToNonDominated(solutions);
        fronts.add(solutions);
        while (rest.size() > 0) {
            solutions = rest;
            rest = reduceToNonDominated(solutions);
            fronts.add(solutions);
        }
        int rank = 1;
        for (int i = 0; i < fronts.size(); ++i) {
            ArrayList<Solution> front = fronts.get(i);
            for (int j = 0; j < front.size(); ++j) {
                front.get(j).getProperties().put(propertyRank, rank);
            }
            rank++;
        }
        return fronts;
    }

    /**
     * Keep the input set of solutions non-dominated.Returns the set of dominate 
     * solutions.
     *
     * @param solutions the input set which is reduced.
     * @return The set of dominated solutions.
     */
    public ArrayList<Solution> reduceToNonDominated(
            ArrayList<Solution> solutions) {
        ArrayList<Solution> rest = new ArrayList<>();
        int compare;
        Solution solI;
        Solution solJ;
        for (int i = 0; i < solutions.size() - 1; i++) {
            solI = solutions.get(i);
            for (int j = i + 1; j < solutions.size(); j++) {
                solJ = solutions.get(j);
                compare = comparator.compare(solI, solJ);
                if (compare < 0) { // i dominates j
                    rest.add(solJ);
                    solutions.remove(j--);
                } else if (compare > 0) { // j dominates i
                    rest.add(solI);
                    solutions.remove(i--);
                    j = solutions.size();
                } else if (solI.equals(solJ)) { // both are equal, just one copy
                    solutions.remove(j--);
                }
            }
        }
        return rest;
    }
}
