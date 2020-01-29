package com.wanderers.kidkit;

import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class ProgressActivity extends AppCompatActivity implements ConnectionReceiver.ConnectionReceiverListener {

    String URL = "https://cardioapp.000webhostapp.com/get_progress.php";

    TextView tvNoResults;

    JSONObject userIPJSON;

    SessionManager sessionManager;

    String user_id;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        checkConnection();

        sessionManager = new SessionManager(this);
        sessionManager.checkLogin();

        HashMap<String, String> user = sessionManager.getUserDetail();

        user_id = user.get(sessionManager.ID);

        tvNoResults = findViewById(R.id.tvNoResults);

        getProgressFromDB(URL, user_id);

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

    public void getProgressFromDB(String url, final String user_id) {

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String success = jsonObject.getString("success");
                    String count = jsonObject.getString("count");
                    JSONArray jsonArrayResults = jsonObject.getJSONArray("progress_results");

//                    userIPJSON = new JSONObject("http://ip-api.com/json");
//                    java.net.URL url1 = new URL("http://ip-api.com/json");
//
//                    BufferedReader br = new BufferedReader(new InputStreamReader(url1.openStream()));
//                    String str = "";
//                    while (null != (str = br.readLine())) {
//                        Toast.makeText(ProgressActivity.this, str, Toast.LENGTH_SHORT).show();
//                    }

//                    String current_timezone = userIPJSON.getString("timezone");

                    ArrayList<ProgressItem> progressList = new ArrayList<>();

                    if (success.equals("1")) {

                        if (Integer.parseInt(count) > 0) {

                            for (int i = 0; i < jsonArrayResults.length(); i++) {

                                JSONObject objectResult = jsonArrayResults.getJSONObject(i);

                                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
                                formatter.setTimeZone(TimeZone.getTimeZone("Asia/Colombo"));
                                DateFormat finalDateFormat = new SimpleDateFormat("dd MMM yyyy");
                                DateFormat finalTimeFormat = new SimpleDateFormat("hh:mm a");

                                String strDatetime = objectResult.getString("datetime").trim();

                                Date date = formatter.parse(strDatetime);
                                String formattedDateString = finalDateFormat.format(date);
                                String formattedTimeString = finalTimeFormat.format(date).toUpperCase();

                                String strResult = objectResult.getString("result").trim();

                                Double k_level = Double.parseDouble(strResult);
                                String result_level = "";

                                if (k_level < 3.5 && k_level >= 0){
                                    result_level = "LOW";
                                } else if (k_level >= 3.5 && k_level <= 6){
                                    result_level = "NORMAL";
                                } else if (k_level > 6){
                                    result_level = "HIGH";
                                }
                                progressList.add(new ProgressItem(formattedDateString, formattedTimeString, strResult, result_level));

                            }

                            mRecyclerView = findViewById(R.id.recyclerView);
                            mRecyclerView.setHasFixedSize(true);
                            mLayoutManager = new LinearLayoutManager(ProgressActivity.this);
                            mAdapter = new ProgressAdapter(progressList);

                            mRecyclerView.setLayoutManager(mLayoutManager);
                            mRecyclerView.setAdapter(mAdapter);

                        } else {
                            tvNoResults.setText("No results found.");
                        }

                    } else {
                        tvNoResults.setText("No results found.");
                    }
                } catch (JSONException | ParseException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ProgressActivity.this, "Unable to load the progress!", Toast.LENGTH_SHORT).show();
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> postData = new HashMap<String, String>();
                postData.put("id", user_id);

                return postData;
            }
        };
        requestQueue.add(stringRequest);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        checkConnection();
        KidKitApplication.getInstance().setConnectionListener(this);
    }


    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if(!isConnected) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("No Internet Connection!")
                    .setCancelable(false)
                    .setNegativeButton("RETRY", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            finish();
                            startActivity(getIntent());
                        }
                    })
                    .create()
                    .show();

        }else{

            finish();
            startActivity(getIntent());

        }
    }

    private void checkConnection() {
        boolean isConnected = ConnectionReceiver.isConnected();
        if(!isConnected) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("No Internet Connection!")
                    .setCancelable(false)
                    .setNegativeButton("RETRY", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            finish();
                            startActivity(getIntent());
                        }
                    })
                    .create()
                    .show();

        }
    }


}
