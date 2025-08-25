package com.mifiservice.device;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.mifiservice.service.MifiService;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import org.eclipse.jetty.http.HttpVersions;

/* loaded from: classes.dex */
public class LedController {
    public static final int COLOR_ALL = 4;
    public static final int COLOR_GREEN = 3;
    public static final int COLOR_OFF = 0;
    public static final int COLOR_ON = 1;
    public static final int COLOR_RED = 2;
    public static final int LED_BLINK_1S_BLINK = 2;
    public static final int LED_BLINK_300MS_BLINK = 1;
    public static final int LED_BLINK_OFF = 0;
    public static final int LED_RED_BLINK_OFF = 0;
    public static final int LED_SIG = 1;
    public static final int LED_SIG_GREEN = 3;
    public static final int LED_SIG_RED = 2;
    public static final int LED_WIFI = 0;
    public static final String TAG = "LedController";
    private static LedController instance = null;
    private Context mContext;
    private GpioController mGpioController;
    final byte[] LIGHT_ON = {50, 53, 53};
    final byte[] LIGHT_OFF = {48};
    private int wifi_brt = 0;
    private int red_brt = 0;
    private int green_brt = 0;
    String WIFI_LED_DEV = "/sys/class/leds/wifi/brightness";
    String RED_LED_DEV = "/sys/class/leds/red/brightness";
    String GREEN_LED_DEV = "/sys/class/leds/green/brightness";
    private int resetkey_count = -1;
    private boolean ready_restore = false;
    Timer timer = new Timer();
    private Map<Integer, Integer> gpioBlinkStatus = new HashMap();
    private Map<Integer, Integer> blinkGpios = new HashMap();
    private Map<Integer, Integer> blinkGpiosStatus = new HashMap();
    private int gpio_current_value = 0;
    private Handler handler = new Handler() { // from class: com.mifiservice.device.LedController.1
        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                if (LedController.this.getResetKey() != 49) {
                    if (LedController.this.ready_restore && LedController.this.resetkey_count >= 0) {
                        LedController.access$204(LedController.this);
                        if (LedController.this.ready_restore && LedController.this.resetkey_count >= 30) {
                            DeviceController.getInstance().restoreDevice();
                            return;
                        }
                    }
                    for (Integer gpio : LedController.this.blinkGpios.keySet()) {
                        Integer status = Integer.valueOf(((Integer) LedController.this.blinkGpiosStatus.get(gpio)).intValue() + 1);
                        if (status.intValue() < ((Integer) LedController.this.blinkGpios.get(gpio)).intValue()) {
                            LedController.this.blinkGpiosStatus.put(gpio, status);
                        } else {
                            LedController.this.toggleGpio(gpio.intValue());
                            LedController.this.blinkGpiosStatus.put(gpio, 0);
                        }
                    }
                } else {
                    DeviceController.getInstance().restoreDevice();
                    return;
                }
            }
            super.handleMessage(msg);
        }
    };
    TimerTask task = new TimerTask() { // from class: com.mifiservice.device.LedController.2
        @Override // java.util.TimerTask, java.lang.Runnable
        public void run() {
            Message message = new Message();
            message.what = 1;
            LedController.this.handler.sendMessage(message);
        }
    };
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() { // from class: com.mifiservice.device.LedController.3
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(LedController.TAG, "action : " + action);
            if (action.equals("alk.hotkey.reset.down")) {
                LedController.this.resetkey_count = 0;
                LedController.this.ready_restore = true;
            } else if (action.equals("alk.hotkey.reset.up")) {
                LedController.this.resetkey_count = -1;
                LedController.this.ready_restore = false;
            } else if (action.equals("alk.hotkey.reset.long")) {
                DeviceController.getInstance().restoreDevice();
            }
        }
    };
    private IntentFilter mIntentFilter = new IntentFilter();

    static /* synthetic */ int access$204(LedController x0) {
        int i = x0.resetkey_count + 1;
        x0.resetkey_count = i;
        return i;
    }

    private int getBrightness(String ledfile) {
        int value = 0;
        try {
            FileInputStream file = new FileInputStream(ledfile);
            value = file.read();
            file.close();
            return value;
        } catch (Exception e) {
            Log.e(TAG, HttpVersions.HTTP_0_9 + e);
            return value;
        }
    }

    private void setBrightness(String ledfile, byte[] value) {
        Log.d(TAG, "ledfile : " + ledfile + " value : " + value);
        try {
            FileOutputStream file = new FileOutputStream(ledfile);
            file.write(value);
            file.close();
        } catch (Exception e) {
            Log.e(TAG, HttpVersions.HTTP_0_9 + e);
        }
    }

    public int getResetKey() {
        int value = 0;
        try {
            FileInputStream file = new FileInputStream("/mnt/obb/reset_key");
            value = file.read();
            Log.d(TAG, "reset_key : " + value);
            file.close();
            return value;
        } catch (Exception e) {
            return value;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.mifiservice.device.LedController$1 */
    /* loaded from: classes.dex */
    public class AnonymousClass1 extends Handler {
        AnonymousClass1() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                if (LedController.this.getResetKey() != 49) {
                    if (LedController.this.ready_restore && LedController.this.resetkey_count >= 0) {
                        LedController.access$204(LedController.this);
                        if (LedController.this.ready_restore && LedController.this.resetkey_count >= 30) {
                            DeviceController.getInstance().restoreDevice();
                            return;
                        }
                    }
                    for (Integer gpio : LedController.this.blinkGpios.keySet()) {
                        Integer status = Integer.valueOf(((Integer) LedController.this.blinkGpiosStatus.get(gpio)).intValue() + 1);
                        if (status.intValue() < ((Integer) LedController.this.blinkGpios.get(gpio)).intValue()) {
                            LedController.this.blinkGpiosStatus.put(gpio, status);
                        } else {
                            LedController.this.toggleGpio(gpio.intValue());
                            LedController.this.blinkGpiosStatus.put(gpio, 0);
                        }
                    }
                } else {
                    DeviceController.getInstance().restoreDevice();
                    return;
                }
            }
            super.handleMessage(msg);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.mifiservice.device.LedController$2 */
    /* loaded from: classes.dex */
    public class AnonymousClass2 extends TimerTask {
        AnonymousClass2() {
        }

        @Override // java.util.TimerTask, java.lang.Runnable
        public void run() {
            Message message = new Message();
            message.what = 1;
            LedController.this.handler.sendMessage(message);
        }
    }

    public void toggleGpio(int led) {
        if (led == 0) {
            if (this.wifi_brt == 0) {
                setBrightness(this.WIFI_LED_DEV, this.LIGHT_ON);
                this.wifi_brt = 1;
                return;
            } else {
                setBrightness(this.WIFI_LED_DEV, this.LIGHT_OFF);
                this.wifi_brt = 0;
                return;
            }
        }
        if (led == 2) {
            if (this.red_brt == 0) {
                setBrightness(this.RED_LED_DEV, this.LIGHT_ON);
                this.red_brt = 1;
                return;
            } else {
                setBrightness(this.RED_LED_DEV, this.LIGHT_OFF);
                this.red_brt = 0;
                return;
            }
        }
        if (led == 3) {
            if (this.green_brt == 0) {
                setBrightness(this.GREEN_LED_DEV, this.LIGHT_ON);
                this.green_brt = 1;
            } else {
                setBrightness(this.GREEN_LED_DEV, this.LIGHT_OFF);
                this.green_brt = 0;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.mifiservice.device.LedController$3 */
    /* loaded from: classes.dex */
    public class AnonymousClass3 extends BroadcastReceiver {
        AnonymousClass3() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(LedController.TAG, "action : " + action);
            if (action.equals("alk.hotkey.reset.down")) {
                LedController.this.resetkey_count = 0;
                LedController.this.ready_restore = true;
            } else if (action.equals("alk.hotkey.reset.up")) {
                LedController.this.resetkey_count = -1;
                LedController.this.ready_restore = false;
            } else if (action.equals("alk.hotkey.reset.long")) {
                DeviceController.getInstance().restoreDevice();
            }
        }
    }

    public LedController(Context context) {
        this.mContext = null;
        this.mContext = context;
        this.mIntentFilter.addAction("alk.hotkey.reset.down");
        this.mIntentFilter.addAction("alk.hotkey.reset.up");
        this.mIntentFilter.addAction("alk.hotkey.reset.long");
        this.mContext.registerReceiver(this.mIntentReceiver, this.mIntentFilter);
        this.mGpioController = new GpioController();
        this.timer.schedule(this.task, 100L, 100L);
    }

    public static LedController getInstance() {
        if (instance == null) {
            instance = new LedController(MifiService.getContext());
        }
        return instance;
    }

    public void setMode(int led, int color, int blink) {
        Log.d(TAG, "led : " + led + " color : " + color + " blink : " + blink);
        if (!DeviceController.getInstance().isRestoreFactory() || blink != 0) {
            if (this.blinkGpios.containsKey(Integer.valueOf(led))) {
                this.blinkGpios.remove(Integer.valueOf(led));
            }
            if (this.blinkGpiosStatus.containsKey(Integer.valueOf(led))) {
                this.blinkGpiosStatus.remove(Integer.valueOf(led));
            }
            switch (led) {
                case 0:
                    if (color == 0) {
                        setBrightness(this.WIFI_LED_DEV, this.LIGHT_OFF);
                        return;
                    }
                    if (blink == 0) {
                        setBrightness(this.WIFI_LED_DEV, this.LIGHT_ON);
                        return;
                    }
                    if (blink == 1) {
                        this.blinkGpios.put(Integer.valueOf(led), 3);
                        this.blinkGpiosStatus.put(Integer.valueOf(led), 0);
                        return;
                    } else {
                        if (blink == 2) {
                            this.blinkGpios.put(Integer.valueOf(led), 10);
                            this.blinkGpiosStatus.put(Integer.valueOf(led), 0);
                            return;
                        }
                        return;
                    }
                case 1:
                    if (this.blinkGpios.containsKey(2)) {
                        this.blinkGpios.remove(2);
                    }
                    if (this.blinkGpiosStatus.containsKey(2)) {
                        this.blinkGpiosStatus.remove(2);
                    }
                    if (this.blinkGpios.containsKey(3)) {
                        this.blinkGpios.remove(3);
                    }
                    if (this.blinkGpiosStatus.containsKey(3)) {
                        this.blinkGpiosStatus.remove(3);
                    }
                    if (color == 0) {
                        setBrightness(this.RED_LED_DEV, this.LIGHT_OFF);
                        setBrightness(this.GREEN_LED_DEV, this.LIGHT_OFF);
                        return;
                    }
                    if (color == 2) {
                        if (blink == 0) {
                            setBrightness(this.RED_LED_DEV, this.LIGHT_ON);
                            setBrightness(this.GREEN_LED_DEV, this.LIGHT_OFF);
                            return;
                        }
                        if (blink == 1) {
                            setBrightness(this.RED_LED_DEV, this.LIGHT_OFF);
                            setBrightness(this.GREEN_LED_DEV, this.LIGHT_OFF);
                            this.blinkGpios.put(2, 3);
                            this.blinkGpiosStatus.put(2, 0);
                            return;
                        }
                        if (blink == 2) {
                            setBrightness(this.RED_LED_DEV, this.LIGHT_OFF);
                            setBrightness(this.GREEN_LED_DEV, this.LIGHT_OFF);
                            this.blinkGpios.put(2, 10);
                            this.blinkGpiosStatus.put(2, 0);
                            return;
                        }
                        return;
                    }
                    if (color == 3) {
                        if (blink == 0) {
                            Log.d(TAG, "led COLOR_GREEN: LED_BLINK_OFF");
                            setBrightness(this.RED_LED_DEV, this.LIGHT_OFF);
                            setBrightness(this.GREEN_LED_DEV, this.LIGHT_ON);
                            return;
                        } else {
                            if (blink == 1) {
                                Log.d(TAG, "led COLOR_GREEN: LED_BLINK_300MS_BLINK");
                                setBrightness(this.RED_LED_DEV, this.LIGHT_OFF);
                                setBrightness(this.GREEN_LED_DEV, this.LIGHT_OFF);
                                this.blinkGpios.put(3, 3);
                                this.blinkGpiosStatus.put(3, 0);
                                return;
                            }
                            if (blink == 2) {
                                Log.d(TAG, "led COLOR_GREEN: LED_BLINK_1S_BLINK");
                                setBrightness(this.RED_LED_DEV, this.LIGHT_OFF);
                                setBrightness(this.GREEN_LED_DEV, this.LIGHT_OFF);
                                this.blinkGpios.put(3, 10);
                                this.blinkGpiosStatus.put(3, 0);
                                return;
                            }
                            return;
                        }
                    }
                    if (color == 4 && blink == 1) {
                        setBrightness(this.RED_LED_DEV, this.LIGHT_OFF);
                        setBrightness(this.GREEN_LED_DEV, this.LIGHT_OFF);
                        this.blinkGpios.put(2, 3);
                        this.blinkGpiosStatus.put(2, 0);
                        this.blinkGpios.put(3, 3);
                        this.blinkGpiosStatus.put(3, 0);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }
}