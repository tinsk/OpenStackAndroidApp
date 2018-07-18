package com.example.tina.openstackclient.controllers;

import android.util.Log;

import com.example.tina.openstackclient.models.Project;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import DatabaseAccess.User;
import DatabaseAccess.UserViewModel;
import OpenStackHttpClient.StackHttpClient;
import OpenStackHttpClient.UnauthorizedException;

/**
 * Klasa upravlja slanjem zahtjeva na Identity API.
 */
public class AccessController {
    public StackHttpClient getClient() {
        return mClient;
    }

    // Dohvaca objekt User koji predstavlja korisnika OpenStack API-ja
    public User getUser() {
        return mUser;
    }

    // Klasu StackHttpClient koristimo za slanje API zahtjeva
    private StackHttpClient mClient;

    // User je korisnik koji salje zahtjeve
    private User mUser;

    /**
     * Konstruktor klase AccessController.
     * Parametar userViewModel pruza pristup bazi podataka
     */
    public AccessController(UserViewModel userViewModel) {
        mClient = StackHttpClient.getClient();
        try {
            // Aplikacija podrzava samo jednog korisnika, tako da
            // uvijek dohvacamo korisnika ciji ID je 0
            mUser = userViewModel.getUserById(0);
            // Pocetni dio URL-a do OpenStack servisa
            mClient.setBaseUri(mUser.getBaseUri());
        } catch (Exception ex) {
            Log.i("AccessController", "Exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Prijavljuje korisnika na Keystone API globalno,
     * dozvoljavajuci pristup listi projekata
     */
    public boolean authenticateGlobal() throws ExecutionException, InterruptedException {
        // Koristimo StackHttpClient klasu za slanje zahtjeva za globalnom autentikacijom
        boolean authResult = mClient.authenticateGlobal(
                mUser.getIdentityUri() + "/auth/tokens",
                mUser.getUsername(), mUser.getPassword(), mUser.getDomain());
        // Vracamo true ako je autentikacija uspjela, odnosno false ako nije
        return authResult;
    }

    /**
     * Prijavljuje korisnika na Identity API za jedan projekt,
     * dozvoljavajuci pristup instancama vezanim za taj projekt
     */
    public boolean authenticateProject(String projectName) throws ExecutionException, InterruptedException {
        // Saljemo zahtjev za autentikacijom za projekt definiran parametrom projectName
        boolean authResult = mClient.authenticateProject(
                mUser.getIdentityUri() + "/auth/tokens",
                mUser.getUsername(), mUser.getPassword(), mUser.getDomain(), projectName);
        // Vracamo true ako je autentikacija uspjela, odnosno false ako nije
        return authResult;
    }

    /**
     * Salje zahtjev Identity API-ju za dohvacanje liste projekata
     * Vraca listu projekata kojima korisnik moze pristupiti
     */
    public List<Project> getProjects() throws InterruptedException, ExecutionException, JSONException, IOException {
        // Na pocetku listu postavimo na null. Ako korisnik nije autoriziran za pristup
        // niti jednom projektu, funkcija ce vratiti null
        List<Project> projects = null;
        // Kreiramu puni URI za dohvacanje projekata
        String projectsUri = mUser.getIdentityUri() + "/auth/projects";
        try {
            // Dohvatimo projekte pomocu StackHttpClient klase
            projects = mClient.getProjects(projectsUri);
        } catch (UnauthorizedException ex) {
            // U slucaju UnauthorizedException iznimke, pokusavamo ponovo
            // autenticirati korisnika
            boolean authenticated = authenticateGlobal();
            // Ako je autentikacija uspjela, do 5 puta pokusamo dohvatiti
            // listu projekata
            if (authenticated) {
                int i=0, maxTries=5;
                boolean authorized = false;
                while (!authorized && i < maxTries) {
                    i++;
                    try {
                        projects = mClient.getProjects(projectsUri);
                        authorized = true;
                    } catch (Exception e) {
                        // U slucaju iznimke, ispisemo iznimku i
                        // pricekamo 500 milisekundi prije sljedeceg pokusaja
                        e.printStackTrace();
                        Thread.sleep(500);
                    }
                }
            }
        }
        return projects;
    }
}
