/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.spec2.flightSimulator;

import java.util.ArrayList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import xdevs.core.modeling.Coupled;
import xdevs.core.modeling.Port;
import xdevs.spec2.flightSimulator.uav.UavModel;
import models.environment.SearchArea;
import models.environment.WindMatrix;
import models.uav.Uav;

/**
 *
 * @author jbbordon
 */
public class FlightSimulator extends Coupled {

    // in Ports of the model
    public Port<SearchArea> fsI1 = new Port<>("zone");
    public Port<WindMatrix> fsI2 = new Port<>("windMatrix");
    public Port<ArrayList<Uav>> fsI3 = new Port<>("uavs");

    // out Ports of the model
    public Port<ArrayList<Uav>> fsO1 = new Port<>("uavPaths");

    public FlightSimulator(int fsIndex, JSONArray uavsJS) {
        super("FS" + fsIndex);

        // EIC & EOC of the model
        super.addInPort(fsI1);
        super.addInPort(fsI2);
        super.addInPort(fsI3);
        super.addOutPort(fsO1);

        // FSC model creation
        FlightSimulatorCore fsc = new FlightSimulatorCore(this.getName(), uavsJS.size());
        super.addComponent(fsc);

        // iUM model creations
        for (int i = 0; i < uavsJS.size(); ++i) {
            // iUM model creation
            UavModel iUM = new UavModel(this.getName(), i + 1, (JSONObject) uavsJS.get(i));
            super.addComponent(iUM);
            // coupling of IC (FSC & iUM)
            super.addCoupling(fsc.fscO1, iUM.uI1);            
            super.addCoupling(fsc.fscO2, iUM.uI2);
            super.addCoupling(fsc.fscO3.get(i), iUM.uI3);            
            super.addCoupling(iUM.uO1, fsc.fscI4.get(i));
        }

        // coupling of EIC & EOC
        super.addCoupling(fsI1, fsc.fscI1);
        super.addCoupling(fsI2, fsc.fscI2);
        super.addCoupling(fsI3, fsc.fscI3);
        super.addCoupling(fsc.fscO4, fsO1);
    }

}
