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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.tina.openstackclient.controllers.AccessController;
import com.example.tina.openstackclient.controllers.ComputeController;
import com.example.tina.openstackclient.models.Instance;

import java.util.ArrayList;
import java.util.List;

import DatabaseAccess.UserViewModel;

/*
 * Prikazuje listu instanci za odabrani projekt. Moguce je klikom na instancu
 * prijeci na ekran koji prikazuje detalje te instance
 * Ako postoji vise od 5 instanci, pokazat ce se polje za filtriranje
 * po nazivu
 */
public class InstanceMenuActivity extends ConnectedActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static String INSTANCE_URI;

    // Pristup objektu User iz baze podataka
    private UserViewModel mUserViewModel;

    @Override
    // Inicijaliziramo aktivnost
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Zadajemo layout koji definira izgled korisnickog sucelja
        setContentView(R.layout.activity_instance_menu);

        // Dodamo elemente zajednicke svim aktivnostima nakon Login-a
        initializeCommonUI();

        // NavigationView je izbornik unutar DrawerLayout-a
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Dohvatimo Intent objekt koji je pokrenuo ovu aktivnost
        Intent intent = getIntent();
        // Iz intent objekta dohvatimo informacije o projektu
        String projectValues = intent.getStringExtra(ProjectsActivity.PROJECT_ID);
        String[] split = projectValues.split("~");
        final String projectId = split[0];
        final String projectName = split[1];
        Log.i("InstanceActivity", "project id " + projectId);
        Log.i("InstanceActivity", "project name " + projectName);

        // Dohvatimo UserViewModel
        mUserViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        try {
            // Inicijaliziramo AccessController i ComputeController
            final AccessController access = new AccessController(mUserViewModel);
            access.authenticateProject(projectName);
            ComputeController compute = new ComputeController(access, projectId, projectName);
            // Pomocu ComputeControllera dohvatimo listu instanci
            final List<Instance> instances = compute.getInstances();
            // Pronadjemo ListView element korisnickog sucelja u kojem se prikazuje lista instanci
            final ListView lv = (ListView) findViewById(R.id.instanceList);
            // Pronadjemo element korisnickog sucelja za unos teksta za filtriranje
            final EditText filterText = findViewById(R.id.txt_filter);

            // Ako postoji vise od 5 instanci, prikazat cemo polje za filtriranje
            if (instances.size() > 5) {
                // Dohvatimo UI referencu na kontejner elemenata za filtriranje instanci
                LinearLayout searchLayout = (LinearLayout) findViewById(R.id.filter_layout);
                // Postavimo vidljivost elementa na VISIBLE
                searchLayout.setVisibility(View.VISIBLE);
                // Dohvatimo UI referencu na gumb za filtriranje
                Button filterButton = (Button) findViewById(R.id.btn_filter);
                // Na klik gumba za filtriranje cemo filtrirati instance prema zadanom tekstu
                filterButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Dohvatimo tekst upisan u polje filterText
                        String text = filterText.getText().toString();
                        Log.i("InstanceMenu","Filtering by: " + text);
                        // Kreiramo novu listu u koju cemo dodati samo instance koje
                        // odgovaraju upisanom tekstu
                        List<Instance> instancesDisplayed = new ArrayList<Instance>();
                        for(Instance instance : instances) {
                            // Ako instanca sadrzi tekst upisan u polje za filtriranje,
                            // dodamo je na listu
                            if(instance.getName().contains(text)) {
                                instancesDisplayed.add(instance);
                            }
                        }
                        // Osvjezimo listu instanci tako da prikaze samo filtrirane instance
                        lv.setAdapter(new ArrayAdapter<Instance>(InstanceMenuActivity.this, R.layout.simple_list_item_1, instancesDisplayed));
                    }
                });
            }

            // Prikazemo instance u ListView elementu
            lv.setAdapter(new ArrayAdapter<Instance>(this, R.layout.simple_list_item_1, instances));
            // Klik na instancu ce pokrenuti aktivnost InstanceDetailsActivity
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                // Poziva se kad korisnik klikne na instancu
                public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
                    // Prikazemo Toast poruku koja sadrzi ID odabrane instance
                    Toast.makeText(getApplicationContext(), instances.get(position).getId(),
                            Toast.LENGTH_SHORT).show();

                    // Kreiramo Intent objekt za prijelaz na InstanceDetailsActivity
                    Intent intent = new Intent(InstanceMenuActivity.this, InstanceDetailsActivity.class);
                    // Dohvatimo URI odabrane instance
                    String instanceLink = instances.get(position).getInfoLink();
                    // Dohvatimo spremljeni Compute URI
                    String computeLink = access.getUser().getComputeUri();
                    // Odgovor API-ja moze sadrzavati "http" link, cak i ako je potreban "https"
                    // Ako URI instance ne pocinje se "https", a Compute URI pocinje sa "https",
                    // trebamo promijeniti URI instance da koristi https
                    if (computeLink.startsWith("https") && !instanceLink.startsWith("https://")) {
                            instanceLink = instanceLink.replace("http://", "https://");
                    }
                    // Dodamo informacije o instanci i projektu u intent objekt, kako bismo ih prenijeli u sljedecu aktivnost
                    intent.putExtra(INSTANCE_URI, instanceLink + " ~ " + instances.get(position).getName() + " ~ " + projectName + " ~ " + projectId);

                    // Prelaizmo na aktivnost InstanceDetailsActivity
                    startActivity(intent);
                }
            });
        } catch (Exception ex) {
            // Ako se dogodila iznimka, ispisemo poruku iznimke
            // i vracamo se na Login ekran
            Log.i("InstanceMenuActivity", "Exception: " + ex.toString());
            Toast.makeText(getApplicationContext(), ex.getMessage(),
                    Toast.LENGTH_SHORT).show();
            returnToLogin();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Kreiranje Options izbornika u gornjem desnom kutu ekrana
        getMenuInflater().inflate(R.menu.instance_menu, menu);
        return true;
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    // Poziva se kad je odabran element Drawer izbornika
    public boolean onNavigationItemSelected(MenuItem item) {
        // Dohvatimo id odabranog elementa
        int id = item.getItemId();

        // Navigiramo na drugu aktivnost, ovisno o odabiru
        if (id == R.id.nav_login) {
            returnToLogin();
        } else if (id == R.id.nav_projects) {
            Intent i = new Intent(this, ProjectsActivity.class);
            startActivity(i);
            finish();
        }

        // Dohvatimo referencu na izbornik
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        // Zatvorimo izbornik
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
