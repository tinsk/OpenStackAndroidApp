package com.example.tina.openstackclient.models;

/*
 * Klasa InstanceDetails predstavlja odgovor koji dobijemo
 * kad na Compute API posaljemo zahtjev za detaljima odredjene
 * instance
 */
public class InstanceDetails {
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    private String id;
    private String status;
    private String updated;
    private String created;
}
