package com.wanderers.kidkit;

import androidx.appcompat.app.AppCompatActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class GraphActivity extends AppCompatActivity {

    SessionManager sessionManager;

    private final String DEVICE_ADDRESS="00:18:E4:34:C7:14";
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");//Serial Port Service ID
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    TextView textView, textViewResult;
    boolean deviceConnected = false;
    byte buffer[];
    boolean stopThread;

    Timer timer = new Timer();

    private Chronometer chronometer;
    private boolean running;

    String data = "";

    String finalResult = "";

    Button btn_function;
    int btn_function_step;

    String SERVER_URL = "http://13.76.155.34:5000/ecgData";    // Azure Server URL
    String RESULT_URL = "https://cardioapp.000webhostapp.com/add_result.php";

    String user_id ="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        sessionManager = new SessionManager(this);
        sessionManager.checkLogin();

        textView = findViewById(R.id.textView);
        textViewResult = findViewById(R.id.textViewResult);

        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.setEnabled(true);

        HashMap<String, String> user = sessionManager.getUserDetail();

        chronometer = findViewById(R.id.chronometer);

        btn_function = findViewById(R.id.buttonFunction);
        btn_function_step = 1;

        user_id = user.get(sessionManager.ID);

    }

    public boolean BTinit() {
        boolean found = false;
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Device doesn't support Bluetooth", Toast.LENGTH_SHORT).show();
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableAdapter, 0);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        if (bondedDevices.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please pair the device first", Toast.LENGTH_SHORT).show();
        } else {
            for (BluetoothDevice iterator : bondedDevices) {
                if (iterator.getAddress().equals(DEVICE_ADDRESS)) {
                    device = iterator;
                    found = true;
                    break;
                }
            }
        }
        return found;
    }

    public boolean BTconnect() {
        boolean connected = true;
        try {
            socket = device.createRfcommSocketToServiceRecord(PORT_UUID);
            socket.connect();
        } catch (IOException e) {
            e.printStackTrace();
            connected = false;
        }
        if (connected) {
            try {
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                inputStream = socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return connected;
    }

    void beginListenForData() {
        final Handler handler = new Handler();
        stopThread = false;
        buffer = new byte[1024];
        Thread thread  = new Thread(new Runnable() {
            public void run() {
                while (!Thread.currentThread().isInterrupted() && !stopThread) {
                    try {
                        int byteCount = inputStream.available();
                        if (byteCount > 0) {
                            byte[] rawBytes = new byte[byteCount];
                            inputStream.read(rawBytes);
                            final String string = new String(rawBytes, "UTF-8");
                            handler.post(new Runnable() {
                                public void run() {
                                    data = data + string;
                                }
                            });
                        }
                    } catch (IOException ex) {
                        stopThread = true;
                    }
                }
            }
        });
        thread.start();
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

    public void onClickFunction(View view) throws IOException {
        if (btn_function_step == 1) {
            if(BTinit()) {
                if (BTconnect()) {
                    deviceConnected = true;
                    beginListenForData();
                    textView.append("\n--> Connection Opened!\n");
                    btn_function.setText("START");
                    btn_function_step = 2;
                } else {
                    Toast.makeText(getApplicationContext(), "Unable to Connect!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Specific device is not found!", Toast.LENGTH_SHORT).show();
            }
        }
        else if (btn_function_step == 2) {
            textView.append("\n--> Reading Started!\n");

            if (!running) {
                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.start();
                running = true;
            }

            btn_function.setText("STOP");
            btn_function_step = 3;
        }
        else if (btn_function_step == 3) {

            textView.append("\n--> Reading Stopped!\n");

            stopThread = true;

            outputStream.close();
            inputStream.close();
            socket.close();

            deviceConnected = false;

            if (running) {
                chronometer.stop();
                running = false;
            }
            if (!data.equals("")) {
                String[] values = data.split(",");
                Map<String, String> data_map = new HashMap<String, String>();
                int i = 0;
                for(String numeric : values) {
                    if(!numeric.equals("")) {
                        data_map.put(String.valueOf(i),numeric);
                        i++;
                    }
                }

                sendDataToServer(SERVER_URL, data_map, new VolleyCallBack() {
                    @Override
                    public void onSuccess(JSONObject object) {
                        try {
                            finalResult = object.getString("K_level");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            btn_function.setText("SHOW RESULT");
            btn_function_step = 4;

        }
        else if (btn_function_step == 4) {

            if (!finalResult.equals("")){
                double k_level = Double.parseDouble(finalResult);
                String k_level_final = String.format("%.2f", k_level);
                textView.append("\n--> Potassium Level : " + k_level_final + "\n");
                textViewResult.setText(k_level_final);
                btn_function.setEnabled(false);

                sendResultToDatabase(RESULT_URL, k_level_final);

            } else {
                Toast.makeText(this, "Waiting for the results!", Toast.LENGTH_SHORT).show();
            }

        }
    }


    public void sendDataToServer(String url, Map<String,String> data, final VolleyCallBack callBack){


        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObj = new JsonObjectRequest(Request.Method.POST, url,new JSONObject(data),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            finalResult = response.getString("K_level");

                            callBack.onSuccess(response);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }
        ){
            //here I want to post data to sever
        };
        requestQueue.add(jsonObj);
    }


    public void sendResultToDatabase(String url, final String data){


        RequestQueue requestQueue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    //Code
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(GraphActivity.this, "Unable to save the result!", Toast.LENGTH_SHORT).show();
                }
            }) {
            protected Map<String, String> getParams() {
                Map<String, String> postData = new HashMap<String, String>();
                postData.put("result", data);
                postData.put("id", user_id);

                return postData;
            }
        };
        requestQueue.add(stringRequest);
    }


}