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
public class Cartesian {

    static double deg2Meters = 111194.9266445587;

    /**
     * This method translates xyPos given an origin.
     * 
     * @param xyPos
     * @param origin
     * @return the result of the translation.
     */
    public static void translate(Cartesian xyPos, Cartesian origin) {
        xyPos.setX(xyPos.getX() - origin.getX());
        xyPos.setY(xyPos.getY() - origin.getY());
    }

    /**
     * This method applies a precise rotation to xyPos given an areaBearing.
     * 
     * @param xyPos
     * @param areaBearing
     */
    public static void preciseRotation(Cartesian xyPos, double areaBearing) {
        // apply area bearing (euler precise rotation)
        double newX;
        double newY;
        double theta = areaBearing * Math.PI / 180.0;
        double[] mTheta = {
            Math.cos(theta),
            Math.sin(theta),
            -Math.sin(theta),
            Math.cos(theta)
        };
        newX = mTheta[0] * xyPos.getX() + mTheta[1] * xyPos.getY();
        newY = mTheta[2] * xyPos.getX() + mTheta[2] * xyPos.getY();
        xyPos.setX(newX);
        xyPos.setY(newY);
    }

    /**
     * This method converts a Cartesian coordinate to its corresponding Geographic.
     * 
     * @param coordinate
     * @return
     */
    public static Geographic toGeographic(Cartesian coordinate) {
        // convert to latitude & longitude
        double lat = (coordinate.getY() / deg2Meters);
        double deg2MetersCorr = deg2Meters
                * Math.cos(coordinate.getY()
                        * Math.PI / 180.0);
        double lon = (coordinate.getX() / deg2MetersCorr);
        // create the new geographic
        Geographic geoCoordinate = new Geographic(lat, lon);
        return geoCoordinate;
    }

    private double x;
    private double y;

    /**
     *
     * @param posJSON a JSON object with the cartesian coordinate
     */
    public Cartesian(JSONObject posJSON) {
        x = (double) posJSON.get("x");
        y = (double) posJSON.get("y");
    }

    /**
     *
     * @param x axis value
     * @param y axis value
     */
    public Cartesian(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * @return the x axis value
     */
    public double getX() {
        return x;
    }

    /**
     * @param x axis value to set
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * @return the y axis value
     */
    public double getY() {
        return y;
    }

    /**
     * @param y axis value to set
     */
    public void setY(double y) {
        this.y = y;
    }

}
