package com.wanderers.kidkit;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private TextView name, email, gender;
    private Button btn_photo_upload;
    private static final String TAG = ProfileActivity.class.getSimpleName();    //getting the info
    SessionManager sessionManager;
    String getID;
    private static String URL_READ = "http://cardioapp.000webhostapp.com/read_detail.php";
    private static String URL_UPLOAD = "http://cardioapp.000webhostapp.com/upload.php";
    private Bitmap bitmap;
    CircleImageView profile_image;
    String strImage = "";
    Button bLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sessionManager = new SessionManager(this);
        sessionManager.checkLogin();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btn_photo_upload = findViewById(R.id.btn_photo);
        profile_image = findViewById(R.id.profile_image);
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        gender = findViewById(R.id.gender);
        bLogout = findViewById(R.id.bLogout);

        HashMap<String, String> user = sessionManager.getUserDetail();
        getID = user.get(sessionManager.ID);

        btn_photo_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseFile();
            }
        });

        bLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            sessionManager.logout();
            Toast.makeText(ProfileActivity.this,"Successfully Logged Out!",Toast.LENGTH_SHORT).show();
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

                                    String strFirstName = object.getString("firstname").trim();
                                    String strLastName = object.getString("lastname").trim();
                                    String strEmail = object.getString("email").trim();
                                    String strGender = object.getString("gender").trim();
                                    strImage = object.getString("image").trim();

                                    name.setText(String.format("%s %s", strFirstName, strLastName));
                                    email.setText(strEmail);
                                    gender.setText(strGender);
                                }

                                try {
                                    if (strImage.equals("")) {
                                        Picasso.get().load(R.drawable.profile_pic_default).into(profile_image);
                                    } else {
                                        Picasso.get().load(strImage).into(profile_image);
                                    }
                                } catch (Exception e) {
                                    Picasso.get().load(R.drawable.profile_pic_default).into(profile_image);
                                }

                            } else {
                                Picasso.get().load(R.drawable.profile_pic_default).into(profile_image);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Picasso.get().load(R.drawable.profile_pic_default).into(profile_image);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Picasso.get().load(R.drawable.profile_pic_default).into(profile_image);
                        Toast.makeText(ProfileActivity.this, "Error Reading Detail!", Toast.LENGTH_SHORT).show();
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

    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void chooseFile() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            try {
//                profile_image.setImageResource(0);
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
//                profile_image.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }

            UploadPicture(getID, getStringImage(bitmap));
        }
    }

    private void UploadPicture(final String id, final String photo) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Uploading...");
            progressDialog.show();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_UPLOAD,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            progressDialog.dismiss();
                            Log.i(TAG, response.toString());
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                String success = jsonObject.getString("success");

                                if (success.equals("1")){
                                    if (strImage.equals("")) {
                                        getUserDetail();
                                    } else {
                                        Picasso.get().load(strImage).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(profile_image);
                                    }
                                    Toast.makeText(ProfileActivity.this, "Successfully Uploaded!", Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                progressDialog.dismiss();
                                Toast.makeText(ProfileActivity.this, "Try Again!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            progressDialog.dismiss();
                            Toast.makeText(ProfileActivity.this, "Try Again!", Toast.LENGTH_SHORT).show();
                        }
                    })
            {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("id", id);
                    params.put("photo", photo);

                    return params;
                }
            };

            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(stringRequest);
    }

    public String getStringImage(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);

        byte[] imageByteArray = byteArrayOutputStream.toByteArray();
        String encodedImage = Base64.encodeToString(imageByteArray, Base64.DEFAULT);

        return encodedImage;
    }
}
