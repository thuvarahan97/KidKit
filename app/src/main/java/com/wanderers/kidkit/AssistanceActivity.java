package com.wanderers.kidkit;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AssistanceActivity extends AppCompatActivity {

    TextView tvHelp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assistance);

        tvHelp = findViewById(R.id.tvHelp);

        tvHelp.setMovementMethod(new ScrollingMovementMethod());
        tvHelp.setEnabled(true);

        tvHelp.setText("KIDKIT \n" +
                "by TEAM WANDERERS SRI LANKA\n" +
                "\n" +
                "SOFTWARE INSTRUCTIONS\n" +
                " \n" +
                "IMPORTANT: Make sure your mobile device is connected to Internet and your device’s Bluetooth is ON.\n" +
                "\n" +
                "1.\tPower ON the KidKit device and connect the three ECG leads on your right arm, left arm and right leg as shown below.\n" +
                "\n" +
                "Right Arm-Red lead\n" +
                "Left Arm- Yellow lead\n" +
                "Right Leg- Green lead\n" +
                "\n" +
                "2.\tHome Screen consists of the tabs as follows:\n" +
                "• New Task:- Will begin a new task of Monitoring Blood potassium level.\n" +
                "• Progress:- Will show your progress related to blood potassium level.\n" +
                "• Help:- This is to support the user.\n" +
                "• Profile:- Will display your profile details.\n" +
                "3.\tPress new task to begin a new blood potassium monitoring task. This will take you to following screen.\n" +
                "\n" +
                "4.\tPress “CONNECT” to connect your mobile device to KidKit hardware device through Bluetooth. (Make sure your Bluetooth is ON).\n" +
                "\n" +
                "Note: App is configured only to connect to a specific KidKit hardware devices, verified by us)\n" +
                "\n" +
                "5.\tWhen the device is connected the button will change to “START”. Press “START” to receive ECG data from the hardware device. When you press start the Timer will begin.\n" +
                "\n" +
                "6.\tPress “STOP” after 15 seconds and then press “SHOW RESULT” to get your blood potassium level.\n" +
                "\n" +
                "7.\tYou can check your progress by pressing “PROGRESS” in the main display.\n");
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
