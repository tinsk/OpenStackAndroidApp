package OpenStackHttpClient;


import android.os.AsyncTask;
import android.util.Log;

import com.example.tina.openstackclient.models.Instance;
import com.example.tina.openstackclient.models.InstanceDetails;
import com.example.tina.openstackclient.models.Project;

import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Singleton klasa za pristup OpenStack API-ju indirektno, koristenjem
 * OSHttpClient klase
 * Omogucuje izvrsavanje zahtjeva na pozadinskoj dretvi
 */
public class StackHttpClient {
    private OSHttpClient client;
    private static StackHttpClient instance;

    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }

    private String baseUri;

    private StackHttpClient() {
        // Kreiramo novi OSHttpClient objekt koji koristimo
        // za slanje direktnih zahtjeva na API
        client = new OSHttpClient();
    }

    // Dohvaca instancu ove klase
    public static StackHttpClient getClient() {
        if (instance == null) {
            instance = new StackHttpClient();
        }
        return instance;
    }

    /**
     * Izvrsava autentikaciju korisnika globalno na pozadinskoj dretvi
     * @param uri
     * @param username
     * @param password
     * @param domain
     * @return je li autentikacija uspjesna
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public boolean authenticateGlobal(String uri, String username, String password, String domain) throws ExecutionException, InterruptedException {
        // Kreiramo novi pozadinski zadatak na kojem se izvrsava zahtjev
        return (new authenticateGlobalTask(client).execute(uri, username, domain, password)).get();
    }

    /**
     * Izvrsava autentikaciju korisnika za projekt projectName na pozadinskoj dretvi
     * @param uri
     * @param username
     * @param password
     * @param domain
     * @param projectName
     * @return je li autentikacija uspjesna
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public boolean authenticateProject(String uri, String username, String password, String domain, String projectName) throws ExecutionException, InterruptedException {
        // Kreiramo novi pozadinski zadatak na kojem se izvrsava zahtjev
        return (new authenticateProjectTask(client).execute(uri, username, password, domain, projectName)).get();
    }

    /**
     * Dohvaca projekte i vraca ih kao listu objekata Project
     * @param uri
     * @return Lista objekata Project
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws JSONException
     * @throws UnauthorizedException
     */
    public List<Project> getProjects(String uri) throws IOException, ExecutionException, InterruptedException, JSONException, UnauthorizedException {
        // Metodom getProjectsString(uri) dobijemo JSON string koji predstavlja listu projekata
        String projectsJson = getProjectsString(uri);
        // Ako korisnik nije autoriziran, bacamo iznimku
        if (projectsJson.equals("Unauthorized")) {
            throw new UnauthorizedException("Not authorized to get projects");
        }
        // Parsamo JSON string u kojem je lista projekata i vracamo objekt tipa List<Project>
        JSONObject jObject = new JSONObject(projectsJson);
        JSONArray projectsObj = jObject.getJSONArray("projects");
        List<Project> projects = new ArrayList<Project>();
        for (int i = 0; i < projectsObj.length(); i++) {
            JSONObject o = (JSONObject) projectsObj.get(i);
            Project p = new Project();
            p.setName(o.getString("name"));
            p.setId(o.getString("id"));
            projects.add(p);
        }
        return projects;
    }

    /**
     * Izvrsava zahtjev za dohvacanjem projekata na pozadinskoj dretvi
     * @param uri
     * @return Sadrzaj odgovora na zahtjev kao String
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public String getProjectsString(String uri) throws IOException, ExecutionException, InterruptedException {
        // Kreiramo novi pozadinski zadatak na kojem se izvrsava zahtjev
        return (new getItemsTask(client).execute(uri)).get();
    }

    /**
     * Dohvaca instance i vraca ih kao listu objekata Instance
     * @param computeUri
     * @param projectId
     * @return Lista objekata Instance
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws JSONException
     * @throws UnauthorizedException
     */
    public List<Instance> getInstances(String computeUri, String projectId) throws IOException, ExecutionException, InterruptedException, JSONException, UnauthorizedException {
        // Dohvatimo listu instanci kao JSON string pomocu metode getInstancesString
        String instancesJson = getInstancesString(computeUri, projectId);
        // Ako korisnik nije autoriziran, bacimo iznimku
        if (instancesJson.equals("Unauthorized")) {
            throw new UnauthorizedException("Not authorized to get instances");
        }
        // Parsamo JSON string u kojem je lista instanci i vracamo objekt tipa List<Instance>
        JSONObject jObject = new JSONObject(instancesJson);
        JSONArray instancesObj = jObject.getJSONArray("servers");
        List<Instance> instances = new ArrayList<Instance>();
        for (int i = 0; i < instancesObj.length(); i++) {
            JSONObject o = (JSONObject) instancesObj.get(i);
            Instance instance = new Instance();
            instance.setName(o.getString("name"));
            instance.setId(o.getString("id"));
            JSONArray links = o.getJSONArray("links");
            for (int j = 0; j < links.length(); j++) {
                JSONObject linkObject = (JSONObject) links.get(j);
                if (linkObject.getString("rel").equals("self")) {
                    instance.setInfoLink(linkObject.getString("href"));
                }
            }
            instances.add(instance);
        }
        return instances;
    }

    /**
     * Izvrsava zahtjev za dohvacanjem liste instanci na pozadinskoj dretvi
     * @param computeUri
     * @param projectId
     * @return String koji predstavlja tijelo odgovora na zahtjev
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public String getInstancesString(String computeUri, String projectId) throws IOException, ExecutionException, InterruptedException {
        // Kreiramo URI na osnovi Compute URI-ja i ID-a projekta
        String uri = computeUri + "/" + projectId + "/servers";
        // Kreiramo novi pozadinski zadatak na kojem se izvrsava zahtjev
        return (new getItemsTask(client).execute(uri)).get();
    }

    /**
     * Dohvaca detalje instance i vraca ih u objektu InstanceDetails
     * @param instanceUri
     * @return Informacije o instaci u objektu InstanceDetails
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws JSONException
     * @throws UnauthorizedException
     */
    public InstanceDetails getInstanceDetails(String instanceUri) throws IOException, ExecutionException, InterruptedException, JSONException, UnauthorizedException {
        // Dohvatimo detalje o instanci metodom getInstanceDetailsString
        String instancesJson = getInstanceDetailsString(instanceUri);
        // Ako korisnik nije autoriziran za ovaj zahtjev, bacamo iznimku
        if (instancesJson.equals("Unauthorized")) {
            throw new UnauthorizedException("Not authorized to get instances");
        }
        // Parsamo JSON string u koji predstavlja detalje instnace i vracamo
        // objekt tipa InstanceDetails
        JSONObject jObject = new JSONObject(instancesJson);
        JSONObject instanceObj = (JSONObject) jObject.get("server");
        InstanceDetails instance = new InstanceDetails();
        instance.setId((String) instanceObj.get("id"));
        instance.setCreated((String) instanceObj.get("created"));
        instance.setUpdated((String) instanceObj.get("updated"));
        instance.setStatus((String) instanceObj.get("status"));
        return instance;
    }

    /**
     * Izvrsava zahtjev za dohvacanjem detalja instance na pozadinskoj dretvi
     * @param instanceUri
     * @return String koji predstavlja tijelo odgovora na zahtjev
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public String getInstanceDetailsString(String instanceUri) throws IOException, ExecutionException, InterruptedException {
        return (new getItemsTask(client).execute(instanceUri)).get();
    }

    /**
     * Izvrsava zahtjev za akcijom na instanci na pozadinskoj dretvi
     * @param instanceUri
     * @param content
     * @return Odgovor na zahtjev kao String
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws UnauthorizedException
     */
    public String executeInstanceAction(String instanceUri, String content) throws ExecutionException, InterruptedException, UnauthorizedException {
        // Kreiramo novi zadatak, izvrsimo zahtjev i dohvatimo rezultat
        String response = (new instanceActionTask(client).execute(instanceUri, content)).get();
        // Ako je rezultat "Unauthorized", bacamo iznimku
        if (response.equals("Unauthorized")) {
            throw new UnauthorizedException("Not authorized to perform instance action");
        }
        // Vracamo rezultat zahtjeva
        return response;
    }

    /**
     * Zadatak za izvrsavanje globalne autentikacije
     */
    private static class authenticateGlobalTask extends AsyncTask<String, Void, Boolean> {
        private OSHttpClient mClient;

        public authenticateGlobalTask(OSHttpClient client) {
            mClient = client;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                // U tijelo zahtjeva dodamo vrijednosti username, password i domain, koje
                // su poslane kao parametri
                String authContent = String.format(OSHttpClient.authenticationGlobal, params[1], params[2], params[3]);
                Log.i("StackHttpClient", "auth content: " + authContent);
                // Saljemo zahtjev za autentikacijom na API
                // params[0] je URI za authentication
                HttpResponse res =
                        mClient.sendAuthPost(params[0], authContent);
                // U odgovoru smo dobili Token, koji spremimo u mClient objekt za buduce zahtjeve
                mClient.setToken(mClient.getAuthTokenFromResponse(res));
                System.out.println(mClient.responseToString(res));

                // Vratimo informaciju o tome je li autentikacija uspjesna
                return mClient.isOK(res);
            } catch (Exception ex) {
                Log.i("StackHttpClient", "Exception: " + ex.getMessage());
                return false;
            }

        }
    }

    /**
     * Zadatak za izvrsavanje autentikacije za projekt
     */
    private static class authenticateProjectTask extends AsyncTask<String, Void, Boolean> {
        private OSHttpClient mClient;

        public authenticateProjectTask(OSHttpClient client) {
            mClient = client;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                // U tijelo zahtjeva dodamo vrijednosti username, password, project ID i domain, koje
                // su poslane kao parametri
                String authContent = String.format(OSHttpClient.authenticationProject, params[1], params[3], params[2], params[4], params[3]);
                Log.i("StackHttpClient", "project auth content: " + authContent);
                // Saljemo zahtjev za autentikacijom na API
                // params[0] je URI za authentication
                HttpResponse res =
                        mClient.sendAuthPost(params[0], authContent);
                // U odgovoru smo dobili Token, koji spremimo u mClient objekt za buduce zahtjeve
                mClient.setToken(mClient.getAuthTokenFromResponse(res));
                System.out.println(mClient.responseToString(res));

                // Vratimo informaciju o tome je li autentikacija uspjesna
                return mClient.isOK(res);
            } catch (Exception ex) {
                Log.i("StackHttpClient", "Exception: " + ex.getMessage());
                return false;
            }
        }
    }

    /**
     * Zadatak za slanje GET zahtjeva koji dohvaca listu objekata
     */
    private static class getItemsTask extends AsyncTask<String, Void, String> {
        private OSHttpClient mClient;

        public getItemsTask(OSHttpClient client) {mClient = client;}

        @Override
        protected String doInBackground(String... params) {
            try {
                // Saljemo GET zahtjev na URI koji smo dobili kao prvi parametar
                HttpResponse res = mClient.get(params[0]);
                // Dohvatimo tijelo odgovora kao string
                String returnMessage = mClient.responseToString(res);
                // Vratimo poruku o tome je li zahtjev uspjesan
                if (!mClient.isOK(res)) {
                    return "Unauthorized";
                }
                return returnMessage;
            } catch (Exception ex) {
                Log.i("StackHttpClient", "Exception: " + ex.getMessage() + "\n");
                ex.printStackTrace();
                return ex.getMessage();
            }
        }
    }

    /**
     * Zadatak za slanje POST zahtjeva koji izvrsava akciju na instanci
     */
    private static class instanceActionTask extends AsyncTask<String, Void, String> {
        private OSHttpClient mClient;

        public instanceActionTask(OSHttpClient client) {
            mClient = client;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                // Saljemo POST zahtjev za izvrsenje akcije na instanci
                // URI je zadan kao prvi parametar, a drugi parametar je tijelo zahtjeva
                HttpResponse res =
                        mClient.sendPost(params[0], params[1]);
                // Dohvatimo tijelo odgovora kao string
                String returnMessage = mClient.responseToString(res);
                // Vratimo poruku o tome je li akcija uspjesno izvrsena
                if (!mClient.isAuthorized(res)) {
                    return "Unauthorized";
                }
                return returnMessage;
            } catch (Exception ex) {
                Log.i("StackHttpClient", "Exception: " + ex.getMessage() + "\n");
                ex.printStackTrace();
                return ex.getMessage();
            }
        }
    }
}
