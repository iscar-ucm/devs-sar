/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.optimizer.comparator;

import java.util.Comparator;
import models.optimizer.Solution;
import org.ejml.dense.row.CommonOps_DDRM;

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

        // sort by the sum of every constraint of each member
        double valueA = CommonOps_DDRM.elementSum(left.getConstraints());
        double valueB = CommonOps_DDRM.elementSum(right.getConstraints());

        // Constraints compare
        if (valueA < valueB) {
            end = true;
            compare = -1;
        } else if (valueB < valueA) {
            // B member has a better fitness so must be selected
            end = true;
            compare = 1;
        }

        int col = 0;        
        
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
