/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lbis.aerovibe.batch.server.schedule.tasks.impl.aqe.model;

/**
 *
 * @author Development User
 */
public class AQEDataStream {

    String id;
    String current_value;
    String at;
    String[] tags;
    AQEUnit unit;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCurrent_value() {
        return current_value;
    }

    public void setCurrent_value(String current_value) {
        this.current_value = current_value;
    }

    public String getAt() {
        return at;
    }

    public void setAt(String at) {
        this.at = at;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public AQEUnit getUnit() {
        return unit;
    }

    public void setUnit(AQEUnit unit) {
        this.unit = unit;
    }
}
