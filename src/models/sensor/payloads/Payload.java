/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.sensor.payloads;

import models.environment.SearchArea;
import models.sensor.SensorState;
import models.uav.UavState;
import org.json.simple.JSONObject;

/**
 *
 * @author jbbordon
 */
public abstract class Payload {

    private PayloadType payloadType;
    // capture At
    private double captureAt;
    // sensor likelihood
    protected Likelihood sensorLikelihood;
    // uav current state
    protected UavState uavState;
    // sensor current state
    protected SensorState sensorState;

    public Payload(JSONObject payloadJS, SearchArea searchArea) {
        payloadType = PayloadType.valueOf((String) payloadJS.get("type"));
        captureAt = (double) payloadJS.get("captureAt");
        sensorLikelihood = new Likelihood(
                searchArea.getxCells(), searchArea.getyCells(),
                searchArea.getxScale(), searchArea.getyScale()
        );
    }

    public Payload(Payload fun) {
        payloadType = fun.getPayloadType();
        captureAt = fun.getCaptureAt();
        sensorLikelihood = new Likelihood(
                fun.getSensorLikelihood().getxCells(),
                fun.getSensorLikelihood().getyCells(),
                fun.getSensorLikelihood().getxScale(),
                fun.getSensorLikelihood().getyScale()
        );
    }

    public Payload(double captureAt, SearchArea searchArea) {    
        this.captureAt = captureAt;
        sensorLikelihood = new Likelihood(
                searchArea.getxCells(), searchArea.getyCells(),
                searchArea.getxScale(), searchArea.getyScale()
        );
    }    

    /**
     * @return the payloadType
     */
    public PayloadType getPayloadType() {
        return payloadType;
    }

    /**
     * @param payloadType the payloadType to set
     */
    public void setPayloadType(PayloadType payloadType) {
        this.payloadType = payloadType;
    }

    /**
     * @return the captureAt
     */
    public double getCaptureAt() {
        return captureAt;
    }

    /**
     * @param captureAt the captureAt to set
     */
    public void setCaptureAt(double captureAt) {
        this.captureAt = captureAt;
    }

    /**
     * @return the sensorLikelihood
     */
    public Likelihood getSensorLikelihood() {
        return sensorLikelihood;
    }

    /**
     * @param sensorLikelihood the sensorLikelihood to set
     */
    public void setSensorLikelihood(Likelihood sensorLikelihood) {
        this.sensorLikelihood = sensorLikelihood;
    }

    /**
     * @return the uavState
     */
    public UavState getUavState() {
        return uavState;
    }

    /**
     * @param uavState the uavState to set
     */
    public void setUavState(UavState uavState) {
        this.uavState = uavState;
    }

    /**
     * @return the sensorState
     */
    public SensorState getSensorState() {
        return sensorState;
    }

    /**
     * @param sensorState the sensorState to set
     */
    public void setSensorState(SensorState sensorState) {
        this.sensorState = sensorState;
    }

    public abstract Likelihood evaluate();

}
