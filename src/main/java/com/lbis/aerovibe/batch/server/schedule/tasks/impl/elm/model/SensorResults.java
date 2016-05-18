/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.lbis.aerovibe.batch.server.schedule.tasks.impl.elm.model;

/**
 *
 * @author administrator
 */
public class SensorResults {
    Sensor station;
    SensorResult[] results;

    public Sensor getStation() {
        return station;
    }

    public void setStation(Sensor station) {
        this.station = station;
    }

    public SensorResult[] getResults() {
        return results;
    }

    public void setResults(SensorResult[] results) {
        this.results = results;
    }
    
    
    
    
}
