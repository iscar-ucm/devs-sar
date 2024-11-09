/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.spec2.evaluator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import utils.CSVHandler;
import xdevs.core.modeling.Coupled;
import xdevs.spec2.flightSimulator.FlightSimulator;

/**
 *
 * @author bordon
 */
public class EvTool2 extends Coupled {

    public EvTool2(JSONObject jsonRoot, boolean fullPath, CSVHandler csvHandler) {
        super("SimulatorTest");

        JSONArray targetsArray = (JSONArray) jsonRoot.get("targets");

        JSONArray uavsArray = (JSONArray) jsonRoot.get("uavs");

        // Test EvInputs model creation        
        EvInputs ti = new EvInputs(jsonRoot, fullPath, csvHandler);
        super.addComponent(ti);

        // EV model creation
        Evaluator ev
                = new Evaluator(1, uavsArray, targetsArray);
        super.addComponent(ev);

        // FS model creation
        FlightSimulator fs
                = new FlightSimulator(1, uavsArray);
        super.addComponent(fs);

        // coupling of IC (TI & FS)
        super.addCoupling(ti.tiO1, fs.fsI1);
        super.addCoupling(ti.tiO3, fs.fsI2);
        super.addCoupling(ti.tiO4, fs.fsI3);

        // coupling of IC (TI & EV)
        super.addCoupling(ti.tiO1, ev.eI1);
        super.addCoupling(ti.tiO2, ev.eI2);
        super.addCoupling(ti.tiO5, ev.eI4);
        super.addCoupling(ev.eO1, ti.tiI1);
        super.addCoupling(ev.eO2, ti.tiI2);

        // coupling of IC (EV & FS)        
        super.addCoupling(fs.fsO1, ev.eI3);
    }
}
