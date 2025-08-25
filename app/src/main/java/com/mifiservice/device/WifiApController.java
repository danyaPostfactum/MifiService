package com.mifiservice.device;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiDevice;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.UserHandle;
import android.util.Log;
import com.mifiservice.Constant;
import com.mifiservice.config.MifiConfiguration;
import com.mifiservice.service.MifiService;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jetty.http.HttpVersions;
import org.eclipse.jetty.util.component.AbstractLifeCycle;

/* loaded from: classes.dex */
public class WifiApController {
    public static final int PASSWORD_OPEN = 0;
    public static final int PASSWORD_WPA2_CCMP = 4;
    public static final int PASSWORD_WPA2_TKIP = 3;
    public static final int PASSWORD_WPA_CCMP = 2;
    public static final int PASSWORD_WPA_TKIP = 1;
    private static final String TAG = "WifiApController";
    private static final String WIFI_AP_ENVENT_WPS_DISABLE = "com.android.mifi.ap.wps.off";
    private static final String WIFI_AP_ENVENT_WPS_ENABLE = "com.android.mifi.ap.wps.on";
    private static final String WIFI_AP_ENVENT_WPS_PBC_START = "com.android.mifi.ap.wps.pbc.start";
    private static final String WIFI_AP_ENVENT_WPS_PBC_TIMEOUT = "com.android.mifi.ap.wps.pbc.timeout";
    private static final int WIFI_AP_STATE_DISABLED = 1;
    private static final int WIFI_AP_STATE_DISABLING = 0;
    private static final int WIFI_AP_STATE_ENABLED = 3;
    private static final int WIFI_AP_STATE_ENABLING = 2;
    private static final int WIFI_AP_STATE_FAILED = 4;
    private static final int WIFI_AP_STATE_UNKNOWN = -1;
    private static WifiApController instance = null;
    private ConnectivityManager connectivityManager;
    Context mContext;
    private WifiManager mWifiManager;
    public MACFILTER macfilter;
    private final String[] WIFI_STATE_TEXTSTATE = {"DISABLING", "DISABLED", "ENABLING", "ENABLED", AbstractLifeCycle.FAILED};
    private List<WifiClient> mWifiClients = new ArrayList();
    private String dhcpLow = "192.168.100.100";
    private String dhcpHigh = "192.168.100.200";
    private BroadcastReceiver mReceiver = new BroadcastReceiver() { // from class: com.mifiservice.device.WifiApController.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.net.conn.TETHER_CONNECT_STATE_CHANGED".equals(action)) {
                boolean isConnected = WifiApController.this.getTetherConnectedStations().size() > 0;
                boolean isEnabled = WifiApController.this.getWifiAPState() == WIFI_AP_STATE_ENABLED;
                if (isConnected) {
                    LedController.getInstance().setMode(0, 1, 2);
                    return;
                } else if (isEnabled) {
                    LedController.getInstance().setMode(0, 1, 0);
                    return;
                } else {
                    LedController.getInstance().setMode(0, 0, 0);
                    return;
                }
            }
            if ("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(action)) {
                int state = intent.getIntExtra("wifi_state", 14);
                boolean isConnected2 = WifiApController.this.getTetherConnectedStations().size() > 0;
                switch (state) {
                    case 11:
                        LedController.getInstance().setMode(0, 0, 0);
                        return;
                    case 12:
                    default:
                        return;
                    case 13:
                        if (isConnected2) {
                            LedController.getInstance().setMode(0, 1, 2);
                            return;
                        } else {
                            LedController.getInstance().setMode(0, 1, 0);
                            return;
                        }
                }
            }
            if (WifiApController.WIFI_AP_ENVENT_WPS_ENABLE.equals(action) || WifiApController.WIFI_AP_ENVENT_WPS_DISABLE.equals(action) || WifiApController.WIFI_AP_ENVENT_WPS_PBC_START.equals(action) || WifiApController.WIFI_AP_ENVENT_WPS_PBC_TIMEOUT.equals(action)) {
            }
        }
    };

    public List<WifiDevice> getTetherConnectedStations() {
        if (Build.VERSION.SDK_INT >= 26) {
            return null;
        }
        List<WifiDevice> tetherList = this.connectivityManager.getTetherConnectedSta();
        return tetherList;
    }


    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.mifiservice.device.WifiApController$1 */
    /* loaded from: classes.dex */
    public class AnonymousClass1 extends BroadcastReceiver {
        AnonymousClass1() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.net.conn.TETHER_CONNECT_STATE_CHANGED".equals(action)) {
                boolean isConnected = WifiApController.this.getTetherConnectedStations().size() > 0;
                boolean isEnabled = WifiApController.this.getWifiAPState() == WIFI_AP_STATE_ENABLED;
                if (isConnected) {
                    LedController.getInstance().setMode(0, 1, 2);
                    return;
                } else if (isEnabled) {
                    LedController.getInstance().setMode(0, 1, 0);
                    return;
                } else {
                    LedController.getInstance().setMode(0, 0, 0);
                    return;
                }
            }
            if ("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(action)) {
                int state = intent.getIntExtra("wifi_state", 14);
                boolean isConnected2 = WifiApController.this.getTetherConnectedStations().size() > 0;
                switch (state) {
                    case 11:
                        LedController.getInstance().setMode(0, 0, 0);
                        return;
                    case 12:
                    default:
                        return;
                    case 13:
                        if (isConnected2) {
                            LedController.getInstance().setMode(0, 1, 2);
                            return;
                        } else {
                            LedController.getInstance().setMode(0, 1, 0);
                            return;
                        }
                }
            }
            if (WifiApController.WIFI_AP_ENVENT_WPS_ENABLE.equals(action) || WifiApController.WIFI_AP_ENVENT_WPS_DISABLE.equals(action) || WifiApController.WIFI_AP_ENVENT_WPS_PBC_START.equals(action) || WifiApController.WIFI_AP_ENVENT_WPS_PBC_TIMEOUT.equals(action)) {
            }
        }
    }

    public WifiApController(Context context) {
        this.mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.mContext = context;
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.TETHER_CONNECT_STATE_CHANGED");
        filter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        filter.addAction(WIFI_AP_ENVENT_WPS_ENABLE);
        filter.addAction(WIFI_AP_ENVENT_WPS_DISABLE);
        filter.addAction(WIFI_AP_ENVENT_WPS_PBC_START);
        filter.addAction(WIFI_AP_ENVENT_WPS_PBC_TIMEOUT);
        this.mContext.registerReceiver(this.mReceiver, filter);
        this.macfilter = new MACFILTER();
        this.macfilter.macaddr = new String[10];
        this.macfilter.type = MifiConfiguration.getInstance().MACFilterType;
        this.macfilter.macaddr[0] = MifiConfiguration.getInstance().MACFilterList[0];
        this.macfilter.macaddr[1] = MifiConfiguration.getInstance().MACFilterList[1];
        this.macfilter.macaddr[2] = MifiConfiguration.getInstance().MACFilterList[2];
        this.macfilter.macaddr[3] = MifiConfiguration.getInstance().MACFilterList[3];
        this.macfilter.macaddr[4] = MifiConfiguration.getInstance().MACFilterList[4];
        this.macfilter.macaddr[5] = MifiConfiguration.getInstance().MACFilterList[5];
        this.macfilter.macaddr[6] = MifiConfiguration.getInstance().MACFilterList[6];
        this.macfilter.macaddr[7] = MifiConfiguration.getInstance().MACFilterList[7];
        this.macfilter.macaddr[8] = MifiConfiguration.getInstance().MACFilterList[8];
        this.macfilter.macaddr[9] = MifiConfiguration.getInstance().MACFilterList[9];
    }

    public static WifiApController getInstance() {
        if (instance == null) {
            instance = new WifiApController(MifiService.getContext());
        }
        return instance;
    }

    public WifiConfiguration getWifiApConfiguration()  {
        return this.mWifiManager.getWifiApConfiguration();
    }


    public boolean setWifiApConfiguration(WifiConfiguration wifiConfig) {
        this.mWifiManager.setWifiApConfiguration(wifiConfig);
        return true;
    }

    public boolean setWifiApWps(String command) {
        return false;
    }

    public boolean setWifiApConfig(String ssid, int pwtype, String password) {
        Log.d(TAG, "setWifiApConfig " + ssid + " " + pwtype + " " + password);
        WifiConfiguration config = getWifiApConfiguration();
        config.SSID = ssid;
        switch (pwtype) {
            case PASSWORD_OPEN:
                config.allowedKeyManagement.clear();
                config.allowedKeyManagement.set(0);
                break;
            case PASSWORD_WPA_TKIP:
                config.allowedKeyManagement.clear();
                config.allowedKeyManagement.set(1);
                config.allowedAuthAlgorithms.clear();
                config.allowedAuthAlgorithms.set(0);
                config.allowedGroupCiphers.clear();
                config.allowedGroupCiphers.set(2);
                config.allowedPairwiseCiphers.clear();
                config.allowedPairwiseCiphers.set(1);
                if (password.length() != 0) {
                    config.preSharedKey = password;
                    break;
                }
                break;
            case PASSWORD_WPA_CCMP:
                config.allowedKeyManagement.clear();
                config.allowedKeyManagement.set(1);
                config.allowedAuthAlgorithms.clear();
                config.allowedAuthAlgorithms.set(0);
                config.allowedGroupCiphers.clear();
                config.allowedGroupCiphers.set(3);
                config.allowedPairwiseCiphers.clear();
                config.allowedPairwiseCiphers.set(2);
                if (password.length() != 0) {
                    config.preSharedKey = password;
                    break;
                }
                break;
            case PASSWORD_WPA2_TKIP:
                config.allowedKeyManagement.clear();
                config.allowedKeyManagement.set(4);
                config.allowedAuthAlgorithms.clear();
                config.allowedAuthAlgorithms.set(0);
                config.allowedGroupCiphers.clear();
                config.allowedGroupCiphers.set(2);
                config.allowedPairwiseCiphers.clear();
                config.allowedPairwiseCiphers.set(1);
                if (password.length() != 0) {
                    config.preSharedKey = password;
                    break;
                }
                break;
            case PASSWORD_WPA2_CCMP:
                config.allowedKeyManagement.clear();
                config.allowedKeyManagement.set(4);
                config.allowedAuthAlgorithms.clear();
                config.allowedAuthAlgorithms.set(0);
                config.allowedGroupCiphers.clear();
                config.allowedGroupCiphers.set(3);
                config.allowedPairwiseCiphers.clear();
                config.allowedPairwiseCiphers.set(2);
                if (password.length() != 0) {
                    config.preSharedKey = password;
                    break;
                }
                break;
            default:
                return false;
        }
        setWifiApConfiguration(config);
        return true;
    }

    public int getWifiAPState() {
        return this.mWifiManager.getWifiApState() - 10;
    }


    public int setWifiApEnabled(WifiConfiguration config, boolean enabled) {
        Log.d(TAG, "*** setWifiApEnabled CALLED **** " + enabled);
        if (enabled && this.mWifiManager.getConnectionInfo() != null) {
            this.mWifiManager.setWifiEnabled(false);
            try {
                Thread.sleep(1500L);
            } catch (Exception e) {
            }
        }
        if (config == null) {
            config = getWifiApConfiguration();
        }
        config.maxSta = MifiConfiguration.getInstance().WIFImaxSta;
        config.SSID = MifiConfiguration.getInstance().WIFIssid;
        config.channel = MifiConfiguration.getInstance().WIFIchannel;
        config.hiddenSSID = MifiConfiguration.getInstance().WIFIssidhidden;
        setWifiApConfiguration(config);
        if (Build.VERSION.SDK_INT < 26) {
            this.mWifiManager.setWifiApEnabled(config, enabled);
        } else if (enabled) {
        }
        if (!enabled) {
            int loopMax = 30;
            while (loopMax > 0 && (getWifiAPState() == WIFI_AP_STATE_DISABLING || getWifiAPState() == WIFI_AP_STATE_ENABLED || getWifiAPState() == WIFI_AP_STATE_FAILED)) {
                try {
                    Thread.sleep(500L);
                    loopMax--;
                } catch (Exception e2) {
                }
            }
            this.mWifiManager.setWifiEnabled(true);
        } else if (enabled) {
            int loopMax2 = 30;
            while (loopMax2 > 0 && (getWifiAPState() == WIFI_AP_STATE_ENABLING || getWifiAPState() == WIFI_AP_STATE_DISABLED || getWifiAPState() == WIFI_AP_STATE_FAILED)) {
                try {
                    Thread.sleep(500L);
                    loopMax2--;
                } catch (Exception e3) {
                }
            }
        }
        if (getWifiAPState() == WIFI_AP_STATE_ENABLED) {
            LedController.getInstance().setMode(0, 1, 0);
        } else {
            LedController.getInstance().setMode(0, 0, 0);
        }
        if (enabled) {
            commitMacfilter();
        }
        return -1;
    }

    public boolean getWlanStatus() {
        return getWifiAPState() == WIFI_AP_STATE_ENABLED;
    }

    public String getWlanIP() {
        return Constant.TCP_SERVER_IP;
    }

    public String getWlanDNS(int index) {
        String response = "8.8.8.8";
        String command = "getprop net.rmnet0.dns" + index;
        try {
            Process process = Runtime.getRuntime().exec(command);
            InputStreamReader ir = new InputStreamReader(process.getInputStream());
            BufferedReader input = new BufferedReader(ir);
            StringBuffer sb = new StringBuffer(HttpVersions.HTTP_0_9);
            while (true) {
                String temp = input.readLine();
                if (temp == null) {
                    break;
                }
                sb.append(temp);
            }
            input.close();
            response = sb.toString();
            if (response.equals(HttpVersions.HTTP_0_9)) {
                response = "8.8.8.8";
            }
        } catch (Exception e) {
            Log.d(TAG, "get DNS error");
        }
        Log.d(TAG, "get DNS response: " + response);
        return response;
    }

    public void setWlanDns(int index, String dns) {
        String command = "getprop net.rmnet0.dns " + index + " " + dns;
        try {
            Process process = Runtime.getRuntime().exec(command);
            InputStreamReader ir = new InputStreamReader(process.getInputStream());
            BufferedReader input = new BufferedReader(ir);
            StringBuffer sb = new StringBuffer(HttpVersions.HTTP_0_9);
            while (true) {
                String temp = input.readLine();
                if (temp != null) {
                    sb.append(temp);
                } else {
                    input.close();
                    Log.d(TAG, "set DNS ok: " + dns);
                    return;
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "get DNS error");
        }
    }

    public String getWlanDHCP_High() {
        return this.dhcpHigh;
    }

    public void setWlanDHCP_High(String ip) {
        this.dhcpHigh = ip;
    }

    public String getWlanDHCP_Low() {
        return this.dhcpLow;
    }

    public void setWlanDHCP_Low(String ip) {
        this.dhcpLow = ip;
    }

    public String getSsid() {
        WifiConfiguration conf = getWifiApConfiguration();
        return conf != null ? conf.SSID : " ";
    }

    public void setSsid(String ssid) {
        Log.d(TAG, "setSsid " + ssid);
        WifiConfiguration conf = getWifiApConfiguration();
        if (conf != null) {
            conf.SSID = ssid;
            setWifiApConfiguration(conf);
        }
    }

    public String getWifiMAC() {
        String mac = HttpVersions.HTTP_0_9;
        try {
            FileInputStream fis = new FileInputStream("sys/class/net/wlan0/address");
            byte[] buffer = new byte[64];
            int byteCount = fis.read(buffer);
            if (byteCount > 0) {
                mac = new String(buffer, 0, byteCount, "utf-8");
            }
            if (mac.length() == 0 || mac == null) {
                return HttpVersions.HTTP_0_9;
            }
        } catch (Exception e) {
        }
        return mac.trim();
    }

    public boolean getWifiSsidHidden() {
        WifiConfiguration conf = getWifiApConfiguration();
        if (conf != null) {
            return conf.hiddenSSID;
        }
        return false;
    }

    public void setWifiSsidHidden(boolean enable) {
        Log.d(TAG, "setWifiSsidHidden " + enable);
        WifiConfiguration conf = getWifiApConfiguration();
        if (conf != null) {
            conf.hiddenSSID = enable;
            setWifiApConfiguration(conf);
        }
    }

    public int getWifiChannel() {
        WifiConfiguration conf = getWifiApConfiguration();
        if (conf != null) {
        }
        return 6;
    }

    public void setWifiChannel(int channel) {
        Log.d(TAG, "setWifiChannel " + channel);
        WifiConfiguration conf = getWifiApConfiguration();
        if (conf != null) {
            setWifiApConfiguration(conf);
        }
    }

    public String getWifiHwMode() {
        WifiConfiguration conf = getWifiApConfiguration();
        return conf != null ? "n" : "n";
    }

    public void setWifiHwMode(String mode) {
        Log.d(TAG, "setWifiHwMode " + mode);
        WifiConfiguration conf = getWifiApConfiguration();
        if (conf != null) {
            setWifiApConfiguration(conf);
        }
    }

    public int getWifiMaxSta() {
        WifiConfiguration conf = getWifiApConfiguration();
        if (conf != null) {
            return conf.maxSta;
        }
        return 10;
    }

    public void setWifiMaxSta(int maxsta) {
        Log.d(TAG, "setWifiMaxSta " + maxsta);
        WifiConfiguration conf = getWifiApConfiguration();
        if (conf != null) {
            conf.maxSta = maxsta;
            setWifiApConfiguration(conf);
        }
    }

    public int getWifiEncryptType() {
        boolean wpa = getWifiApConfiguration().allowedKeyManagement.get(1);
        boolean wpa2 = getWifiApConfiguration().allowedKeyManagement.get(4);
        boolean ccmp = getWifiApConfiguration().allowedPairwiseCiphers.get(2);
        boolean tkip = getWifiApConfiguration().allowedPairwiseCiphers.get(1);
        if (wpa && tkip) {
            return 1;
        }
        if (wpa && ccmp) {
            return 2;
        }
        if (wpa2 && tkip) {
            return 3;
        }
        if (wpa2 && ccmp) {
            return 4;
        }
        return 0;
    }

    /* loaded from: classes.dex */
    public class WifiClient {
        public String ip;
        public String mac;
        public String media;
        public String name;

        public WifiClient(String n, String i, String ma, String med) {
            this.name = n;
            this.ip = i;
            this.mac = ma;
            this.media = med;
        }
    }

    private String getConnectedHotIP(String mac) {
        String ip = HttpVersions.HTTP_0_9;
        try {
            FileReader fr = new FileReader("/proc/net/arp");
            BufferedReader br = new BufferedReader(fr);
            while (true) {
                String line = br.readLine();
                if (line != null) {
                    String[] splitted = line.split(" +");
                    if (splitted != null && splitted.length >= 4 && mac.equals(splitted[3])) {
                        ip = splitted[0];
                        break;
                    }
                } else {
                    break;
                }
            }
            fr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ip;
    }

    public List<WifiClient> getWifiClients() {
        List<WifiDevice> tetherList = getTetherConnectedStations();
        this.mWifiClients.clear();
        for (int i = 0; i < tetherList.size(); i++) {
            WifiDevice device = tetherList.get(i);
            if (device.deviceState == 1) {
                String ip = getConnectedHotIP(device.deviceAddress);
                Log.d(TAG, "---->>ip = " + ip);
                this.mWifiClients.add(new WifiClient(device.deviceName, ip, device.deviceAddress, "WIFI"));
            }
        }
        return this.mWifiClients;
    }

    public int getClientNumber() {
        return getTetherConnectedStations().size();
    }

    /* loaded from: classes.dex */
    public class MACFILTER {
        public String[] macaddr;
        public int type;

        public MACFILTER() {
        }
    }

    public MACFILTER getMacfilter() {
        return this.macfilter;
    }

    public boolean setMacfilterType(int type) {
        if (type != 0 && type != 1 && type != 3) {
            return false;
        }
        this.macfilter.type = type;
        return true;
    }

    public boolean setMacfilterMac(int id, String mac) {
        if (id < 0 || id > 9) {
            return false;
        }
        this.macfilter.macaddr[id] = mac;
        return true;
    }

    public boolean commitMacfilter() {
        Intent intent = new Intent("com.android.IwprivService.MACADDR");
        Intent intentWifiList = new Intent("com.android.IwprivService.WIFILISTMAC");
        Intent intentExcute = new Intent("com.android.IwprivService.EXCUTE");
        UserHandle uh = android.os.Process.myUserHandle();
        try {
            intentExcute.putExtra("type", this.macfilter.type);
            if (this.macfilter.type == 3) {
                this.mContext.sendBroadcastAsUser(intentExcute, UserHandle.CURRENT_OR_SELF);
                return true;
            }
            for (int i = 0; i < this.macfilter.macaddr.length; i++) {
                intent.putExtra("macaddr" + i, this.macfilter.macaddr[i]);
            }
            this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT_OR_SELF);
            if (this.macfilter.type == 1) {
                int i2 = 0;
                for (WifiClient client : getWifiClients()) {
                    Log.d(TAG, "client.mac: " + client.mac);
                    intentWifiList.putExtra("wifilistmac" + i2, client.mac);
                    i2++;
                }
                this.mContext.sendBroadcastAsUser(intentWifiList, UserHandle.CURRENT_OR_SELF);
            }
            this.mContext.sendBroadcast(intentExcute);
            return true;
        } catch (Exception e) {
            Log.d(TAG, "commitMacfilter error");
            return false;
        }
    }

    public void restartAP() {
        Log.d(TAG, "restartAP ");
        setWifiApEnabled(null, false);
        setWifiApEnabled(null, true);
    }
}