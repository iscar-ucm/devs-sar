/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.environment;

import org.json.simple.JSONObject;

/**
 *
 * @author jbbordon
 */
public class Zone {

    private Geographic origin;
    private double xWidth;
    private double yHeight;
    private int xCells;
    private int yCells;
    private double xScale;
    private double yScale;
    private double areaBearing;

    /**
     *
     * @param zoneJSON
     */
    public Zone(JSONObject zoneJSON) {
        double latitude = (double) zoneJSON.get("latitude");
        double longitude = (double) zoneJSON.get("longitude");
        origin = new Geographic(latitude, longitude);
        xWidth = (double) zoneJSON.get("xWidth");
        yHeight = (double) zoneJSON.get("yHeight");
        xCells = (int) (long) zoneJSON.get("xCells");
        yCells = (int) (long) zoneJSON.get("yCells");
        xScale = xWidth / xCells;
        yScale = yHeight / yCells;
        areaBearing = (double) zoneJSON.get("areaBearing");
    }

    /**
     * @return the origin
     */
    public Geographic getOrigin() {
        return origin;
    }

    /**
     * @param origin the SW corner of the search area
     */
    public void setOrigin(Geographic origin) {
        this.origin = origin;
    }

    /**
     * @return the xWidth
     */
    public double getxWidth() {
        return xWidth;
    }

    /**
     * @param xWidth the xWidth to set
     */
    public void setxWidth(double xWidth) {
        this.xWidth = xWidth;
    }

    /**
     * @return the yHeight
     */
    public double getyHeight() {
        return yHeight;
    }

    /**
     * @param yHeight the yHeight to set
     */
    public void setyHeight(double yHeight) {
        this.yHeight = yHeight;
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
    public void setxCells(short xCells) {
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
    public void setyCells(short yCells) {
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
     * @return the areaBearing
     */
    public double getAreaBearing() {
        return areaBearing;
    }

    /**
     * @param areaBearing the areaBearing to set
     */
    public void setAreaBearing(double areaBearing) {
        this.areaBearing = areaBearing;
    }

}
