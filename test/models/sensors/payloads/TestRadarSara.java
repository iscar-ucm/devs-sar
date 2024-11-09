/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.sensors.payloads;

import java.io.IOException;
import org.ejml.ops.MatrixIO;
import models.environment.Geographic;
import models.environment.SearchArea;
import models.sensor.payloads.Likelihood;
import models.sensor.payloads.RadarSara;
import models.uav.UavState;

/**
 *
 * @author jbbordon
 */
public class TestRadarSara {

    public static void main(String[] args) {
        Geographic searchAreaPos = new Geographic(0.0, 0.0);
        SearchArea searchArea = new SearchArea(searchAreaPos, 15000, 15000, 20, 20, 0.0);
        RadarSara p1 = new RadarSara("saraTesRes", 5.0, searchArea);
        UavState uavState = new UavState(7500, 7500, 200, 0, 0, 0, 0);
        p1.setUavState(uavState);
        Likelihood sensorNDP = p1.evaluate();
        try {
            MatrixIO.saveDenseCSV(sensorNDP.getMatrix(), "test/models/sensors/payloads/likelihood_radar.csv");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
