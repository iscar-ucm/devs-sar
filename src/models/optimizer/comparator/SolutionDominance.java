/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.optimizer.comparator;

import java.util.Comparator;
import models.optimizer.Solution;
import org.ejml.data.BMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;

/**
 *
 * @author juanbordonruiz
 */
public class SolutionDominance implements Comparator<Solution> {

    @Override
    public int compare(Solution s1, Solution s2) {

        // compute the sum of every constraint of each member
        double valueA = CommonOps_DDRM.elementSum(s1.getConstraints());
        double valueB = CommonOps_DDRM.elementSum(s2.getConstraints());

        // Constraints compare
        if (valueA < valueB) {
            return -1;
        } else if (valueB < valueA) {
            // B member has a better fitness so must be selected
            return 1;
        } else {
            // Paretos compare
            BMatrixRMaj dominationMatrix
                    = new BMatrixRMaj(1, s1.getParetos().getNumCols());

            /**
             * ************************ A DOMINATES B ************************
             */            
            
            // check 1st condition (all paretosA are less or equal to paretosB)
            CommonOps_DDRM.elementLessThanOrEqual(
                    s1.getParetos(), s2.getParetos(), dominationMatrix);

            if (dominationMatrix.sum() == s1.getParetos().getNumCols()) {

                // check 2nd condition (at least one paretosA is < than paretosB)
                CommonOps_DDRM.elementLessThan(
                        s1.getParetos(), s2.getParetos(), dominationMatrix);
                // check all elements
                if (dominationMatrix.sum() > 0) {
                    // all conditions are met
                    return -1;
                } else {
                    return 0;
                }
            } else {
            /**
             * ************************ B DOMINATES A ************************
             */
            
                // check 1st condition (all paretosB are less or equal to paretosA)
                CommonOps_DDRM.elementLessThanOrEqual(
                        s2.getParetos(), s1.getParetos(), dominationMatrix);
                if (dominationMatrix.sum() == s2.getParetos().getNumCols()) {

                    // 2nd condition (at least one paretosA is < than paretosB)
                    CommonOps_DDRM.elementLessThan(
                            s2.getParetos(), s1.getParetos(), dominationMatrix);
                    // check all elements
                    if (dominationMatrix.sum() > 0) {
                        // all conditions are met
                        return 1;
                    } else {
                        return 0;
                    }
                } else {
                    return 0;
                }
            }
        }
    }
}
