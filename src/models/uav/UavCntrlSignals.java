/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.uav;

import com.opencsv.bean.CsvBindByName;

/**
 *
 * @author jbbordon
 */
public class UavCntrlSignals {

    @CsvBindByName(column = "cSpeed", required = true)
    private double cSpeed;
    @CsvBindByName(column = "cHeading", required = true)    
    private double cHeading; // radians
    @CsvBindByName(column = "cElevation", required = true)     
    private double cElevation;
    @CsvBindByName(column = "time", required = true)      
    private double time;
    private double smooth;    

    public UavCntrlSignals() {
        cSpeed = 0.0;
        cHeading = 0.0;
        cElevation = 0.0;
        time = 0.0;
        smooth = 0.0;
    }

    /**
     * @param speed
     * @param heading
     * @param elevation
     * @param time
     */
    public UavCntrlSignals(double speed, double heading, double elevation, double time) {
        this.cSpeed = speed;
        this.cHeading = heading;
        this.cElevation = elevation;
        this.time = time;
        smooth = 0.0;
    }
    
    /**
     * @param speed
     * @param heading
     * @param elevation
     * @param time
     */
    public UavCntrlSignals(double speed, double heading, double elevation, double time, double smooth) {
        this.cSpeed = speed;
        this.cHeading = heading;
        this.cElevation = elevation;
        this.time = time;
        this.smooth = smooth;
    }    

    /**
     * @return the cSpeed
     */
    public double getcSpeed() {
        return cSpeed;
    }

    /**
     * @param cSpeed the speed to set
     */
    public void setcSpeed(double cSpeed) {
        this.cSpeed = cSpeed;
    }

    /**
     * @return the cHeading
     */
    public double getcHeading() {
        return cHeading;
    }

    /**
     * @param cHeading the heading to set
     */
    public void setcHeading(double cHeading) {
        this.cHeading = cHeading;
    }

    /**
     * @return the cElevation
     */
    public double getcElevation() {
        return cElevation;
    }

    /**
     * @param cElevation the elevation to set
     */
    public void setcElevation(double cElevation) {
        this.cElevation = cElevation;
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
     * @return the smooth
     */
    public double getSmooth() {
        return smooth;
    }

    /**
     * @param smooth the smooth to set
     */
    public void setSmooth(double smooth) {
        this.smooth = smooth;
    }

    @Override
    public UavCntrlSignals clone() {
        return new UavCntrlSignals(
                this.cSpeed,
                this.cHeading,
                this.cElevation,
                this.time, this.getSmooth());
    }    
    
}
