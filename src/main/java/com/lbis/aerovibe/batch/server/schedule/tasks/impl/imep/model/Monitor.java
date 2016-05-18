/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lbis.aerovibe.batch.server.schedule.tasks.impl.imep.model;

/**
 *
 * @author Development User
 */
public class Monitor {

    int ChannelID;
    String Name;
    boolean Active;
    int TypeID;
    String Units;
    Value LatestValue;

    public int getChannelID() {
        return ChannelID;
    }

    public void setChannelID(int ChannelID) {
        this.ChannelID = ChannelID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public boolean isActive() {
        return Active;
    }

    public void setActive(boolean Active) {
        this.Active = Active;
    }

    public int getTypeID() {
        return TypeID;
    }

    public void setTypeID(int TypeID) {
        this.TypeID = TypeID;
    }

    public String getUnits() {
        return Units;
    }

    public void setUnits(String Units) {
        this.Units = Units;
    }

    public Value getLatestValue() {
        return LatestValue;
    }

    public void setLatestValue(Value LatestValue) {
        this.LatestValue = LatestValue;
    }

}
