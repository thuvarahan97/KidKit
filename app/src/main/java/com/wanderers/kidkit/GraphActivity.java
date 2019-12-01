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
    Button startButton, sendButton, stopButton, resultButton;
    TextView textView;
    TextView textViewResult;
    boolean deviceConnected = false;
    Thread thread;
    byte buffer[];
    boolean stopThread;

    Timer timer = new Timer();

    private Chronometer chronometer;
    private boolean running;

    String data = "";

    Double finalResultDouble;
    String finalResult;

    Button btn_function;
    int btn_function_step;

    String url = "http://13.76.155.34:5000/ecgData";    // Azure Server URL

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        sessionManager = new SessionManager(this);
        sessionManager.checkLogin();

        startButton = (Button) findViewById(R.id.buttonStart);
        sendButton = (Button) findViewById(R.id.buttonSend);
        stopButton = (Button) findViewById(R.id.buttonStop);
        resultButton = (Button) findViewById(R.id.buttonResult);
        textView = (TextView) findViewById(R.id.textView);
        textViewResult = (TextView) findViewById(R.id.textViewResult);
        setUiEnabled(false);

        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.setEnabled(true);

        HashMap<String, String> user = sessionManager.getUserDetail();

        finalResult = "";
        finalResultDouble = 4.83;

        chronometer = findViewById(R.id.chronometer);

        btn_function = findViewById(R.id.buttonFunction);
        btn_function_step = 1;

    }

    public void setUiEnabled(boolean bool) {
        startButton.setEnabled(!bool);
        sendButton.setEnabled(bool);
        stopButton.setEnabled(bool);
        resultButton.setEnabled(bool);
        textView.setEnabled(bool);
    }

    public boolean BTinit() {
        boolean found = false;
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Device does not Support Bluetooth", Toast.LENGTH_SHORT).show();
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

    public void onClickStart(View view) {
        if(BTinit()) {
            if (BTconnect()) {
                startButton.setEnabled(false);
                sendButton.setEnabled(true);
                textView.setEnabled(true);
                deviceConnected = true;
                beginListenForData();
                textView.append("\n--> Connection Opened!\n");
            } else {
                Toast.makeText(getApplicationContext(), "Unable to Connect!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Specific device is not found!", Toast.LENGTH_SHORT).show();
        }
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

    public void onClickSend(View view) {
        textView.append("\n--> Reading Started!\n");

        sendButton.setEnabled(false);
        stopButton.setEnabled(true);

        final int[] minuteCount = {1};
        final int MINUTES = 1;

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                String valuestr = data;
                data = "";
                if (!valuestr.equals("")) {
                    String[] values = valuestr.split(",");

                    String key = "minute" + minuteCount[0];

                    List<Integer> intList = new ArrayList<Integer>();
                    for(String numeric : values) {
                        if(!numeric.equals("")) {
                            intList.add(Integer.parseInt(numeric));
                        }
                    }

                    minuteCount[0]++;

                } else {
                    timer.cancel();
                    return;
                }

            }
        }, 15000, 1000 * 15 * MINUTES);
        // 1000 milliseconds in a second * 60 per minute * the MINUTES variable.

        if (!running) {
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();
            running = true;
        }

    }

    public void onClickStop(View view) throws IOException {
        String string = "0";
        string.concat("\n");
        try {
            outputStream.write(string.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        textView.append("\n--> Reading Stopped!\n");

        stopThread = true;

        outputStream.close();
        inputStream.close();
        socket.close();

        timer.cancel();

        stopButton.setEnabled(false);
        resultButton.setEnabled(true);
        deviceConnected = false;

        if (running) {
            chronometer.stop();
            running = false;
        }

    }

    public void onClickResult(View view) {
        if (finalResult.equals("0")) {
            textView.append("\n--> Waiting for the results...\n");
        } else {
            textView.append("\nPotassium Level : " + finalResult + "\n");
            textViewResult.setText(finalResult);
            resultButton.setEnabled(false);
        }
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

            final int[] minuteCount = {1};
            final int MINUTES = 1;
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    String valuestr = data;
                    data = "";
                    if (!valuestr.equals("")) {
                        String[] values = valuestr.split(",");
                        String key = "minute" + minuteCount[0];
                        List<Integer> intList = new ArrayList<Integer>();
                        for(String numeric : values) {
                            if(!numeric.equals("")) {
                                intList.add(Integer.parseInt(numeric));
                            }
                        }

                        minuteCount[0]++;

                    } else {
                        timer.cancel();
                        return;
                    }
                }
            }, 15000, 1000 * 15 * MINUTES);

            if (!running) {
                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.start();
                running = true;
            }

            btn_function.setText("STOP");
            btn_function_step = 3;
        }
        else if (btn_function_step == 3) {
            String string = "0";
            string.concat("\n");
            try {
                outputStream.write(string.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            textView.append("\n--> Reading Stopped!\n");

            stopThread = true;

            outputStream.close();
            inputStream.close();
            socket.close();

            timer.cancel();

            deviceConnected = false;

            if (running) {
                chronometer.stop();
                running = false;
            }

            btn_function.setText("SHOW RESULT");
            btn_function_step = 4;

        }
        else if (btn_function_step == 4) {

                textView.append("\n--> Potassium Level : " + finalResult + "\n");
                textViewResult.setText(finalResult);
                btn_function.setEnabled(false);

        }
    }


    public String postData(String url, Map<String,String> data){

        final String[] result = {""};

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObj = new JsonObjectRequest(Request.Method.POST, url,new JSONObject(data),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            result[0] = response.getString("K_level");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(GraphActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
        ){
            //here I want to post data to sever
        };
        requestQueue.add(jsonObj);
        return result[0];
    }
}
