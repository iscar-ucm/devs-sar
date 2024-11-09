/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.optimizer;

/**
 *
 * @author juanbordonruiz
 */
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import models.target.Target;
import models.uav.Uav;
import org.ejml.data.DMatrixRMaj;

public class Solution {

    private ArrayList<Uav> uavs;
    private ArrayList<Target> tgts;
    private DMatrixRMaj constraints;
    private DMatrixRMaj paretos;
    private HashMap<String, Number> properties;

    /**
     * This constructor is used to clone a solution.
     *
     * @param uavs
     * @param tgts
     * @param constraints
     * @param paretos
     * @param properties
     */
    public Solution(
            ArrayList<Uav> uavs,
            ArrayList<Target> tgts,
            DMatrixRMaj constraints,
            DMatrixRMaj paretos,
            HashMap<String, Number> properties) {

        this.uavs = new ArrayList<>();
        for (int u = 0; u < uavs.size(); ++u) {
            // clone each uav
            this.uavs.add(uavs.get(u).clone());
        }

        this.tgts = new ArrayList<>();
        for (int t = 0; t < tgts.size(); ++t) {
            // clone each tgt
            this.tgts.add(tgts.get(t).clone());
        }
        
        this.constraints = constraints.copy();
        this.paretos = paretos.copy();

        this.properties = new HashMap<String, Number>();
        properties.entrySet().forEach(entry -> {
            this.properties.put(entry.getKey(), entry.getValue());
        });
    }

    /**
     * This constructor is used to copy an existing solution.
     *
     * @param uavs
     * @param tgts
     */
    public Solution(ArrayList<Uav> uavs, ArrayList<Target> tgts) {

        this.uavs = new ArrayList<>();
        for (int u = 0; u < uavs.size(); ++u) {
            // clone each uav
            this.uavs.add(uavs.get(u).copy());
        }

        this.tgts = new ArrayList<>();
        for (int t = 0; t < tgts.size(); ++t) {
            // clone each tgt
            this.tgts.add(tgts.get(t).copy());
        }
    }

    /**
     * This constructor is used to create a new solution with the result of an
     * evaluation.
     *
     */
    public Solution(ArrayList<Uav> uavs, ArrayList<Target> tgts, Objectives objectives) {   

        this.uavs = new ArrayList<>();
        for (int u = 0; u < uavs.size(); ++u) {
            // clone each uav
            this.uavs.add(uavs.get(u).clone());
        }

        this.tgts = new ArrayList<>();
        for (int t = 0; t < tgts.size(); ++t) {
            // clone each tgt
            this.tgts.add(tgts.get(t).clone());
        }

        constraints = new DMatrixRMaj(1, objectives.getConstraints().length);
        paretos = new DMatrixRMaj(1, objectives.getParetos().length);

        // for each uav
        for (int u = 0; u < this.uavs.size(); ++u) {

            // retrieve the u Uav
            Uav uav = this.uavs.get(u);

            // for each constraint         
            for (int c = 0; c < constraints.getNumCols(); ++c) {

                switch (objectives.getConstraints()[c]) {
                    case nfz:
                        // add the u Uav constraint value
                        constraints.add(0, c, uav.getNFZs());
                        break;

                    case collision:
                        // add the u Uav constraint value
                        constraints.add(0, c, uav.getCol());
                        break;

                }
            }

            // for each pareto         
            for (int p = 0; p < paretos.getNumCols(); ++p) {

                switch (objectives.getParetos()[p]) {
                    case fuel:
                        // calculate fuel consumption
                        double fuelConsumption
                                = uav.getInitState().getFuel()
                                - uav.getFinalState().getFuel();
                        // apply pareto factor and round it
                        fuelConsumption
                                = Math.round(fuelConsumption
                                        * objectives.getParetosFactors()[p])
                                / objectives.getParetosFactors()[p];
                        paretos.add(0, p, fuelConsumption);
                        break;

                    case smooth:
                        // calculate uav smoothness
                        double smoothValue
                                = uav.getSmoothValue();
                        // apply pareto factor and round it
                        smoothValue
                                = Math.round(smoothValue
                                        * objectives.getParetosFactors()[p])
                                / objectives.getParetosFactors()[p];
                        paretos.add(0, p, smoothValue);
                }
            }
        }
        // for each pareto      
        for (int p = 0; p < paretos.getNumCols(); ++p) {

            switch (objectives.getParetos()[p]) {

                case etd:
                    // ETD for all the tgts
                    double globalETD = 0.0;
                    // for each target
                    for (int t = 0; t < this.tgts.size(); ++t) {
                        // retrieve the t Uav
                        Target target = this.tgts.get(t);
                        globalETD
                                += target.getEtd();
                    }
                    // apply pareto factor and round it
                    globalETD
                            = Math.round(globalETD
                                    * objectives.getParetosFactors()[p])
                            / objectives.getParetosFactors()[p];
                    // add the global ETD pareto value
                    paretos.add(0, p, globalETD);
                    break;

                case pd:
                    // nDP for all the tgts
                    double globalNDP = 1.0;
                    // for each target
                    for (int t = 0; t < this.tgts.size(); ++t) {
                        // retrieve the t Uav
                        Target target = this.tgts.get(t);
                        globalNDP
                                -= (target.getPd())
                                / tgts.size();
                    }
                    // apply pareto factor and round it
                    globalNDP
                            = Math.round(globalNDP
                                    * objectives.getParetosFactors()[p])
                            / objectives.getParetosFactors()[p];
                    // add the global nDP pareto value
                    paretos.add(0, p, globalNDP);
                    break;

                case myo:
                    // myo for all the tgts
                    double globalMyo = 0.0;
                    for (int t = 0; t < this.tgts.size(); ++t) {
                        // retrieve the t Uav
                        Target target = this.tgts.get(t);
                        globalMyo
                                += (target.getMyo())
                                / tgts.size();
                    }
                    // apply pareto factor and round it
                    globalMyo
                            = Math.round(globalMyo
                                    * objectives.getParetosFactors()[p])
                            / objectives.getParetosFactors()[p];
                    // add the global myo pareto value
                    paretos.add(0, p, globalMyo);
                    break;

            }
        }
        properties = new HashMap<String, Number>();
    }

    /**
     * @return the uavs
     */
    public ArrayList<Uav> getUavs() {
        return uavs;
    }

    /**
     * @param uavs the uavs to set
     */
    public void setUavs(ArrayList<Uav> uavs) {
        this.uavs = uavs;
    }

    /**
     * @return the tgts
     */
    public ArrayList<Target> getTgts() {
        return tgts;
    }

    /**
     * @return the constraints
     */
    public DMatrixRMaj getConstraints() {
        return constraints;
    }

    /**
     * @return the paretos
     */
    public DMatrixRMaj getParetos() {
        return paretos;
    }

    /**
     * @return the constraints and paretos together
     */
    public ArrayList<Double> getResults() {
        ArrayList<Double> objectives = new ArrayList<>();
        for (int c = 0; c < getConstraints().getNumCols(); ++c) {
            objectives.add(getConstraints().get(0, c));
        }
        for (int p = 0; p < getParetos().getNumCols(); ++p) {
            objectives.add(getParetos().get(0, p));
        }
        return objectives;
    }

    public HashMap<String, Number> getProperties() {
        return properties;
    }

    public Solution copy() {
        return new Solution(getUavs(), getTgts());
    }

    @SuppressWarnings("unchecked")
    @Override
    public Solution clone() {
        return new Solution(getUavs(), getTgts(), getConstraints(), getParetos(), getProperties());
    }

    public int compareTo(Solution solution, Comparator<Solution> comparator) {
        return comparator.compare(this, solution);
    }

    @SuppressWarnings("unchecked")
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        return buffer.toString();
    }
}
