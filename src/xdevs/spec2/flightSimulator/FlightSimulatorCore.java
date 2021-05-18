/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.spec2.flightSimulator;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import xdevs.core.modeling.Atomic;
import xdevs.core.modeling.Port;
import models.environment.SearchArea;
import models.environment.WindMatrix;
import models.uav.Uav;

/**
 *
 * @author jbbordon
 */
public class FlightSimulatorCore extends Atomic {

    private static final Logger LOGGER = Logger.getLogger(FlightSimulatorCore.class.getName());

    // in Ports of the model
    public Port<SearchArea> fscI1 = new Port<>("zone");
    public Port<WindMatrix> fscI2 = new Port<>("windMatrix");
    public Port<ArrayList<Uav>> fscI3 = new Port<>("uavs");
    public ArrayList<Port> fscI4 = new ArrayList<>();  // simulated iUav  

    // out Ports of the model
    public Port<SearchArea> fscO1 = new Port<>("zone");    
    public Port<WindMatrix> fscO2 = new Port<>("windMatrix");    
    public ArrayList<Port> fscO3 = new ArrayList<>(); // iUav
    public Port<ArrayList<Uav>> fscO4 = new Port<>("simulatedUavs");

    // internal data
    protected SearchArea myZone;
    protected WindMatrix windMatrix;
    protected ArrayList<Uav> scenarioUavs;
    protected int windIdx, uavsRcvd;
    protected double clock, scenarioTime;

    public FlightSimulatorCore(String coupledName, int numUavs) {
        super(coupledName + " FSC");
        // Ports of the Atomic model
        super.addInPort(fscI1);
        super.addInPort(fscI2);
        super.addInPort(fscI3);
        for (int i = 1; i <= numUavs; ++i) {
            // i ports fscI4 (one per uav in the scenario)
            Port<Uav> iPortI4 = new Port<>("uav" + i);
            fscI4.add(iPortI4);
            super.addInPort(iPortI4);
            // i ports fscO3 (one per uav in the scenario)
            Port<Uav> iPortO1 = new Port<>("sUav" + i);
            fscO3.add(iPortO1);
            super.addOutPort(iPortO1);
        }
        super.addOutPort(fscO1);        
        super.addOutPort(fscO2);
        super.addOutPort(fscO4);
    }

    @Override
    public void initialize() {
        myZone = null;
        windMatrix = null;
        scenarioUavs = new ArrayList<>();
        windIdx = 0;
        uavsRcvd = 0;
        clock = 0.0;
        scenarioTime = 0.0;
        super.passivate();
    }

    @Override
    public void exit() {
        myZone = null;
        windMatrix = null;
        scenarioUavs = new ArrayList<>();
        windIdx = 0;
        uavsRcvd = 0;
        clock = 0.0;
        scenarioTime = 0.0;
        super.passivate();
    }

    @Override
    public void deltint() {
        if (phaseIs("start")) {
            // wait until all uav simulations arrive
            clock = Double.MAX_VALUE;
            super.holdIn("simulate", clock);
        } else if (phaseIs("end")) {
            LOGGER.log(
                    Level.FINEST,
                    String.format(
                            "%1$s: FLIGHT SIMULATION ENDS",
                            this.getName()
                    )
            );
            exit();
        }
    }

    @Override
    public void deltext(double e) {
        if (phaseIs("passive")) {
            // model is waiting for input data to start simulation
            if (!fscI1.isEmpty()) {
                myZone = fscI1.getSingleValue();
            }
            if (!fscI2.isEmpty()) {
                windMatrix = fscI2.getSingleValue();
            }
            if (!fscI3.isEmpty()) {
                scenarioUavs = fscI3.getSingleValue();
            }
            if (myZone != null && windMatrix != null && scenarioUavs.size() > 0) {
                // start simulation as all data has been received
                super.holdIn("start", clock);
                LOGGER.log(
                        Level.FINEST,
                        String.format(
                                "%1$s: FLIGHT SIMULATION STARTS",
                                this.getName()
                        )
                );
            }
        } else if (phaseIs("simulate")) {
            // check iUM in ports for new uav simulations
            for (int i = 0; i < scenarioUavs.size(); ++i) {
                if (!fscI4.get(i).isEmpty()) {
                    // new uav simulation has arrived
                    scenarioUavs.set(i, (Uav) fscI4.get(i).getSingleValue());
                    uavsRcvd++;
                }
            }
            // check if all uav simulations have been received
            if (uavsRcvd == scenarioUavs.size()) {
                // simulation shall be finished
                clock = 0.0;
                super.holdIn("end", clock);
            }
        }
    }

    @Override
    public void lambda() {
        if (phaseIs("start")) {
            // output the init data to the iUM models
            fscO1.addValue(myZone);            
            fscO2.addValue(windMatrix);            
            for (int i = 0; i < scenarioUavs.size(); ++i) {
                fscO3.get(i).addValue(scenarioUavs.get(i));
            }
        } else if (phaseIs("end")) {
            // output the scenario UAVs with the simulated path
            fscO4.addValue(scenarioUavs);
        }
    }
}
