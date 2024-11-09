/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.flightSimulator.uav.sensor;

import java.util.logging.Logger;
import models.sensor.Sensor;
import models.sensor.motionModels.MotionModel;
import models.sensor.SensorCntrlSignals;
import models.sensor.SensorState;
import models.sensor.motionModels.DinamicModel;
import xdevs.core.modeling.Atomic;
import xdevs.core.modeling.Port;

/**
 *
 * @author jbbordon
 */
public class SensorControl extends Atomic {

    private static final Logger LOGGER = Logger.getLogger(SensorControl.class.getName());

    // in Ports of the model
    public Port<Sensor> scI1 = new Port<>("sensor");
    public Port<SensorState> scI2 = new Port<>("newSensorState");

    // out Ports of the model
    public Port<MotionModel> scO1 = new Port<>("motionModel");
    public Port<SensorState> scO2 = new Port<>("initState");
    public Port<Double> scO3 = new Port<>("endTime");
    public Port<SensorCntrlSignals> scO4 = new Port<>("currentCntrlSignals");
    public Port<Sensor> scO5 = new Port<>("fsSensor");

    // internal data
    private Sensor mySensor;
    private DinamicModel sensorMM;
    private SensorState mySensorState;
    private int currentIdx;
    private double clock, scenarioTime;

    public SensorControl(String coupledName, int index) {
        super(coupledName + " SC" + index);
        // Ports of the Atomic model
        super.addInPort(scI1);
        super.addInPort(scI2);
        super.addOutPort(scO1);
        super.addOutPort(scO2);
        super.addOutPort(scO3);
        super.addOutPort(scO4);
        super.addOutPort(scO5);
    }

    @Override
    public void initialize() {
        mySensor = null;
        sensorMM = null;
        mySensorState = null;
        currentIdx = 0;
        clock = 0.0;
        scenarioTime = 0.0;
        super.passivate();
    }

    @Override
    public void exit() {
        mySensor = null;
        sensorMM = null;
        mySensorState = null;
        currentIdx = 0;
        clock = 0.0;
        scenarioTime = 0.0;
        super.passivate();
    }

    @Override
    public void deltint() {
        if (phaseIs("start")) {
            if (!mySensor.getCntrlSignals().isEmpty()) {
                // wait until first control update            
                clock = mySensor.getCntrlSignals().get(currentIdx).getTime();
                super.holdIn("control", clock);
            } else {
                // cntrlSignals are empty so wait until uav endTime
                clock = mySensor.getSeqEndTime();
                super.holdIn("waiting", clock);
            }

        } else if (phaseIs("control")) {
            // update cntrlSignals pointer
            currentIdx++;
            if (currentIdx < mySensor.getCntrlSignals().size()) {
                // wait until next control update 
                clock = nextCntrlUpdate();
                super.holdIn("control", clock);
            } else {
                // all uav cntrlSignals have been processed, so wait until
                // uav endTime
                clock = mySensor.getSeqEndTime() - scenarioTime;
                super.holdIn("waiting", clock);
            }

        } else if (phaseIs("waiting")) {
            // this is to allow SMM model to report last data
            super.holdIn("end", 0.0);

        } else if (phaseIs("end")) {
            exit();
        }
    }

    @Override
    public void deltext(double e) {

        if (phaseIs("passive")) {
            // model is waiting for input data to start simulation
            if (!scI1.isEmpty()) {
                mySensor = scI1.getSingleValue();
            }
            if (mySensor != null) {
                // start simulation
                sensorMM = (DinamicModel) mySensor.getMotionModel();
                super.holdIn("start", clock);
            }

        } else if (phaseIs("control") || phaseIs("waiting")) {
            // check scI3 port for new sensorState
            if (!scI2.isEmpty()) {
                // add the newState received from SMM model
                mySensorState = scI2.getSingleValue();
                mySensor.getPath().add(mySensorState);
                scenarioTime = mySensorState.getTime();
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
            // output the init data to SMM            
            scO1.addValue(sensorMM);
            scO2.addValue(mySensor.getInitState());
            scO3.addValue(mySensor.getSeqEndTime());

        } else if (phaseIs("control")) {
            // update scenario time & output sensor cntrl signals
            scenarioTime = mySensor.getCntrlSignals().get(currentIdx).getTime();
            scO4.addValue(mySensor.getCntrlSignals().get(currentIdx));

        } else if (phaseIs("end")) {
            // simulation has ended, output the sensor with the simulated path
            scO5.addValue(mySensor);

        }
    }

    private double nextCntrlUpdate() {
        return mySensor.getCntrlSignals().get(currentIdx).getTime() - scenarioTime;
    }

}
