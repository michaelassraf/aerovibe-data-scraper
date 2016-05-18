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
public class AQEResult {

    Long id;
    String title;
    String[] tags;
    String description;
    String feed;
    String status;
    String updated;
    AQEDataStream[] datastreams;
    AQELocation location;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public String getFeed() {
        return feed;
    }

    public void setFeed(String feed) {
        this.feed = feed;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public AQEDataStream[] getDatastreams() {
        return datastreams;
    }

    public void setDatastreams(AQEDataStream[] datastreams) {
        this.datastreams = datastreams;
    }

    public AQELocation getLocation() {
        return location;
    }

    public void setLocation(AQELocation location) {
        this.location = location;
    }

}
