package com.wanderers.kidkit;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class VerificationActivity extends AppCompatActivity {

    EditText etCode;
    Button bVerify;
    TextView tvDescription;

    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        getWindow().setBackgroundDrawableResource(R.drawable.screen_bg2);

        etCode = findViewById(R.id.etCode);
        bVerify = findViewById(R.id.bVerify);
        tvDescription = findViewById(R.id.tvDescription);

        Intent intent = getIntent();
        final String USER_ID = intent.getStringExtra("id");
        final String METHOD = intent.getStringExtra("method");

        if (METHOD.equals("register")) {
            tvDescription.setText("A verification code has been sent to your email. Enter the verification code to activate your user account.");
        } else {
            tvDescription.setText("Enter the verification code sent to your email to activate your user account.");
        }

        sessionManager = new SessionManager(this);

        bVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String CODE = etCode.getText().toString();
                verifyAccount(USER_ID, CODE, METHOD);
            }
        });
    }

    public void verifyAccount(final String user_id, final String code, final String method) {

        String url = "http://cardioapp.000webhostapp.com/verify_account.php";

        RequestQueue VerificationRequestQueue = Volley.newRequestQueue(this);

        StringRequest VerificationStringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    String success = jsonResponse.getString("success");

                    switch (success) {
                        case "1": {
                            if (method.equals("login")) {
                                String firstname = jsonResponse.getString("firstname").trim();
                                String lastname = jsonResponse.getString("lastname").trim();
                                String email = jsonResponse.getString("email").trim();

                                sessionManager.createSession(firstname, lastname, email, user_id);

                                final Intent login_intent = new Intent(VerificationActivity.this, HomeActivity.class);
                                login_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                login_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                                AlertDialog.Builder builder = new AlertDialog.Builder(VerificationActivity.this);
                                builder.setMessage("Account verification succeeded.")
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                VerificationActivity.this.startActivity(login_intent);
                                                Toast.makeText(VerificationActivity.this, "Successfully Logged In!", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .create()
                                        .show();
                            } else if (method.equals("register")) {
                                final Intent register_intent = new Intent(VerificationActivity.this, LoginActivity.class);
                                register_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                register_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                                AlertDialog.Builder builder = new AlertDialog.Builder(VerificationActivity.this);
                                builder.setMessage("Account verification succeeded.")
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                VerificationActivity.this.startActivity(register_intent);
                                                Toast.makeText(VerificationActivity.this, "Successfully Registered!", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .create()
                                        .show();
                            }
                            break;
                        }
                        case "2": {
                            AlertDialog.Builder builder2 = new AlertDialog.Builder(VerificationActivity.this);
                            builder2.setMessage("Invalid Verification Code!")
                                    .setNegativeButton("Retry", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            etCode.setText("");
                                        }
                                    })
                                    .create()
                                    .show();
                            break;
                        }
                        case "3": {
                            String firstname = jsonResponse.getString("firstname").trim();
                            String lastname = jsonResponse.getString("lastname").trim();
                            String email = jsonResponse.getString("email").trim();

                            sessionManager.createSession(firstname, lastname, email, user_id);

                            final Intent login_intent = new Intent(VerificationActivity.this, HomeActivity.class);
                            login_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            login_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                            AlertDialog.Builder builder2 = new AlertDialog.Builder(VerificationActivity.this);
                            builder2.setMessage("Account has been already verified!")
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            VerificationActivity.this.startActivity(login_intent);
                                            Toast.makeText(VerificationActivity.this, "Successfully Logged In!", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .create()
                                    .show();
                            break;
                        }
                        default: {
                            AlertDialog.Builder builder = new AlertDialog.Builder(VerificationActivity.this);
                            builder.setMessage("Account Verification Failed")
                                    .setNegativeButton("Retry", null)
                                    .create()
                                    .show();
                            break;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(VerificationActivity.this, "Verification Error!", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(VerificationActivity.this, "Verification Error!", Toast.LENGTH_SHORT).show();
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", user_id);
                params.put("code", code);

                return params;
            }
        };

        VerificationRequestQueue.add(VerificationStringRequest);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
