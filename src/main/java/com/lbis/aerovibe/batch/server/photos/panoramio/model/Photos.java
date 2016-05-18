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
public class Photos {

    Integer count;
    Boolean has_more;
    Photo[] photos;

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Boolean isHas_more() {
        return has_more;
    }

    public void setHas_more(Boolean has_more) {
        this.has_more = has_more;
    }

    public Photo[] getPhotos() {
        return photos;
    }

    public void setPhotos(Photo[] photos) {
        this.photos = photos;
    }

}
