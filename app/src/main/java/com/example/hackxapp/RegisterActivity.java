package com.example.hackxapp;

import androidx.annotation.IntRange;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.ConfirmPassword;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.Max;
import com.mobsandgeeks.saripaar.annotation.Min;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Or;
import com.mobsandgeeks.saripaar.annotation.Password;
import com.mobsandgeeks.saripaar.annotation.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class RegisterActivity extends AppCompatActivity implements Validator.ValidationListener {

    private String districtName;
    private String genderName;

//    @NotEmpty
//    EditText etNic;

    @NotEmpty
    EditText etFirstName;

    @NotEmpty
    EditText etLastName;

    Spinner spGender;

    @NotEmpty
    @Min(value = 1, message = "Should be greater than 1")
    @Or
    @Max(value = 150, message = "Should be less than 150")
    EditText etAge;

//    Spinner spDistrict;

    @NotEmpty
    EditText etTelephone;

    @NotEmpty
    @Email
    EditText etEmail;

    @NotEmpty
    @Password(min = 6, scheme = Password.Scheme.ALPHA_NUMERIC_MIXED_CASE_SYMBOLS)
    EditText etPassword;

    @NotEmpty
    @ConfirmPassword
    EditText etCPassword;

//    @NotEmpty
//    EditText etVerification;

    Button bRegister;

    private Validator validator;

    TextView loginLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        getWindow().setBackgroundDrawableResource(R.drawable.screen_bg2);

//        etNic = findViewById(R.id.etNic);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        spGender = findViewById(R.id.spGender);
        etAge = findViewById(R.id.etAge);
//        spDistrict = findViewById(R.id.spDistrict);
        etTelephone = findViewById(R.id.etTelephone);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etCPassword = findViewById(R.id.etCPassword);
//        etVerification = findViewById(R.id.etVerification);
        bRegister = findViewById(R.id.bRegister);

        loginLink = findViewById(R.id.tvSignIn);

        validator = new Validator(this);
        validator.setValidationListener(this);

//        districtName = "";
        genderName = "";

//        ArrayAdapter<CharSequence> adapterDistricts = ArrayAdapter.createFromResource(this, R.array.districts, R.layout.spinner_item);
//        adapterDistricts.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
//        spDistrict.setAdapter(adapterDistricts);
//        spDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                districtName = adapterView.getItemAtPosition(i).toString();
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//                districtName = adapterView.getItemAtPosition(0).toString();
//            }
//        });

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

        loginLink.setOnClickListener(new View.OnClickListener(){
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
//        final String nic = etNic.getText().toString();
        final String nic = "";
        final String firstname = etFirstName.getText().toString();
        final String lastname = etLastName.getText().toString();
        final String gender = genderName;
        final String age = etAge.getText().toString();
//        final String district = districtName;
        final String district = "";
        final String telephone = etTelephone.getText().toString();
        final String email = etEmail.getText().toString();
        final String password = etPassword.getText().toString();
        final String c_password = etCPassword.getText().toString();
//        final String code = etVerification.getText().toString();
        final String code = "";

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    String success = jsonResponse.getString("success");

                    switch (success) {
                        case "1":
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            RegisterActivity.this.startActivity(intent);
                            Toast.makeText(RegisterActivity.this,"Successfully Registered!",Toast.LENGTH_SHORT).show();
                            break;
                        case "3":
                            Toast.makeText(RegisterActivity.this, "Passwords don't match!", Toast.LENGTH_SHORT).show();
                            break;
                        case "4":
                            Toast.makeText(RegisterActivity.this, "A user with given NIC already exists!", Toast.LENGTH_SHORT).show();
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

        RegisterRequest registerRequest = new RegisterRequest(nic, firstname, lastname, gender, age, district, telephone, email, password, c_password, code, responseListener);
        RequestQueue queue = Volley.newRequestQueue(RegisterActivity.this);
        queue.add(registerRequest);
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
