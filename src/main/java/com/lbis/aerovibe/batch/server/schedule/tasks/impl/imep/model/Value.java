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
public class Value {

    float Value;
    int Status;
    boolean Valid;
    String DateTime;

    public float getValue() {
        return Value;
    }

    public void setValue(float Value) {
        this.Value = Value;
    }

    public int getStatus() {
        return Status;
    }

    public void setStatus(int Status) {
        this.Status = Status;
    }

    public boolean isValid() {
        return Valid;
    }

    public void setValid(boolean Valid) {
        this.Valid = Valid;
    }

    public String getDateTime() {
        return DateTime;
    }

    public void setDateTime(String DateTime) {
        this.DateTime = DateTime;
    }

}
