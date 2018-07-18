package com.example.tina.openstackclient.models;

import java.util.List;

/*
 * Klasa Instance sadrzi elemente koji odgovaraju
 * informacijama koje Compute API vraca za svaku instancu
 * kad zatrazimo listu instanci
 */
public class Instance {
    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInfoLink() {
        return infoLink;
    }

    public void setInfoLink(String infoLink) {
        this.infoLink = infoLink;
    }

    @Override
    public String toString() {
        return getName();
    }

    private List<Link> links;

    private String id;

    private String name;

    private String infoLink;
}
