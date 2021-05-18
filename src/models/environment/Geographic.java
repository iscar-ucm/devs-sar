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
public class Geographic {

    static double deg2Meters = 111194.9266445587;

    /**
     * This method translates geoPos given an origin.
     * 
     * @param geoPos
     * @param origin
     * @return the result of the translation.
     */
    public static Geographic translate(Geographic geoPos, Geographic origin) {
        double lat = geoPos.getLatitude() - origin.getLatitude();
        double lon = geoPos.getLongitude() - origin.getLongitude();
        return new Geographic(lat, lon);
    }

    /**
     * This method converts a Geographic coordinate to its corresponding Cartesian.
     * 
     * @param uavPos the geographic coordinate to convert.
     * @param searchAreaPos the origin of the searchArea.
     * @param areaBearing the bearing of the searchArea.
     * @return the new Cartesian coordinate.
     */
    public static Cartesian toCartesian(
            Geographic uavPos,
            Geographic searchAreaPos,
            double areaBearing) {
        // translate uavPos with searchAreaPos as axis origin
        Geographic coordinate = translate(uavPos, searchAreaPos);
        // convert to xy coordinates
        double y = coordinate.getLatitude() * deg2Meters;
        double x = coordinate.getLongitude()
                * Math.cos(coordinate.getLatitude() * Math.PI / 180.0)
                * deg2Meters;
        Cartesian xyCoordinate = new Cartesian(x, y);
        // apply areaBearing as an euler precise rotation
        // preciseRotation(xyCoordinate, areaBearing);
        return xyCoordinate;
    }

    private double latitude; // decimal degrees
    private double longitude; // decimal degrees

    /**
     *
     * @param posJSON a JSON object with the geographic coordinate
     */
    public Geographic(JSONObject posJSON) {
        latitude = (double) posJSON.get("latitude");
        longitude = (double) posJSON.get("longitude");
    }

    /**
     *
     * @param latitude value
     * @param longitude value
     */
    public Geographic(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * @return the latitude value
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * @param latitude value to set
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * @return the longitude value
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * @param longitude value to set
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

}
