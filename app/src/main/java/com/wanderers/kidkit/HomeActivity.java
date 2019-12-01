package com.wanderers.kidkit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AppCompatActivity{

    SessionManager sessionManager;
    String getID;
    private static String URL_READ = "http://cardioapp.000webhostapp.com/read_detail.php";
    private static final String TAG = HomeActivity.class.getSimpleName();

    private TextView user_name, user_email;

    CircleImageView profile_image_small;
    String strImage;

    CardView bNewTask;
    CardView bProgress;
    CardView bAssist;
    CardView bProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sessionManager = new SessionManager(this);
        sessionManager.checkLogin();
        
        HashMap<String, String> user = sessionManager.getUserDetail();
        String mFirstName = user.get(sessionManager.FIRSTNAME);
        String mLasttName = user.get(sessionManager.LASTNAME);
        String mEmail = user.get(sessionManager.EMAIL);
        getID = user.get(sessionManager.ID);

        user_name = findViewById(R.id.user_name);
        user_email = findViewById(R.id.user_email);
        profile_image_small = findViewById(R.id.profile_image_small);

        user_name.setText(mFirstName + " " + mLasttName);
        user_email.setText(mEmail);


        if (sessionManager.isLogin()== true) {
            getUserDetail();
        }

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
                                        Picasso.get().load(R.drawable.profile_pic_default).into(profile_image_small);
                                    } else {
                                        Picasso.get().load(strImage).into(profile_image_small);
                                    }
                                } catch (Exception e) {
                                    Picasso.get().load(R.drawable.profile_pic_default).into(profile_image_small);
                                }

                            } else {
                                Picasso.get().load(R.drawable.profile_pic_default).into(profile_image_small);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Picasso.get().load(R.drawable.profile_pic_default).into(profile_image_small);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Picasso.get().load(R.drawable.profile_pic_default).into(profile_image_small);
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

    @Override
    protected void onResume() {
        super.onResume();
        getUserDetail();
    }

}
