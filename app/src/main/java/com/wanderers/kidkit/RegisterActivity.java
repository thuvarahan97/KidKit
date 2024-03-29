package com.wanderers.kidkit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.ConfirmPassword;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Password;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class RegisterActivity extends AppCompatActivity implements Validator.ValidationListener {

    @NotEmpty
    EditText etFirstName;
    @NotEmpty
    EditText etLastName;
    Spinner spGender;
    @NotEmpty
    @Email
    EditText etEmail;
    @NotEmpty
    @Password(min = 6, scheme = Password.Scheme.ALPHA_NUMERIC_MIXED_CASE_SYMBOLS, message = "Password should contain at least one uppercase letter, one lower case letter, a symbol and a number (6 characters minimum)")
    EditText etPassword;
    @NotEmpty
    @ConfirmPassword
    EditText etCPassword;
    Button bRegister;
    TextView loginLink;
    private String genderName;
    private Validator validator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        getWindow().setBackgroundDrawableResource(R.drawable.screen_bg2);

        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        spGender = findViewById(R.id.spGender);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etCPassword = findViewById(R.id.etCPassword);
        bRegister = findViewById(R.id.bRegister);
        loginLink = findViewById(R.id.tvSignIn);

        validator = new Validator(this);
        validator.setValidationListener(this);

        genderName = "";

        ArrayAdapter<CharSequence> adapterGender = ArrayAdapter.createFromResource(this, R.array.gender, R.layout.spinner_item);
        adapterGender.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spGender.setAdapter(adapterGender);
        spGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                genderName = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                genderName = adapterView.getItemAtPosition(0).toString();
            }
        });

        bRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validator.validate();
            }
        });

        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                RegisterActivity.this.startActivity(loginIntent);
            }
        });
    }

    @Override
    public void onValidationSucceeded() {
        final String firstname = etFirstName.getText().toString();
        final String lastname = etLastName.getText().toString();
        final String gender = genderName;
        final String email = etEmail.getText().toString();
        final String password = etPassword.getText().toString();
        final String c_password = etCPassword.getText().toString();

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    String success = jsonResponse.getString("success");
                    String method = "register";

                    switch (success) {
                        case "1":
                            String id = jsonResponse.getString("id").trim();

                            Intent intent = new Intent(RegisterActivity.this, VerificationActivity.class);
                            intent.putExtra("id", id);
                            intent.putExtra("method", method);
                            RegisterActivity.this.startActivity(intent);
                            RegisterActivity.this.finish();
                            break;
                        case "3":
                            Toast.makeText(RegisterActivity.this, "Passwords don't match!", Toast.LENGTH_SHORT).show();
                            break;
                        case "4":
                            Toast.makeText(RegisterActivity.this, "User already exists!", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                            builder.setMessage("Register Failed")
                                    .setNegativeButton("Retry", null)
                                    .create()
                                    .show();
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(RegisterActivity.this, "Register Error!", Toast.LENGTH_SHORT).show();
                }
            }
        };

        RegisterRequest registerRequest = new RegisterRequest(firstname, lastname, gender, email, password, c_password, responseListener);
        RequestQueue queue = Volley.newRequestQueue(RegisterActivity.this);
        queue.add(registerRequest);
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(this);

            if (view instanceof EditText) {
                ((EditText) view).setError(message);
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        }
    }
}
