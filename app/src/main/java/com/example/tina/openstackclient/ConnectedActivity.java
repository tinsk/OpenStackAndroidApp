package com.example.tina.openstackclient;

import android.content.Intent;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

/*
 * Klasa koju nasljedjuju aktivnosti koje se pokrecu
 * nakon sto je korisnik autenticiran na OpenStack API
 */
public abstract class ConnectedActivity extends AppCompatActivity {

    /*
     * Inicijalizira UI elemenate koji su zajednicki svim aktivnostima
     */
    public void initializeCommonUI() {
        // Pronadjemo element toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // Zadamo toolbar kao ActionBar {android.support.v7.app.ActionBar}  za ovu aktivnost
        setSupportActionBar(toolbar);

        // Pronadjemo element drawer_layout
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        // Zadajemo funkcionalnosti za drawer_layout
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    /*
     * Prelazi na Login ekran i brise povijest navigiranja
     */
    public void returnToLogin() {
        // Ako smo kliknuli na Login, kreiramo Intent za
        // prijelaz na Login aktivnost
        Intent i = new Intent(this, LoginActivity.class);
        // U slucaju povratka na Login zelimo izbrisati "povijest" navigiranja,
        // tako da nas "Back" gumb ne vrati prethodnu aktivnost
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // Prelazimo na Login aktivnost
        startActivity(i);
        // Zavrsavamo trenutnu aktivnost i oslobadjamo resurse
        finish();
    }

    @Override
    // Poziva se kad korisnik pritisne Back tipku
    public void onBackPressed() {
        // Drawer izbornik ce se zatvoriti ako pritisnemo "Back" tipku
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    // Poziva se kad korisnik odabere element
    // iz gornjeg desnog "options" izbornika
    public boolean onOptionsItemSelected(MenuItem item) {
        // Dohvatimo id elementa koji smo kliknuli
        int id = item.getItemId();

        // Ako je kliknut "Sign Out" element, vracamo se
        // na pocetni Login ekran
        if (id == R.id.action_settings) {
            returnToLogin();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
