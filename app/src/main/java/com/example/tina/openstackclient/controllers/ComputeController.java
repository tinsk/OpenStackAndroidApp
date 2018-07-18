package com.example.tina.openstackclient.controllers;

import com.example.tina.openstackclient.models.Instance;
import com.example.tina.openstackclient.models.InstanceDetails;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import OpenStackHttpClient.UnauthorizedException;

/**
 * Klasa upravlja slanjem zahtjeva na Compute API
 */
public class ComputeController {
    private AccessController mAccessController;
    private String mProjectName;
    private String mProjectId;

    /**
     * Konstruktor klase. Zadajemo AccessController kako bismo omogucili
     * ponovno logiranje u slucaju isteka tokena
     * @param projectId je ID projekta cijim instancama pristupamo
     * @param projectName je ime projekta cijim instancama pristupamo
     */
    public ComputeController(AccessController accessController, String projectId, String projectName) {
        mAccessController = accessController;
        mProjectName = projectName;
        mProjectId = projectId;
    }

    /**
     * Dohvaca instance za projekt ciji ID je mProjectID
     */
    public List<Instance> getInstances() throws InterruptedException, ExecutionException, JSONException, IOException {
        // Na pocetku listu instanci postavimo na null. Ako ne uspijemo
        // dohvatiti listu instanci, funkcija ce vratiti null
        List<Instance> instances = null;
        try {
            // Pokusamo dohvatiti listu instanci pozivom Compute API servisa,
            // koristeci objekt StackHttpClient, na koji imamo referencu u
            // objektu mAccessController
            return mAccessController.getClient()
                        .getInstances(mAccessController.getUser().getComputeUri(), mProjectId);
        } catch (UnauthorizedException ex) {
            // Ako je istekao token, OpenStackHttpClient ce baciti
            // iznimku UnauthorizedException

           // Postavimo varijablu authorized na false. Koristit cemo je za
            // provjeru da je korisnik uspjesno dohvatio listu instanci
            boolean authorized = false;

            // Pokusamo se ponovo autenticirati za projekt
            boolean authenticated = mAccessController.authenticateProject(mProjectName);

            // Ako smo se autenticirali, do 5 puta cemo pokusati ponovo dohvatiti
            // listu instanci
            if (authenticated) {
                int i = 0, maxTries = 5;
                while (!authorized && i < maxTries) {
                    i++;
                    try {
                        // Spremimo rezultat API poziva koji dohvaca listu
                        // instanci u varijablu instances
                        instances = mAccessController.getClient()
                                .getInstances(mAccessController.getUser().getComputeUri(), mProjectId);

                        // Ako StackHttpClient nije bacio Exception, autorizacija je uspjela
                        authorized = true;
                    } catch (Exception e) {
                        // Ako se dogodila iznimka, ispisemo sadrzaj iznimke i pricekamo
                        // 500 milisekundi prije sljedeceg pokusaja
                        e.printStackTrace();
                        Thread.sleep(500);
                    }
                }
            }
        }
        return instances;
    }

    /**
     * Dohvaca detalje o instanci, kojoj je moguce pristupiti putem URI
     * adrese instanceUri
     */
    public InstanceDetails getInstanceDetails(String instanceUri) throws InterruptedException, ExecutionException, JSONException, IOException {
        // Za pocetak objekt koji predstavlja detalje instance postavimo na null.
        // Ako ne uspijemo dohvatiti detalje instance, funkcija ce vratiti null.
        InstanceDetails instance = null;
        try {
            // Pokusamo dohvatiti detalje o  instanci pozivom Compute API servisa,
            // koristeci objekt StackHttpClient, na koji imamo referencu u
            // objektu mAccessController
            return mAccessController.getClient().getInstanceDetails(instanceUri);
        } catch (UnauthorizedException ex) {
            // Ako je istekao token, OpenStackHttpClient ce baciti
            // iznimku UnauthorizedException

            // Postavimo varijablu authorized na false. Koristit cemo je za
            // provjeru da se korisnik uspjesno autorizirao za pristup projektu
            int i = 0, maxTries = 5;

            // Varijabla autorized predstavlja uspjesnost autorizacije korisnika za
            // pristup detaljima instance
            boolean authorized = false;

            // Pokusamo se ponovo autenticirati za projekt
            boolean authenticated = mAccessController.authenticateProject(mProjectName);

            // Ako smo se autenticirali, do 5 puta cemo pokusati ponovo dohvatiti
            // detalje instance
            if (authenticated) {
                while (!authorized && i < maxTries) {
                    i++;
                    try {
                        instance = mAccessController.getClient().getInstanceDetails(instanceUri);
                        // Ako StackHttpClient nije bacio Exception, autorizacija je uspjela
                        authorized = true;
                    } catch (Exception e) {
                        // Ako se dogodila iznimka, ispisemo sadrzaj iznimke i pricekamo
                        // 500 milisekundi prije sljedeceg pokusaja
                        e.printStackTrace();
                        Thread.sleep(500);
                    }
                }
            }
        }
        return instance;
    }

    /**
     * Izvrsava zadanu akciju na instanci, kojoj je moguce pristupiti putem URI
     * adrese instanceUri
     * Akcija je zadana u parametru content
     * @param instanceActionUri - URI na koji saljemo zahtjev
     * @param content - tijelo zahtjeva
     */
    public String executeInstanceAction(String instanceActionUri, String content) throws ExecutionException, InterruptedException {
        // Vrijednost odgovora postavimo na "Error"
        String response = "Error";
        try {
            // Pozivamo API metodu za izvrsavanje akcije na instanci.
            return mAccessController.getClient().executeInstanceAction(instanceActionUri, content);
        } catch (UnauthorizedException ex) {
            // U slucaju iznimke, ponovimo zahtjev za autentikacijom
            boolean authenticated = mAccessController.authenticateProject(mProjectName);
            try {
                // Ponovo pokusamo izvrsiti akciju na instanci
                return mAccessController.getClient().executeInstanceAction(instanceActionUri, content);
            } catch (UnauthorizedException e) {
                e.printStackTrace();
            }
        }
        // Ako je zahtjev za izvrsavanjem akcije na instanci bacio exception u oba pokusaja,
        // vracamo poruku "Error"
        return response;
    }
}
