/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.evaluator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import utils.CSVHandler;
import xdevs.core.modeling.Coupled;
import xdevs.flightSimulator.FlightSimulator;

/**
 *
 * @author bordon
 */
public class EvTool1 extends Coupled {

    public EvTool1(JSONObject jsonRoot, boolean fullPath, CSVHandler csvHandler) {
        super("SimulatorTest");

        JSONArray targetsArray = (JSONArray) jsonRoot.get("targets");
        JSONArray uavsArray = (JSONArray) jsonRoot.get("uavs");

        // Test EvInputs model creation        
        EvInputs ti = new EvInputs(jsonRoot, fullPath, csvHandler);
        super.addComponent(ti);

        // FS model creation
        FlightSimulator fs
                = new FlightSimulator(1, uavsArray);
        super.addComponent(fs);

        // EV model creation
        Evaluator ev
                = new Evaluator(1, uavsArray, targetsArray);
        super.addComponent(ev);

        // coupling of IC (TI & FS)
        super.addCoupling(ti.tiO1, fs.fsI1);
        super.addCoupling(ti.tiO3, fs.fsI2);
        int numSensor = 0;
        for (int u = 0; u < uavsArray.size(); ++u) {

            super.addCoupling(ti.tiO4.get(u), fs.fsI3.get(u));

            // coupling of IC (FS & EV)
            super.addCoupling(fs.fsO2.get(u), ev.eI5.get(u));
            JSONObject uavJS = (JSONObject) uavsArray.get(u);
            JSONArray sensorsArray = (JSONArray) uavJS.get("sensors");
            for (int s = 0; s < sensorsArray.size(); ++s) {
                super.addCoupling(fs.fsO1.get(numSensor), ev.eI4.get(numSensor));
                numSensor++;
            }

            // coupling of IC (TI & EV)
            super.addCoupling(ev.eO1.get(u), ti.tiI1.get(u));
        }

        // coupling of IC (TI & EV)
        super.addCoupling(ti.tiO1, ev.eI1);
        super.addCoupling(ti.tiO2, ev.eI2);
        for (int t = 0; t < targetsArray.size(); ++t) {
            super.addCoupling(ti.tiO5.get(t), ev.eI3.get(t));
            super.addCoupling(ev.eO2.get(t), ti.tiI2.get(t));
        }
    }
}
