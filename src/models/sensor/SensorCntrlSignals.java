/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.sensor;

import com.opencsv.bean.CsvBindByName;

/**
 *
 * @author jbbordon
 */
public class SensorCntrlSignals {

    @CsvBindByName(column = "cAzimuth", required = true)    
    private double cAzimuth;
    @CsvBindByName(column = "cElevation", required = true)      
    private double cElevation;
    @CsvBindByName(column = "time", required = true)      
    private double time;

    public SensorCntrlSignals() {
        this.cAzimuth = 0.0;
        this.cElevation = 0.0;
        this.time = 0.0;
    }

    /**
     *
     * @param cntrlSignals object to create a copy
     */    
    public SensorCntrlSignals(SensorCntrlSignals cntrlSignals) {
        this.cAzimuth = cntrlSignals.getcAzimuth();
        this.cElevation = cntrlSignals.getcElevation();
        this.time = cntrlSignals.getTime();
    }        
    
    /**
     *
     * @param azimuth
     * @param elevation
     * @param time of the cntrl signals
     */
    public SensorCntrlSignals(double azimuth, double elevation, double time) {
        this.cAzimuth = azimuth;
        this.cElevation = elevation;
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

}
