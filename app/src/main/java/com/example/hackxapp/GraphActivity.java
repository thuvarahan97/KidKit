package com.example.hackxapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
    int bufferPosition;
    boolean stopThread;

    Timer timer = new Timer();

    private Chronometer chronometer;
    private boolean running;

//    long firstTime;
//    int xValue;
//    String yValue;
//    private int lastX = 0;

    String mainParent;

    String data = "";

    Double finalResultDouble;
    String finalResult;

//    GraphView graphView;
//    LineGraphSeries series;

    FirebaseDatabase database;
    DatabaseReference reference;
    DatabaseReference parent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        sessionManager = new SessionManager(this);
        sessionManager.checkLogin();

//        firstTime = 0;
//        xValue = 0;
//        yValue = "";

        startButton = (Button) findViewById(R.id.buttonStart);
        sendButton = (Button) findViewById(R.id.buttonSend);
        stopButton = (Button) findViewById(R.id.buttonStop);
        resultButton = (Button) findViewById(R.id.buttonResult);
        textView = (TextView) findViewById(R.id.textView);
        textViewResult = (TextView) findViewById(R.id.textViewResult);
        setUiEnabled(false);

        textView.setMovementMethod(new ScrollingMovementMethod());

//        graphView = findViewById(R.id.graph);
//        series = new LineGraphSeries();
//        graphView.addSeries(series);

        HashMap<String, String> user = sessionManager.getUserDetail();
        mainParent = user.get(sessionManager.ID);

        database = FirebaseDatabase.getInstance();
        reference = database.getReference(mainParent);
//        Date date = new Date();
//        long taskTime = date.getTime();

        parent = reference.child("newtask");
        parent.setValue(null);
        parent.removeValue();

        finalResult = "";
        finalResultDouble = 4.6;

        chronometer = findViewById(R.id.chronometer);

        //setListeners();

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
                textView.append("\nConnection Opened!\n");
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
//                                    Date date = new Date();
//                                    long currTime = date.getTime();
//                                    if (firstTime == 0) {
//                                        firstTime = currTime;
//                                    }
//                                    long subVal = currTime - firstTime;
//                                    String timeString = subVal + "";
//                                    xValue = Integer.parseInt(timeString);
//                                    yValue = string;

                                    //series.appendData(new DataPoint(lastX++, RANDOM.nextDouble() * 10d), true, 10);

//                                    String[] lines = string.split("\\r?\\n");
//                                    for (String line : lines) {
//                                        textView.append("line " + lastX++ + " : " + line + "\n");
//                                    }

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
//        String string = "1";
//        string.concat("\n");
//        try {
//            outputStream.write(string.getBytes());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        textView.append("\nReading Started!\n");

        sendButton.setEnabled(false);
        stopButton.setEnabled(true);

        final int[] minuteCount = {1};
        final int MINUTES = 1; // The delay in minutes
//        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() { // Function runs every MINUTES minutes.
                String valuestr = data;
                data = "";
                if (!valuestr.equals("")) {
                    String[] values = valuestr.split(",");

//                    String id = parent.push().getKey();

                    String key = "minute" + minuteCount[0];
//                    String resultKey = "result" + minuteCount[0];

                    List<Integer> intList = new ArrayList<Integer>();
                    for(String numeric : values) {
                        if(!numeric.equals("")) {
                            intList.add(Integer.parseInt(numeric));
                        }
                    }

//                    List valList = new ArrayList<String>(Arrays.asList(values));
//                    valList.removeAll(Collections.singletonList(""));

//                    parent.child(resultKey).setValue("");
                    parent.child(key).setValue(intList);

                    minuteCount[0]++;

                } else {
                    timer.cancel();
                    return;
                }

                //String x = "0";
                //String y = values;

                //PointValue pointValue = new PointValue(x,y);

                //reference.child(id).setValue(pointValue);
            }
        }, 15000, 1000 * 15 * MINUTES);
        // 1000 milliseconds in a second * 60 per minute * the MINUTES variable.

        parent.child("result2").child("0").setValue(4.6);

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
        textView.append("\nReading Stopped!\n");

        stopThread = true;

        outputStream.close();
        inputStream.close();
        socket.close();

//        data = "";

        timer.cancel();

        stopButton.setEnabled(false);
        resultButton.setEnabled(true);
        deviceConnected = false;
//        textView.append("\nConnection Closed!\n");

        if (running) {
            chronometer.stop();
            running = false;
        }

    }

    public void onClickResult(View view) {
        if (finalResult.equals("0")) {
            textView.append("\nWaiting for the results...\n");
        } else {
//            beginListenForData();
//            String string = finalResult;
//            string.concat("\n");
//            float result = Float.parseFloat(finalResult);
//            String string = "";
//            if (result < 3.5) {
//                string = "b";
//            } else if (result >= 3.5 && result <= 5) {
//                string = "g";
//            } else if (result > 5) {
//                string = "r";
//            }
//
//            try {
//                outputStream.write(string.getBytes());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            textView.append("\nPotassium Level : " + finalResult + "\n");
            textViewResult.setText(finalResult);
//            textView.append("\nSent Result to the device!\n");

//            outputStream.close();
//            inputStream.close();
//            socket.close();

            resultButton.setEnabled(false);

//            deviceConnected = false;
//            textView.append("\nConnection Closed!\n");
        }
    }


//    private void setListeners() {
//
//    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//
//        reference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                DataPoint[] dp = new DataPoint[(int) dataSnapshot.getChildrenCount()];
//                int index = 0;
//
//                for(DataSnapshot myDataSnapshot : dataSnapshot.getChildren())
//                {
//                    PointValue pointValue = myDataSnapshot.getValue(PointValue.class);
//                    dp[index] = new DataPoint(Integer.parseInt(pointValue.getxValue()), Double.parseDouble(pointValue.getyValue()));
//                    index++;
//                }
//
//                series.resetData(dp);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//    }


    @Override
    protected void onStart() {
        super.onStart();

        parent.child("result2").child("0").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                finalResultDouble = (Double) snapshot.getValue();
                finalResult = String.valueOf(finalResultDouble);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
