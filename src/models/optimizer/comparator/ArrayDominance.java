/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.optimizer.comparator;

import java.util.ArrayList;
import java.util.Comparator;

/**
 *
 * @author juanbordonruiz
 */
public class ArrayDominance implements Comparator<ArrayList<Double>> {

  @Override
  public int compare(ArrayList<Double> left, ArrayList<Double> right) {
    int i, n;
    double diff;
    n = Math.min(left.size(), right.size());

    boolean bigger = false;
    boolean smaller = false;
    boolean indiff = false;
    for (i = 0; !(indiff) && i < n; i++) {
      diff = left.get(i) - right.get(i);
      if (diff > 0) {
        bigger = true;
      }
      if (diff <= 0) {
        smaller = true;
      }
      indiff = (bigger && smaller);
    }
    if (indiff) {
      return 0;
    } else if (smaller) {
      return -1;
    }
    return 1;
  }
}
