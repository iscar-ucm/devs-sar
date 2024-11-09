/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.sensor.payloads;

import utils.JSONLoader;
import models.environment.SearchArea;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.json.simple.JSONObject;

/**
 *
 * @author jbbordon
 */
public class Ideal extends Payload {

    // sensor specific parameters
    private double pMax;

    public Ideal(String sensorType, JSONObject payloadJS, SearchArea searchArea) {
        super(payloadJS, searchArea);
        // read sensor type specific flight parameters
        JSONObject sensorParameters
                = JSONLoader.getSensorParameters(sensorType.toString());
        pMax = (double) sensorParameters.get("pMax");
    }

    public Ideal(Ideal fun) {
        super(fun);
        pMax = fun.getpMax();
    }

    public Ideal(String sensorType, double captureAt, SearchArea searchArea) {
        super(captureAt, searchArea);
        // read sensor type specific flight parameters
        JSONObject sensorParameters
                = JSONLoader.getSensorParameters(sensorType);
        pMax = (double) sensorParameters.get("pMax");
    }

    /**
     * @return the pMax
     */
    public double getpMax() {
        return pMax;
    }

    /**
     * This method return the sensor likelihood as NDP. This method is overrided
     * for the ideal sensor.
     *
     * @return Likelihood as NDP.
     */
    @Override
    public Likelihood evaluate() {

        DMatrixRMaj newMatrix = new DMatrixRMaj(
                sensorLikelihood.getxCells(), sensorLikelihood.getyCells());

        // calculate in which searchArea cells is the uav
        int uavXCell
                = (int) Math.floor(uavState.getY() / sensorLikelihood.getyScale());
        int uavYCell
                = (int) Math.floor(uavState.getX() / sensorLikelihood.getxScale());

        // apply detection probability to sensor matrix       
        if (uavXCell >= 0 && uavXCell < sensorLikelihood.getxCells()
                && uavYCell >= 0 && uavYCell < sensorLikelihood.getyCells()) {
            newMatrix.set(uavXCell, uavYCell, pMax);
        }

        // change to non detection probability
        CommonOps_DDRM.add(newMatrix, -1.0);
        CommonOps_DDRM.changeSign(newMatrix);
        sensorLikelihood.setMatrix(newMatrix);

        // finally return ndp matrix
        return sensorLikelihood;
    }

}
