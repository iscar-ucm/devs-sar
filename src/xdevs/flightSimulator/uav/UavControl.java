/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.flightSimulator.uav;

import org.json.simple.JSONArray;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import models.environment.SearchArea;
import xdevs.core.modeling.Atomic;
import xdevs.core.modeling.Port;
import models.uav.Uav;
import models.uav.motionModels.MotionModel;
import models.uav.UavCntrlSignals;
import models.uav.UavState;
import models.environment.Wind;
import models.environment.WindMatrix;
import models.sensor.Sensor;

/**
 *
 * @author jbbordon
 */
public class UavControl extends Atomic {

    private static final Logger LOGGER = Logger.getLogger(UavControl.class.getName());

    // in Ports of the model
    public Port<SearchArea> ucI1 = new Port<>("searchArea");
    public Port<WindMatrix> ucI2 = new Port<>("windMatrix");
    public Port<Uav> ucI3 = new Port<>("uav");
    public Port<UavState> ucI4 = new Port<>("newState");
    public ArrayList<Port> ucI5 = new ArrayList<>(); // sensor simulation from SM models

    // out Ports of the model
    public Port<MotionModel> ucO1 = new Port<>("motionModel");
    public Port<UavState> ucO2 = new Port<>("initState");
    public Port<Double> ucO3 = new Port<>("endTime");
    public Port<UavCntrlSignals> ucO4 = new Port<>("currentCntrlSignals");
    public Port<Wind> ucO5 = new Port<>("wind");
    public ArrayList<Port> ucO6 = new ArrayList<>(); // uav sensors to SM models
    public Port<Uav> ucO7 = new Port<>("uavPath");

    // internal data
    protected SearchArea mySearchArea;
    protected Uav myUav;
    protected UavState myUavState;
    protected ArrayList<Sensor> mySensors;
    protected WindMatrix windMatrix;
    protected Wind prevWind, currentWind;
    protected int cntrlSignalIdx;
    protected double clock, scenarioTime;
    protected String prevPhase;

    public UavControl(String coupledName, JSONArray sensorJSArray) {
        super(coupledName + " UC");
        // Ports of the Atomic model
        super.addInPort(ucI1);
        super.addInPort(ucI2);
        super.addInPort(ucI3);
        super.addInPort(ucI4);
        super.addOutPort(ucO1);
        super.addOutPort(ucO2);
        super.addOutPort(ucO3);
        super.addOutPort(ucO4);
        super.addOutPort(ucO5);
        super.addOutPort(ucO7);
        for (int j = 1; j <= sensorJSArray.size(); ++j) {
            // i ports ucI5 & ucO6 (one per sensor in the uav)
            Port<Sensor> jPortI5 = new Port<>("fsSensor" + j);
            ucI5.add(jPortI5);
            super.addInPort(jPortI5);
            Port<Sensor> jPortO7 = new Port<>("sensor" + j);
            ucO6.add(jPortO7);
            super.addOutPort(jPortO7);
        }
    }

    @Override
    public void initialize() {
        mySearchArea = null;
        myUav = null;
        myUavState = null;
        mySensors = new ArrayList<>();
        windMatrix = null;
        currentWind = null;
        prevWind = null;
        cntrlSignalIdx = 0;
        clock = 0.0;
        scenarioTime = 0.0;
        prevPhase = "";
        super.passivate();
    }

    @Override
    public void exit() {
        mySearchArea = null;
        myUav = null;
        myUavState = null;
        mySensors = new ArrayList<>();
        windMatrix = null;
        currentWind = null;
        prevWind = null;
        cntrlSignalIdx = 0;
        clock = 0.0;
        scenarioTime = 0.0;
        prevPhase = "";
        super.passivate();
    }

    @Override
    public void deltint() {
        if (phaseIs("start")) {
            if (!myUav.getCntrlSignals().isEmpty()) {
                // wait until first control update            
                clock = myUav.getCntrlSignals().get(cntrlSignalIdx).getTime();
                super.holdIn("control", clock);
            } else {
                // cntrlSignals are empty so wait until uav endTime
                clock = myUav.getSeqEndTime();
                super.holdIn("waiting", clock);                
            }

        } else if (phaseIs("control")) {
            // update cntrlSignals pointer
            cntrlSignalIdx++;
            if (cntrlSignalIdx < myUav.getCntrlSignals().size()) {
                // wait until next control update 
                clock = nextCntrlUpdate();
                super.holdIn("control", clock);
            } else {
                // all uav cntrlSignals have been processed, so wait until
                // uav endTime
                clock = myUav.getSeqEndTime() - scenarioTime;
                super.holdIn("waiting", clock);
            }

        } else if (phaseIs("updateWind")) {
            // wait remaining time
            super.holdIn(prevPhase, clock);

        } else if (phaseIs("waiting")) {
            // this is to allow UMM & SM models to report last data
            super.holdIn("end", 1.0);

        } else if (phaseIs("end")) {
            LOGGER.log(
                    Level.ALL,
                    String.format(
                            "%1$s: UAV FLIGHT SIMULATION END",
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
            if (!ucI1.isEmpty()) {
                mySearchArea = ucI1.getSingleValue();
            }
            if (!ucI2.isEmpty()) {
                windMatrix = ucI2.getSingleValue();
            }
            if (!ucI3.isEmpty()) {
                myUav = ucI3.getSingleValue();
                mySensors = myUav.getSensors();
            }
            if (mySearchArea != null && myUav != null && windMatrix != null) {
                // start simulation as all int data has been received
                currentWind = windMatrix.getUavWind(myUav.getInitState());
                super.holdIn("start", clock);
                LOGGER.log(
                        Level.ALL,
                        String.format(
                                "%1$s: UAV FLIGHT SIMULATION STARTS",
                                this.getName()
                        )
                );
            }

        } else if (phaseIs("control") || phaseIs("waiting") || phaseIs("end")) {

            // check new state from UMM model
            if (!ucI4.isEmpty()) {

                // a new state has arrived
                myUavState = ucI4.getSingleValue();
                myUav.getPath().add(myUavState);
                scenarioTime = myUavState.getTime();

                // check if wind should be updated or not
                prevWind = currentWind;
                currentWind = windMatrix.getUavWind(myUavState);
                if (!Wind.equals(prevWind, currentWind) && !phaseIs("end")) {
                    clock -= e;
                    prevPhase = super.getPhase();
                    super.holdIn("updateWind", 0.0);
                }
            }

            for (int j = 0; j < mySensors.size(); ++j) {
                // check new fsSensor from iSM model
                if (!ucI5.get(j).isEmpty()) {
                    // new fsSensor arrived
                    mySensors.set(j, (Sensor) ucI5.get(j).getSingleValue());
                }
            }
        }
    }

    @Override
    public void deltcon() {
        deltext(0);
        deltint();
    }    
    
    @Override
    public void lambda() {

        if (phaseIs("start")) {
            // output the init data to UMM
            ucO1.addValue(myUav.getMotionModel());
            ucO2.addValue(myUav.getInitState());
            ucO3.addValue(myUav.getSeqEndTime());
            ucO5.addValue(currentWind);
            // output the init data to SMM
            for (int j = 0; j < mySensors.size(); ++j) {
                ucO6.get(j).addValue(mySensors.get(j));
            }

        } else if (phaseIs("control")) {
            // update scenario time & output uav cntrl signals
            scenarioTime = myUav.getCntrlSignals().get(cntrlSignalIdx).getTime();
            ucO4.addValue(myUav.getCntrlSignals().get(cntrlSignalIdx));

        } else if (phaseIs("updateWind")) {
            // update uav wind
            ucO5.addValue(currentWind);

        } else if (phaseIs("end")) {
            // simulation has ended, output the uav with the simulated path & sensors
            ucO7.addValue(myUav);
        }
    }

    private double nextCntrlUpdate() {
        return myUav.getCntrlSignals().get(cntrlSignalIdx).getTime() - scenarioTime;
    }
}
