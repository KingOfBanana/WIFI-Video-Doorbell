package com.example.androidtcp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {
	
	@Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, LongRunningService.class);
        context.startService(serviceIntent);
    }
}
