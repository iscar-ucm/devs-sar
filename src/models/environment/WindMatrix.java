/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.environment;

import models.uav.UavState;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author Juan
 */
public class WindMatrix {

    private int xCells;
    private int yCells;
    private double xScale;
    private double yScale;
    private Wind[][] winds;

    /**
     *
     * @param windJSON
     */
    public WindMatrix(JSONObject windJSON) {
        xCells = (int) (long) windJSON.get("xCells");
        yCells = (int) (long) windJSON.get("yCells");
        xScale = (double) windJSON.get("xScale");
        yScale = (double) windJSON.get("yScale");
        // read constraints
        JSONArray filesArray = (JSONArray) windJSON.get("winds");
        JSONArray columnsArray = (JSONArray) filesArray.get(0);
        winds = new Wind[filesArray.size()][columnsArray.size()];
        for (int i = 0; i < filesArray.size(); i++) {
            columnsArray = (JSONArray) filesArray.get(i);
            for (int j = 0; j < columnsArray.size(); j++) {
                Wind wind = new Wind((JSONObject) columnsArray.get(j));
                winds[i][j] = wind;
            }
        }

    }

    /**
     * @return the xCells
     */
    public int getxCells() {
        return xCells;
    }

    /**
     * @param xCells the xCells to set
     */
    public void setxCells(int xCells) {
        this.xCells = xCells;
    }

    /**
     * @return the yCells
     */
    public int getyCells() {
        return yCells;
    }

    /**
     * @param yCells the yCells to set
     */
    public void setyCells(int yCells) {
        this.yCells = yCells;
    }

    /**
     * @return the xScale
     */
    public double getxScale() {
        return xScale;
    }

    /**
     * @param xScale the xScale to set
     */
    public void setxScale(double xScale) {
        this.xScale = xScale;
    }

    /**
     * @return the yScale
     */
    public double getyScale() {
        return yScale;
    }

    /**
     * @param yScale the yScale to set
     */
    public void setyScale(double yScale) {
        this.yScale = yScale;
    }

    /**
     * @return the winds
     */
    public Wind[][] getWinds() {
        return winds;
    }

    /**
     * @param winds the winds to set
     */
    public void setWinds(Wind[][] winds) {
        this.winds = winds;
    }

    /**
     * @param state uav current state used to locate it in the matrix
     * @return the wind that applies to the uav state
     */
    public Wind getUavWind(UavState state) {
        // locate uav in the matrix and return the corresponding wind
        Wind uavWind;
        int uavXCell
                = (int) Math.floor(state.getX() / getxScale());
        int uavYCell
                = (int) Math.floor(state.getY() / getyScale());
        if (uavXCell >= 0 && uavXCell < xCells
                && uavYCell >= 0 && uavYCell < yCells) {
            uavWind = winds[uavYCell][uavXCell];
        } else {
            uavWind = new Wind();
        }
        return uavWind;
    }

}
