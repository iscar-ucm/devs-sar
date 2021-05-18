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
public class SearchArea {

    private Geographic geoPos; // SW corner of the search area
    private double width;
    private double height;
    private int xCells;
    private int yCells;
    private double xScale;
    private double yScale;
    private double areaBearing;

    /**
     *
     * @param zoneJSON
     */
    public SearchArea(JSONObject zoneJSON) {
        double latitude = (double) zoneJSON.get("latitude");
        double longitude = (double) zoneJSON.get("longitude");
        geoPos = new Geographic(latitude, longitude);
        width = (double) zoneJSON.get("xWidth");
        height = (double) zoneJSON.get("yHeight");
        xCells = (int) (long) zoneJSON.get("xCells");
        yCells = (int) (long) zoneJSON.get("yCells");
        xScale = width / xCells;
        yScale = height / yCells;
        areaBearing = (double) zoneJSON.get("areaBearing");
    }

    /**
     *
     * @param geoPos
     * @param width
     * @param height
     * @param xCells
     * @param yCells
     * @param areaBearing
     */
    public SearchArea(Geographic geoPos, double width, double height, int xCells, int yCells, double areaBearing) {
        this.geoPos = geoPos;
        this.width = width;
        this.height = height;
        this.xCells = xCells;
        this.yCells = yCells;
        xScale = width / xCells;
        yScale = height / yCells;
        this.areaBearing = areaBearing;
    }

    /**
     * @return the geoPos
     */
    public Geographic getGeoPos() {
        return geoPos;
    }

    /**
     * @param geoPos the SW corner of the search area
     */
    public void setGeoPos(Geographic geoPos) {
        this.geoPos = geoPos;
    }

    /**
     * @return the width
     */
    public double getWidth() {
        return width;
    }

    /**
     * @param width the width to set
     */
    public void setWidth(double width) {
        this.width = width;
    }

    /**
     * @return the height
     */
    public double getHeight() {
        return height;
    }

    /**
     * @param height the height to set
     */
    public void setHeight(double height) {
        this.height = height;
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
