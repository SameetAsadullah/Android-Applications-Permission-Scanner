package com.sameetasadullah.project;

import static android.content.ContentValues.TAG;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AppOpsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class trackApp extends AppCompatActivity {
    List<ApplicationInfo> susApps;
    static TextView displayText;
    TextView chooseFeatures;
    Button button;
    CheckBox checkBoxCamera, checkBoxMic;
    static String appName;
    String appPackageName;
    boolean isCameraInUse = false, isMicInUse = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_app);

        displayText = findViewById(R.id.tv_displayText);
        chooseFeatures = findViewById(R.id.tv_choose_features);
        button = findViewById(R.id.button);
        checkBoxCamera = findViewById(R.id.checkBoxCamera);
        checkBoxMic = findViewById(R.id.checkBoxMic);
        susApps = (List<ApplicationInfo>) getIntent().getSerializableExtra("susApps");

        Intent intent = getIntent();
        appPackageName = intent.getStringExtra("susAppPackage");
        appName = getIntent().getStringExtra("susAppName");
        chooseFeatures.setText("Choose what \"" + appName + "\" uses in the application");

        button.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                checkBoxCamera.setEnabled(false);
                checkBoxMic.setEnabled(false);

                if (checkBoxCamera.isChecked() && checkBoxMic.isChecked()) {
                    displayText.setTextColor(Color.parseColor("#00CC06"));
                    displayText.setText("The app \"" + appName + "\" is safe. Good to Go.");
                }

                else {
                    ActivityCompat.requestPermissions(trackApp.this, new String[] {
                            "android.permission.RECORD_AUDIO",
                            "android.permission.PACKAGE_USAGE_STATS",
                            "android.permission.CAMERA"
                    }, 1);

                    AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
                    int mode = appOps.checkOpNoThrow("android:get_usage_stats",
                            android.os.Process.myUid(), getPackageName());
                    boolean granted = mode == AppOpsManager.MODE_ALLOWED;
                    if (!granted) {
                        startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                        Toast.makeText(trackApp.this, "Kindly give permission to our application for tracking.", Toast.LENGTH_LONG).show();
                    }

                    displayText.setTextColor(Color.parseColor("#FFFFFF"));
                    displayText.setText
                            ("We are tracking the suspicious application \"" + appName + "\". Kindly minimize this " +
                                    "application, open \"" + appName + "\" and use it for " +
                                    "at least five minutes. We will update this text after we are done with " +
                                    "the tracking. Thank You.");

                    Intent serviceIntent = new Intent(trackApp.this, trackerService.class);
                    serviceIntent.putExtra("susAppPackage", appPackageName);
                    if (!checkBoxCamera.isChecked()) {
                        serviceIntent.putExtra("checkCamera", 1);
                    }
                    if (!checkBoxMic.isChecked()) {
                        serviceIntent.putExtra("checkMic", 1);
                    }
                    startService(serviceIntent);
                }
            }
        });
    }

    public static void setTextView(boolean isCameraInUse, boolean isMicInUse) {
        if (isCameraInUse && isMicInUse) {
            displayText.setTextColor(Color.parseColor("#FF0000"));
            displayText.setText("\"" + appName + "\" is using your camera and " +
                    "microphone without your consent. Delete the application immediately.");
        }
        else if (isCameraInUse) {
            displayText.setTextColor(Color.parseColor("#FF0000"));
            displayText.setText("\"" + appName + "\" is using your camera without " +
                    "your consent. Delete the application immediately.");
        }
        else if (isMicInUse){
            displayText.setTextColor(Color.parseColor("#FF0000"));
            displayText.setText("\"" + appName + "\" is using your microphone without " +
                    "your consent. Delete the application immediately.");
        }
        else {
            displayText.setTextColor(Color.parseColor("#00CC06"));
            displayText.setText("The app \"" + appName + "\" is safe. Good to Go.");
        }
    }

    public static class broadCastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isCameraInUse = intent.getBooleanExtra("isCameraInUse", false);
            boolean isMicInUse = intent.getBooleanExtra("isMicInUse", false);
            trackApp.setTextView(isCameraInUse, isMicInUse);
        }
    }
}