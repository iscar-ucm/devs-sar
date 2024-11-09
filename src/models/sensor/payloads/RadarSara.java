/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.sensor.payloads;

import org.json.simple.JSONObject;
import utils.JSONLoader;
import models.environment.SearchArea;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;

/**
 *
 * @author jbbordon
 */
public class RadarSara extends Payload {

    // radar specific parameters    
    private double pstd; // probability of detection at a distance dstd.
    private double dstd; // distance dstd in meters.
    private double rMax; // maximum range of the radar.
    private double pfa; // ???
    private double tnr; // ???
    private double a;   // ???

    public RadarSara(String sensorType, JSONObject payloadJS, SearchArea searchArea) {
        super(payloadJS, searchArea);
        // read sensor type specific flight parameters
        JSONObject sensorParameters
                = JSONLoader.getSensorParameters(sensorType);
        pstd = (double) sensorParameters.get("pstd");
        dstd = (double) sensorParameters.get("dstd");
        rMax = (double) sensorParameters.get("rMax");
        pfa = 0.000001;
        tnr = -Math.log(pfa);
        a = -(tnr / Math.log(pstd) + 1.0) * (Math.pow(dstd, 4.0));
    }

    public RadarSara(RadarSara fun) {
        super(fun);
        pstd = fun.getPstd();
        dstd = fun.getDstd();
        rMax = fun.getrMax();
        pfa = fun.getPfa();
        tnr = fun.getTnr();
        a = fun.getA();
    }

    public RadarSara(String sensorType, double captureAt, SearchArea searchArea) {
        super(captureAt, searchArea);
        // read sensor type specific flight parameters
        JSONObject sensorParameters
                = JSONLoader.getSensorParameters(sensorType);
        pstd = (double) sensorParameters.get("pstd");
        dstd = (double) sensorParameters.get("dstd");
        rMax = (double) sensorParameters.get("rMax");
        pfa = 0.000001;
        tnr = -Math.log(pfa);
        a = -(tnr / Math.log(pstd) + 1.0) * (Math.pow(dstd, 4.0));
    }

    /**
     * @return the pstd
     */
    public double getPstd() {
        return pstd;
    }

    /**
     * @param pstd the pstd to set
     */
    public void setPstd(double pstd) {
        this.pstd = pstd;
    }

    /**
     * @return the dstd
     */
    public double getDstd() {
        return dstd;
    }

    /**
     * @param dstd the dstd to set
     */
    public void setDstd(double dstd) {
        this.dstd = dstd;
    }

    /**
     * @return the rMax
     */
    public double getrMax() {
        return rMax;
    }

    /**
     * @return the pfa
     */
    public double getPfa() {
        return pfa;
    }

    /**
     * @return the tnr
     */
    public double getTnr() {
        return tnr;
    }

    /**
     * @return the a
     */
    public double getA() {
        return a;
    }

    /**
     * This method return the sensor likelihood as NDP. This method is overrided
     * for the radar.
     *
     * @return Likelihood as NDP.
     */
    @Override
    public Likelihood evaluate() {

        DMatrixRMaj newMatrix = new DMatrixRMaj(
                sensorLikelihood.getxCells(), sensorLikelihood.getyCells());
        double xScale = sensorLikelihood.getxScale();
        double yScale = sensorLikelihood.getyScale();

        // calculate possible cells affected by the footPrint 
        int nCell
                = (int) Math.floor((uavState.getY() + getrMax()) / yScale) - 1; // N cell          
        int eCell
                = (int) Math.floor((uavState.getX() + getrMax()) / xScale) - 1; // E cell
        int sCell
                = (int) Math.floor((uavState.getY() - getrMax()) / yScale) - 1; // S cell        
        int wCell
                = (int) Math.floor((uavState.getX() - getrMax()) / xScale) - 1; // W cell

        // border cases
        int incX = 0;
        int incY = 0;
        if (Math.ceil((uavState.getX() - getrMax()) / xScale)
                == Math.floor((uavState.getX() - getrMax()) / xScale)) {
            incX = 1;
        }
        if (Math.ceil((uavState.getY() - getrMax()) / yScale)
                == Math.floor((uavState.getY() - getrMax()) / yScale)) {
            incY = 1;
        }

        // setup sensor matrix indexes affected by footprint
        int[] indexX = new int[eCell - wCell + 1];
        for (int i = wCell; i <= eCell; ++i) {
            indexX[i - wCell] = i + incX;
        }
        int[] indexY = new int[nCell - sCell + 1];
        for (int i = sCell; i <= nCell; ++i) {
            indexY[i - sCell] = i + incY;
        }

        // apply values to sensor matrix
        for (int x = 0; x < indexX.length; ++x) {
            // check indexes are inside valid boundaries
            for (int y = 0; y < indexY.length; ++y) {
                if (indexX[x] >= 0 && indexX[x] < sensorLikelihood.getyCells()
                        && indexY[y] >= 0 && indexY[y] < sensorLikelihood.getxCells()) {

                    // calculate the center of the cell
                    double cellX, cellY;
                    cellX
                            = (indexX[x] * xScale) + xScale / 2;
                    cellY
                            = (indexY[y] * yScale) + yScale / 2;

                    // calculate distance from the uav actual position to the center
                    // of the cell
                    double distance
                            = Math.sqrt(
                                    Math.pow(uavState.getHeight(), 2.0)
                                    + Math.pow(uavState.getX() - cellX, 2.0)
                                    + Math.pow(uavState.getY() - cellY, 2.0));

                    // calculate snr
                    double snr
                            = a / Math.pow(distance, 4.0);

                    // calculate radar cell measure
                    double cellMeasure
                            = (1.0 + 2.0 * snr * tnr / Math.pow(2.0 + snr, 2.0))
                            * Math.exp(-2.0 * tnr / (snr + 2.0));

                    newMatrix.set(indexY[y], indexX[x], cellMeasure);
                }
            }
        }

        // change to non detection probability
        CommonOps_DDRM.add(newMatrix,
                -1.0);
        CommonOps_DDRM.changeSign(newMatrix);

        sensorLikelihood.setMatrix(newMatrix);

        // return sensorLikelihood
        return sensorLikelihood;
    }

}
