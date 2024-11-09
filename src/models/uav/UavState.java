/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.uav;

import models.environment.Geographic;
import models.environment.Cartesian;

/**
 *
 * @author jbbordon
 */
public class UavState {

    // uav state variables
    private Cartesian xyPos;
    private Geographic geoPos;
    private double height;
    private double heading;
    private double airSpeed;
    private double fuel;
    private double time;
    private int col; // terrain & uavs colisions
    private int nfzs; // non flying zones overfly 
    // uav mm variables that have to be stored for sequence optimizations
    private double xVa;
    private double xHeading;
    private double xHeight;
    private double xPIDi;
    private double xPIDd;
    // uav cntrl variables that have to be stored for sequence optimizations
    private double cSpeed;
    private double cHeading; // radians
    private double cElevation;

    /**
     *
     * @param x
     * @param y
     * @param height
     * @param heading
     * @param airSpeed
     * @param time
     * @param fuel
     */
    public UavState(double x, double y, double height, double heading, double airSpeed, double fuel, double time) {
        this.xyPos = new Cartesian(x, y);
        this.height = height;
        this.heading = heading;
        this.airSpeed = airSpeed;
        this.fuel = fuel;        
        this.time = time;
        col = 0;
        nfzs = 0;   
        xVa = 0.0;
        xHeading = 0.0;
        xHeight = 0.0;
        xPIDi = 0.0;
        xPIDd = 0.0;
        cSpeed = airSpeed;
        cHeading = Math.PI * heading / 180.0;
        cElevation = height;
    }

    /**
     *
     * @param x
     * @param y
     * @param height
     * @param heading
     * @param airSpeed
     * @param fuel 
     * @param time
     * @param col
     * @param nfzs
     * @param xVa
     * @param xHeading
     * @param xHeight
     * @param xPIDi
     * @param xPIDd
     * @param cSpeed
     * @param cHeading
     * @param cElevation
     */
    public UavState(
            double x, double y, double height, double heading, double airSpeed,
            double fuel, double time, int col, int nfzs, double xVa, double xHeading,
            double xHeight, double xPIDi, double xPIDd, double cSpeed, double cElevation,
            double cHeading) {
        this.xyPos = new Cartesian(x, y);
        this.height = height;
        this.heading = heading;
        this.airSpeed = airSpeed;
        this.fuel = fuel;        
        this.time = time;
        this.col = col;
        this.nfzs = nfzs;
        this.xVa = xVa;
        this.xHeading = xHeading;
        this.xHeight = xHeight;
        this.xPIDi = xPIDi;
        this.xPIDd = xPIDd;
        this.cSpeed = cSpeed;
        this.cElevation = cElevation;
        this.cHeading = cHeading;
    }

    /**
     * @return the xyPos
     */
    public Cartesian getXyPos() {
        return xyPos;
    }

    /**
     * @param xyPos the xyPos to set
     */
    public void setXyPos(Cartesian xyPos) {
        this.xyPos = xyPos;
    }

    /**
     * @return the geoPos
     */
    public Geographic getGeoPos() {
        return geoPos;
    }

    /**
     * @param geoPos the geoPos to set
     */
    public void setGeoPos(Geographic geoPos) {
        this.geoPos = geoPos;
    }

    /**
     * @return the x component of xyPos
     */
    public double getX() {
        return getXyPos().getX();
    }

    /**
     * @param x component of xyPos to set
     */
    public void setX(double x) {
        this.getXyPos().setX(x);
    }

    /**
     * @return the y component of xyPos
     */
    public double getY() {
        return getXyPos().getY();
    }

    /**
     * @param y component of xyPos to set
     */
    public void setY(double y) {
        this.getXyPos().setY(y);
    }

    /**
     * @return the latitude component of geoPos
     */
    public double getLatitude() {
        return getGeoPos().getLatitude();
    }

    /**
     * @param latitude component of geoPos to set
     */
    public void setLatitude(double latitude) {
        this.getGeoPos().setLatitude(latitude);
    }

    /**
     * @return the longitude component of geoPos
     */
    public double getLongitude() {
        return getGeoPos().getLongitude();
    }

    /**
     * @param longitude component of geoPos to set
     */
    public void setLongitude(double longitude) {
        this.getGeoPos().setLongitude(longitude);
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
     * @return the heading
     */
    public double getHeading() {
        return heading;
    }

    /**
     * @param heading the heading to set
     */
    public void setHeading(double heading) {
        this.heading = heading;
    }

    /**
     * @return the airSpeed
     */
    public double getAirSpeed() {
        return airSpeed;
    }

    /**
     * @param airSpeed the airSpeed to set
     */
    public void setAirSpeed(double airSpeed) {
        this.airSpeed = airSpeed;
    }

    /**
     * @return the fuel
     */
    public double getFuel() {
        return fuel;
    }

    /**
     * @param fuel the fuel to set
     */
    public void setFuel(double fuel) {
        this.fuel = fuel;
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
     * @return the col
     */
    public int getCol() {
        return col;
    }

    /**
     * @param col the col to set
     */
    public void setCol(int col) {
        this.col = col;
    }

    /**
     * @return the nfzs
     */
    public int getNfzs() {
        return nfzs;
    }

    /**
     * @param nfzs the nfzs to set
     */
    public void setNfzs(int nfzs) {
        this.nfzs = nfzs;
    }

    /**
     * @return the xVa
     */
    public double getxVa() {
        return xVa;
    }

    /**
     * @param xVa the xVa to set
     */
    public void setxVa(double xVa) {
        this.xVa = xVa;
    }

    /**
     * @return the xHeading
     */
    public double getxHeading() {
        return xHeading;
    }

    /**
     * @param xHeading the xHeading to set
     */
    public void setxHeading(double xHeading) {
        this.xHeading = xHeading;
    }

    /**
     * @return the xHeight
     */
    public double getxHeight() {
        return xHeight;
    }

    /**
     * @param xHeight the xHeight to set
     */
    public void setxHeight(double xHeight) {
        this.xHeight = xHeight;
    }

    /**
     * @return the xPIDi
     */
    public double getxPIDi() {
        return xPIDi;
    }

    /**
     * @param xPIDi the xPIDi to set
     */
    public void setxPIDi(double xPIDi) {
        this.xPIDi = xPIDi;
    }

    /**
     * @return the xPIDd
     */
    public double getxPIDd() {
        return xPIDd;
    }

    /**
     * @param xPIDd the xPIDd to set
     */
    public void setxPIDd(double xPIDd) {
        this.xPIDd = xPIDd;
    }

    /**
     * @return the cSpeed
     */
    public double getcSpeed() {
        return cSpeed;
    }

    /**
     * @param cSpeed the cSpeed to set
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
     * @param cHeading the cHeading to set
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
     * @param cElevation the cElevation to set
     */
    public void setcElevation(double cElevation) {
        this.cElevation = cElevation;
    }

    @Override
    public UavState clone() {
        return new UavState(
                this.xyPos.getX(),
                this.xyPos.getY(),
                this.height,
                this.heading,
                this.airSpeed,
                this.fuel,
                this.time,
                this.col,
                this.nfzs,                
                this.xVa,
                this.xHeading,
                this.xHeight,
                this.xPIDi,
                this.xPIDd,
                this.cSpeed,
                this.cElevation,
                this.cHeading);
    }

}
