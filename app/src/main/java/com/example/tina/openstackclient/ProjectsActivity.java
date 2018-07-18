package com.example.tina.openstackclient;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import com.example.tina.openstackclient.controllers.AccessController;
import com.example.tina.openstackclient.models.Project;

import java.util.List;

import DatabaseAccess.UserViewModel;
import OpenStackHttpClient.UnauthorizedException;

/*
 * Prikazuje listu projekata kojima korisnik moze pristupiti
 */
public class ProjectsActivity extends ConnectedActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static String PROJECT_ID;

    // Pristup objektu User iz baze podataka
    private UserViewModel mUserViewModel;

    /**
     * Inicijaliziramo aktivnost
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Zadajemo layout koji definira izgled korisnickog sucelja
        setContentView(R.layout.activity_projects);

        // Dodamo elemente zajednicke svim aktivnostima nakon Login-a
        initializeCommonUI();

        // NavigationView je izbornik unutar DrawerLayout-a
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Dohvatimo UserViewModel
        mUserViewModel = ViewModelProviders.of(this).get(UserViewModel.class);

        try {
            // Autenticiramo korisnika pomocu objekta AccessController
            AccessController mAccessController = new AccessController(mUserViewModel);
            boolean authenticated = mAccessController.authenticateGlobal();
            if (!authenticated) {
                // Ako autentikacija nije uspjela, zapisemo poruku i bacimo iznimku
                Log.i("ProjectsActivity", "NOT AUTHENTICATED");
                throw new UnauthorizedException("Global authentication failed");
            }
            // Dohvacamo listu projekata pomocu objekta AccessController
            final List<Project> projects = mAccessController.getProjects();

            // Dohvatimo referencu na UI element u kojem ce biti lista projekata
            ListView lv = (ListView) findViewById(R.id.list);
            // Postavimo sadrzaj ListView elementa na listu projekata,
            // od kojih je svaki prikazan u elementu oblika simple_list_item_1
            lv.setAdapter(new ArrayAdapter<Project>(this, R.layout.simple_list_item_1, projects));
            // Klik na projekt ce pokrenuti ProjectsActivity
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
                    // Pokazemo Toast poruku koja sadrzi ID odabranog projekta
                    Toast.makeText(getApplicationContext(), projects.get(position).getId(),
                            Toast.LENGTH_SHORT).show();

                    // Kreiramo Intent objekt za prijelaz na aktivnost ProjectsActivity
                    Intent intent = new Intent(ProjectsActivity.this, InstanceMenuActivity.class);
                    // Dodamo ID i naziv projekta u intent
                    intent.putExtra(PROJECT_ID, projects.get(position).getId() + "~" + projects.get(position).getName());

                    // Prelazimo na ProjectsActivity
                    startActivity(intent);
                }

            });
        } catch (UnauthorizedException e) {
            // Ako se korisnik nije autenticirano, ispisujemo poruku i
            // aplikacija se vraca na Login aktivnost
            Log.i("ProjectsActivity", "Exception: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            returnToLogin();
        } catch (Exception e) {
            // Ako se dogodila neka druga iznimka, samo ispisemo sadrzaj iznimke
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Kreiranje Options izbornika u gornjem desnom kutu ekrana
        getMenuInflater().inflate(R.menu.projects, menu);
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    // Poziva se kad je odabran element Drawer izbornika
    public boolean onNavigationItemSelected(MenuItem item) {
        // Dohvatimo id odabranog elementa
        int id = item.getItemId();

        // Navigiramo na drugu aktivnost, ovisno o odabiru
        if (id == R.id.nav_loginP) {
            // Jedina opcija na ProjectsActivity ekranu je povratak na Login
            returnToLogin();
        }

        // Dohvatimo referencu na izbornik
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        // Zatvorimo izbornik
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
