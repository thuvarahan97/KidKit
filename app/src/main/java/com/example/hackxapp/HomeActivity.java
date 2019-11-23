package com.example.hackxapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.felipecsl.gifimageview.library.GifImageView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.squareup.picasso.Picasso;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageButton;

public class HomeActivity extends AppCompatActivity{

    SessionManager sessionManager;
    String getID;
    private static String URL_READ = "http://cardioapp.000webhostapp.com/read_detail.php";
    private static final String TAG = HomeActivity.class.getSimpleName();    //getting the info
//    private DrawerLayout mDrawerLayout;
//    private ActionBarDrawerToggle mToggle;
    private TextView user_name, user_email;
//    private Button btn_newTask;
    CircleImageView profile_image_small;
    String strImage;

//    GifImageButton heartImage;
//    GifDrawable gifImage;

    CardView bNewTask;
    CardView bProgress;
    CardView bAssist;
    CardView bProfile;

//    CarouselView carouselView;
//
//    int[] carouselImages = {R.drawable.c1, R.drawable.c2, R.drawable.c3, R.drawable.c4, R.drawable.c5};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sessionManager = new SessionManager(this);
        sessionManager.checkLogin();
        
        HashMap<String, String> user = sessionManager.getUserDetail();
        String mName = user.get(sessionManager.NAME);
        String mEmail = user.get(sessionManager.EMAIL);
        getID = user.get(sessionManager.ID);

//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

//        mDrawerLayout = findViewById(R.id.drawer);
//        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.open, R.string.close);
//        mDrawerLayout.addDrawerListener(mToggle);
//        mToggle.syncState();

//        NavigationView navigationView = findViewById(R.id.design_navigation_view);
//        navigationView.setNavigationItemSelectedListener(this);

//        View headerView = navigationView.getHeaderView(0);
        user_name = findViewById(R.id.user_name);
        user_email = findViewById(R.id.user_email);
        profile_image_small = findViewById(R.id.profile_image_small);

        user_name.setText(mName);
        user_email.setText(mEmail);


        if (sessionManager.isLogin()== true) {
            getUserDetail();
        }

//        btn_newTask = findViewById(R.id.bTask);
//        btn_newTask.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent taskIntent = new Intent(HomeActivity.this, GraphActivity.class);
//                startActivity(taskIntent);
//            }
//        });

//        carouselView = (CarouselView) findViewById(R.id.carouselView);
//        carouselView.setPageCount(carouselImages.length);
//
//        carouselView.setImageListener(imageListener);

//        heartImage = findViewById(R.id.heartImage);
//        try {
//            gifImage = new GifDrawable( getResources(), R.drawable.heart );
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        heartImage.setImageDrawable(gifImage);
//        gifImage.stop();
//        gifImage.seekToFrame(0);
//
//        heartImage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                gifImage.start();
//                TimerTask task = new TimerTask() {
//                    @Override
//                    public void run() {
//                        Intent taskIntent = new Intent(HomeActivity.this, GraphActivity.class);
//                        startActivity(taskIntent);
//                        gifImage.stop();
//                        gifImage.seekToFrame(0);
//                    }
//                };
//                Timer t = new Timer();
//                t.schedule(task, 2000);
//            }
//        });


        bNewTask = findViewById(R.id.bNewTask);
        bProgress = findViewById(R.id.bProgress);
        bAssist = findViewById(R.id.bAssist);
        bProfile = findViewById(R.id.bProfile);

        bNewTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newTaskIntent = new Intent(HomeActivity.this, GraphActivity.class);
                startActivity(newTaskIntent);
            }
        });

        bProgress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent progressIntent = new Intent(HomeActivity.this, AssistanceActivity.class);
//                startActivity(progressIntent);
            }
        });

        bAssist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent assistanceIntent = new Intent(HomeActivity.this, AssistanceActivity.class);
                startActivity(assistanceIntent);
            }
        });

        bProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent profileIntent = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(profileIntent);
            }
        });

    }

//    ImageListener imageListener = new ImageListener() {
//        @Override
//        public void setImageForPosition(int position, ImageView imageView) {
//            imageView.setImageResource(carouselImages[position]);
//        }
//    };

    private void getUserDetail(){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_READ,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG, response.toString());
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String success = jsonObject.getString("success");
                            JSONArray jsonArray = jsonObject.getJSONArray("read");

                            if (success.equals("1")) {

                                for (int i=0; i < jsonArray.length(); i++) {
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    strImage = object.getString("image").trim();
                                }
                                try {
                                    if (strImage.equals("")) {
                                        Picasso.get().load(R.drawable.profile_photo).into(profile_image_small);
                                    } else {
                                        Picasso.get().load(strImage).into(profile_image_small);
                                    }
                                } catch (Exception e) {
                                    Picasso.get().load(R.drawable.profile_photo).into(profile_image_small);
                                }

                            } else {
                                Picasso.get().load(R.drawable.profile_photo).into(profile_image_small);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Picasso.get().load(R.drawable.profile_photo).into(profile_image_small);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Picasso.get().load(R.drawable.profile_photo).into(profile_image_small);
                        Toast.makeText(HomeActivity.this, "Error Viewing Profile Image!", Toast.LENGTH_SHORT).show();
                    }
                })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("id", getID);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        getUserDetail();
//    }


//    public void onBackPressed() {
//        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
//            mDrawerLayout.closeDrawer(GravityCompat.START);
//        }
//        super.onBackPressed();
//    }

//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        if (mToggle.onOptionsItemSelected(item)){
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

//    @Override
//    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//        int itemId = item.getItemId();
//
//        if (itemId == R.id.logout){
//            sessionManager.logout();
//            Toast.makeText(this,"Successfully Logged Out!",Toast.LENGTH_SHORT).show();
//        }
//
//        if (itemId == R.id.profile){
//            Intent profileIntent = new Intent(HomeActivity.this, ProfileActivity.class);
//            HomeActivity.this.startActivity(profileIntent);
//        }
//
//        if (itemId == R.id.prevention){
//            Intent preventionIntent = new Intent(HomeActivity.this, PreventionMenuActivity.class);
//            HomeActivity.this.startActivity(preventionIntent);
//        }
//
//        if (itemId == R.id.kidney_disease){
//            Intent kidneyDiseaseIntent = new Intent(HomeActivity.this, KidneyDiseaseMenuActivity.class);
//            HomeActivity.this.startActivity(kidneyDiseaseIntent);
//        }
//
//        if (itemId == R.id.patients){
//            Intent patientsIntent = new Intent(HomeActivity.this, PatientsMenuActivity.class);
//            HomeActivity.this.startActivity(patientsIntent);
//        }
//
//        if (itemId == R.id.organ){
//            Intent organIntent = new Intent(HomeActivity.this, OrganDonationMenuActivity.class);
//            HomeActivity.this.startActivity(organIntent);
//        }
//
//        if (itemId == R.id.events){
//            Intent eventsIntent = new Intent(HomeActivity.this, EventsMenuActivity.class);
//            HomeActivity.this.startActivity(eventsIntent);
//        }
//
//        if (itemId == R.id.professionals){
//            Intent professionalsIntent = new Intent(HomeActivity.this, ProfessionalsMenuActivity.class);
//            HomeActivity.this.startActivity(professionalsIntent);
//        }
//
//        if (itemId == R.id.advocacy){
//            Intent advocacyIntent = new Intent(HomeActivity.this, AdvocacyMenuActivity.class);
//            HomeActivity.this.startActivity(advocacyIntent);
//        }
//
//        if (itemId == R.id.donate){
//            Intent donateIntent = new Intent(HomeActivity.this, DonateMenuActivity.class);
//            HomeActivity.this.startActivity(donateIntent);
//        }
//
//        return false;
//    }

}
