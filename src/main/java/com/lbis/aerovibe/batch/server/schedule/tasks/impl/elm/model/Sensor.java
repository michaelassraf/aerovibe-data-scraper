/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lbis.aerovibe.batch.server.schedule.tasks.impl.elm.model;

/**
 *
 * @author Development User
 */
public class Sensor {

    Integer sensorID;
    String address;
    Double latitude;
    Double longitude;
    String altitude;
    String altitudeFromGround;
    String description;
    String detailes;
    String sensorSource;
    String indexFromSource;
    String dateLastWorked;
    Boolean sensorPublic;
    Boolean sensorOutdoor;
    Float sensorGMTByUser;
    Integer sensorsLast30minAvarageLevel;
    Integer sensorsLast5minAvarageLevel;

    public Integer getSensorID() {
        return sensorID;
    }

    public void setSensorID(Integer sensorID) {
        this.sensorID = sensorID;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getAltitude() {
        return altitude;
    }

    public void setAltitude(String altitude) {
        this.altitude = altitude;
    }

    public String getAltitudeFromGround() {
        return altitudeFromGround;
    }

    public void setAltitudeFromGround(String altitudeFromGround) {
        this.altitudeFromGround = altitudeFromGround;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDetailes() {
        return detailes;
    }

    public void setDetailes(String detailes) {
        this.detailes = detailes;
    }

    public String getSensorSource() {
        return sensorSource;
    }

    public void setSensorSource(String sensorSource) {
        this.sensorSource = sensorSource;
    }

    public String getIndexFromSource() {
        return indexFromSource;
    }

    public void setIndexFromSource(String indexFromSource) {
        this.indexFromSource = indexFromSource;
    }

    public String getDateLastWorked() {
        return dateLastWorked;
    }

    public void setDateLastWorked(String dateLastWorked) {
        this.dateLastWorked = dateLastWorked;
    }

    public Boolean isSensorPublic() {
        return sensorPublic;
    }

    public void setSensorPublic(Boolean sensorPublic) {
        this.sensorPublic = sensorPublic;
    }

    public Boolean isSensorOutdoor() {
        return sensorOutdoor;
    }

    public void setSensorOutdoor(Boolean sensorOutdoor) {
        this.sensorOutdoor = sensorOutdoor;
    }

    public Integer getSensorsLast30minAvarageLevel() {
        return sensorsLast30minAvarageLevel;
    }

    public void setSensorsLast30minAvarageLevel(Integer sensorsLast30minAvarageLevel) {
        this.sensorsLast30minAvarageLevel = sensorsLast30minAvarageLevel;
    }

    public Integer getSensorsLast5minAvarageLevel() {
        return sensorsLast5minAvarageLevel;
    }

    public void setSensorsLast5minAvarageLevel(Integer sensorsLast5minAvarageLevel) {
        this.sensorsLast5minAvarageLevel = sensorsLast5minAvarageLevel;
    }

}
