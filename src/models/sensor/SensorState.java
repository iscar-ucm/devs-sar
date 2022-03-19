/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.sensor;

/**
 *
 * @author jbbordon
 */
public class SensorState {

    private double azimuth;
    private double elevation;
    private double time;
    // sensor cntrl variables that have to be stored for sequence optimizations
    private double cAzimuth;   // radians
    private double cElevation; // radians   

    /**
     * @param time
     */
    public SensorState(double time) {
        this.time = time;
    }

    /**
     * @param azimuth
     * @param elevation
     * @param time
     */
    public SensorState(double azimuth, double elevation, double time) {
        this.azimuth = azimuth;
        this.elevation = elevation;
        this.time = time;
        cAzimuth = Math.PI * azimuth / 180.0;
        cElevation = Math.PI * elevation / 180.0;
    }

    /**
     * @param azimuth
     * @param elevation
     * @param time
     * @param cAzimuth
     * @param cElevation
     */
    public SensorState(double azimuth, double elevation, double time,
            double cAzimuth, double cElevation) {
        this.azimuth = azimuth;
        this.elevation = elevation;
        this.time = time;
        this.cAzimuth = cAzimuth;
        this.cElevation = cElevation;
    }

    /**
     * @return the azimuth
     */
    public double getAzimuth() {
        return azimuth;
    }

    /**
     * @param azimuth the azimuth to set
     */
    public void setAzimuth(double azimuth) {
        this.azimuth = azimuth;
    }

    /**
     * @return the elevation
     */
    public double getElevation() {
        return elevation;
    }

    /**
     * @param elevation the elevation to set
     */
    public void setElevation(double elevation) {
        this.elevation = elevation;
    }

    /**
     * @return the time
     */
    public double getTime() {
        return time;
    }

    /**
     * @param time the time to set
     */
    public void setTime(double time) {
        this.time = time;
    }

    /**
     * @return the cAzimuth
     */
    public double getcAzimuth() {
        return cAzimuth;
    }

    /**
     * @param cAzimuth the cAzimuth to set
     */
    public void setcAzimuth(double cAzimuth) {
        this.cAzimuth = cAzimuth;
    }

    /**
     * @return the cElevation
     */
    public double getcElevation() {
        return cElevation;
    }

    /**
     * @param cElevation the cElevation to set
     */
    public void setcElevation(double cElevation) {
        this.cElevation = cElevation;
    }

    @Override
    public SensorState clone() {
        return new SensorState(
                this.azimuth,
                this.elevation,
                this.time,
                this.cAzimuth,
                this.cElevation);
    }
}
