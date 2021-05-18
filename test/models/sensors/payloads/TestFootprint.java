/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.sensors.payloads;

import java.io.IOException;
import org.ejml.ops.MatrixIO;
import org.ejml.dense.row.DMatrixVisualization;
import models.environment.Geographic;
import models.environment.SearchArea;
import models.sensor.payloads.Footprint;
import models.sensor.payloads.Likelihood;
import models.uav.UavState;

/**
 *
 * @author jbbordon
 */
public class TestFootprint {

    public static void main(String[] args) {
        Geographic searchAreaPos = new Geographic(0.0, 0.0);
        SearchArea searchArea = new SearchArea(searchAreaPos, 2000, 2000, 20, 20, 0.0);
        Footprint p1 = new Footprint("footprint", 5.0, searchArea);
        UavState uavState = new UavState(1150, 1150, 50, 0, 0, 0, 0);
        p1.setUavState(uavState);
        Likelihood sensorNDP = p1.evaluate();
        try {
            MatrixIO.saveDenseCSV(sensorNDP.getMatrix(), "matrix_file.csv");
            DMatrixVisualization.show(sensorNDP.getMatrix(), "testFootprint");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
