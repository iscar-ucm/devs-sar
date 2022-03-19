/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.spec2.flightSimulator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import utils.CSVHandler;
import xdevs.core.modeling.Coupled;

/**
 *
 * @author bordon
 */
public class FsTool2 extends Coupled {

    public FsTool2(JSONObject jsonRoot, CSVHandler csvHandler) {
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
        super.addCoupling(fsti.tiO3, fs.fsI3);
        super.addCoupling(fs.fsO1, fsti.tiI1);
        }
}
