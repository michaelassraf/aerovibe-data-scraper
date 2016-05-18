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
public class Photos {

    Photo[] photo;
    Long page;
    Long pages;
    Long perpage;
    Long total;

    public Photos(Photo[] photos, Long page, Long pages, Long perpage, Long total) {
        this.photo = photos;
        this.page = page;
        this.pages = pages;
        this.perpage = perpage;
        this.total = total;
    }

    public Photo[] getPhoto() {
        return photo;
    }

    public void setPhoto(Photo[] photo) {
        this.photo = photo;
    }

    public Long getPage() {
        return page;
    }

    public void setPage(Long page) {
        this.page = page;
    }

    public Long getPages() {
        return pages;
    }

    public void setPages(Long pages) {
        this.pages = pages;
    }

    public Long getPerpage() {
        return perpage;
    }

    public void setPerpage(Long perpage) {
        this.perpage = perpage;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

}
