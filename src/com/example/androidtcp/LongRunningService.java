package com.example.androidtcp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import java.io.File;
import java.util.Date;

public class LongRunningService  extends Service {

	File jpgFile = new File(Environment.getExternalStorageDirectory(), "testJpg0.jpg");
	private long jpglenth = 0;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    /*    new Thread(new Runnable() {
            @Override
            public void run() {
                if((jpgFile.exists() == true )&&(jpgFile.isDirectory() == false)) {
                	//Log.d("LongRunningService", "The length of file is : " + jpgFile.length());
                    if(jpglenth == jpgFile.length()) {
                    	
                    }
                    else {
                    	jpglenth = jpgFile.length();
                    }
                }
            }
        }).start();*/
        Intent broadcastIntent = new Intent("com.example.androidtcp.receiver");
    	sendBroadcast(broadcastIntent);
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int aSecond = 3000;
        long triggerAtTime = SystemClock.elapsedRealtime() + aSecond;
        Intent intentReceive = new Intent(this, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, intentReceive, 0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);      
        return super.onStartCommand(intent, flags, startId);
    }
}
