package OpenStackHttpClient;

import android.annotation.SuppressLint;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * Klasa koja salje direktne zahtjeve na OpenStack API
 */
@SuppressLint("NewApi")
public class OSHttpClient {
    // HttpClient objekt koristimo za slanje HTTP zahtjeva
    private final HttpClient client;
    // Token za autentikaciju
    private String token;

    // Tijelo zahtjeva za globalnu autentikaciju
    // Vrijednosti name, domain id i password
    // moraju biti zamijenjene stvarnim vrijednostima prije
    // slanja zahtjeva
    public static String authenticationGlobal = "{ \n"
            + "    \"auth\": {\n"
            + "    \"identity\": {\n"
            + "      \"methods\": [\"password\"],\n"
            + "      \"password\": {\n"
            + "        \"user\": {\n"
            + "          \"name\": \"%s\",\n"
            + "          \"domain\": { \"id\": \"%s\" },\n"
            + "          \"password\": \"%s\"\n"
            + "        }\n"
            + "      }\n"
            + "    }\n"
            + "  }\n"
            + "}";

    // Tijelo zahjtjeva za autentikaciju u opsegu projekta
    // Vrijednosti name, domain id, password i project name
    // moraju biti zamijenjene stvarnim vrijednostima prije
    // slanja zahtjeva
    public static String authenticationProject = "{ \n" +
            "    \"auth\": {\n" +
            "    \"identity\": {\n" +
            "      \"methods\": [\"password\"],\n" +
            "      \"password\": {\n" +
            "        \"user\": {\n" +
            "          \"name\": \"%s\",\n" +
            "          \"domain\": { \"id\": \"%s\" },\n" +
            "          \"password\": \"%s\"\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    \"scope\": {\n" +
            "      \"project\": {\n" +
            "        \"name\": \"%s\",\n" +
            "        \"domain\": { \"id\": \"%s\" }\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";

    public OSHttpClient() {
        // Kreiramo novi HttpClient objekt
        client = new DefaultHttpClient();
    }

    /**
     * Salje POST zahtjev za autentikaciju korisnika
     * @param uri
     * @param content
     * @return HttpResponse objekt koji predstavlja rezultat zahtjeva
     * @throws IOException
     */
    public HttpResponse sendAuthPost(String uri, String content) throws IOException {
        // Kreiramo novi HttpPost objekt
        HttpPost post = new HttpPost(uri);
        // Postavljamo tijelo zahtjeva
        StringEntity requestEntity = new StringEntity(
                content,"UTF-8");
        post.setEntity(requestEntity);
        // Postavljamo zaglavlja zahtjeva
        post.addHeader("type", "text/javascript");
        post.setHeader("Content-Type", "application/json");
        // Izvrsimo POST zahtjev i vratimo rezultat
        return client.execute(post);
    }

    /**
     * Salje POST zahtjev autenticiranog korisnika
     * @param uri
     * @param content
     * @return HttpResponse objekt koji predstavlja rezultat zahtjeva
     * @throws IOException
     */
    public HttpResponse sendPost(String uri, String content) throws IOException {
        // Kreiramo novi HttpPost objekt
        HttpPost post = new HttpPost(uri);
        // Postavljamo token koji smo dobili prilikom autentikacije
        // kako bi API mogao provjeriti da imamo dozvolu za zahtjev koji saljemo
        post.setHeader("X-Auth-Token", this.token);
        // Zadajemo tijelo zahtjeva
        StringEntity requestEntity = new StringEntity(
                content, "UTF-8");
        post.setEntity(requestEntity);
        // Zadajemo Content-Type u zaglavlju
        post.setHeader("Content-Type", "application/json");
        // Izvrsimo POST zahtjev i vratimo rezultat
        return client.execute(post);
    }

    /**
     * Salje GET zahtjev autenticiranog korisnika
     * @param uri
     * @return HttpResponse objekt koji predstavlja rezultat zahtjeva
     * @throws IOException
     */
    public HttpResponse get(String uri) throws IOException {
        // Kreiramo novi HttpGet objekt
        HttpGet get = new HttpGet(uri);
        System.out.println("getting uri: " + uri);
        // Postavimo vrijednosti zaglavlja
        get.setHeader("X-Auth-Token", this.token);
        get.setHeader("Content-Type", "application/json");
        // Izvrsimo GET zahtjev
        return client.execute(get);
    }

    /**
     * Pretvara tijelo HTTP odgovora u string
     * @param response
     * @return Vraca tijelo HTTP odgovora kao string
     * @throws IOException
     */
    public String responseToString(HttpResponse response) throws IOException {
        // Koristimo BufferedReader objekt za citanje sadrzaja odgovora
        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        // Kreiramo StringBuffer objekt u koji spremamo rezultat
        StringBuffer result = new StringBuffer();
        String line = "";
        // Citamo liniju po liniju iz BufferedReader objekta i
        // dodajemo u rezultat
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        // StringBuffer objekt u kojem je rezultat pretvorimo u String
        return result.toString();
    }

    /**
     * Dohvaca token iz odgovora na zahtjev za autentikacijom
     * @param response
     * @return Vraca token koji se nalazi u zaglavlju uspjesnog odgovora
     */
    public String getAuthTokenFromResponse(HttpResponse response) {
        String token = null;
        // Iteriramo kroz sve vrijednosti u zaglavlju
        for (Header h : response.getAllHeaders()) {
            System.out.println(h.getName() + " = " + h.getValue());
            // Kad nadjemo token, dohvatimo i spremimo vrijednost
            if (h.getName().equals("X-Subject-Token")) {
                token = h.getValue();
            }
        }
        return token;
    }

    /**
     * Vraca istinu ako je odgovor tipa 20x OK
     * @param response
     * @return je li status odgovora OK
     */
    public boolean isOK(HttpResponse response) {
        return (int)(response.getStatusLine().getStatusCode() / 100) == 2;
    }

    /**
     * Vraca istinu ako je status odgovora bilo sto osim 401: Not Authorized
     * @param response
     * @return je li korisnik autoriziran za zahtjev koji je poslao
     */
    public boolean isAuthorized(HttpResponse response) {
        return response.getStatusLine().getStatusCode() != 401;
    }

    /**
     * @return the token
     */
    public String getToken() {
        return token;
    }

    /**
     * @param token the token to set
     */
    public void setToken(String token) {
        this.token = token;
    }
}
