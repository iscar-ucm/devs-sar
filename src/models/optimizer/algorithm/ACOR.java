/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.optimizer.algorithm;

import java.util.ArrayList;
import models.optimizer.Problem;
import models.optimizer.Solution;
import models.target.Target;
import models.uav.Uav;
import org.json.simple.JSONObject;

/**
 *
 * @author Juan
 */
public class ACOR extends Algorithm {

    /**
     *
     * @param algorithmJSON
     */
    public ACOR(JSONObject algorithmJSON) {
        super(algorithmJSON);
    }      

    @Override
    public ArrayList<Solution> initialize(ArrayList<Problem> problems) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }    

    @Override
    public ArrayList<Solution> getSolutions() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public ArrayList<Solution> getFirstFront() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void evaluate(ArrayList<ArrayList<Uav>> evaluatedUavs, ArrayList<ArrayList<Target>> evaluatedTgts) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ArrayList<Solution> iterate() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void tradeIn(ArrayList<Solution> solutions) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ArrayList<Solution> tradeOut() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
