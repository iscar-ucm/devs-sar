/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.spec2.evaluator;

import java.util.ArrayList;
import xdevs.spec2.evaluator.uav.UavModel;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import java.util.logging.Logger;
import xdevs.core.modeling.Coupled;
import xdevs.core.modeling.Port;
import xdevs.evaluator.target.TargetModel;
import models.environment.SearchArea;
import models.environment.Nfz;
import models.uav.Uav;
import models.target.Target;

/**
 *
 * @author jbbordon
 */
public class Evaluator extends Coupled {

    private static final Logger LOGGER = Logger.getLogger(Evaluator.class.getName());

    // in Ports of the model
    public Port<SearchArea> eI1 = new Port<>("searchArea");
    public Port<Nfz[]> eI2 = new Port<>("nfzs");
    public Port<ArrayList<Uav>> eI3 = new Port<>("uavs");
    public Port<ArrayList<Target>> eI4 = new Port<>("targets");

    // out Ports of the model
    public Port<ArrayList<Uav>> eO1 = new Port<>("uavs");
    public Port<ArrayList<Target>> eO2 = new Port<>("targets");

    public Evaluator(int eIndex, JSONArray uavsJS, JSONArray targetsJS) {
        super("EV" + eIndex);

        // EIC & EOC of the model
        super.addInPort(eI1);
        super.addInPort(eI2);
        super.addInPort(eI3);
        super.addInPort(eI4);
        super.addOutPort(eO1);
        super.addOutPort(eO2);

        // EC model creation
        EvaluatorFunction ef = new EvaluatorFunction(this.getName(), uavsJS.size(), targetsJS.size());
        super.addComponent(ef);

        // variables to be used in the model creation process
        UavModel[] iUMArray = new UavModel[uavsJS.size()];
        int numSensors = 0;

        // iUM model creations
        for (int i = 0; i < uavsJS.size(); ++i) {
            JSONObject iuavJS = (JSONObject) uavsJS.get(i);
            UavModel iUM = new UavModel(this.getName(), i + 1, iuavJS);
            iUMArray[i] = iUM;
            super.addComponent(iUM);
            // coupling of IC (EC & iUM)
            super.addCoupling(ef.efO1.get(i), iUM.uI1);
            // update numSensors
            JSONArray sensorJS = (JSONArray) iuavJS.get("sensors");
            numSensors += sensorJS.size();            
        }

        // tTM model creations
        for (int t = 0; t < targetsJS.size(); ++t) {
            // tTM model creation       
            TargetModel tTM
                    = new TargetModel(this.getName(), t + 1, (JSONObject) targetsJS.get(t), numSensors);
            super.addComponent(tTM);
            // coupling of IC (EC & tTM)
            super.addCoupling(ef.efO2.get(t), tTM.tI1);
            super.addCoupling(tTM.tO1, ef.efI5.get(t));
            // coupling of IC (iUM & tTM)
            numSensors = 0;
            for (int i = 0; i < uavsJS.size(); ++i) {
                for (int k = 0; k < iUMArray[i].uO1.size(); ++k) {
                    super.addCoupling(iUMArray[i].uO1.get(k), tTM.tI2.get(numSensors));
                    numSensors++;
                }
            }
        }

        // coupling of EIC & EOC
        super.addCoupling(eI1, ef.efI1);
        super.addCoupling(eI2, ef.efI2);
        super.addCoupling(eI3, ef.efI3);
        super.addCoupling(eI4, ef.efI4);
        super.addCoupling(ef.efO3, eO1);
        super.addCoupling(ef.efO4, eO2);
    }
}
