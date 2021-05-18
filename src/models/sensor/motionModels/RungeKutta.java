/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.sensor.motionModels;

import models.sensor.SensorCntrlSignals;
import utils.JSONLoader;
import models.sensor.SensorState;
import org.json.simple.JSONObject;

/**
 *
 * @author jbbordon
 */
public class RungeKutta extends DinamicModel {

    // sensor type specific parameters
    private double aLimit;   // azimuth limit
    private double eLimit;   // elevation limit
    private double aGain;    // azimuth gain
    private double eGain;    // elevation gain      

    // current step variables
    private double[] inputVector;
    private double[] cntrlVector;

    /**
     *
     * @param sensorType
     * @param motionModelJSON
     */
    public RungeKutta(String sensorType, JSONObject motionModelJSON) {
        super(motionModelJSON);
        // read sensor type specific flight parameters
        JSONObject sensorParameters = JSONLoader.getSensorParameters(sensorType);
        aLimit = (double) sensorParameters.get("aLimit");
        aLimit = Math.PI * aLimit / 180.0;
        eLimit = (double) sensorParameters.get("eLimit");
        eLimit = Math.PI * eLimit / 180.0;
        aGain = (double) sensorParameters.get("aGain");
        eGain = (double) sensorParameters.get("eGain");        
    }

    public RungeKutta(RungeKutta motionModel) {
        // copy the given motion model
        super(motionModel);
        aLimit = motionModel.getaLimit();
        eLimit = motionModel.geteLimit();
        aGain = motionModel.getaGain();
        eGain = motionModel.geteGain();
    }

    /**
     * @return the aLimit
     */
    public double getaLimit() {
        return aLimit;
    }

    /**
     * @return the eLimit
     */
    public double geteLimit() {
        return eLimit;
    }

    /**
     * @return the aGain
     */
    public double getaGain() {
        return aGain;
    }

    /**
     * @return the eGain
     */
    public double geteGain() {
        return eGain;
    }

    private void buildInputVector() {
        inputVector[0] = Math.PI * currentState.getAzimuth() / 180.0;
        inputVector[1] = Math.PI * currentState.getElevation() / 180.0;
    }

    private void buildCntrlVector() {
        // for each decision variable
        for (int i = 0; i < getDecisionArray().length; ++i) {
            switch (getDecisionArray()[i].getName()) {
                case azimuth:
                    switch (getDecisionArray()[i].getType()) {
                        case rate:
                            // azimuth rate change
                            cntrlVector[0]
                                    += (Math.PI * cntrlSignals.getcAzimuth() / 180.0) * at;
                            cntrlVector[0]
                                    = // make sure azimuth is in range
                                    Math.atan2(Math.sin(cntrlVector[0]), Math.cos(cntrlVector[0]));
                            break;
                        case increment:
                        case absolute:
                            cntrlVector[0]
                                    = Math.PI * cntrlSignals.getcAzimuth() / 180.0;
                            break;
                        case noaction:
                            break;
                    }
                    break;
                case elevation: // Elevation control
                    switch (getDecisionArray()[i].getType()) {
                        case rate:
                            // elevation rate change
                            cntrlVector[1]
                                    += (Math.PI * cntrlSignals.getcElevation() / 180.0) * at;
                            cntrlVector[1]
                                    = // make sure azimuth is in range
                                    Math.atan2(Math.sin(cntrlVector[1]), Math.cos(cntrlVector[1]));
                            break;
                        case increment:
                        case absolute:
                            cntrlVector[1]
                                    = Math.PI * cntrlSignals.getcElevation() / 180.0;
                            break;
                        case noaction:
                            break;
                    }
                    break;
            }
        }
    }

    private void analyzeOutputVector(double[] rkVector) {
        double newAzimuth = rkVector[0] * 180.0 / Math.PI;
        if (Math.abs(newAzimuth) > 180) {
            if (newAzimuth > 180) {
                newAzimuth -= 360;
            } else {
                newAzimuth += 360;
            }
        }
        double newElevation = rkVector[1] * 180.0 / Math.PI;
        if (Math.abs(newElevation) > 180) {
            if (newElevation > 180) {
                newElevation -= 360;
            } else {
                newElevation += 360;
            }
        }        
        currentState = new SensorState(
                newAzimuth,
                newElevation,
                currentState.getTime() + at,
                cntrlVector[0], // cAzimuth
                cntrlVector[1]  // cElevation
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
        double[] dx = new double[2];

        // azimuth control   
        dx[0] = cntrlVector[0] - inputVector[0];
        dx[0] = aGain * dx[0];        
        if (dx[0] > aLimit) {
            dx[0] = aLimit;
        } else if (dx[0] < -aLimit) {
            dx[0] = -aLimit;
        }

        // elevation control      
        dx[1] = cntrlVector[1] - inputVector[1];
        dx[1] = eGain * dx[1];        
        if (dx[1] > eLimit) {
            dx[1] = eLimit;
        } else if (dx[1] < -eLimit) {
            dx[1] = -eLimit;
        }
        dx[1] = eGain * dx[1];

        // return de output vector
        return dx;
    }

    @Override
    public void initModel(SensorState initState) {
        // init internal variables
        currentState = initState;
        inputVector = new double[2];
        cntrlVector = new double[2];
        cntrlSignals = new SensorCntrlSignals(
                initState.getcAzimuth() * 180.0 / Math.PI,
                initState.getcElevation() * 180.0 / Math.PI,
                initState.getTime());        
        cntrlVector[0] = initState.getcAzimuth();
        cntrlVector[1] = initState.getcElevation();
    }

    @Override
    public SensorState stepModel() {
        // prepare inputs
        buildInputVector();
        // prepare cntrl vector
        buildCntrlVector();
        // call runge kutta method
        double[] dx = fjmode45();
        // update data using output vector
        analyzeOutputVector(dx);
        // return current uavState
        return currentState;
    }

    @Override
    public void resetModel() {
    }

}
