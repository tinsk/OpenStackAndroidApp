package com.example.tina.openstackclient;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.concurrent.ExecutionException;

import DatabaseAccess.User;
import DatabaseAccess.UserViewModel;

/**
 * Login ekran koji omogucuje autorizaciju upisom korisnickog imena i lozinke
 */
public class LoginActivity extends AppCompatActivity /*implements LoaderCallbacks<Cursor>*/ {

    // Pratimo login zadatak da bismo ga mogli prekinuti ako je potrebno.
    private UserLoginTask mAuthTask = null;

    // Reference na elemente korisnickog sucelja
    private AutoCompleteTextView mUsernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private FloatingActionButton mSettingsButton;

    // Pristup objektu User iz baze podataka
    private UserViewModel mUserViewModel;

    public UserViewModel getUserViewModel() {
        return mUserViewModel;
    }

    /**
     * Inicijaliziramo aktivnost
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Dohvatimo UserViewModel
        mUserViewModel= ViewModelProviders.of(this).get(UserViewModel.class);

        // Kreiramo objekt User
        final User user = new User();
        try {
            // Ako postoji korisnik u bazi, popunimo parametre korisnika
            // vrijednostima iz baze
            User existingUser = mUserViewModel.getUserById(0);
            if (existingUser != null) {
                user.setPassword(existingUser.getPassword());
                user.setUsername(existingUser.getUsername());
                user.setIdentityUri(existingUser.getIdentityUri());
                user.setComputeUri(existingUser.getComputeUri());
                user.setBaseUri(existingUser.getBaseUri());
                user.setDomain(existingUser.getDomain());
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Zadajemo layout koji definira izgled korisnickog sucelja
        setContentView(R.layout.activity_login);

        // Pronadjemo referencu na gumb za postavke
        mSettingsButton = (FloatingActionButton) findViewById(R.id.click_settings);
        // Kad kliknemo gumb za postavke, prikazat ce se fragment URISettings
        mSettingsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dohvatimo referencu na container koji sadrzi URISettings fragment
                LinearLayout fragmentContainer=(LinearLayout) findViewById(R.id.fragment_container);
                // Ucinimo container vidljivim
                fragmentContainer.setVisibility(View.VISIBLE);
            }
        });

        // Postavimo Login formu
        mUsernameView = (AutoCompleteTextView) findViewById(R.id.email);
        mUsernameView.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Postavimo tekst na korisnicko ime korisnika iz baze
                mUsernameView.setText(user.getUsername());
                mUsernameView.setSelection(mUsernameView.getText().length());
            }
        },500);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Postavimo password na lozinku korisnika iz baze
                mPasswordView.setText(user.getPassword());
                mPasswordView.setSelection(mPasswordView.getText().length());
            }
        },500);

        // Kad korisnik pritisne Enter unutar Password polja,
        // aplikacija ce pokusati odraditi login
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        // Dohvatimo referencu na Sign In gumb
        Button mUserSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        // Kad korisnik klikne Sign In, aplikacija ce pokusati prijaviti korisnika
        mUserSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        // Dohvatimo UI  reference na Login formu i progress animaciju
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        // Dohvatimo UI referencu na settings fragment
        View settingsView = findViewById(R.id.fragment_container);
        // Sakrijemo fragment
        settingsView.setVisibility(View.GONE);
    }

    /**
     * Pokusa prijaviti korisnika. Ako je polje username  ili password prazno,
     * ispise se odgovarajuca poruka i prekida se pokusaj autentikacije
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Izbrisemo poruke o gresci
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Spremimo vrijednosti u trenutku pokusaja logina
        String usrName = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Ako je korisnik unio lozinku, provjerimo je li validna
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Provjerimo je li uneseno korisnicko ime
        if (TextUtils.isEmpty(usrName)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel) {
            // Dogodila se greska. Ne pokusavamo login i fokusiramo
            // prvo polje forme gdje se dogodila greska
            focusView.requestFocus();
        } else {
            // Prikazemo progress spinner i zapocnemo pozadinski
            // zadatak koji izvrsava pokusaj logiranja
            showProgress(true);
            mAuthTask = new UserLoginTask(usrName, password);
            mAuthTask.execute((Void) null);
        }
    }

    // Provjerava je li lozinka validna prije pokusaja logina
    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    /**
     * Prikazuje progress UI i skriva Login formu
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // Honeycomb MR2 podrzava ViewPropertyAnimator APIje, koji
        // dozvoljavaju lako animiranje progress spinner-a
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // Nisu dostupni ViewPropertyAnimator APIji, pa jednostavno
            // prikazemo, odnosno sakrijemo, relevantne UI komponente
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Predstavlja asinkroni login zadatak koji koristimo za
     * autentikaciju korisnika
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUsrName;
        private final String mPassword;
        private User user;
        private UserViewModel uvm;

        // Parametri konstruktora su upisano korisnicko ime
        // i lozinka
        UserLoginTask(String username, String password) {
            mUsrName = username;
            mPassword = password;
            uvm = getUserViewModel();
            try {
                user = uvm.getUserById(0);
            } catch(Exception ex) {
                Log.i("logintag", "EXCEPTION " + ex.getMessage());
            }

        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                // Spremimo podatke o korisniku u bazu
                // Ako korisnik vec postoji, samo azuriramo korisnicko
                // ime i lozinku
                if(user == null) {
                    user = new User();
                    user.setUid(0);
                }
                user.setUsername(this.mUsrName);
                user.setPassword(this.mPassword);
                uvm.insertOrUpdate(user);

                // Prelazimo na ProjectsActivity ekran
                Intent myIntent = new Intent(LoginActivity.this, ProjectsActivity.class);
                LoginActivity.this.startActivity(myIntent);
            } catch (Exception e) {
                Log.i("LoginActivity", "Exception: " + e.getMessage() + " " + e.getCause());
                return false;
            }

            return true;
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

