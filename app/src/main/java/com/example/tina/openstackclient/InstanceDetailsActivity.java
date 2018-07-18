package com.example.tina.openstackclient;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tina.openstackclient.controllers.AccessController;
import com.example.tina.openstackclient.controllers.ComputeController;
import com.example.tina.openstackclient.models.InstanceDetails;

import DatabaseAccess.UserViewModel;

/*
 * Aktivnost InstanceDetailsActivity prikazuje ekran s detaljima
 * o instanci i podrzanim akcijama na toj instanci
 */
public class InstanceDetailsActivity extends ConnectedActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // Reference na elemente korisnickog sucelja
    Button mPause;
    Button mRestart;
    Button mStart;
    Button mStop;
    Button mUnpause;
    Button mRefresh;

    // ID i naziv projekta kojem pripada instanca
    String mProjectId;
    String mProjectName;

    /**
     * Inicijaliziramo aktivnost
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Zadajemo layout koji definira izgled korisnickog sucelja
        setContentView(R.layout.activity_instance_details);

        // Dodamo elemente zajednicke svim aktivnostima nakon Login-a
        initializeCommonUI();

        // NavigationView je izbornik unutar DrawerLayout-a
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Dohvatimo Intent objekt koji je pokrenuo ovu aktivnost
        Intent intent = getIntent();
        // Iz intent objekta dohvatimo informacije o instanci
        String instanceInfo = intent.getStringExtra(InstanceMenuActivity.INSTANCE_URI);
        String[] split = instanceInfo.split(" ~ ");
        final String instanceUri = split[0];
        final String instanceName = split[1];
        final String projectName = split[2];
        final String projectId = split[3];
        mProjectId = projectId;
        mProjectName = projectName;
        Log.i("InstanceDetailsActivity", "Instance URI " + instanceUri);

        // Kreiramo AccessController i ComputeController objekte
        final AccessController access = new AccessController(
                ViewModelProviders.of(this).get(UserViewModel.class));
        final ComputeController compute = new ComputeController(access, projectId, projectName);
        // Ispisujemo detalje o instanci pozivom metode na ComputeController objektu
        updateInstanceDetails(compute, instanceUri, instanceName);

        // Zadamo dogadjaje za klik pojedinog gumba
        mPause = findViewById(R.id.btn_pause);
        mPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Sadrzaj koji saljemo kao tijelo zahtjeva za pauziranje instance je {"pause" : "null"}
                performClickAction(compute, instanceUri, "{\"pause\":null}", instanceName);
            }
        });

        mStart = findViewById(R.id.btn_start);
        mStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Sadrzaj koji saljemo kao tijelo zahtjeva za pokretanje instance je {"os-start" : "null"}
                performClickAction(compute, instanceUri, "{\"os-start\":null}", instanceName);
            }
        });

        mUnpause = findViewById(R.id.btn_unpause);
        mUnpause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Sadrzaj koji saljemo kao tijelo zahtjeva za nastavak rada pauzirane instance je {"unapuse" : "null"}
                performClickAction(compute, instanceUri, "{\"unpause\":null}", instanceName);
            }
        });

        mRestart = findViewById(R.id.btn_restart);
        mRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // String content je sadrzaj koji saljemo kao tijelo zahtjeva za restartanje instance
                String content = "{\n" +
                        "    \"reboot\" : {\n" +
                        "        \"type\" : \"HARD\"\n" +
                        "    }\n" +
                        "}";
                performClickAction(compute, instanceUri, content, instanceName);
            }
        });

        mStop = findViewById(R.id.btn_stop);
        mStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Sadrzaj koji saljemo kao tijelo zahtjeva za zaustavljanje  instance je {"os-stop" : "null"}
                performClickAction(compute, instanceUri, "{\"os-stop\":null}", instanceName);
            }
        });

        mRefresh = findViewById(R.id.btn_refresh);
        mRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateInstanceDetails(compute, instanceUri, instanceName);
            }
        });
    }

    /*
     * Izvrsava odabranu akciju na instanci i prikazuje rezultat u Toast obavijesti
     */
    private void performClickAction(ComputeController compute, String instanceUri, String content, String instanceName) {
        String result = "";
        // Kreiramo URI putanju do endpoint-a za izvrsavanje akcije
        String instanceActionUri = instanceUri + "/action";
        try {
            // Izvrsavamo akciju pomocu ComputeController objekta.
            // Metoda executeInstanceAction ce vratiti poruku u slucaju greske, ili prazan
            // string u slucaju uspjeha
            result = compute.executeInstanceAction(instanceActionUri, content);
            if (result == null || result.length() < 2) {
                // Ako nije doslo do greske, poruka ce biti "OK"
                result = "OK";
            }
            // Ako je rezultat "Error", vracamo se na Login aktivnost
            if (result == "Error") {
                returnToLogin();
            }

            // Osvjezimo podatke o instanci odmah i dvije sekunde nakon izvrsenja aktivnosti
            // kako bismo vidjeli osvjezeno stanje za jednostavne aktivnosti bez potrebe za klikanjem
            // Refresh gumba
            final ComputeController computeLocal = compute;
            final String instanceUriLocal = instanceUri;
            final String instanceNameLocal = instanceName;
            findViewById(R.id.btn_unpause).postDelayed(new Runnable() {
                @Override
                public void run() {
                    updateInstanceDetails(computeLocal, instanceUriLocal, instanceNameLocal);
                }
            }, 2000);
            updateInstanceDetails(compute, instanceUri, instanceName);
        } catch (Exception e) {
            // Ako se dogodila iznimka, logirat cemo sadrzaj i ispisati
            // poruku iznimke u Toast obavijesti
            Log.i("InstanceDetailsActivity", "Exception performing action: " + content + ". ~ " + e.getMessage());
            e.printStackTrace();
            result = e.getMessage();
            // Kako se iznimka baca jedino u slucaju ako ComputeController nije uspio
            // osvjeziti token, vracamo se na Login aktivnost
            Intent i = new Intent(this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        }
        // Kreiramo Toast poruku s informacijom o izvrsenju akcije
        Toast.makeText(getApplicationContext(), result,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Kreiranje Options izbornika u gornjem desnom kutu ekrana
        getMenuInflater().inflate(R.menu.instance_details, menu);
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    // Dogadjaj koji se okida kad je odabran element Drawer
    // izbornika
    public boolean onNavigationItemSelected(MenuItem item) {
        // Dohvatimo id odabranog elementa
        int id = item.getItemId();

        // Navigiramo na drugu aktivnost, ovisno o odabiru
        if (id == R.id.nav_login1) {
            returnToLogin();
        } else if (id == R.id.nav_projects1) {
            // Ako smo kliknuli na Projects, prelazimo na listu projekata
            Intent i = new Intent(this, ProjectsActivity.class);
            startActivity(i);
            finish();
        } else if (id == R.id.nav_instances) {
            // Kreiramo Intent objekt za prijelaz na IntanceMenuActivity
            Intent i = new Intent(this, InstanceMenuActivity.class);
            // Aktivnost InstanceMenuActivity ocekuje informaciju o projektu
            // za koji prikazuje listu instanci
            i.putExtra(ProjectsActivity.PROJECT_ID, mProjectId + "~" + mProjectName);
            // Prelazimo na aktivnost InstanceMenuActivity
            startActivity(i);
            finish();
        }

        // Dohvatimo referencu na izbornik
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        // Zatvorimo izbornik
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /*
     * Osvjezava informacije o instanci. Poziva Compute API pomocu
     * ComputeController klase i prikazuje rezultat kao tekst
     */
    private void updateInstanceDetails(ComputeController compute, String instanceUri, String name) {
        try {
            // Pomocu klase ComputeController pozivamo ComputeAPI kako bismo
            // dobili informacije o instanci
            InstanceDetails instance = compute.getInstanceDetails(instanceUri);
            // Pronadjemo referencu na TextView koji nam sluzi za prikaz informacija o instanci
            TextView tw = findViewById(R.id.instance_details);
            // Zadamo da sadrzaj bude rezultat koji smo dobili pozivom getInstanceDetails metode
            tw.setText("Name: " + name + "\nID: " + instance.getId() + "\nStatus: " + instance.getStatus()
                    + "\nUpdated: " + instance.getUpdated() + "\nCreated: " + instance.getCreated());
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
    }
}
