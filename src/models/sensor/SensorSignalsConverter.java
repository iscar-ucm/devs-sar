/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.sensor;

import java.util.ArrayList;
import models.uav.UavCntrlSignals;

/**
 *
 * @author juanbordonruiz
 */
public class SensorSignalsConverter {

    public ArrayList<SensorCntrlSignals> absToInc(ArrayList<SensorCntrlSignals> absoluteSignals) {

        ArrayList<SensorCntrlSignals> incrementalSignals = new ArrayList<>();

        SensorCntrlSignals currentSignals;
        double currentElevation = 0;
        double currentAzimuth = 0;

        for (short i = 0; i < absoluteSignals.size(); i++) {

            currentSignals = absoluteSignals.get(i);
            double elevation = currentSignals.getcElevation();
            double azimuth = currentSignals.getcAzimuth();
            double time = currentSignals.getTime();

            SensorCntrlSignals deltaSignals = new SensorCntrlSignals(
                    elevation - currentElevation,
                    azimuth - currentAzimuth,
                    time);

            currentElevation = elevation;
            currentAzimuth = azimuth;

            incrementalSignals.add(deltaSignals);

        }

        return incrementalSignals;
    }

    public ArrayList<SensorCntrlSignals> incToAbs(ArrayList<SensorCntrlSignals> incrementalSignals) {

        ArrayList<SensorCntrlSignals> absoluteSignals = new ArrayList<>();

        double currentElevation = 0;
        double currentAzimuth = 0;

        for (short i = 0; i < incrementalSignals.size(); i++) {

            SensorCntrlSignals currentSignals = incrementalSignals.get(i);

            double deltaElevation = currentSignals.getcElevation();
            double deltaAzimuth = currentSignals.getcAzimuth();
            double time = currentSignals.getTime();
            
            double elevation = currentElevation + deltaElevation;
            double azimuth = currentAzimuth + deltaAzimuth;

            SensorCntrlSignals absoluteSignal = new SensorCntrlSignals(elevation, azimuth, time);

            currentElevation = elevation;
            currentAzimuth = azimuth;

            absoluteSignals.add(absoluteSignal);
        }

        return absoluteSignals;
    }

}
