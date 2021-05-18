/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.flightSimulator;

import java.util.ArrayList;
import models.environment.SearchArea;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import xdevs.core.modeling.Coupled;
import xdevs.core.modeling.Port;
import models.environment.WindMatrix;
import models.sensor.payloads.Likelihood;
import models.uav.Uav;
import xdevs.flightSimulator.uav.UavModel;

/**
 *
 * @author jbbordon
 */
public class FlightSimulator extends Coupled {

    // in Ports of the model
    public Port<SearchArea> fsI1 = new Port<>("zone");
    public Port<WindMatrix> fsI2 = new Port<>("windMatrix");
    public ArrayList<Port> fsI3 = new ArrayList<>(); // iUAV to simulate

    // out Ports of the model
    public ArrayList<Port> fsO1 = new ArrayList<>(); // u*s sensor Likelihood at time t
    public ArrayList<Port> fsO2 = new ArrayList<>(); // iUAV simulation

    public FlightSimulator(int fsIndex, JSONArray uavsJS) {
        super("FS" + fsIndex);

        // EIC of the model
        super.addInPort(fsI1);
        super.addInPort(fsI2);

        // loop uavs JSONArray
        int numSensor = 0;
        for (int i = 0; i < uavsJS.size(); ++i) {

            // variables to be used in the model creation process
            JSONObject iuavJS = (JSONObject) uavsJS.get(i);
            JSONArray sensorJS = (JSONArray) iuavJS.get("sensors");

            // EIC of the model: i ports fsI3 (one per uav in the scenario)
            Port<Uav> iPortI3 = new Port<>("uav" + i);
            fsI3.add(iPortI3);
            super.addInPort(iPortI3);

            Port<Uav> iPortO2 = new Port<>("fsUav" + i);
            fsO2.add(iPortO2);
            super.addOutPort(iPortO2);

            // iUM model creation
            UavModel iUM
                    = new UavModel(this.getName(), i + 1, iuavJS);
            super.addComponent(iUM);

            // i * s ports fsO1 (one per sensor in the scenario)
            for (int s = 0; s < sensorJS.size(); ++s) {
                Port<Likelihood> iPortO1 = new Port<>("sensorLikelihood U" + i + "S" + s);
                fsO1.add(iPortO1);
                super.addOutPort(iPortO1);
                // coupling of EOC
                super.addCoupling(iUM.uO1.get(s), fsO1.get(numSensor));
                numSensor++;
            }

            // coupling of EIC
            super.addCoupling(fsI1, iUM.uI1);
            super.addCoupling(fsI2, iUM.uI2);
            super.addCoupling(fsI3.get(i), iUM.uI3);

            // coupling of EOC
            super.addCoupling(iUM.uO2, fsO2.get(i));
        }
    }
}
