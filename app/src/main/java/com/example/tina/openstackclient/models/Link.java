package com.example.tina.openstackclient.models;

/*
 * Link je dio odgovora API-ja koji predstavlja URI do nekog resursa
 */
public class Link {
    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getRel() {
        return rel;
    }

    public void setRel(String rel) {
        this.rel = rel;
    }

    private String href;
    private String rel;
}
