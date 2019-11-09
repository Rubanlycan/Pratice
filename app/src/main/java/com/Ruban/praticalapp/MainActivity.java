package com.Ruban.praticalapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.Ruban.praticalapp.Database.myDatabase;
import com.Ruban.praticalapp.ModelClass.ModelData;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int G_SIGN_IN = 100;
    private static final int LOCATION_REQUEST_CODE = 101;
    private static final int GALLERY_CODE = 102;

    myDatabase db;
    SQLiteDatabase db_write;

    private SignInButton glogin;
    private EditText locationAddress, username;
    Geocoder geocoder;
    List<Address> address;
    LocationManager locationManager;
    String prophoto = "";

    Button save;
    ImageView userView;
    TextView useremail, logout;

    //    String longitute,latitude;
    private FirebaseAuth mAuth;

    ProgressBar progressBar;
    GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new myDatabase(this);
        db_write = db.getWritableDatabase();

        geocoder = new Geocoder(this, Locale.getDefault());

        logout = findViewById(R.id.logout);
        username = findViewById(R.id.name);
        useremail = findViewById(R.id.email);
        userView = findViewById(R.id.profile);
        save = findViewById(R.id.save);

        locationAddress = findViewById(R.id.address);
        progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.INVISIBLE);
        mAuth = FirebaseAuth.getInstance();
        glogin = findViewById(R.id.glogin);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();


        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
        glogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        if (mAuth.getCurrentUser() != null) {
            FirebaseUser user = mAuth.getCurrentUser();
            updateUI(user);
        }
    }


    private void signIn() {
        progressBar.setVisibility(View.VISIBLE);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, G_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case G_SIGN_IN:
                if (requestCode == G_SIGN_IN) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        if (account != null) firebaseAuthWithGoogle(account);
                    } catch (ApiException e) {
                        progressBar.setVisibility(View.INVISIBLE);
                        Log.w("TAG", "Google sign in failed", e);
                    }
                }
                break;
            case GALLERY_CODE:
                if (requestCode == GALLERY_CODE && resultCode == RESULT_OK) {
                    if (data != null) {
                        Uri uri = data.getData();
                        prophoto = String.valueOf(uri);
                        Glide.with(this).load(prophoto).into(userView);
                    }
                }

        }


    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount gaccount) {
        Log.d("TAG", "firebaseAuthWithGoogle:" + gaccount.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(gaccount.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        progressBar.setVisibility(View.INVISIBLE);
                        logout.setVisibility(View.VISIBLE);
                        locationAddress.setVisibility(View.INVISIBLE);
                        Log.d("TAG", "signInWithCredential:success");

                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        progressBar.setVisibility(View.INVISIBLE);

                        Log.w("TAG", "signInWithCredential:failure", task.getException());

                        Toast.makeText(MainActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }


                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {

            username.setVisibility(View.VISIBLE);
            useremail.setVisibility(View.VISIBLE);
            userView.setVisibility(View.VISIBLE);
            glogin.setVisibility(View.INVISIBLE);
            logout.setVisibility(View.VISIBLE);
            save.setVisibility(View.VISIBLE);
            locationAddress.setVisibility(View.VISIBLE);


            String[] data = {ModelData.DatabaseModelView.COLUMN_USER_NAME, ModelData.DatabaseModelView.COLUMN_IMAGE, ModelData.DatabaseModelView.COLUMN_EMAIL, ModelData.DatabaseModelView.COLUMN_USER_ADDRESS};
            Cursor cursor = db_write.query(ModelData.DatabaseModelView.TABLE_NAME, data, null, null, null, null, null);
            cursor.moveToFirst();

            String name = user.getDisplayName();
            String email = user.getEmail();
            prophoto = String.valueOf(user.getPhotoUrl());
            Log.i(TAG, "updateUI: " + prophoto);


//            for (int i = 1;i<cursor.getCount();i++)
//            {

//                Log.i(TAG, "updateUI: count "+cursor.getString(2));

//            }

            userView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent, "Select Profile Pic"), GALLERY_CODE);
                }
            });

            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveUserData(username.getText().toString().trim().toLowerCase(), prophoto, useremail.getText().toString().trim().toLowerCase(), locationAddress.getText().toString().trim().toLowerCase());
                }
            });

            username.setText(name);
            useremail.setText(email);
            Glide.with(MainActivity.this).load(prophoto).into(userView);

            locationPermission();


        } else {
            userView.setVisibility(View.INVISIBLE);
            locationAddress.setVisibility(View.INVISIBLE);
            logout.setVisibility(View.INVISIBLE);
            username.setVisibility(View.INVISIBLE);
            useremail.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            glogin.setVisibility(View.VISIBLE);
            save.setVisibility(View.INVISIBLE);
        }
    }

    void logout() {
        FirebaseAuth.getInstance().signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                task -> updateUI(null));
    }

    private void locationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            openAlertDialogue();
        } else {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            } else {
                onGpsEnabled();
            }
        }

    }

    void openAlertDialogue() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Enable GPS Location on Your Phone").setCancelable(false);
        builder.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                } else {
                    onGpsEnabled();
                }
                dialog.dismiss();
            }

        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create().show();

    }

    void saveUserData(String str_name, String str_image, String str_email, String str_address) {

        if (db.checkIfRecordExist(str_email)) {
            db.updateValues(str_name, str_image, str_email, str_address);
            Toast.makeText(MainActivity.this, "User Updated", Toast.LENGTH_SHORT).show();
        } else {
            db.valuesInsert(str_name, str_image, str_email, str_address);
            Toast.makeText(MainActivity.this, "User Saved", Toast.LENGTH_SHORT).show();

        }
    }

    private void onGpsEnabled() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager
                .PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermission();
        } else {
            Location locationgps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location networkProvider = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Location passiveProvider = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            if (locationgps != null) {
                double lat = locationgps.getLatitude();
                double log = locationgps.getLongitude();
                try {
                    address = geocoder.getFromLocation(lat, log, 1);
                    String Address = address.get(0).getAddressLine(0);
                    String area = address.get(0).getLocality();
                    String city = address.get(0).getAdminArea();
                    String postal_code = address.get(0).getPostalCode();

                    String fullAddress = Address + " " + area + " " + city + " " + postal_code;

                    locationAddress.setText(Address);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (networkProvider != null) {
                double lat = networkProvider.getLatitude();
                double log = networkProvider.getLongitude();

                try {
                    address = geocoder.getFromLocation(lat, log, 1);
                    String Address = address.get(0).getAddressLine(0);
                    String area = address.get(0).getLocality();
                    String city = address.get(0).getAdminArea();
                    String postal_code = address.get(0).getPostalCode();

                    String fullAddress = Address + " " + area + " " + city;

                    locationAddress.setText(Address);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (passiveProvider != null) {
                double lat = passiveProvider.getLatitude();
                double log = passiveProvider.getLongitude();
                try {
                    address = geocoder.getFromLocation(lat, log, 1);
                    String Address = address.get(0).getAddressLine(0);
                    String area = address.get(0).getLocality();
                    String city = address.get(0).getAdminArea();
                    String postal_code = address.get(0).getPostalCode();

                    String fullAddress = Address + " " + area + " " + city + " " + postal_code;

                    locationAddress.setText(Address);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "Can't get Location!", Toast.LENGTH_SHORT).show();
            }
        }

    }

//    void retrieveData(int a)
//    {
//        String[] projection = {ModelData.DatabaseModelView.COLUMN_USER_NAME,ModelData.DatabaseModelView.COLUMN_IMAGE,ModelData.DatabaseModelView.COLUMN_USER_ADDRESS};
//        Cursor c = db_write.query(ModelData.DatabaseModelView.TABLE_NAME,projection,null,null,null,null,null);
//        c.getString(Mode)
//    }

    @Override
    protected void onStart() {
        super.onStart();

    }


}
