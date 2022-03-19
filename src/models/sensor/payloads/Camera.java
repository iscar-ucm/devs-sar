/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.sensor.payloads;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import models.environment.Cartesian;
import utils.JSONLoader;
import models.environment.SearchArea;
import org.json.simple.JSONObject;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import static utils.CommonOperations.isEqual;

/**
 *
 * @author jbbordon
 */
public class Camera extends Payload {

    private static final Logger LOGGER = Logger.getLogger(Camera.class.getName());

    // sensor specific parameters
    private double pMax;
    private double difX; // differential in meters for the x axis of each camera measure point.
    private double difY; // differential in meters for the y axis of each camera measure point.
    private double hFoV; // camera horizontal aperture a in degrees.
    private double vFoV; // camera vertical aperture in degrees.
    private Cartesian cameraLoS; // camera Line of Sight or the center of the FoV
    private ArrayList<Cartesian> cameraFoV; // camera Field of View
    private int numPointsxCell; // number of points per cell
    private DMatrixRMaj cameraMatrix; // a matrix xPoints*yPoints with the camera
    // measures given the uavHeight and sensorElevation    

    /**
     *
     * @param sensorType
     * @param payloadJS
     * @param searchArea
     */
    public Camera(String sensorType, JSONObject payloadJS, SearchArea searchArea) {
        super(payloadJS, searchArea);
        // read sensor type specific flight parameters
        JSONObject sensorParameters
                = JSONLoader.getSensorParameters(sensorType.toString());
        pMax = (double) sensorParameters.get("pMax");
        difX = (double) sensorParameters.get("difX");
        difY = (double) sensorParameters.get("difY");
        hFoV = (double) sensorParameters.get("hFoV");
        hFoV = (hFoV * Math.PI) / 180.0;
        vFoV = (double) sensorParameters.get("vFoV");
        vFoV = (vFoV * Math.PI) / 180.0;
    }

    public Camera(Camera fun) {
        super(fun);
        pMax = fun.getpMax();
        difX = fun.getDifX();
        difY = fun.getDifY();
        hFoV = fun.gethFoV();
        vFoV = fun.getvFoV();
    }

    public Camera(String sensorType, double captureAt, SearchArea searchArea) {
        super(captureAt, searchArea);
        // read sensor type specific flight parameters
        JSONObject sensorParameters
                = JSONLoader.getSensorParameters(sensorType);
        pMax = (double) sensorParameters.get("pMax");
        difX = (double) sensorParameters.get("difX");
        difY = (double) sensorParameters.get("difY");
        hFoV = (double) sensorParameters.get("hFoV");
        hFoV = (hFoV * Math.PI) / 180.0;
        vFoV = (double) sensorParameters.get("vFoV");
        vFoV = (vFoV * Math.PI) / 180.0;
    }

    /**
     * @return the pMax
     */
    public double getpMax() {
        return pMax;
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
     * @return the hFoV
     */
    public double gethFoV() {
        return hFoV;
    }

    /**
     * @return the vFoV
     */
    public double getvFoV() {
        return vFoV;
    }

    /**
     * This method return the sensor likelihood as NDP. This method is overrided
     * for the camera.
     *
     * @return Likelihood as NDP.
     */
    @Override
    public Likelihood evaluate() {

        DMatrixRMaj newMatrix = new DMatrixRMaj(
                sensorLikelihood.getxCells(), sensorLikelihood.getyCells());

        // update cameraLoS
        calculateLoS();
        calculateFoV();
        calculateCamerarMatrix();

        // loop each point of the radarMatrix
        for (int x = 0; x < cameraMatrix.getNumRows(); ++x) {

            for (int y = 0; y < cameraMatrix.getNumCols(); ++y) {

                // current point X,Y coordinates (cameraLoS is at the center of the matrix)
                double pointX, pointY;
                pointX
                        = -(cameraMatrix.getNumRows() / 2) * difX + x * difX;
                pointY
                        = -(cameraMatrix.getNumCols() / 2) * difX + y * difY;

                // calculate in which searchArea cell is the point
                int xCell
                        = (int) Math.ceil((pointY + cameraLoS.getY()) / sensorLikelihood.getxScale());
                int yCell
                        = (int) Math.ceil((pointX + cameraLoS.getX()) / sensorLikelihood.getyScale());

                // check the point is inside the searchArea
                if (xCell >= 0 && xCell < sensorLikelihood.getxCells()
                        && yCell >= 0 && yCell < sensorLikelihood.getyCells()) {
                    // apply point probability
                    newMatrix.add(xCell, yCell, cameraMatrix.get(x, y) / numPointsxCell);
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

    /**
     * This function return the distance of point (x,y) to cameraFoV when it is
     * inside the FoV polygon. If the point is outside then dist is negative
     *
     * @param x coordinate x of the point to check
     * @param y coordinate y of the point to check
     *
     * @return dist Distance of (x,y) to border of polygon (-1 if it's outside).
     */
    private double isInsideFoV(double x, double y) {

        int counter = 0;
        double dist = 2.0 * cameraFoV.size();
        double distP1P2;
        double p2x;
        double p2y;
        double p21x;
        double p21y;

        // Ver si está dentro o fuera
        double p1x = cameraFoV.get(0).getX();
        double p1y = cameraFoV.get(0).getY();

        for (int k = 1; k < cameraFoV.size(); k++) {
            p2x = cameraFoV.get(k).getX();
            p2y = cameraFoV.get(k).getY();

            p21x = p2x - p1x;
            p21y = p2y - p1y;

            //Ver si está cerca del borde del segmento [p2_p1]     
            distP1P2 = Math.pow(p21x, 2.0) + Math.pow(p21y, 2.0);
            double d = (x - p1x) * p21x + (y - p1y) * p21y;
            double u = d / distP1P2;

            if ((u >= 0.0) && (u <= 1.0)) { // Punto entre los extremos del segmento 
                double px = p1x + u * p21x;
                double py = p1y + u * p21y;
                double dseg = Math.sqrt(Math.pow(px - x, 2.0) + Math.pow(py - y, 2.0));
                dist = Math.min(dseg, dist);
            }

            // Tipo de punto: interior o exterior 
            if (y > Math.min(p1y, p2y)) {
                if (y <= Math.max(p1y, p2y)) {
                    if (x <= Math.max(p1x, p2x)) {
                        if (p1y != p2y) {
                            double xinters = (y - p1y) * p21x / p21y + p1x;
                            if ((isEqual(p1x, p2x)) || (x <= xinters)) {
                                counter = counter + 1;
                            }
                        }
                    }
                }
            }
            p1x = p2x;
            p1y = p2y;
        }

        if (counter % 2 == 0) {
            dist = -dist;
        }

        return dist;
    }

    /**
     * This method calculate the camera Line of Sight.
     *
     */
    private void calculateLoS() {

        // conversion to radians
        double sensorElevation = (sensorState.getElevation() * Math.PI) / 180.0;
        double sensorAzimuth = (sensorState.getAzimuth() * Math.PI) / 180.0;
        double uavHeading = (uavState.getHeading() * Math.PI) / 180.0;

        // distance from the UAV to the cameraLoS
        double sRange;
        if (sensorElevation == 0) {
            sRange = uavState.getHeight();
        } else {
            sRange = uavState.getHeight() / Math.sin(sensorElevation);
        }

        // camera LoS coordinates in uav axis
        double cameraU = sRange * Math.cos(sensorAzimuth)
                * Math.cos(sensorElevation);

        double cameraV = sRange * Math.sin(sensorAzimuth)
                * Math.cos(sensorElevation);

        double cameraW = sRange * Math.sin(sensorElevation);

        // camera LoS translation to x,y,z
        double cameraX = cameraU * Math.cos(uavHeading)
                - cameraV * Math.sin(uavHeading) + uavState.getX();

        double cameraY = cameraU * Math.sin(uavHeading)
                + cameraV * Math.cos(uavHeading) + uavState.getY();

        cameraLoS = new Cartesian(cameraX, cameraY);

    }

    /**
     * This method calculate the camera Field of View represented as a polygon.
     * FoV is calculated every time either the sensorElevation or the uavHeight
     * changes.
     *
     */
    private void calculateFoV() {

        // conversion to radians
        double sensorElevation = (sensorState.getElevation() * Math.PI) / 180.0;
        double sensorAzimuth = (sensorState.getAzimuth() * Math.PI) / 180.0;
        double uavHeading = (uavState.getHeading() * Math.PI) / 180.0;

        // FoV horizontal and vertical angles 
        double v1 = sensorElevation - vFoV / 2.0;
        double v2 = sensorElevation + vFoV / 2.0;

        // FoV horizontal & vertical vectors
        double sr1 = uavState.getHeight() / Math.sin(v1);
        double sr2 = uavState.getHeight() / Math.sin(v2);
        double sv1 = sr1 / Math.cos(hFoV / 2.0);
        double sv2 = sr2 / Math.cos(hFoV / 2.0);
        double distSR1 = sr1 * Math.cos(v1); // distance from UAV to sr1 (height = 0)
        double distSR2 = sr2 * Math.cos(v2); // distance from UAV to st2 (height = 0)

        // FoV lengths & angles to rotate the corners
        double lh1 = sr1 * Math.sin(hFoV / 2.0);
        double lh2 = sr2 * Math.sin(hFoV / 2.0);
        double rh1 = Math.atan(lh1 / distSR1);
        double rh2 = Math.atan(lh2 / distSR2);

        // Polar coordinates of the FoV corners:      
        DMatrixRMaj p1b = new DMatrixRMaj(4, 1);
        DMatrixRMaj p2b = new DMatrixRMaj(4, 1);
        DMatrixRMaj p3b = new DMatrixRMaj(4, 1);
        DMatrixRMaj p4b = new DMatrixRMaj(4, 1);

        p1b.set(0, 0, sv2 * Math.cos(v2) * Math.cos(sensorAzimuth + rh2));
        p1b.set(1, 0, sv2 * Math.cos(v2) * Math.sin(sensorAzimuth + rh2));
        p1b.set(2, 0, sr2 * Math.sin(v2));
        p1b.set(3, 0, 1.0);
        p2b.set(0, 0, sv1 * Math.cos(v1) * Math.cos(sensorAzimuth + rh1));
        p2b.set(1, 0, sv1 * Math.cos(v1) * Math.sin(sensorAzimuth + rh1));
        p2b.set(2, 0, sr1 * Math.sin(v1));
        p2b.set(3, 0, 1.0);
        p3b.set(0, 0, sv1 * Math.cos(v1) * Math.cos(sensorAzimuth - rh1));
        p3b.set(1, 0, sv1 * Math.cos(v1) * Math.sin(sensorAzimuth - rh1));
        p3b.set(2, 0, sr1 * Math.sin(v1));
        p3b.set(3, 0, 1.0);
        p4b.set(0, 0, sv2 * Math.cos(v2) * Math.cos(sensorAzimuth - rh2));
        p4b.set(1, 0, sv2 * Math.cos(v2) * Math.sin(sensorAzimuth - rh2));
        p4b.set(2, 0, sr2 * Math.sin(v2));
        p4b.set(3, 0, 1.0);

        // Rotation matrix:
        DMatrixRMaj cbe = new DMatrixRMaj(4, 4);

        cbe.set(0, 0, Math.cos(uavHeading));
        cbe.set(0, 1, -Math.sin(uavHeading));
        cbe.set(0, 2, 0.0);
        cbe.set(0, 3, uavState.getX());
        cbe.set(1, 0, Math.sin(uavHeading));
        cbe.set(1, 1, Math.cos(uavHeading));
        cbe.set(1, 2, 0.0);
        cbe.set(1, 3, uavState.getY());
        cbe.set(2, 0, 0.0);
        cbe.set(2, 1, 0.0);
        cbe.set(2, 2, -1.0);
        cbe.set(2, 3, uavState.getHeight());
        cbe.set(3, 0, 0.0);
        cbe.set(3, 1, 0.0);
        cbe.set(3, 2, 0.0);
        cbe.set(3, 3, 1.0);

        // FoV earth points:
        DMatrixRMaj posG1 = new DMatrixRMaj(4, 1);
        DMatrixRMaj posG2 = new DMatrixRMaj(4, 1);
        DMatrixRMaj posG3 = new DMatrixRMaj(4, 1);
        DMatrixRMaj posG4 = new DMatrixRMaj(4, 1);

        // polar to earth
        CommonOps_DDRM.mult(cbe, p1b, posG1);
        CommonOps_DDRM.mult(cbe, p2b, posG2);
        CommonOps_DDRM.mult(cbe, p3b, posG3);
        CommonOps_DDRM.mult(cbe, p4b, posG4);

        // Finally set FoV points
        cameraFoV = new ArrayList<>();
        cameraFoV.add(new Cartesian(posG1.get(0, 0), posG1.get(1, 0)));
        cameraFoV.add(new Cartesian(posG2.get(0, 0), posG2.get(1, 0)));
        cameraFoV.add(new Cartesian(posG3.get(0, 0), posG3.get(1, 0)));
        cameraFoV.add(new Cartesian(posG4.get(0, 0), posG4.get(1, 0)));
    }

    /**
     * This method calculate the camera measure points whithin the FOV of the
     * sensor.
     *
     */
    private void calculateCamerarMatrix() {

        // variables to hold the FoV x&y max & min values
        double pxMax = cameraFoV.get(0).getX();
        double pyMax = cameraFoV.get(0).getY();
        double pxMin = cameraFoV.get(0).getX();
        double pyMin = cameraFoV.get(0).getY();
        double xMax, yMax;

        // calculate the FoV x&y max & min values
        for (int k = 1; k < cameraFoV.size(); k++) {
            pxMax = Math.max(cameraFoV.get(k).getX(), pxMax);
            pyMax = Math.max(cameraFoV.get(k).getY(), pyMax);
            pxMin = Math.min(cameraFoV.get(k).getX(), pxMin);
            pyMin = Math.min(cameraFoV.get(k).getY(), pyMin);
        }

        // FoV is centrate in LoS
        xMax = 2.0 * Math.max(pxMax - cameraLoS.getX(), cameraLoS.getX() - pxMin);
        yMax = 2.0 * Math.max(pyMax - cameraLoS.getY(), cameraLoS.getY() - pyMin);

        int xPoints, yPoints;
        xPoints = (int) Math.ceil(xMax / difX);
        yPoints = (int) Math.ceil(yMax / difY);

        try {

            cameraMatrix = new DMatrixRMaj(xPoints + 1, yPoints + 1);

            // compute num of points x cell
            numPointsxCell = (int) Math.ceil(sensorLikelihood.getxScale() / difX)
                    * (int) Math.ceil(sensorLikelihood.getyScale() / difY);

            // JALO: Para cambiar en un futuro y mejorar la estima de pMax
            // ver detectionProb() de Matlab
            double sensorElevation = (sensorState.getElevation() * Math.PI) / 180.0;
            double sRange;
            if (sensorElevation == 0) {
                sRange = uavState.getHeight();
            } else {
                sRange = uavState.getHeight() / Math.sin(sensorElevation);
            }

            // loop each cell of the radarMatrix
            for (int x = 0; x < cameraMatrix.getNumRows(); ++x) {

                for (int y = 0; y < cameraMatrix.getNumCols(); ++y) {

                    // current point X,Y coordinates (uav is at the center of the matrix)
                    double pointX
                            = cameraLoS.getX() - (cameraMatrix.getNumRows() / 2) * difX + x * difX;
                    double pointY
                            = cameraLoS.getY() - (cameraMatrix.getNumCols() / 2) * difX + y * difY;

                    // Check the point is inside FoV
                    if (isInsideFoV(pointX, pointY) < 0) {
                        cameraMatrix.set(x, y, 0.0);
                    } else {
                        pMax = (5400.0 - 0.4 * sRange) / 5000.0;
                        if (pMax > 1.0) {
                            pMax = 1.0;
                        }
                        if (pMax < 0.0) {
                            pMax = 0.0;
                        }
                        // Se hace según modelo que si distancia entre el target
                        // y camara (LoS) es menor de 1000 es 1.0 y baja linealmente
                        // según curva del PADS
                        cameraMatrix.set(x, y, pMax);
                    }

                }
            }
        } catch (OutOfMemoryError oome) {

            LOGGER.log(Level.SEVERE,
                    String.format("Out of memory due to camera elevation %1$s",
                    sensorState.getElevation()        
                    )
            );

        } catch (NegativeArraySizeException nase) {

            LOGGER.log(Level.SEVERE,
                    String.format("Camera matrix too big due to camera elevation %1$s",
                    sensorState.getElevation()
                    )
            );
        }
    }
}
