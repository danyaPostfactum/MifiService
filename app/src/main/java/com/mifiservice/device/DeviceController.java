package com.mifiservice.device;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import com.mifiservice.config.MifiConfiguration;
import com.mifiservice.service.MifiService;
import java.lang.reflect.Method;
import org.eclipse.jetty.http.HttpVersions;

/* loaded from: classes.dex */
public class DeviceController {
    private static final String TAG = "DeviceController";
    private static DeviceController instance = null;
    private Context mContext;
    private PowerManager pPowerManager;
    private boolean isCharging = false;
    private int batteryCapacity = 1;
    private boolean isReFac = false;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() { // from class: com.mifiservice.device.DeviceController.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int status = intent.getIntExtra("status", 0);
            int health = intent.getIntExtra("health", 0);
            boolean present = intent.getBooleanExtra("present", false);
            int level = intent.getIntExtra("level", 0);
            int scale = intent.getIntExtra("scale", 100);
            int plugged = intent.getIntExtra("plugged", 0);
            int voltage = intent.getIntExtra("voltage", 0);
            int temperature = intent.getIntExtra("temperature", 0);
            String technology = intent.getStringExtra("technology");
            String statusString = HttpVersions.HTTP_0_9;
            int percent = (level * 100) / scale;
            DeviceController.this.batteryCapacity = percent;
            switch (status) {
                case 1:
                    statusString = "unknown";
                    DeviceController.this.isCharging = false;
                    break;
                case 2:
                    statusString = "charging";
                    DeviceController.this.isCharging = true;
                    break;
                case 3:
                    statusString = "discharging";
                    DeviceController.this.isCharging = false;
                    break;
                case 4:
                    statusString = "not charging";
                    DeviceController.this.isCharging = false;
                    break;
                case 5:
                    statusString = "full";
                    DeviceController.this.isCharging = false;
                    break;
            }
            String healthString = HttpVersions.HTTP_0_9;
            switch (health) {
                case 1:
                    healthString = "unknown";
                    break;
                case 2:
                    if (status != 5 && status == 2) {
                    }
                    healthString = "good";
                    break;
                case 3:
                    healthString = "overheat";
                    break;
                case 4:
                    healthString = "dead";
                    break;
                case 5:
                    healthString = "voltage";
                    break;
                case 6:
                    healthString = "unspecified failure";
                    break;
            }
            String acString = HttpVersions.HTTP_0_9;
            switch (plugged) {
                case 1:
                    acString = "plugged ac";
                    break;
                case 2:
                    acString = "plugged usb";
                    break;
            }
            Log.i("cat", statusString);
            Log.i("cat", healthString);
            Log.i("cat", String.valueOf(present));
            Log.i("cat", String.valueOf(level));
            Log.i("cat", String.valueOf(scale));
            Log.i("cat", acString);
            Log.i("cat", String.valueOf(voltage));
            Log.i("cat", String.valueOf(temperature));
            Log.i("cat", technology);
            if (action.equals("android.intent.action.BATTERY_CHANGED")) {
                Log.d("Battery", HttpVersions.HTTP_0_9 + intent.getIntExtra("level", 0));
                Log.d("Battery", HttpVersions.HTTP_0_9 + intent.getIntExtra("scale", 0));
                Log.d("Battery", HttpVersions.HTTP_0_9 + intent.getIntExtra("voltage", 0));
                Log.d("Battery", HttpVersions.HTTP_0_9 + intent.getIntExtra("temperature", 0));
                Log.d("Battery", "ss" + intent.getIntExtra("status", 2));
                Log.d("Battery", HttpVersions.HTTP_0_9 + intent.getIntExtra("plugged", 0));
                Log.d("Battery", HttpVersions.HTTP_0_9 + intent.getIntExtra("health", 1));
            }
        }
    };
    private IntentFilter mIntentFilter = new IntentFilter();

    public boolean isCharging() {
        return this.isCharging;
    }

    public int getBatteryCapacity() {
        return this.batteryCapacity;
    }

    public boolean isRestoreFactory() {
        return this.isReFac;
    }

    public DeviceController(Context context) {
        this.pPowerManager = null;
        this.mContext = null;
        this.mContext = context;
        this.pPowerManager = (PowerManager) this.mContext.getSystemService(Context.POWER_SERVICE);
        this.mIntentFilter.addAction("android.intent.action.BATTERY_CHANGED");
        this.mContext.registerReceiver(this.mIntentReceiver, this.mIntentFilter);
    }

    public static DeviceController getInstance() {
        if (instance == null) {
            instance = new DeviceController(MifiService.getContext());
        }
        return instance;
    }

    public String getMaunufactor() {
        return Build.MANUFACTURER;
    }

    public String getDeviceName() {
        return Build.MODEL;
    }

    public String getSWVersion() {
        return Build.DISPLAY;
    }

    public void rebootDevice() {
        this.pPowerManager.reboot("user command");
    }

    public void rebootBootloader() {
        this.pPowerManager.reboot("bootloader");
    }

    public void shutdownDevice() {
        try {
            Class<?> ServiceManager = Class.forName("android.os.ServiceManager");
            Method getService = ServiceManager.getMethod("getService", String.class);
            Object oRemoteService = getService.invoke(null, "power");
            Class<?> cStub = Class.forName("android.os.IPowerManager$Stub");
            Method asInterface = cStub.getMethod("asInterface", IBinder.class);
            Object oIPowerManager = asInterface.invoke(null, oRemoteService);
            Method shutdown = oIPowerManager.getClass().getMethod("shutdown", Boolean.TYPE, Boolean.TYPE);
            shutdown.invoke(oIPowerManager, false, true);
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
        }
    }

    public void restoreDevice() {
        this.isReFac = true;
        MifiConfiguration.getInstance().reset();
        WanApnController.getInstance().restoreAPN();
        try {
            Runtime.getRuntime().exec("sync");
            Runtime.getRuntime().exec("reboot");
        } catch (Exception e) {
            Log.d(TAG, "sync reboot error");
        }
    }
}