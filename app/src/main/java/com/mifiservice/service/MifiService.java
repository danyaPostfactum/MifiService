package com.mifiservice.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import com.mifiservice.config.MifiConfiguration;
import com.mifiservice.device.DeviceController;
import com.mifiservice.device.GpsController;
import com.mifiservice.device.LedController;
import com.mifiservice.device.WanApnController;
import com.mifiservice.device.WanDataController;
import com.mifiservice.device.WifiApController;
import com.mifiservice.ota.Updater;
import com.mifiservice.server.JServer;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.commons.io.FileUtils;

/* loaded from: classes.dex */
public class MifiService extends Service {
    private static final String TAG = "MifiService";
    private static Context context;
    private AlarmManager alarmManager;
    private DeviceController mDeviceController;
    private GpsController mGpsController;
    private WanApnController mWanApnController;
    private WanDataController mWanDataController;
    private WifiApController mWifiApController;
    private MifiConfiguration mifiConfiguration;
    private PendingIntent timerIntent;
    private RefreshAlarmReceiver timerReceiver;
    private Updater updater;
    private boolean licensed = false;
    private PowerManager.WakeLock wakeLock = null;
    JServer mServer = new JServer(80);
    private long txBytes = 0;
    private long rxBytes = 0;
    private boolean blink = false;
    private boolean switchsim = false;
    private int reset_times = 0;
    Timer timer = new Timer();
    private Handler handler = new Handler() { // from class: com.mifiservice.service.MifiService.1
        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                if (MifiService.this.mWanDataController.getNetStatus().equals("Connected")) {
                    MifiService.this.reset_times = 0;
                    if (MifiService.this.txBytes == MifiService.this.mWanDataController.getMobileTxBytes() / FileUtils.ONE_KB && MifiService.this.rxBytes == MifiService.this.mWanDataController.getMobileRxBytes() / FileUtils.ONE_KB) {
                        if (MifiService.this.blink) {
                            Log.d(MifiService.TAG, "blink");
                            LedController.getInstance().setMode(1, 3, 0);
                            MifiService.this.blink = false;
                        } else {
                            Log.d(MifiService.TAG, "!blink");
                        }
                    } else {
                        Log.d(MifiService.TAG, "txBytes: " + MifiService.this.txBytes);
                        Log.d(MifiService.TAG, "rxBytes: " + MifiService.this.rxBytes);
                        Log.d(MifiService.TAG, "MobileTxBytes: " + (MifiService.this.mWanDataController.getMobileTxBytes() / FileUtils.ONE_KB));
                        Log.d(MifiService.TAG, "MobileRxBytes: " + (MifiService.this.mWanDataController.getMobileRxBytes() / FileUtils.ONE_KB));
                        if (!MifiService.this.blink) {
                            LedController.getInstance().setMode(1, 3, 2);
                            MifiService.this.blink = true;
                        }
                        MifiService.this.txBytes = MifiService.this.mWanDataController.getMobileTxBytes() / FileUtils.ONE_KB;
                        MifiService.this.rxBytes = MifiService.this.mWanDataController.getMobileRxBytes() / FileUtils.ONE_KB;
                    }
                } else {
                    Log.d(MifiService.TAG, "RefreshAlarmReceiver one");
                    MifiService.this.blink = false;
                    if (!MifiService.this.mWanDataController.stopCheckNetwork && MifiConfiguration.getInstance().WANautoConnect) {
                        if (MifiService.this.mifiConfiguration.WANnetworkType == 0) {
                            if (MifiService.this.mWanDataController.isChinaTelecom()) {
                                MifiService.this.HandleResetData(2);
                            } else {
                                MifiService.this.HandleResetData(MifiConfiguration.getInstance().WANnetworkType);
                            }
                        }
                        MifiService.this.mWanDataController.setMobileDataEnable(true);
                    }
                }
            }
            super.handleMessage(msg);
        }
    };
    TimerTask task = new TimerTask() { // from class: com.mifiservice.service.MifiService.2
        @Override // java.util.TimerTask, java.lang.Runnable
        public void run() {
            Message message = new Message();
            message.what = 1;
            MifiService.this.handler.sendMessage(message);
        }
    };

    public static Context getContext() {
        return context;
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Mifi Service created");
        context = this;
        IntentFilter timerFilter = new IntentFilter(RefreshAlarmReceiver.TIMER_UPDATE);
        this.timerReceiver = new RefreshAlarmReceiver();
        registerReceiver(this.timerReceiver, timerFilter);
        this.alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        restartTimer();
        this.mWifiApController = WifiApController.getInstance();
        this.mWanDataController = WanDataController.getInstance();
        this.mDeviceController = DeviceController.getInstance();
        this.mWanApnController = WanApnController.getInstance();
        this.mifiConfiguration = MifiConfiguration.getInstance();
        LedController.getInstance().setMode(1, 0, 0);
        if (this.mWanDataController.getNetStatus().equals("Connected")) {
            LedController.getInstance().setMode(1, 3, 0);
        }
        try {
            Thread.sleep(500L);
        } catch (InterruptedException e) {
        }
        this.licensed = isLicensed();
        Log.d(TAG, "check l: " + this.licensed);
        if (this.licensed) {
            if (this.mWifiApController.getWifiAPState() == 1 || this.mWifiApController.getWifiAPState() == -1) {
                HandleBindCommand(this.mifiConfiguration.WIFIssid, this.mifiConfiguration.WIFIpassword);
            }
            int apnid = this.mWanApnController.getDefaultApnId();
            if (MifiConfiguration.getInstance().WANdefaultAPNSave != apnid && apnid != 0) {
                MifiConfiguration.getInstance().WANdefaultAPNSave = apnid;
                MifiConfiguration.getInstance().saveToFile();
                this.mWanApnController.setCurrentAPN(0);
                this.switchsim = true;
            }
            if (!this.mWanDataController.getNetStatus().equals("Connected")) {
                if (this.mWanDataController.isChinaTelecom()) {
                    HandleResetData(2);
                } else if (this.mifiConfiguration.WANnetworkType == 0) {
                    this.mWanDataController.HandleCheckNetwork();
                    this.mWanDataController.setPreferedNetworkTypeAtcmd(2);
                    rePowerModem();
                }
            } else if (!this.mWanDataController.getNetMode().equals("LTE") && this.mifiConfiguration.WANnetworkType == 0) {
                this.mWanDataController.HandleCheckNetwork();
                this.mWanDataController.setPreferedNetworkTypeAtcmd(2);
            }
            this.mWanDataController.setMobileDataEnable(MifiConfiguration.getInstance().WANautoConnect);
            if (MifiConfiguration.getInstance().WANdefaultAPN > 0) {
            }
            if (!this.mServer.isStarted()) {
                this.mServer.start(getApplicationContext());
            }
        } else {
            this.mWanDataController.setMobileDataEnable(false);
        }
        acquireWakeLock();
        this.timer.schedule(this.task, 5000L, 5000L);
    }

    @Override // android.app.Service
    public void onDestroy() {
        Log.d(TAG, "mifi service destroy!");
        if (this.timerIntent != null) {
            this.alarmManager.cancel(this.timerIntent);
        }
        super.onDestroy();
    }

    private void acquireWakeLock() {
        if (this.wakeLock == null) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            this.wakeLock = pm.newWakeLock(PowerManager.ON_AFTER_RELEASE | PowerManager.PARTIAL_WAKE_LOCK, TAG);
            if (this.wakeLock != null) {
                this.wakeLock.acquire();
            }
        }
    }

    private void releaseWakeLock() {
        if (this.wakeLock != null) {
            this.wakeLock.release();
            this.wakeLock = null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isLicensed() {
        return true;
    }

    private void restartTimer() {
        if (this.timerIntent == null) {
            Intent intent = new Intent(RefreshAlarmReceiver.TIMER_UPDATE);
            this.timerIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        } else {
            this.alarmManager.cancel(this.timerIntent);
        }
        long initialRefreshTime = SystemClock.elapsedRealtime() + 25000;
        Log.d(TAG, "restartTimer: 20000 initialRefreshTime: " + initialRefreshTime);
        this.alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, initialRefreshTime, 20000, this.timerIntent);
    }

    /* loaded from: classes.dex */
    public class RefreshAlarmReceiver extends BroadcastReceiver {
        public static final String TIMER_UPDATE = "com.mifiservice.TIMER_UPDATE";

        public RefreshAlarmReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            int apnid;
            MifiService.this.licensed = MifiService.this.isLicensed();
            Log.d(MifiService.TAG, "check l: " + MifiService.this.licensed);
            if (MifiService.this.licensed) {
                if (!MifiService.this.switchsim && MifiConfiguration.getInstance().WANdefaultAPNSave != (apnid = MifiService.this.mWanApnController.getDefaultApnId()) && apnid != 0) {
                    MifiConfiguration.getInstance().WANdefaultAPNSave = apnid;
                    MifiConfiguration.getInstance().saveToFile();
                    MifiService.this.mWanApnController.setCurrentAPN(0);
                    MifiService.this.switchsim = true;
                }
                if (MifiService.this.mWifiApController.getWifiAPState() == 1 || MifiService.this.mWifiApController.getWifiAPState() == -1) {
                    MifiService.this.HandleBindCommand(MifiService.this.mifiConfiguration.WIFIssid, MifiService.this.mifiConfiguration.WIFIpassword);
                }
                if (MifiService.this.mWifiApController.getWifiAPState() == 3 && !MifiService.this.mServer.isStarted()) {
                    MifiService.this.mServer.start(MifiService.this.getApplicationContext());
                    return;
                }
                return;
            }
            if (MifiService.this.mWifiApController.getWifiAPState() == 3) {
                MifiService.this.HandleUnBindCommand();
            }
            if (MifiService.this.mWanDataController.getMobileDataEnable()) {
                MifiService.this.mWanDataController.setMobileDataEnable(false);
            }
            if (MifiService.this.mServer.isStarted()) {
                MifiService.this.mServer.stop();
            }
        }
    }

    public void setAirplaneMode(boolean enable) {
        Settings.Global.putInt(context.getContentResolver(), "airplane_mode_on", enable ? 1 : 0);
        Intent intent = new Intent("android.intent.action.AIRPLANE_MODE");
        intent.putExtra("state", enable);
        context.sendBroadcast(intent);
    }

    /* JADX WARN: Type inference failed for: r0v12, types: [com.mifiservice.service.MifiService$3] */
    public void HandleResetData(int mode) {
        Log.d(TAG, "restart data enable ");
        if (this.mWanDataController.repower_times >= 5) {
            Log.d(TAG, "repower_times: " + this.mWanDataController.repower_times);
            return;
        }
        if (this.reset_times == 0) {
            this.reset_times++;
            this.mWanDataController.setPreferedNetworkType(mode);
            this.mWanDataController.setcfun(0);
            this.mWanDataController.setcfun(1);
            this.mWanDataController.repower_times++;
            new Thread() { // from class: com.mifiservice.service.MifiService.3
                @Override // java.lang.Thread, java.lang.Runnable
                public void run() {
                    Log.d(MifiService.TAG, "HandleResetData thread ");
                    while (MifiService.this.mWanDataController.getCfun() == 0) {
                        Log.d(MifiService.TAG, "HandleResetData setcfun 1");
                        MifiService.this.mWanDataController.setcfun(1);
                        try {
                            Thread.sleep(1000L);
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }.start();
            return;
        }
        int i = this.reset_times + 1;
        this.reset_times = i;
        if (i >= 12) {
            this.reset_times = 0;
        }
    }

    public void rePowerModem() {
        Log.d(TAG, "rePowerModem ");
        this.mWanDataController.setcfun(0);
        this.mWanDataController.setcfun(1);
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [com.mifiservice.service.MifiService$4] */
    public void HandleBindCommand(final String ssid, final String password) {
        new Thread() { // from class: com.mifiservice.service.MifiService.4
            @Override // java.lang.Thread, java.lang.Runnable
            public void run() {
                Log.d(MifiService.TAG, "handle binding " + ssid);
                MifiService.this.mWifiApController.setWifiApConfig(ssid, MifiService.this.mifiConfiguration.WIFIencrypt, password);
                WifiConfiguration config = MifiService.this.mWifiApController.getWifiApConfiguration();
                config.hiddenSSID = MifiService.this.mifiConfiguration.WIFIssidhidden;
                MifiService.this.mWifiApController.setWifiApEnabled(config, true);
            }
        }.start();
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [com.mifiservice.service.MifiService$5] */
    public void HandleUnBindCommand() {
        new Thread() { // from class: com.mifiservice.service.MifiService.5
            @Override // java.lang.Thread, java.lang.Runnable
            public void run() {
                Log.d(MifiService.TAG, "handle unbinding ");
                MifiService.this.mWifiApController.setWifiApEnabled(null, false);
            }
        }.start();
    }
}