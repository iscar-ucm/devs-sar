/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.environment;

import org.json.simple.JSONObject;

/**
 *
 * @author jbbordon
 */
public class Nfz {

    // Nfz searchArea cell
    private int xRow;
    private int yCol;

    /**
     *
     * @param nfzJSON
     */
    public Nfz(JSONObject nfzJSON) {
        xRow = (int) (long) nfzJSON.get("xRow");
        yCol = (int) (long) nfzJSON.get("yCol");
    }

    /**
     * @return the row number of the Nfz cell
     */
    public int getxRow() {
        return xRow;
    }

    /**
     * @param xRow the row number to set
     */
    public void setxRow(short xRow) {
        this.xRow = xRow;
    }

    /**
     * @return the col number of the Nfz cell
     */
    public int getyCol() {
        return yCol;
    }

    /**
     * @param yCol the col number to set
     */
    public void setyCol(short yCol) {
        this.yCol = yCol;
    }

}
