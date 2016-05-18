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
public class Station {

    private Integer ID;
    private String Name;
    private Location Location;
    private boolean Active;
    private String Owner;

    public Station(int ID, String Name, Location Location, boolean Active, String Owner) {
        this.ID = ID;
        this.Name = Name;
        this.Location = Location;
        this.Active = Active;
        this.Owner = Owner;
    }

    public Integer getID() {
        return ID;
    }

    public void setID(Integer ID) {
        this.ID = ID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public Location getLocation() {
        return Location;
    }

    public void setLocation(Location Location) {
        this.Location = Location;
    }

    public boolean isActive() {
        return Active;
    }

    public void setActive(boolean Active) {
        this.Active = Active;
    }

    public String getOwner() {
        return Owner;
    }

    public void setOwner(String Owner) {
        this.Owner = Owner;
    }

}
