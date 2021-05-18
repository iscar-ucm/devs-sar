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
import models.sensor.SensorState;
import models.sensor.payloads.Likelihood;
import models.sensor.payloads.Camera;
import models.uav.UavState;

/**
 *
 * @author jbbordon
 */
public class TestCamera {

    public static void main(String[] args) {
        Geographic searchAreaPos = new Geographic(0.0, 0.0);
        SearchArea searchArea = new SearchArea(searchAreaPos, 10000, 10000, 25, 25, 0.0);
        Camera p1 = new Camera("cameraA", 5.0, searchArea);
        UavState uavState = new UavState(5000, 5000, 2000, 0, 0, 0, 0);
        p1.setUavState(uavState);
        SensorState camState = new SensorState(0.0, 45.0, 0.0, 0.0, 0.0);            
        p1.setSensorState(camState);        
        Likelihood sensorNDP = p1.evaluate();
        try {
            MatrixIO.saveDenseCSV(sensorNDP.getMatrix(), "test/models/sensors/payloads/likelihood_cameraA.csv");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
