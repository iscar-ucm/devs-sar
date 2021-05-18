/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.evaluator;

import java.util.ArrayList;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import java.util.logging.Logger;
import models.environment.Nfz;
import models.environment.SearchArea;
import models.sensor.payloads.Likelihood;
import models.target.Target;
import models.uav.Uav;
import xdevs.core.modeling.Coupled;
import xdevs.core.modeling.Port;
import xdevs.evaluator.target.TargetModel;

/**
 *
 * @author jbbordon
 */
public class Evaluator extends Coupled {

    private static final Logger LOGGER = Logger.getLogger(Evaluator.class.getName());

    // in Ports of the model
    public Port<SearchArea> eI1 = new Port<>("searchArea");
    public Port<Nfz[]> eI2 = new Port<>("nfzs");
    public ArrayList<Port> eI3 = new ArrayList<>(); // t Target to evaluate    
    public ArrayList<Port> eI4 = new ArrayList<>(); // u * s sensorLikelihoods at time t
    public ArrayList<Port> eI5 = new ArrayList<>(); // u fsUav from FS UM    

    // out Ports of the model
    public ArrayList<Port> eO1 = new ArrayList<>(); // u eUav
    public ArrayList<Port> eO2 = new ArrayList<>(); // t evaluatedTarget         

    public Evaluator(int eIndex, JSONArray uavsJS, JSONArray targetsJS) {
        super("EV" + eIndex);

        // variables to be used in the model creation process
        int numSensors = 0;
        for (int u = 0; u < uavsJS.size(); ++u) {
            JSONObject iuavJS = (JSONObject) uavsJS.get(u);
            JSONArray sensorJS = (JSONArray) iuavJS.get("sensors");
            numSensors += sensorJS.size();
        }

        super.addInPort(eI1);
        super.addInPort(eI2);

        // EF model creation       
        EvaluatorFunction ef
                = new EvaluatorFunction(this.getName(), uavsJS.size(), targetsJS.size());
        super.addComponent(ef);
        // coupling of EIC
        super.addCoupling(eI1, ef.efI1);
        super.addCoupling(eI2, ef.efI2);

        for (int u = 0; u < uavsJS.size(); ++u) {
            // u ports eI5 & eO1 (one per uav in the scenario)
            Port<Uav> uPortI5 = new Port<>("fsUav" + u);
            eI5.add(uPortI5);
            super.addInPort(uPortI5);
            Port<Uav> uPortO1 = new Port<>("eUav" + u);
            eO1.add(uPortO1);
            super.addOutPort(uPortO1);
            // coupling of EIC                      
            super.addCoupling(eI5.get(u), ef.efI3.get(u));
            // coupling of EOC     
            super.addCoupling(ef.efO1.get(u), eO1.get(u));

        }

        // for each target to be evaluated
        for (int t = 0; t < targetsJS.size(); ++t) {

            // tTM model creation       
            TargetModel tTM
                    = new TargetModel(this.getName(), t + 1, (JSONObject) targetsJS.get(t), numSensors);
            super.addComponent(tTM);

            // t ports eI3 (one per target in the scenario)
            Port<Target> tPortI1 = new Port<>("target" + t);
            eI3.add(tPortI1);
            super.addInPort(tPortI1);
            // coupling of EIC                       
            super.addCoupling(eI3.get(t), tTM.tI1);

            for (int s = 0; s < numSensors; ++s) {
                // u * s ports eI4 (one per sensor in the scenario)
                Port<Likelihood> uPortI4 = new Port<>("sensorLikelihood" + s);
                eI4.add(uPortI4);
                super.addInPort(uPortI4);
                // coupling of EIC
                super.addCoupling(eI4.get(s), tTM.tI2.get(s));
            }

            // t ports eO2 (one per target in the scenario)
            Port<Target> tPortO1 = new Port<>("eTarget" + t);
            eO2.add(tPortO1);
            super.addOutPort(tPortO1);

            // coupling of IC (tTM & EF)
            super.addCoupling(tTM.tO1, ef.efI4.get(t));

            // coupling of EOC     
            super.addCoupling(ef.efO2.get(t), eO2.get(t));
        }
    }
}
