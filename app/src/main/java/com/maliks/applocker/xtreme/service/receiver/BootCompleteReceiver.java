package com.maliks.applocker.xtreme.service.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import androidx.annotation.RequiresApi;
import com.maliks.applocker.xtreme.service.AppLockerService;

public class BootCompleteReceiver extends BroadcastReceiver {
    @RequiresApi(Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d("BootCompleteReceiver", "Boot completed. Starting AppLockerService.");

            Intent serviceIntent = new Intent(context, AppLockerService.class);
            context.startForegroundService(serviceIntent);
        }
    }
}

