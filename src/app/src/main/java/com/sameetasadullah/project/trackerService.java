package com.sameetasadullah.project;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.io.File;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class trackerService extends Service {
    private static final boolean TODO = true;
    String appPackageName;
    int checkCamera, checkMic;
    boolean isMicInUse = false, isCameraInUse = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        appPackageName = intent.getStringExtra("susAppPackage");
        checkCamera = intent.getIntExtra("checkCamera", 0);
        checkMic = intent.getIntExtra("checkMic", 0);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(15000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String foregroundAppPackageName = getCurrentAppInForeground();
                while (foregroundAppPackageName == null || !foregroundAppPackageName.equals(appPackageName)) {
                    foregroundAppPackageName = getCurrentAppInForeground();
                }

                while (foregroundAppPackageName.equals(appPackageName) && (!isCameraInUse || !isMicInUse)) {
                    if (checkMic == 1 && isMicInUse()) {
                        isMicInUse = true;
                    }
                    if (checkCamera == 1 && isCameraInUse()) {
                        isCameraInUse = true;
                    }
                    foregroundAppPackageName = getCurrentAppInForeground();
                }
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction("sendInformation");
                broadcastIntent.setClass(trackerService.this, trackApp.broadCastReceiver.class);
                broadcastIntent.putExtra("isMicInUse", isMicInUse);
                broadcastIntent.putExtra("isCameraInUse", isCameraInUse);
                sendBroadcast(broadcastIntent);
                stopSelf();
            }
        }).start();
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    private String getCurrentAppInForeground() {
        if (Build.VERSION.SDK_INT >= 21) {
            String currentApp = null;
            UsageStatsManager usm = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();
            List<UsageStats> applist = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 1000, time);
            if (applist != null && applist.size() > 0) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<>();
                for (UsageStats usageStats : applist) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (mySortedMap != null && !mySortedMap.isEmpty()) {
                    currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                }
            }
            return currentApp;
        }
        else {
            ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            String mm=(manager.getRunningTasks(1).get(0)).topActivity.getPackageName();
            return mm;
        }
    }

    private boolean isMicInUse() {
        MediaRecorder recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        recorder.setOutputFile(new File(getCacheDir(), "MediaUtil#micAvailTestFile").getAbsolutePath());
        boolean available = false;
        try {
            recorder.prepare();
            recorder.start();
        }
        catch (Exception exception) {
            available = true;
        }
        recorder.release();
        return available;
    }

    public boolean isCameraInUse() {
        boolean available = false;
        Camera camera = null;

        try {
            camera = Camera.open();
        }
        catch (RuntimeException e) {
            available = true;
        }

        if (camera != null) {
            camera.release();
        }
        return available;
    }
}