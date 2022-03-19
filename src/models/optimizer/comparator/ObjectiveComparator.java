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
public class ObjectiveComparator implements Comparator<Solution> {

	protected int obj = 0;

	public ObjectiveComparator(int obj) {
		this.obj = obj;
	}

        @Override
	public int compare(Solution left, Solution right) {
		if (left.getObjectives().get(obj) < right.getObjectives().get(obj)) {
			return -1;
		} else if (left.getObjectives().get(obj) > right.getObjectives().get(obj)) {
			return 1;
		} else {
			return 0;
		}
	}

	public void setObj(int obj) {
		this.obj = obj;
	}
}
