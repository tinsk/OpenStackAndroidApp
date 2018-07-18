package com.example.tina.openstackclient;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;

import DatabaseAccess.User;
import DatabaseAccess.UserViewModel;

/**
 * Fragment za unos URI adresa OpenStack API-ja
 */
public class URISettings extends Fragment {

    // Reference na elemente korisnickog sucelja
    private EditText mURIText;
    private EditText mIdentityText;
    private EditText mComputeText;
    private EditText mDomainText;
    private Button mSaveButton;
    private Button mCancelButton;

    // Pristup objektu User iz baze podataka
    private UserViewModel mUserViewModel;

    /**
     * Inicijaliziramo fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Zadajemo layout koji definira izgled korisnickog sucelja
        View fragmentView = inflater.inflate(R.layout.urisettings_fragment, container, false);

        // Dohvatimo objekt User iz baze podataka
        mUserViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        final User user = new User();
        try {
            User tmpUser = mUserViewModel.getUserById(0);
            if (tmpUser != null) {
                // ako korisnik vec postoji u bazi, koristimo spremljene podatke
                user.setBaseUri(tmpUser.getBaseUri());
                user.setComputeUri(tmpUser.getComputeUri());
                user.setIdentityUri(tmpUser.getIdentityUri());
                user.setDefaultProject(tmpUser.getDefaultProject());
                user.setDomain(tmpUser.getDomain());
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Postavimo tekst u poljima za unos na vrijednosti iz baze podataka

        // Osnovni URI OpenStack servera. Ako koristimo DevStack, dovoljno
        // je popuniti osnovni URI, a ostali endpointi ce se sami popuniti
        mURIText = (EditText) fragmentView.findViewById(R.id.uriEditText);
        mURIText.setText(user.getBaseUri());
        if (user.getBaseUri() == null || user.getBaseUri().length() < 2) {
            // Ako ne postoji spremljen URI, postavimo tekst na "http://"
            mURIText.setText("http://");
        }

        // URI za Compute API
        mComputeText = fragmentView.findViewById(R.id.computeEditText);
        mComputeText.setText(user.getComputeUri());

        // URI za Identity API
        mIdentityText = fragmentView.findViewById(R.id.identityEditText);
        mIdentityText.setText(user.getIdentityUri());

        // Naziv domene
        mDomainText = fragmentView.findViewById(R.id.domainEditText);
        mDomainText.setText((user.getDomain() != null && user.getDomain().length() > 1)
                ? user.getDomain() : "default");

        // Dohvatimo UI referncu na Save gumb
        mSaveButton = (Button) fragmentView.findViewById(R.id.saveUriButton);

        // Kad korisnik klikne Save, spremimo podatke u bazu
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Spremimo URI  ako polje nije prazno
                if (TextUtils.isEmpty(mURIText.getText())) {
                    // Ako je URI polje prazno, prikazemo poruku u Toast obavijesti
                    Toast.makeText(
                            getActivity().getApplicationContext(),
                            "Changes not saved. Not a valid URI.",
                            Toast.LENGTH_LONG).show();
                } else {
                    // Ako URI polje nije prazno, spremimo vrijednost u varijablu uriText
                    String uriText = mURIText.getText().toString();

                    User user = null;
                    try {
                        // Provjeravamo postoji li vec korisnik u bazi
                        user = mUserViewModel.getUserById(0);
                    } catch (Exception ex) {
                        // Ako se dogodila iznimka prilikom citanja baze,
                        // prikazemo tekst iznimke u Toast obavijesti
                        Log.println(Log.INFO, "URISettings", "EXCEPTION! " + ex.getMessage());
                        Toast.makeText(
                                getActivity().getApplicationContext(),
                                "EXCEPTION! " + ex.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                    if (user == null) {
                        // Ako ne postoji korisnik u bazi, ispisujemo poruku da dodajemo  novog korisnika
                        // i kreiramo novi User objekt
                        user = new User();
                        user.setUid(0);
                        Toast.makeText(
                                getActivity().getApplicationContext(),
                                "adding new user ",
                                Toast.LENGTH_LONG).show();
                    } else {
                        // Ako korisnik postoji, ispisujemo poruku da azuriramo podatke
                        Toast.makeText(
                                getActivity().getApplicationContext(),
                                "updated",
                                Toast.LENGTH_LONG).show();
                    }
                    // Postavimo korisnikov BaseUri na vrijednost uriText
                    user.setBaseUri(uriText);

                    // Dohvatimo tekst u polju Identity API.
                    String identityText = mIdentityText.getText().toString();
                    Log.i("URISettings", "Identity: " + identityText);
                    if (identityText.length() < 2) {
                        // Ako nije unesen Identity URI, generiramo ga na temelju
                        // osnovnog BaseURI-ja
                        user.setIdentityUri(user.getBaseUri() + "/identity/v3");
                    } else {
                        // Ako je unesen Indentity URI, spremimo  tu vrijednost
                        // u objekt User
                        user.setIdentityUri(identityText);
                    }
                    // Dohvatimo tekst u polju Compute API
                    String computeText = mComputeText.getText().toString();
                    Log.i("URISettings", "Compute: " + computeText);
                    if (computeText.length() < 2) {
                        // Ako nije unesen Compute URI, generiramo ga na temelju
                        // osnovnog BaseURI-ja
                        user.setComputeUri(user.getBaseUri() + "/compute/v2.1");
                    } else {
                        // Ako je unesen Compute URI, spremimo  tu vrijednost
                        // u objekt User
                        user.setComputeUri(computeText);
                    }
                    // Dohvatimo tekst u polju Domain
                    String domainText = mDomainText.getText().toString();
                    Log.i("URISettings", "Domain: " + domainText);
                    if (domainText.length() < 1) {
                        // Ako nije unesen domain, postavimo vrijednost na "default"
                        user.setDomain("default");
                    } else {
                        // Ako je unesen domain, spremimo ga u objekt User
                        user.setDomain(domainText);
                    }
                    // Zapisemo promjene u bazu
                    mUserViewModel.insertOrUpdate(user);
                    // Prikazemo Toast obavijest da su promjene spremljene
                    Toast.makeText(
                            getActivity().getApplicationContext(),
                            "Changes saved.",
                            Toast.LENGTH_LONG).show();
                }

                // Zatvorimo Settings fragment
                getActivity().findViewById(R.id.fragment_container).setVisibility(View.GONE);
            }
        });

        // Dohvatimo UI referencu za Cancel gumb
        mCancelButton = fragmentView.findViewById(R.id.settingsCancelButton);
        // Ako je kliknut Cancel, samo zatvorimo Settings fragment
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().findViewById(R.id.fragment_container).setVisibility(View.GONE);
            }
        });
        return fragmentView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

}
