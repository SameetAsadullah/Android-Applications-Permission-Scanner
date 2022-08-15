package com.sameetasadullah.project;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class scanApps extends AppCompatActivity {
    ProgressBar progressBar;
    RelativeLayout relativeLayout;
    int i;
    private Handler handler;
    List<ApplicationInfo> susApps;
    TextView susAppsTV;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_apps);

        progressBar = findViewById(R.id.progressBar);
        relativeLayout = findViewById(R.id.scanRL);
        susAppsTV = findViewById(R.id.susAppsTV);
        susApps = new ArrayList<>();
        handler = new Handler();
        i = progressBar.getProgress();

        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanApplications();
            }
        });

        ActivityCompat.requestPermissions(scanApps.this, new String[] {
                "android.permission.RECORD_AUDIO",
                "android.permission.PACKAGE_USAGE_STATS",
                "android.permission.CAMERA"
        }, 1);

        final PackageManager pm = getPackageManager();
        final List<ApplicationInfo> installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA);

//        for (int i = 0; i < installedApps.size(); ++i) {
//            if (installedApps.get(i).packageName.contains("com.xiaomi.cameratest")) {
//                System.out.println("Package Name: " + installedApps.get(i).packageName);
//                System.out.println("AAAAAAAAA");
//                System.out.println("Installer: " + pm.getInstallerPackageName(installedApps.get(i).packageName));
//            }
//        }
    }

    private void scanApplications() {
        final PackageManager pm = getPackageManager();
        final List<ApplicationInfo> installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        progressBar.setMax(installedApps.size());

        new Thread(new Runnable() {
            public void run() {
                while (i < installedApps.size()) {
                    ApplicationInfo app = installedApps.get(i);
                    if ((pm.getInstallerPackageName(app.packageName) != null &&
                            pm.getInstallerPackageName(app.packageName).
                                    equals("com.google.android.packageinstaller"))) {
                        try {
                            PackageInfo packageInfo = pm.getPackageInfo(app.packageName, PackageManager.GET_PERMISSIONS);
                            String[] requestedPermissions = packageInfo.requestedPermissions;
                            if (requestedPermissions != null) {
                                for (int i = 0; i < requestedPermissions.length; i++) {
                                    if (requestedPermissions[i].equals("android.permission.CAMERA") ||
                                            requestedPermissions[i].equals("android.permission.RECORD_AUDIO")) {
                                        susApps.add(app);
                                        break;
                                    }
                                }
                            }
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    i += 1;
                    handler.post(new Runnable() {
                        public void run() {
                            progressBar.setProgress(i);
                        }
                    });

                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        int totalSusApps = susApps.size();
                        if (totalSusApps == 0) {
                            susAppsTV.setTextColor(Color.parseColor("#00CC06"));
                            susAppsTV.setText("No suspicious app found. Safe to go.");
                        } else {
                            susAppsTV.setText(Integer.toString(totalSusApps) + " suspicious app found. Tap to view.");
                            susAppsTV.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(scanApps.this, showSusApps.class);
                                    intent.putExtra("susApps", (Serializable) susApps);
                                    startActivity(intent);
                                }
                            });
                        }
                    }
                });
            }
        }).start();
    }
}