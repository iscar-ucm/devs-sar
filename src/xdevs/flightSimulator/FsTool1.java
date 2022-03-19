/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.flightSimulator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import utils.CSVHandler;
import xdevs.core.modeling.Coupled;

/**
 *
 * @author bordon
 */
public class FsTool1 extends Coupled {

    public FsTool1(JSONObject jsonRoot, CSVHandler csvHandler) {
        super("FlightSimulatorTest");

        JSONArray uavsArray = (JSONArray) jsonRoot.get("uavs");

        // FS Test Inputs model creation        
        FstInputs fsti = new FstInputs(jsonRoot, csvHandler);
        super.addComponent(fsti);

        // FS model creation
        FlightSimulator fs
                = new FlightSimulator(1, uavsArray);
        super.addComponent(fs);

        // coupling of IC (FSTI & FS)
        super.addCoupling(fsti.tiO1, fs.fsI1);
        super.addCoupling(fsti.tiO2, fs.fsI2);
        for (int i = 0; i < uavsArray.size(); ++i) {
            super.addCoupling(fsti.tiO3.get(i), fs.fsI3.get(i));
            super.addCoupling(fs.fsO2.get(i), fsti.tiI1.get(i));
        }
    }
}
