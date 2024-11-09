/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.uav.motionModels;

import models.uav.UavCntrlSignals;
import utils.JSONLoader;
import models.uav.UavState;
import org.json.simple.JSONObject;

/**
 *
 * @author jbbordon
 */
public class RungeKutta extends MotionModel {

    // dinamic model paramters
    private final double pPID;
    private final double iPID;
    private final double dPID;
    private final double nPID;
    private final double bPID;
    private final double cPID;
    private final double ka;        // gain for air speed (sec^-1)
    private final double kh;        // gain for altitude (sec^-1)
    private final double kji;       // gain for track angle (sec^-1)

    // current step variables
    private double[] inputVector;
    private double[] cntrlVector;
    private double uavFuel;

    /**
     *
     * @param uavType
     * @param motionModelJSON
     */
    public RungeKutta(String uavType, JSONObject motionModelJSON) {
        super(uavType, motionModelJSON);
        // read runge kutta specific parameters
        JSONObject rungeKuttaJSON = JSONLoader.getRungeKuttaParam();
        pPID = (double) rungeKuttaJSON.get("PIDP");
        iPID = (double) rungeKuttaJSON.get("PIDI");
        dPID = (double) rungeKuttaJSON.get("PIDD");
        nPID = (double) rungeKuttaJSON.get("PIDN");
        bPID = (double) rungeKuttaJSON.get("PIDB");
        cPID = (double) rungeKuttaJSON.get("PIDC");
        ka = (double) rungeKuttaJSON.get("KA");
        kh = (double) rungeKuttaJSON.get("KH");
        kji = (double) rungeKuttaJSON.get("KJI");
    }

    /**
     *
     * @param motionModel
     */
    public RungeKutta(RungeKutta motionModel) {
        // copy the given motion model
        super(motionModel);
        pPID = motionModel.getpPID();
        iPID = motionModel.getiPID();
        dPID = motionModel.getdPID();
        nPID = motionModel.getnPID();
        bPID = motionModel.getbPID();
        cPID = motionModel.getcPID();
        ka = motionModel.getKa();
        kh = motionModel.getKh();
        kji = motionModel.getKji();
    }

    /**
     * @return the pPID
     */
    public double getpPID() {
        return pPID;
    }

    /**
     * @return the iPID
     */
    public double getiPID() {
        return iPID;
    }

    /**
     * @return the dPID
     */
    public double getdPID() {
        return dPID;
    }

    /**
     * @return the nPID
     */
    public double getnPID() {
        return nPID;
    }

    /**
     * @return the bPID
     */
    public double getbPID() {
        return bPID;
    }

    /**
     * @return the cPID
     */
    public double getcPID() {
        return cPID;
    }

    /**
     * @return the ka
     */
    public double getKa() {
        return ka;
    }

    /**
     * @return the kh
     */
    public double getKh() {
        return kh;
    }

    /**
     * @return the kji
     */
    public double getKji() {
        return kji;
    }

    private void buildInputVector() {
        inputVector[0] = uavState.getX();
        inputVector[1] = uavState.getY();
        inputVector[2] = 0.0;
        inputVector[3] = uavState.getAirSpeed();
        inputVector[4] = uavState.getxVa();
        inputVector[5] = Math.PI * uavState.getHeading() / 180.0;
        inputVector[6] = uavState.getxHeading();
        inputVector[7] = uavState.getHeight();
        inputVector[8] = uavState.getxHeight();
        inputVector[9] = uavState.getxPIDi();
        inputVector[10] = uavState.getxPIDd();
    }

    private void buildCntrlVector() {
        // for each decision variable
        for (int i = 0; i < getDecisionArray().length; ++i) {
            switch (getDecisionArray()[i].getName()) {
                case heading: // Yaw control
                    switch (getDecisionArray()[i].getType()) {
                        case rate:
                            cntrlVector[0]
                                    += (Math.PI * cntrlSignals.getcHeading() / 180.0) * at;
                            cntrlVector[0]
                                    = // make sure yaw is in range
                                    Math.atan2(Math.sin(cntrlVector[0]), Math.cos(cntrlVector[0]));
                            break;
                        case absolute:
                        case increment:
                            cntrlVector[0]
                                    = Math.PI * cntrlSignals.getcHeading() / 180.0;
                            cntrlVector[0]
                                    = // make sure yaw is in range
                                    Math.atan2(Math.sin(cntrlVector[0]), Math.cos(cntrlVector[0]));
                            break;
                        case noaction:
                            break;
                    }
                    break;
                case elevation: // Elevation control
                    switch (getDecisionArray()[i].getType()) {
                        case rate:
                            cntrlVector[1]
                                    += cntrlSignals.getcElevation() * at;
                            break;
                        case absolute:
                        case increment:
                            cntrlVector[1]
                                    = cntrlSignals.getcElevation();
                            break;
                        case noaction:
                            break;
                    }
                    break;
                case speed:
                    switch (getDecisionArray()[i].getType()) {
                        case rate:
                            cntrlVector[2]
                                    += cntrlSignals.getcSpeed() * at;
                            break;
                        case absolute:
                        case increment:
                            cntrlVector[2]
                                    = cntrlSignals.getcSpeed();
                            break;
                        case noaction:
                            break;
                    }
                    break;
            }
        }
    }

    private void analyzeOutputVector(double[] rkVector) {
        double newHeading = rkVector[5] * 180.0 / Math.PI;
        if (Math.abs(newHeading) > 180) {
            if (newHeading > 180) {
                newHeading -= 360;
            } else {
                newHeading += 360;
            }
        }
        uavFuel -= rkVector[2];
        uavState = new UavState(
                rkVector[0], // x
                rkVector[1], // y
                rkVector[7], // height
                newHeading,
                rkVector[3], // airSpeed
                uavFuel,
                uavState.getTime() + at,
                0, // collisions
                0, // nfzs
                rkVector[4], // xVa
                rkVector[6], // xHeading 
                rkVector[8], // xHeight
                rkVector[9], // xPIDi
                rkVector[10], // xPIDd
                cntrlVector[2], // cSpeed
                cntrlVector[1], // cElevation
                cntrlVector[0] // cHeading
        );
    }

    private double[] fjmode45() {

        // 1st call
        double[] xd = modUAV_dx_fjmode45(inputVector);

        double[] xa = new double[xd.length];
        for (int i = 0; i < xa.length; ++i) {
            xa[i] = xd[i] * at;
        }
        double[] x = new double[inputVector.length];
        for (int i = 0; i < inputVector.length; ++i) {
            x[i] = inputVector[i] + 0.5 * xa[i];
        }

        // 2nd call        
        xd = modUAV_dx_fjmode45(x);

        double[] q = new double[xd.length];
        for (int i = 0; i < q.length; ++i) {
            q[i] = xd[i] * at;
            x[i] = inputVector[i] + 0.5 * q[i];
            xa[i] = xa[i] + 2 * q[i];
        }

        // 3rd call        
        xd = modUAV_dx_fjmode45(x);

        for (int i = 0; i < q.length; ++i) {
            q[i] = xd[i] * at;
            x[i] = inputVector[i] + q[i];
            xa[i] = xa[i] + 2 * q[i];
        }

        // 4th call        
        xd = modUAV_dx_fjmode45(x);

        for (int i = 0; i < q.length; ++i) {
            x[i] = inputVector[i] + (xa[i] + xd[i] * at) / 6.0;
        }

        return x;
    }

    private double[] modUAV_dx_fjmode45(double[] x) {
        // output vector
        double[] dx = new double[11];

        // environment wind action
        double windNorth = wind.getSpeed() * Math.cos(wind.getAngle());
        double windEast = wind.getSpeed() * Math.sin(wind.getAngle());
        double sVa = x[3];
        if (sVa >= vMax) { // airspeed saturation
            sVa = vMax;
        } else if (sVa <= vMin) {
            sVa = vMin;
        }
        dx[0] = windNorth + sVa * Math.cos(x[5]);
        dx[1] = windEast + sVa * Math.sin(x[5]);

        // Air Velocity PID
        double AP = (getbPID() * cntrlVector[2] - sVa) * getpPID(); // Proportional action
        dx[9] = (cntrlVector[2] - sVa) * getiPID(); // Integral action
        dx[10] = ((getcPID() * cntrlVector[2] - sVa) * getdPID() - x[10]) * getnPID(); // Derivative action 
        double sPID = AP + x[9] + dx[10]; // PID output

        // AirSpeed filter
        dx[4] = (sPID - x[4]) / tauVa;
        dx[3] = getKa() * x[4];

        // Fuel Integration
        dx[2] = sVa * kFuel;

        // Heading Integration
        double ji = Math.atan2(dx[1], dx[0]);
        double angdif = cntrlVector[0] - ji; // + Math.PI) % (2 * Math.PI)) - Math.PI;
        while (angdif < -Math.PI) {
            angdif += 2 * Math.PI;
        }
        while (angdif > Math.PI) {
            angdif -= 2 * Math.PI;
        }

        dx[6] = (angdif - x[6]) / tauRoll;
        dx[5] = getKji() * x[6];
        if (dx[5] > jiMax) {
            dx[5] = jiMax;
        } else if (dx[5] < -jiMax) {
            dx[5] = -jiMax;
        }

        // Height integraton
        double e = cntrlVector[1] - x[7];
        dx[8] = (e - x[8]) / tauH;
        if (dx[8] > srMax) {
            dx[8] = srMax;
        } else if (dx[8] < -srMax) {
            dx[8] = -srMax;
        }
        double hdot = x[8];
        if (hdot > vhMax) {
            hdot = vhMax;
        } else if (hdot < -vhMax) {
            hdot = -vhMax;
        }
        dx[7] = getKh() * hdot;

        // return de output vector
        return dx;
    }

    @Override
    public void initModel(UavState initState) {
        // init internal variables
        inputVector = new double[11];
        cntrlVector = new double[3];
        uavState = initState;
        uavFuel = initState.getFuel();
        cntrlSignals = new UavCntrlSignals(
                initState.getcSpeed(),
                initState.getcHeading() * 180.0 / Math.PI,
                initState.getcElevation(),
                initState.getTime());
        cntrlVector[0] = initState.getcHeading();
        cntrlVector[1] = initState.getcElevation();
        cntrlVector[2] = initState.getcSpeed();
    }

    @Override
    public UavState stepModel() {
        // prepare inputs
        buildInputVector();
        // prepare cntrl vector
        buildCntrlVector();
        // call runge kutta method
        double[] dx = fjmode45();
        // update data using output vector
        analyzeOutputVector(dx);
        // return current uavState
        return uavState;
    }

    @Override
    public void resetModel() {
        uavState = null;
    }

}
