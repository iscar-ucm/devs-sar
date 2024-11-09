/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.optimizer;

import org.json.simple.JSONObject;
import utils.CSVHandler;
import xdevs.core.modeling.Coupled;

/**
 *
 * @author bordon
 */
public class OpTool1 extends Coupled {

    public OpTool1(JSONObject jsonRoot, CSVHandler csvHandler) {
        super("OptimizatorToolSpec1");
        
            // Test Inputs model creation        
            OpInputs ti = new OpInputs(jsonRoot, csvHandler);
            super.addComponent(ti);

            // Op model creation
            Optimizer op = new Optimizer(jsonRoot, 1, csvHandler);
            super.addComponent(op);

            // coupling of IC (TI & PL)
            super.addCoupling(ti.tiO1, op.opI1);
            super.addCoupling(op.opO2, ti.tiI1);

    }
}
