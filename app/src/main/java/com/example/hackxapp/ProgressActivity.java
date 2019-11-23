package com.example.hackxapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class ProgressActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
