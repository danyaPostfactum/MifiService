package com.mifiservice.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.mifiservice.device.DeviceController;
import com.mifiservice.device.LedController;

/* loaded from: classes.dex */
public class ShutdownReceiver extends BroadcastReceiver {
    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        Log.i("ShutdownReceiver", "shutdowning ...");
        if (DeviceController.getInstance().isRestoreFactory()) {
            LedController.getInstance().setMode(0, 1, 1);
            LedController.getInstance().setMode(1, 4, 1);
        }
    }
}