package com.wanderers.kidkit;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class AssistanceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assistance);
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
