package com.example.hackxapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class LoginActivity extends AppCompatActivity implements Validator.ValidationListener {

    SessionManager sessionManager;
    Validator validator;

    @NotEmpty
    private EditText etNic;

    @NotEmpty
    private EditText etPassword;

    private CheckBox mCheckBoxRememberMe;
    private SharedPreferences mPrefs;
    private static final String PREFS_NAME = "PrefsFile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getWindow().setBackgroundDrawableResource(R.drawable.screen_bg2);

        sessionManager = new SessionManager(this);

        mPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        etNic = findViewById(R.id.etNic);

        etPassword = findViewById(R.id.etPassword);

        final Button bLogin = findViewById(R.id.bLogin);
        final TextView registerLink = findViewById(R.id.tvRegisterHere);
        mCheckBoxRememberMe = findViewById(R.id.checkBoxRememberMe);

        validator = new Validator(this);
        validator.setValidationListener(this);

        registerLink.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                LoginActivity.this.startActivity(registerIntent);
            }
        });

        bLogin.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                validator.validate();
            }
        });

        SharedPreferences sp = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        if (sp.contains("pref_nic")) {
            String u = sp.getString("pref_nic", "not found.");
            etNic.setText(u.toString());
        }
        if (sp.contains("pref_pass")) {
            String p = sp.getString("pref_pass", "not found.");
            etPassword.setText(p.toString());
        }
        if (sp.contains("pref_check")) {
            Boolean b = sp.getBoolean("pref_check", false);
            mCheckBoxRememberMe.setChecked(b);
        }

//        if (isNetwork(getApplicationContext())){
//            Toast.makeText(getApplicationContext(), "Internet Connected", Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(getApplicationContext(), "Internet Disconnected", Toast.LENGTH_SHORT).show();
//        }

    }

//    public boolean isNetwork(Context context) {
//        ConnectivityManager cm = (ConnectivityManager) context
//                .getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo netInfo = cm.getActiveNetworkInfo();
//        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
//            return true;
//        }
//        return false;
//    }

    @Override
    public void onValidationSucceeded() {
        final String nic = etNic.getText().toString();
        final String password = etPassword.getText().toString();

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    String success = jsonResponse.getString("success");

                    switch (success) {
                        case "1": {
                            String name = jsonResponse.getString("name").trim();
                            String email = jsonResponse.getString("email").trim();
                            String id = jsonResponse.getString("id").trim();

                            sessionManager.createSession(name, email, id);

                            if (mCheckBoxRememberMe.isChecked()) {
                                Boolean boolIsChecked = mCheckBoxRememberMe.isChecked();
                                SharedPreferences.Editor editor = mPrefs.edit();
                                editor.putString("pref_nic", nic);
                                editor.putString("pref_pass", password);
                                editor.putBoolean("pref_check", boolIsChecked);
                                editor.apply();
                                Toast.makeText(getApplicationContext(), "Settings have been saved.", Toast.LENGTH_SHORT).show();
                            } else {
                                mPrefs.edit().clear().apply();
                            }

                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            intent.putExtra("name", name);
                            intent.putExtra("email", email);

                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                            LoginActivity.this.startActivity(intent);
                            Toast.makeText(LoginActivity.this, "Successfully Logged In!", Toast.LENGTH_SHORT).show();
                            break;
                        }
                        case "2": {
                            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                            builder.setMessage("Incorrect Password!")
                                    .setNegativeButton("Retry", null)
                                    .create()
                                    .show();
                            break;
                        }
                        case "3": {
                            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                            builder.setMessage("User does not exist!")
                                    .setNegativeButton("Retry", null)
                                    .create()
                                    .show();
                            break;
                        }
                        default: {
                            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                            builder.setMessage("Login Failed")
                                    .setNegativeButton("Retry", null)
                                    .create()
                                    .show();
                            break;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(LoginActivity.this, "Login Error!", Toast.LENGTH_SHORT).show();
                }
            }
        };

        LoginRequest loginRequest = new LoginRequest(nic, password, responseListener);
        RequestQueue queue = Volley.newRequestQueue(LoginActivity.this);
        queue.add(loginRequest);
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(this);

            // Display error messages ;)
            if (view instanceof EditText) {
                ((EditText) view).setError(message);
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        }
    }
}
