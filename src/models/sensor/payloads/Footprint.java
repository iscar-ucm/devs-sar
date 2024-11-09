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
public class Footprint extends Payload {

    // sensor specific parameters
    private double footPrint;
    private double pMax;

    public Footprint(String sensorType, JSONObject payloadJS, SearchArea searchArea) {
        super(payloadJS, searchArea);
        // read sensor type specific flight parameters
        JSONObject sensorParameters
                = JSONLoader.getSensorParameters(sensorType.toString());
        footPrint = (double) sensorParameters.get("footPrint");
        pMax = (double) sensorParameters.get("pMax");        
    }

    public Footprint(Footprint fun) {
        super(fun);
        footPrint = fun.getFootPrint();
        pMax = fun.getpMax();          
    }

    public Footprint(String sensorType, double captureAt, SearchArea searchArea) {
        super(captureAt, searchArea);
        // read sensor type specific flight parameters
        JSONObject sensorParameters
                = JSONLoader.getSensorParameters(sensorType);
        footPrint = (double) sensorParameters.get("footPrint");
        pMax = (double) sensorParameters.get("pMax");        
    }        
    
    /**
     * @return the footPrint
     */
    public double getFootPrint() {
        return footPrint;
    }

    /**
     * @return the pMax
     */
    public double getpMax() {
        return pMax;
    }   

    /**
     * This method return the sensor likelihood as NDP. This method is overrided
     * for the footprint sensor.
     *
     * @return Likelihood as NDP.
     */    
    @Override
    public Likelihood evaluate() {

        double footPrint2 = footPrint / 2;
        double xScale = sensorLikelihood.getxScale();
        double yScale = sensorLikelihood.getyScale();

        // calculate possible cells affected by the footPrint 
        int nCell
                = (int) Math.floor((uavState.getY() + footPrint2) / yScale) - 1; // N cell          
        int eCell
                = (int) Math.floor((uavState.getX() + footPrint2) / xScale) - 1; // E cell
        int sCell
                = (int) Math.floor((uavState.getY() - footPrint2) / yScale) - 1; // S cell        
        int wCell
                = (int) Math.floor((uavState.getX() - footPrint2) / xScale) - 1; // W cell

        // calculate rem area outside affected cells
        double nRem
                = 1 - (((uavState.getY() + footPrint2) % yScale) / yScale); // N cell
        double eRem
                = 1 - (((uavState.getX() + footPrint2) % xScale) / xScale); // E cell        
        double sRem = ((uavState.getY() - footPrint2) % yScale) / yScale; // S cell
        double wRem = ((uavState.getX() - footPrint2) % xScale) / xScale; // W cell         

        // border cases
        int incX = 0;
        int incY = 0;
        if (Math.ceil((uavState.getX() - footPrint2) / xScale)
                == Math.floor((uavState.getX() - footPrint2) / xScale)) {
            incX = 1;
        }
        if (Math.ceil((uavState.getY() - footPrint2) / yScale)
                == Math.floor((uavState.getY() - footPrint2) / yScale)) {
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

        // calculate footPrint area for each cell
        double[] partX = new double[indexX.length];
        double[] partY = new double[indexY.length];
        for (int i = 1; i < partX.length - 1; ++i) {
            partX[i] = 1.0;
            partY[i] = 1.0;
        }
        partX[0] = eRem;
        partX[indexX.length - 1] = wRem;
        partY[0] = sRem;
        partY[indexY.length - 1] = nRem;

        // apply values to sensor matrix
        DMatrixRMaj newMatrix = new DMatrixRMaj(
                sensorLikelihood.getxCells(), sensorLikelihood.getyCells());
        for (int x = 0; x < indexX.length; ++x) {
            // check indexes are inside valid boundaries
            for (int y = 0; y < indexY.length; ++y) {
                if (indexX[x] >= 0 && indexX[x] < sensorLikelihood.getyCells()
                        && indexY[y] >= 0 && indexY[y] < sensorLikelihood.getxCells()) {
                    newMatrix.set(indexY[y], indexX[x], partX[x] * partY[y] * pMax);
                }
            }
        }

        // change to non detection probability
        CommonOps_DDRM.add(newMatrix, -1.0);
        CommonOps_DDRM.changeSign(newMatrix);
        sensorLikelihood.setMatrix(newMatrix);

        // finally return ndp matrix
        return sensorLikelihood;
    }

}
