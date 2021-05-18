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
public class Wind {

    private double speed;
    private double angle;

    /**
     * Generic constructor.
     */
    public Wind() {
        this.speed = 0.0;
        this.angle = 0.0;
    }

    /**
     *
     * @param windJSON
     */
    public Wind(JSONObject windJSON) {
        this.speed = (double) windJSON.get("speed");
        this.angle = (double) windJSON.get("angle");
    }

    /**
     * @return the speed
     */
    public double getSpeed() {
        return this.speed;
    }

    /**
     * @param speed the speed to set
     */
    public void setSpeed(double speed) {
        this.speed = speed;
    }

    /**
     * @return the direction
     */
    public double getAngle() {
        return this.angle;
    }

    /**
     * @param angle the angle to set
     */
    public void setAngle(double angle) {
        this.angle = angle;
    }

    /**
     * @param windA
     * @param windB
     * @return true if wind speed and angle are the same for both inputs
     */
    public static boolean equals(Wind windA, Wind windB) {
        return (windA.getAngle() == windB.getAngle() 
                && windB.getSpeed() == windB.getSpeed());
    }       
    
}
