/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.islands;

import java.util.ArrayList;
import models.optimizer.TopologyType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import utils.CSVHandler;
import xdevs.core.modeling.Coupled;
import xdevs.optimizer.Optimizer;

/**
 *
 * @author juanbordonruiz
 */
public class Islands extends Coupled {

    public Islands(JSONObject jsonRoot, String devspec, String tool, String scenario) {
        super("IslandsSpec1");

        // read optimization algorithms & create one Island per each one
        JSONArray algoArray = (JSONArray) jsonRoot.get("algorithms");
        ArrayList<Optimizer> opModels = new ArrayList<>();

        for (int i = 1; i <= algoArray.size(); ++i) {

            // setup of the csvHandler
            CSVHandler csvHandler = new CSVHandler(devspec, tool, scenario);

            // Test Inputs model creation        
            IslandInputs ti = new IslandInputs(jsonRoot, i, csvHandler);
            super.addComponent(ti);

            // Op model creation
            Optimizer op = new Optimizer(jsonRoot, i, csvHandler);
            super.addComponent(op);
            opModels.add(op);

            // coupling of IC (TI & PL)
            super.addCoupling(ti.tiO1, op.opI1);
            super.addCoupling(op.opO2, ti.tiI1);
        }

        // coupling of IC of OPs
        Optimizer opA, opB;
        JSONObject cntrlParamJS = (JSONObject) jsonRoot.get("cntrlParams");
        TopologyType topology
                = TopologyType.valueOf((String) cntrlParamJS.get("topology"));
        switch (topology) {
            case chain:
                for (int i = 0; i < algoArray.size() - 2; ++i) {
                    opA = opModels.get(i);
                    opB = opModels.get(i + 1);
                    // coupling of exchange solutions
                    super.addCoupling(opA.opO1, opB.opI2.get(i));
                    super.addCoupling(opB.opO1, opA.opI2.get(i));                    
                }                
                break;
                
            case ring:
                for (int i = 0; i < algoArray.size() - 1; ++i) {
                    opA = opModels.get(i);
                    if (i == algoArray.size() - 1) {
                        opB = opModels.get(0);
                    } else {
                        opB = opModels.get(i + 1);
                    }
                    // coupling of exchange solutions
                    super.addCoupling(opA.opO1, opB.opI2.get(i));
                    super.addCoupling(opB.opO1, opA.opI2.get(i));                    
                }
                break;
                
            case ring1_2:
                for (int i = 0; i < algoArray.size() - 1; ++i) {
                    opA = opModels.get(i);
                    Optimizer opC;
                    if (i == algoArray.size() - 1) {
                        opB = opModels.get(0);
                        opC = opModels.get(1);
                    } else if (i == algoArray.size() - 2) {
                        opB = opModels.get(i + 1);
                        opC = opModels.get(0);                        
                    } else { 
                        opB = opModels.get(i + 1);
                        opC = opModels.get(i + 2);
                    }
                    // coupling of exchange solutions
                    super.addCoupling(opA.opO1, opB.opI2.get(i));
                    super.addCoupling(opB.opO1, opA.opI2.get(i)); 
                    super.addCoupling(opA.opO1, opC.opI2.get(i));
                    super.addCoupling(opC.opO1, opA.opI2.get(i));                     
                }                
                break;
                
            case fullConnected:
                // coupling of IC of OPs
                for (int i = 0; i < algoArray.size() - 1; ++i) {
                    opA = opModels.get(i);
                    for (int j = i + 1; j < algoArray.size(); ++j) {
                        opB = opModels.get(j);
                        // coupling of exchange solutions
                        super.addCoupling(opA.opO1, opB.opI2.get(i));
                        super.addCoupling(opB.opO1, opA.opI2.get(j));
                    }
                }
                break;
        }
    }
}
