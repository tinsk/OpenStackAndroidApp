package com.example.tina.openstackclient.models;

import java.util.List;

/*
 * Project  je jedan od projekata u listi
 * koju nam daje Identitiy API
 */
public class Project {
    public boolean isIs_domain() {
        return is_domain;
    }

    public void setIs_domain(boolean is_domain) {
        this.is_domain = is_domain;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getLinks() {
        return links;
    }

    public void setLinks(List<String> links) {
        this.links = links;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDomain_id() {
        return domain_id;
    }

    public void setDomain_id(String domain_id) {
        this.domain_id = domain_id;
    }

    public String getParent_id() {
        return parent_id;
    }

    public void setParent_id(String parent_id) {
        this.parent_id = parent_id;
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

    @Override
    public String toString() {
        return getName();
    }

    private boolean is_domain;

    private String description;

    private List<String> links;

    private boolean enabled;

    private String domain_id;

    private String parent_id;

    private String id;

    private String name;
}
