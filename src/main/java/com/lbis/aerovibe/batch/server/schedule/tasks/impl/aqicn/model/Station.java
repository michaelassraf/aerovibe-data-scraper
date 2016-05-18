/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lbis.aerovibe.batch.server.schedule.tasks.impl.aqicn.model;

/**
 *
 * @author Development User
 */
public class Station {

    Long Timestamp;
    Float Temperature;
    Float So2;
    String Coordinates;
    Float Humidity;
    Float Pressure;
    String Time;
    Float O3;
    Float Wind;
    String Name;
    Float PM10;
    Float PM2_5;
    Float No2;

    public Long getTimestamp() {
        return Timestamp;
    }

    public void setTimestamp(Long Timestamp) {
        this.Timestamp = Timestamp;
    }
    
    public Float getTemperature() {
        return Temperature;
    }

    public void setTemperature(Float Temperature) {
        this.Temperature = Temperature;
    }

    public Float getSo2() {
        return So2;
    }

    public void setSo2(Float So2) {
        this.So2 = So2;
    }

    public String getCoordinates() {
        return Coordinates;
    }

    public void setCoordinates(String Coordinates) {
        this.Coordinates = Coordinates;
    }

    public Float getHumidity() {
        return Humidity;
    }

    public void setHumidity(Float Humidity) {
        this.Humidity = Humidity;
    }

    public Float getPressure() {
        return Pressure;
    }

    public void setPressure(Float Pressure) {
        this.Pressure = Pressure;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String Time) {
        this.Time = Time;
    }

    public Float getO3() {
        return O3;
    }

    public void setO3(Float O3) {
        this.O3 = O3;
    }

    public Float getWind() {
        return Wind;
    }

    public void setWind(Float Wind) {
        this.Wind = Wind;
    }

    public String getName() {
        return Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public Float getPM10() {
        return PM10;
    }

    public void setPM10(Float PM10) {
        this.PM10 = PM10;
    }

    public Float getPM2_5() {
        return PM2_5;
    }

    public void setPM2_5(Float PM2_5) {
        this.PM2_5 = PM2_5;
    }

    public Float getNo2() {
        return No2;
    }

    public void setNo2(Float No2) {
        this.No2 = No2;
    }

}
