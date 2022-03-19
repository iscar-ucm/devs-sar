package models.target.motionModels;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Juan
 */
public class Point {
   
    public static final int CENTER = 0;     
    public static final int NORTH = 1;     
    public static final int NORTHEAST = 2; 
    public static final int EAST = 3;     
    public static final int SOUTHEAST = 4;     
    public static final int SOUTH = 5; 
    public static final int SOUTHWEST = 6;
    public static final int WEST = 7;     
    public static final int NORTHWEST = 8;    
    
    // the xy cell of the given point
    private int xCell;
    private int yCell;
    // an array with the nine possible actions for each direction:
    // 0C, 1N, 2NE, 3E, 4SE, 5S, 6SW, 7W, 8NW     
    private double[] cardinalActions;

    /**
     *
     * @param pointJSON
     */
    public Point(JSONObject pointJSON) {
        xCell = (int) (long) pointJSON.get("xCell");
        yCell = (int) (long) pointJSON.get("yCell");
        // read actions
        double sum = 0.0;        
        JSONArray actionsArray = (JSONArray) pointJSON.get("actions");
        cardinalActions = new double[actionsArray.size()];
        for (int i = 0; i < actionsArray.size(); ++i) {
            cardinalActions[i] = (double) actionsArray.get(i);
            sum += cardinalActions[i];
        }
        // normalize actions to a probability of 1
        for (int i = 0; i < cardinalActions.length; ++i) {
            cardinalActions[i] = cardinalActions[i] / sum;
        }        
    }

    /**
     * @return the xCell
     */
    public int getxCell() {
        return xCell;
    }

    /**
     * @param xCell the xCell to set
     */
    public void setxCell(int xCell) {
        this.xCell = xCell;
    }

    /**
     * @return the yCell
     */
    public int getyCell() {
        return yCell;
    }

    /**
     * @param yCell the yCell to set
     */
    public void setyCell(int yCell) {
        this.yCell = yCell;
    }

    /**
     * @return the cardinalActions
     */
    public double[] getCardinalActions() {
        return cardinalActions;
    }

    /**
     * @param cardinalActions the cardinalActions to set
     */
    public void setCardinalActions(double[] cardinalActions) {
        this.cardinalActions = cardinalActions;
    }

}
