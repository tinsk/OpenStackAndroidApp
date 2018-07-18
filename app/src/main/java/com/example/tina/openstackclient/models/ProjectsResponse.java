package com.example.tina.openstackclient.models;

import java.util.List;

/*
 * Klasa ProjectResponse predstavlja odgovor koji Identity API
 * vrati kad posaljemo zahtjev za listom projekata
 */
public class ProjectsResponse {
    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    List<Link> links;
    List<Project> projects;
}
