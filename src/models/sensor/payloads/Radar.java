/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.sensor.payloads;

import org.json.simple.JSONObject;
import utils.JSONLoader;
import models.environment.SearchArea;
import models.uav.UavState;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;

/**
 *
 * @author jbbordon
 */
public class Radar extends Payload {

    // radar specific parameters    
    private double pstd; // probability of detection at a distance dstd.
    private double dstd; // distance dstd in meters.
    private double rMax; // maximum range of the radar.
    private double difX; // differential in meters for the x axis of each radar measure point.
    private double difY; // differential in meters for the y axis of each radar measure point.
    private double pfa; // ???
    private double tnr; // ???
    private double a;   // ???
    private int numPointsxCell; // number of points per cell    
    private UavState prevState;
    private DMatrixRMaj radarMatrix; // a matrix xPoints*yPoints with the radar
    // measures given the uavHeight

    public Radar(String sensorType, JSONObject payloadJS, SearchArea searchArea) {
        super(payloadJS, searchArea);
        prevState = null;
        // read sensor type specific flight parameters
        JSONObject sensorParameters
                = JSONLoader.getSensorParameters(sensorType.toString());
        pstd = (double) sensorParameters.get("pstd");
        dstd = (double) sensorParameters.get("dstd");
        rMax = (double) sensorParameters.get("rMax");
        difX = (double) sensorParameters.get("difX");
        difY = (double) sensorParameters.get("difY");
        pfa = 0.000001;
        tnr = -Math.log(pfa);
        a = -(tnr / Math.log(pstd) + 1.0) * (Math.pow(dstd, 4.0));        
    }

    public Radar(Radar fun) {
        super(fun);
        prevState = null;
        pstd = fun.getPstd();
        dstd = fun.getDstd();
        rMax = fun.getrMax();
        difX = fun.getDifX();
        difY = fun.getDifY();
        pfa = fun.getPfa();
        tnr = fun.getTnr();
        a = fun.getA();
    }

    public Radar(String sensorType, double captureAt, SearchArea searchArea) {
        super(captureAt, searchArea);
        prevState = null;
        // read sensor type specific flight parameters
        JSONObject sensorParameters
                = JSONLoader.getSensorParameters(sensorType);
        pstd = (double) sensorParameters.get("pstd");
        dstd = (double) sensorParameters.get("dstd");
        rMax = (double) sensorParameters.get("rMax");
        difX = (double) sensorParameters.get("difX");
        difY = (double) sensorParameters.get("difY");
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
     * @return the difX
     */
    public double getDifX() {
        return difX;
    }

    /**
     * @return the difY
     */
    public double getDifY() {
        return difY;
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
     * @return the numPointsxCell
     */
    public int getNumPointsxCell() {
        return numPointsxCell;
    }

    /**
     * @param uavState the uavState to set
     */
    @Override
    public void setUavState(UavState uavState) {
        this.uavState = uavState;
        // check if uavHeight has changed
        if (prevState == null
                || uavState.getHeight() != prevState.getHeight()) {
            // if so recalculation of radarMatrix is necessary
            calculateRadarMatrix();
        }
        prevState = this.uavState;
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

        // loop each point of the radarMatrix
        for (int x = 0; x < radarMatrix.getNumRows(); ++x) {

            for (int y = 0; y < radarMatrix.getNumCols(); ++y) {

                // current point X,Y coordinates (uav is at the center of the matrix)
                double pointX, pointY;
                pointX
                        = -(radarMatrix.getNumRows() / 2) * difX + x * difX;
                pointY
                        = -(radarMatrix.getNumCols() / 2) * difX + y * difY;

                // calculate in which searchArea cell is the point
                int xCell
                        = (int) Math.ceil((pointY + uavState.getY()) / sensorLikelihood.getxScale());
                int yCell
                        = (int) Math.ceil((pointX + uavState.getX()) / sensorLikelihood.getyScale());

                // check the point is inside the searchArea
                if (xCell >= 0 && xCell < sensorLikelihood.getxCells()
                        && yCell >= 0 && yCell < sensorLikelihood.getyCells()) {
                    // apply point probability
                    newMatrix.add(xCell, yCell, radarMatrix.get(x, y) / numPointsxCell);
                }
            }
        }

        // change to non detection probability
        CommonOps_DDRM.add(newMatrix, -1.0);
        CommonOps_DDRM.changeSign(newMatrix);
        sensorLikelihood.setMatrix(newMatrix);

        // return sensorLikelihood
        return sensorLikelihood;
    }

    /**
     * This method calculate the radar measure points whithin the scope of the
     * sensor. The scope is calculated by the radar maxRadius which depends on the
     * current uavHeight.
     *
     */        
    private void calculateRadarMatrix() {

        // compute number of points of the radar given the uavHeight
        double maxRadius
                = Math.sqrt(Math.pow(rMax, 2.0)
                        - Math.pow(uavState.getHeight(), 2.0));
        int xPoints, yPoints;
        xPoints = (int) Math.ceil(maxRadius / difX);
        yPoints = (int) Math.ceil(maxRadius / difY);
        radarMatrix = new DMatrixRMaj(xPoints + 1, yPoints + 1);

        // compute num of points x cell
        numPointsxCell = (int) Math.ceil(sensorLikelihood.getxScale() / difX)
                * (int) Math.ceil(sensorLikelihood.getyScale() / difY);

        // loop each cell of the radarMatrix
        for (int x = 0; x < radarMatrix.getNumRows(); ++x) {

            for (int y = 0; y < radarMatrix.getNumCols(); ++y) {

                // current point X,Y coordinates (uav is at the center of the matrix)
                double pointX
                        = -(radarMatrix.getNumRows() / 2) * difX + x * difX;
                double pointY
                        = -(radarMatrix.getNumCols() / 2) * difX + y * difY;

                // calculate radius & distance to the current point
                double radius
                        = Math.sqrt(Math.pow(pointX, 2.0)
                                + Math.pow(pointY, 2.0));

                double distance
                        = Math.sqrt(
                                Math.pow(uavState.getHeight(), 2.0)
                                + Math.pow(radius, 2.0));

                // calculate snr
                double snr
                        = a / Math.pow(distance, 4.0);

                // finally calculate cell value and assign it
                double cellValue
                        = (1.0 + 2.0 * snr * tnr / Math.pow(2.0 + snr, 2.0))
                        * Math.exp(-2.0 * tnr / (snr + 2.0));

                radarMatrix.set(x, y, cellValue);
            }
        }
    }
}
