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
public class PropertyComparator implements Comparator<Solution> {

    protected String propertyName;

    public PropertyComparator(String propertyName) {
        this.propertyName = propertyName;
    }

    public int compare(Solution left, Solution right) {
        if (left.getProperties().get(propertyName).doubleValue() < right.getProperties().get(propertyName).doubleValue()) {
            return -1;
        } else if (left.getProperties().get(propertyName).doubleValue() > right.getProperties().get(propertyName).doubleValue()) {
            return 1;
        } else {
            return 0;
        }
    }
}
