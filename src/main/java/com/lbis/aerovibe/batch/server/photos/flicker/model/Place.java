/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lbis.aerovibe.batch.server.photos.flicker.model;

/**
 *
 * @author Development User
 */
public class Place {

    String place_id;
    String woeid;
    Double latitude;
    Double longitude;
    String place_url;
    String place_type;
    Integer place_type_id;
    String timezone;
    String name;
    String woe_name;

    public String getPlace_id() {
        return place_id;
    }

    public void setPlace_id(String place_id) {
        this.place_id = place_id;
    }

    public String getWoeid() {
        return woeid;
    }

    public void setWoeid(String woeid) {
        this.woeid = woeid;
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

    public String getPlace_url() {
        return place_url;
    }

    public void setPlace_url(String place_url) {
        this.place_url = place_url;
    }

    public String getPlace_type() {
        return place_type;
    }

    public void setPlace_type(String place_type) {
        this.place_type = place_type;
    }

    public Integer getPlace_type_id() {
        return place_type_id;
    }

    public void setPlace_type_id(Integer place_type_id) {
        this.place_type_id = place_type_id;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWoe_name() {
        return woe_name;
    }

    public void setWoe_name(String woe_name) {
        this.woe_name = woe_name;
    }

}
