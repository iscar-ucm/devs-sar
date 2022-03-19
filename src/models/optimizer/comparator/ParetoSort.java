/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.optimizer.comparator;

import java.util.Comparator;
import models.optimizer.Solution;

/**
 *
 * @author juanbordonruiz
 */
public class ParetoSort implements Comparator<Solution> {

    public ParetoSort() {
    }

    @Override
    public int compare(Solution left, Solution right) {
        int compare = 0;
        boolean end = false;
        int col = 0;

        // sort by constraint order
        while (!end && col < left.getConstraints().getNumCols()) {
            if (left.getConstraints().get(0, col) < right.getConstraints().get(0, col)) {
                end = true;
                compare = -1;
            } else if ((left.getConstraints().get(0, col) > right.getConstraints().get(0, col))) {
                end = true;
                compare = 1;
            } else {
                col++;
            }
        }

        if (!end) {
            // 2nd lever order, sort by paretos
            col = 0;
            while (!end && col < left.getParetos().getNumCols()) {
                if (left.getParetos().get(0, col) < right.getParetos().get(0, col)) {
                    end = true;
                    compare = -1;
                } else if ((left.getParetos().get(0, col) > right.getParetos().get(0, col))) {
                    end = true;
                    compare = 1;
                } else {
                    col++;
                }
            }
        }

        // return compare result
        return compare;
    }
}
