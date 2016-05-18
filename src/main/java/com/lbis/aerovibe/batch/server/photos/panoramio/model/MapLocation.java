/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lbis.aerovibe.batch.server.photos.panoramio.model;

/**
 *
 * @author Assraf
 */
public class MapLocation {

    Double lat;
    Double lon;
    Integer panoramio_zoom;

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public Integer getPanoramio_zoom() {
        return panoramio_zoom;
    }

    public void setPanoramio_zoom(Integer panoramio_zoom) {
        this.panoramio_zoom = panoramio_zoom;
    }

}
