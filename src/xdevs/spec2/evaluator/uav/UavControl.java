/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.spec2.evaluator.uav;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import xdevs.core.modeling.Atomic;
import xdevs.core.modeling.Port;
import models.uav.Uav;
import models.uav.UavState;
import models.sensor.Sensor;

/**
 *
 * @author jbbordon
 */
public class UavControl extends Atomic {

    private static final Logger LOGGER = Logger.getLogger(UavControl.class.getName());

    // in Ports of the model
    public Port<Uav> ucI1 = new Port<>("uav");

    // out Ports of the model
    public ArrayList<Port> ucO1 = new ArrayList<>(); // isensor to iSP model
    public Port<UavState> ucO2 = new Port<>("uavState"); // current uav state to iSP models

    // internal data
    protected Uav myUav;
    protected int currentStateIdx;
    protected double clock, scenarioTime;

    public UavControl(String coupledName, int numSensors) {
        super(coupledName + " UC");
        // Ports of the Atomic model
        super.addInPort(ucI1);
        // s ports ucO1 (one per sensor of the uav)
        for (int s = 1; s <= numSensors; ++s) {
            Port<Sensor> ucPortO1 = new Port<>("sensor" + s);
            ucO1.add(ucPortO1);
            super.addOutPort(ucPortO1);
        }
        super.addOutPort(ucO2);
    }

    @Override
    public void initialize() {
        myUav = null;
        currentStateIdx = 0;
        clock = 0.0;
        scenarioTime = 0.0;
        super.passivate();
    }

    @Override
    public void exit() {
        myUav = null;
        currentStateIdx = 0;
        clock = 0.0;
        scenarioTime = 0.0;
        super.passivate();
    }

    @Override
    public void deltint() {
        if (phaseIs("start")) {
            // wait until next uav state          
            clock = nextUavUpdate();
            super.holdIn("flying", clock);
            
        } else if (phaseIs("flying")) {
            // update state pointer       
            currentStateIdx++;
            if (currentStateIdx < myUav.getPath().size()) {
                // wait until next uav state 
                clock = nextUavUpdate() - scenarioTime;
                super.holdIn("flying", clock);
            } else {
                exit();
                LOGGER.log(
                        Level.ALL,
                        String.format(
                                "%1$s: UAV EVALUATION END",
                                this.getName()
                        )
                );
            }
        } 
    }

    @Override
    public void deltext(double e) {
        if (phaseIs("passive")) {
            // model is waiting for input data to start simulation
            if (!ucI1.isEmpty()) {
                myUav = ucI1.getSingleValue();
            }
            if ( myUav != null) {
                // start simulation
                super.holdIn("start", clock);
                LOGGER.log(
                        Level.ALL,
                        String.format(
                                "%1$s: UAV EVALUATION START",
                                this.getName()
                        )
                );
            }
        }
    }

    @Override
    public void lambda() {
        if (phaseIs("start")) {
            // output the init data to SPM modules
            for (int s = 0; s < myUav.getSensors().size(); ++s) {
                // output sensor
                ucO1.get(s).addValue(myUav.getSensors().get(s));
            }
            
        } else if (phaseIs("flying")) {
            // update scenario time & output uav new state
            scenarioTime = nextUavUpdate();
            ucO2.addValue(nextUavState());
            
        }
    }

    private UavState nextUavState() {
        return myUav.getPath().get(currentStateIdx);
    }

    private double nextUavUpdate() {
        return myUav.getPath().get(currentStateIdx).getTime();
    }
}
