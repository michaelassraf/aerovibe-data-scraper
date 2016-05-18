/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lbis.aerovibe.batch.server.location.geonames.model;

/**
 *
 * @author Development User
 */
public class Location {

    String countryId;
    String adminCode1;
    String countryName;
    String fclName;
    String countryCode;
    String lng;
    String fcodeName;
    String distance;
    String toponymName;
    String fcl;
    String name;
    String fcode;
    Long geonameId;
    String lat;
    String adminName1;
    Long population;

    public String getCountryId() {
        return countryId;
    }

    public void setCountryId(String countryId) {
        this.countryId = countryId;
    }

    public String getAdminCode1() {
        return adminCode1;
    }

    public void setAdminCode1(String adminCode1) {
        this.adminCode1 = adminCode1;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getFclName() {
        return fclName;
    }

    public void setFclName(String fclName) {
        this.fclName = fclName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getFcodeName() {
        return fcodeName;
    }

    public void setFcodeName(String fcodeName) {
        this.fcodeName = fcodeName;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getToponymName() {
        return toponymName;
    }

    public void setToponymName(String toponymName) {
        this.toponymName = toponymName;
    }

    public String getFcl() {
        return fcl;
    }

    public void setFcl(String fcl) {
        this.fcl = fcl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFcode() {
        return fcode;
    }

    public void setFcode(String fcode) {
        this.fcode = fcode;
    }

    public Long getGeonameId() {
        return geonameId;
    }

    public void setGeonameId(Long geonameId) {
        this.geonameId = geonameId;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getAdminName1() {
        return adminName1;
    }

    public void setAdminName1(String adminName1) {
        this.adminName1 = adminName1;
    }

    public Long getPopulation() {
        return population;
    }

    public void setPopulation(Long population) {
        this.population = population;
    }

}
