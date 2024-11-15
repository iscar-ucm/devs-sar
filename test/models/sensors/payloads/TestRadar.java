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
import models.sensor.payloads.Radar;
import models.uav.UavState;

/**
 *
 * @author jbbordon
 */
public class TestRadar {

    public static void main(String[] args) {
        Geographic searchAreaPos = new Geographic(0.0, 0.0);
        SearchArea searchArea = new SearchArea(searchAreaPos, 30000, 20000, 80, 80, 0.0);
        Radar p1 = new Radar("radarTest", 5.0, searchArea);
        UavState uavState = new UavState(30000, 20000, 975, 0, 0, 0, 0);
        p1.setUavState(uavState);
        Likelihood sensorNDP = p1.evaluate();
        try {
            MatrixIO.saveDenseCSV(sensorNDP.getMatrix(), "test/models/sensors/payloads/likelihood_radar.csv");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
