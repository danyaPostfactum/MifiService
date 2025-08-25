package com.mifiservice.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.mifiservice.service.MifiService;

/* loaded from: classes.dex */
public class BootCompletedReceiver extends BroadcastReceiver {
    static final String BOOT_ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(BOOT_ACTION)) {
            Log.d("BootCompletedReceiver", "received BOOT_ACTION, start MIFI service");
            context.startService(new Intent(context, (Class<?>) MifiService.class));
        }
    }
}