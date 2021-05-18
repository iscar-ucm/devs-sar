/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.planner;

import java.util.ArrayList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import models.uav.Uav;
import models.target.Target;
import models.environment.SearchArea;
import models.environment.Nfz;
import models.environment.WindMatrix;

/**
 *
 * @author jbbordon
 */
public class Scenario {

    private SearchArea searchArea;
    private ArrayList<Uav> uavs;
    private ArrayList<Target> targets;
    private Nfz[] nfzs;
    private WindMatrix windMatrix;

    /**
     *
     * @param scenarioJSON
     */
    public Scenario(JSONObject scenarioJSON) {
        
        // read the search area
        JSONObject searchAreaJS = (JSONObject) scenarioJSON.get("zone");
        searchArea = new SearchArea(searchAreaJS);

        // read nfzs
        JSONArray nfzsArray = (JSONArray) scenarioJSON.get("nfzs");
        nfzs = new Nfz[nfzsArray.size()];
        for (int i = 0; i < nfzsArray.size(); ++i) {
            Nfz iNfz = new Nfz((JSONObject) nfzsArray.get(i));
            nfzs[i] = iNfz;
        }

        // read winds matrix
        JSONObject windMatrixJS = (JSONObject) scenarioJSON.get("windMatrix");
        windMatrix = new WindMatrix(windMatrixJS);
        
        
        // read scenario uavs
        JSONArray uavsArray = (JSONArray) scenarioJSON.get("uavs");        
        uavs = new ArrayList<>();
        for (int i = 0; i < uavsArray.size(); ++i) {
            // read iUAV
            JSONObject iUavJS = (JSONObject) uavsArray.get(i);
            uavs.add(new Uav(iUavJS, searchArea));
        }

        // read scenario targets
        JSONArray targetsArray = (JSONArray) scenarioJSON.get("targets");        
        targets = new ArrayList<>();
        for (int i = 0; i < targetsArray.size(); ++i) {
            // read iTarget
            JSONObject iTargetJS = (JSONObject) targetsArray.get(i);
            targets.add(new Target(iTargetJS, searchArea));
        }
    }

    /**
     * @return the searchArea
     */
    public SearchArea getSearchArea() {
        return searchArea;
    }

    /**
     * @param searchArea the searchArea to set
     */
    public void setSearchArea(SearchArea searchArea) {
        this.searchArea = searchArea;
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
     * @return the targets
     */
    public ArrayList<Target> getTargets() {
        return targets;
    }

    /**
     * @param targets the targets to set
     */
    public void setTargets(ArrayList<Target> targets) {
        this.targets = targets;
    }

    /**
     * @return the nfzs
     */
    public Nfz[] getNfzs() {
        return nfzs;
    }

    /**
     * @param nfzs the nfzs to set
     */
    public void setNfzs(Nfz[] nfzs) {
        this.nfzs = nfzs;
    }

    /**
     * @return the windMatrix
     */
    public WindMatrix getWindMatrix() {
        return windMatrix;
    }

    /**
     * @param windMatrix the windMatrix to set
     */
    public void setWindArray(WindMatrix windMatrix) {
        this.windMatrix = windMatrix;
    }

}
