/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lbis.aerovibe.batch.server.schedule.tasks.impl.aqe.model;

import java.util.ArrayList;

/**
 *
 * @author Development User
 */
public class AQEResults {

    Integer totalResults;
    Integer itemsPerPage;
    Integer startIndex;
    ArrayList<AQEResult> results;

    public ArrayList<AQEResult> getResults() {
        return results;
    }

    public void setResults(ArrayList<AQEResult> results) {
        this.results = results;
    }

    public Integer getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(Integer totalResults) {
        this.totalResults = totalResults;
    }

    public Integer getItemsPerPage() {
        return itemsPerPage;
    }

    public void setItemsPerPage(Integer itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }

    public Integer getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(Integer startIndex) {
        this.startIndex = startIndex;
    }
}
