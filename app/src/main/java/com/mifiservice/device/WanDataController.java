package com.mifiservice.device;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.os.AsyncTask;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
//import com.android.internal.telephony.Phone;
import com.mifiservice.Constant;
import com.mifiservice.config.MifiConfiguration;
import com.mifiservice.service.MifiService;
import com.mifiservice.utils.ShellUtils;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import org.eclipse.jetty.http.HttpVersions;
import org.eclipse.jetty.util.StringUtil;

/* loaded from: classes.dex */
public class WanDataController {
    public static final String CONNECTIVITY_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    public static final int NETWORN_2G = 2;
    public static final int NETWORN_3G = 3;
    public static final int NETWORN_4G = 4;
    public static final int NETWORN_NONE = 0;
    public static final String TAG = "WanDataController";
    private static WanDataController instance = null;
    private ConnectivityManager connectivityManager;
    private String imeiString;
    private Context mContext;
    private TelephonyManager telephonyManager;
    int last_network_type = 0;
    private MyPhoneStateListener myListener = null;
    private int last_signallevel = 0;
    //private Phone phone = null;
    public boolean stopCheckNetwork = false;
    private int dbm = 10000;
    public int repower_times = 0;

    /* loaded from: classes.dex */
    private class MyPhoneStateListener extends PhoneStateListener {
        private MyPhoneStateListener() {
        }

        @Override // android.telephony.PhoneStateListener
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            WanDataController.this.last_signallevel = signalStrength.getLevel();
            int curdbm = signalStrength.getDbm();
            if (curdbm <= -30) {
                WanDataController.this.dbm = curdbm;
            } else {
                Log.d(WanDataController.TAG, "curdbm:  " + curdbm);
            }
        }

        @Override // android.telephony.PhoneStateListener
        public void onDataConnectionStateChanged(int state, int networkType) {
            WanDataController.this.updateNetworkType(networkType);
        }

        @Override // android.telephony.PhoneStateListener
        public void onServiceStateChanged(ServiceState serviceState) {
        }
    }

    public WanDataController(Context context) {
        this.imeiString = "0000";
        this.mContext = null;
        this.telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.mContext = context;
        this.imeiString = this.telephonyManager.getDeviceId();
        Log.d(TAG, "imeiString:  " + this.imeiString);
        MyPhoneStateListener myListener = new MyPhoneStateListener();
        this.telephonyManager.listen(myListener, 321);
        registerData();
    }

    public static WanDataController getInstance() {
        if (instance == null) {
            instance = new WanDataController(MifiService.getContext());
        }
        return instance;
    }

    public void setMobileDataEnable(boolean enabled) {
        if (enabled) {
            this.telephonyManager.enableDataConnectivity();
        } else {
            this.telephonyManager.disableDataConnectivity();
        }
    }

    public boolean getMobileDataEnable() {
        return this.telephonyManager.isDataConnectivityPossible();
    }

    public boolean getMobileDataState() {
        NetworkInfo networkInfo = this.connectivityManager.getActiveNetworkInfo();
        networkInfo.isAvailable();
        int networkType = networkInfo.getType();
        return 1 != networkType && networkType == 0;
    }

    public String getMobileIP() {
        String ip = StringUtil.ALL_INTERFACES;
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                NetworkInterface intf = en.nextElement();
                Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();
                while (enumIpAddr.hasMoreElements()) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address)) {
                        ip = inetAddress.getHostAddress().toString();
                        Log.d(TAG, "getMobileIP:  " + ip + "intf: " + intf.getName());
                        if (intf.getName().contains("rmnet")) {
                            return ip;
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
        return ip;
    }

    public String getGatewayIP() {
        return Constant.TCP_SERVER_IP;
    }

    public String getNetMode() {
        int type = this.telephonyManager.getNetworkType();
        if (type == TelephonyManager.NETWORK_TYPE_UNKNOWN) {
            type = this.telephonyManager.getVoiceNetworkType();
        }
        switch (type) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
                return "GPRS";
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return "EDGE";
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return "UMTS";
            case TelephonyManager.NETWORK_TYPE_CDMA:
                return "CDMA";
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                return "CDMA - EvDo rev. 0";
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                return "CDMA - EvDo rev. A";
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                return "CDMA - 1xRTT";
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                return "HSDPA";
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                return "HSUPA";
            case TelephonyManager.NETWORK_TYPE_HSPA:
                return "HSPA";
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "iDEN";
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                return "CDMA - EvDo rev. B";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "LTE";
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                return "CDMA - eHRPD";
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "HSPA+";
            default:
                return "UNKNOWN";
        }
    }

    public String getRegNetworkOperatorName() {
        return this.telephonyManager.getNetworkOperatorName();
    }

    public String getNetworkOperatorName() {
        String imsi = this.telephonyManager.getSubscriberId();
        if (imsi != null && imsi.length() >= 5) {
            String mccmnc = imsi.substring(0, 5);
            int nm = Integer.parseInt(mccmnc);
            switch (nm) {
                case 46000:
                case 46002:
                case 46007:
                case 46020:
                    return "中国移动";
                case 46001:
                case 46006:
                    return "中国联通";
                case 46003:
                case 46005:
                case 46011:
                    return "中国电信";
            }
        }
        return this.telephonyManager.getSimOperatorName();
    }

    public String getNetStatus() {
        NetworkInfo.State state;
        NetworkInfo info = null;
        NetworkInfo[] infos = this.connectivityManager.getAllNetworkInfo();
        if (infos != null) {
            for (int i = 0; i < infos.length; i++) {
                if (infos[i].getType() == 0) {
                    info = infos[i];
                }
            }
        }
        if (info == null || (state = info.getState()) == NetworkInfo.State.DISCONNECTED) {
            return "Disconnected";
        }
        if (state == NetworkInfo.State.CONNECTING) {
            return "Connecting";
        }
        if (state == NetworkInfo.State.CONNECTED) {
            return "Connected";
        }
        if (state == NetworkInfo.State.DISCONNECTING) {
            return "Disconnecting";
        }
        return "Disconnected";
    }

    public boolean isChinaTelecom() {
        String imsi = getIMSI();
        if (imsi == null || imsi.length() < 5) {
            return false;
        }
        String mccmnc = imsi.substring(0, 5);
        int nm = Integer.parseInt(mccmnc);
        return nm == 46003 || nm == 46005 || nm == 46011;
    }

    public String getIMSI() {
        return this.telephonyManager.getSubscriberId();
    }

    public String getICCID() {
        return this.telephonyManager.getSimSerialNumber();
    }

    public String getIMEI() {
        if (this.imeiString == null || this.imeiString.length() < 8) {
            this.imeiString = this.telephonyManager.getDeviceId();
            Log.d(TAG, "imeiString:  " + this.imeiString);
        }
        return this.imeiString == null ? "0" : this.imeiString;
    }

    public int getSignalStrength() {
        if (this.last_signallevel == 0) {
            if (getNetMode().equals("UNKNOWN")) {
                this.last_signallevel = 0;
            } else {
                this.last_signallevel = 2;
            }
        }
        return this.last_signallevel;
    }

    public int getDbm() {
        return this.dbm;
    }

    public String getSIMStatus() {
        int simstate = this.telephonyManager.getSimState();
        if (simstate == 0) {
            return "Unknown";
        }
        if (simstate == 1) {
            return "Absent";
        }
        if (simstate == 2) {
            return "Pin Required";
        }
        if (simstate == 3) {
            return "PUK Required";
        }
        if (simstate == 4) {
            return "Network Locked";
        }
        if (simstate == 5) {
            return "Ready";
        }
        return "Unknown";
    }

    public long getMobileRxBytes() {
        if (TrafficStats.getMobileRxBytes() == -1) {
            return 0L;
        }
        return TrafficStats.getMobileRxBytes();
    }

    public long getMobileTxBytes() {
        if (TrafficStats.getMobileRxBytes() == -1) {
            return 0L;
        }
        return TrafficStats.getMobileTxBytes();
    }

    public static NetworkInfo getActiveNetwork(Context context) {
        ConnectivityManager mConnMgr;
        if (context == null || (mConnMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)) == null) {
            return null;
        }
        return mConnMgr.getActiveNetworkInfo();
    }

    public void registerData() {
        NetWorkBroadcast mNetWorkReceiver = new NetWorkBroadcast();
        IntentFilter filter = new IntentFilter();
        filter.addAction(CONNECTIVITY_CHANGE_ACTION);
        this.mContext.registerReceiver(mNetWorkReceiver, filter);
    }

    /* loaded from: classes.dex */
    public class NetWorkBroadcast extends BroadcastReceiver {
        public NetWorkBroadcast() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            boolean connected = false;
            if (WanDataController.CONNECTIVITY_CHANGE_ACTION.equals(intent.getAction())) {
                NetworkInfo activeNetInfo = WanDataController.this.connectivityManager.getActiveNetworkInfo();
                if (activeNetInfo != null && activeNetInfo.isAvailable()) {
                    NetworkInfo networkInfo = WanDataController.this.connectivityManager.getNetworkInfo(0);
                    if (networkInfo != null) {
                        NetworkInfo.State state = networkInfo.getState();
                        networkInfo.getSubtypeName();
                        if (state != null) {
                            if (state == NetworkInfo.State.CONNECTED) {
                                connected = true;
                            }
                            WanDataController.this.updateNetworkType(WanDataController.this.telephonyManager.getNetworkType());
                        }
                    }
                } else {
                    connected = false;
                }
                if (connected) {
                    LedController.getInstance().setMode(1, 3, 0);
                } else {
                    LedController.getInstance().setMode(1, 2, 0);
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateNetworkType(int network_type) {
        int type;
        switch (network_type) {
            case 1:
            case 2:
            case 4:
            case 7:
            case 11:
                type = NETWORN_2G;
                break;
            case 3:
            case 5:
            case 6:
            case 8:
            case 9:
            case 10:
            case 12:
            case 14:
            case 15:
                type = NETWORN_3G;
                break;
            case 13:
                type = NETWORN_4G;
                break;
            default:
                type = NETWORN_NONE;
                break;
        }
        if (this.last_network_type != type) {
            if (type != NETWORN_NONE && type != NETWORN_2G && type != NETWORN_3G && type == NETWORN_4G) {
            }
            this.last_network_type = type;
        }
    }

    /* loaded from: classes.dex */
    class ChangeSimTask extends AsyncTask<Integer, Void, Boolean> {
        String switchBand;

        public ChangeSimTask(String string) {
            this.switchBand = null;
            this.switchBand = string;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public Boolean doInBackground(Integer... integers) {
            String resetSlaveCommand = this.switchBand;
            return Boolean.valueOf(WanDataController.this.doShellCommand(resetSlaveCommand));
        }
    }

    public boolean doShellCommand(String command) {
        BufferedReader successResult;
        Exception e;
        StringBuilder stringBuilder;
        StringBuilder stringBuilder2;
        boolean retValue;
        Throwable th;
        BufferedReader successResult2 = null;
        BufferedReader errorResult = null;
        Process p = null;
        DataOutputStream os = null;
        // TODO mess
        try {
            p = Runtime.getRuntime().exec(ShellUtils.COMMAND_SH);
            DataOutputStream os2 = new DataOutputStream(p.getOutputStream());
            try {
                os2.write(command.getBytes());
                os2.writeBytes("\n");
                os2.flush();
                os2.writeBytes(ShellUtils.COMMAND_EXIT);
                os2.flush();
                int result = p.waitFor();
                StringBuilder successMsg = new StringBuilder();
                try {
                    StringBuilder errorMsg = new StringBuilder();
                    try {
                        successResult = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    } catch (Exception e2) {
                        e = e2;
                        os = os2;
                        stringBuilder = errorMsg;
                        stringBuilder2 = successMsg;
                        try {
                            e.printStackTrace();
                            retValue = false;
                            Log.e(TAG, "doShellCommand command =" + command);
                            if (os != null) {
                                try {
                                    Log.e(TAG, "-----------os.close");
                                    os.close();
                                } catch (IOException e3) {
                                    e3.printStackTrace();
                                    if (p != null) {
                                        p.destroy();
                                    }
                                    Log.e(TAG, "----------------------end");
                                    return retValue;
                                }
                            }
                            if (successResult2 != null) {
                                Log.e(TAG, "-----------successResult");
                                successResult2.close();
                            }
                            if (errorResult != null) {
                                Log.e(TAG, "-----------errorResult");
                                errorResult.close();
                            }
                            if (p != null) {
                                p.destroy();
                            }
                            Log.e(TAG, "----------------------end");
                            return retValue;
                        } catch (Throwable th2) {
                            th = th2;
                            Log.e(TAG, "doShellCommand command =" + command);
                            if (os != null) {
                                try {
                                    Log.e(TAG, "-----------os.close");
                                    os.close();
                                } catch (IOException e32) {
                                    e32.printStackTrace();
                                    if (p != null) {
                                        p.destroy();
                                    }
                                    throw th;
                                }
                            }
                            if (successResult2 != null) {
                                Log.e(TAG, "-----------successResult");
                                successResult2.close();
                            }
                            if (errorResult != null) {
                                Log.e(TAG, "-----------errorResult");
                                errorResult.close();
                            }
                            if (p != null) {
                                p.destroy();
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        os = os2;
                        stringBuilder = errorMsg;
                        stringBuilder2 = successMsg;
                        Log.e(TAG, "doShellCommand command =" + command);
                        if (os != null) {
                            Log.e(TAG, "-----------os.close");
                            os.close();
                        }
                        if (successResult2 != null) {
                            Log.e(TAG, "-----------successResult");
                            successResult2.close();
                        }
                        if (errorResult != null) {
                            Log.e(TAG, "-----------errorResult");
                            errorResult.close();
                        }
                        if (p != null) {
                            p.destroy();
                        }
                        throw th;
                    }
                    try {
                        String s;
                        BufferedReader errorResult2 = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                        while (true) {
                            try {
                                s = successResult.readLine();
                                if (s != null) {
                                    successMsg.append(s);
                                } else {
                                    //while (true) {
                                    //}
                                    Log.e(TAG, "----------------------2");
                                    retValue = true;
                                    Log.e(TAG, "doShellCommand command =" + command);
                                    if (os2 != null) {
                                        try {
                                            Log.e(TAG, "-----------os.close");
                                            os2.close();
                                        } catch (IOException e322) {
                                            e322.printStackTrace();
                                        }
                                    }
                                    if (successResult != null) {
                                        Log.e(TAG, "-----------successResult");
                                        successResult.close();
                                    }
                                    if (errorResult2 != null) {
                                        Log.e(TAG, "-----------errorResult");
                                        errorResult2.close();
                                    }
                                    if (p != null) {
                                        p.destroy();
                                    }
                                    os = os2;
                                    errorResult = errorResult2;
                                    successResult2 = successResult;
                                    stringBuilder = errorMsg;
                                    stringBuilder2 = successMsg;
                                    Log.e(TAG, "----------------------end");
                                    return retValue;
                                }
                            } catch (Exception e4) {
                                e = e4;
                                os = os2;
                                errorResult = errorResult2;
                                successResult2 = successResult;
                                stringBuilder = errorMsg;
                                stringBuilder2 = successMsg;
                            } catch (Throwable th4) {
                                th = th4;
                                os = os2;
                                errorResult = errorResult2;
                                successResult2 = successResult;
                                stringBuilder = errorMsg;
                                stringBuilder2 = successMsg;
                            }
                        }
                        /*s = errorResult2.readLine();
                        if (s != null) {
                            errorMsg.append(s);
                        } else {
                            Log.e(TAG, "----------------------2");
                            retValue = true;
                            Log.e(TAG, "doShellCommand command =" + command);
                            if (os2 != null) {
                                Log.e(TAG, "-----------os.close");
                                os2.close();
                            }
                            if (successResult != null) {
                                Log.e(TAG, "-----------successResult");
                                successResult.close();
                            }
                            if (errorResult2 != null) {
                                Log.e(TAG, "-----------errorResult");
                                errorResult2.close();
                            }
                            if (p != null) {
                                p.destroy();
                            }
                            os = os2;
                            errorResult = errorResult2;
                            successResult2 = successResult;
                            stringBuilder = errorMsg;
                            stringBuilder2 = successMsg;
                            Log.e(TAG, "----------------------end");
                            return retValue;
                        }*/
                    } catch (Exception e5) {
                        e = e5;
                        os = os2;
                        successResult2 = successResult;
                        stringBuilder = errorMsg;
                        stringBuilder2 = successMsg;
                        e.printStackTrace();
                        retValue = false;
                        Log.e(TAG, "doShellCommand command =" + command);
                        if (os != null) {
                            Log.e(TAG, "-----------os.close");
                            os.close();
                        }
                        if (successResult2 != null) {
                            Log.e(TAG, "-----------successResult");
                            successResult2.close();
                        }
                        if (errorResult != null) {
                            Log.e(TAG, "-----------errorResult");
                            errorResult.close();
                        }
                        if (p != null) {
                            p.destroy();
                        }
                        Log.e(TAG, "----------------------end");
                        return retValue;
                    } catch (Throwable th5) {
                        th = th5;
                        os = os2;
                        successResult2 = successResult;
                        stringBuilder = errorMsg;
                        stringBuilder2 = successMsg;
                        Log.e(TAG, "doShellCommand command =" + command);
                        if (os != null) {
                            Log.e(TAG, "-----------os.close");
                            os.close();
                        }
                        if (successResult2 != null) {
                            Log.e(TAG, "-----------successResult");
                            successResult2.close();
                        }
                        if (errorResult != null) {
                            Log.e(TAG, "-----------errorResult");
                            errorResult.close();
                        }
                        if (p != null) {
                            p.destroy();
                        }
                        throw th;
                    }
                } catch (Exception e6) {
                    e = e6;
                    os = os2;
                    stringBuilder2 = successMsg;
                    e.printStackTrace();
                    retValue = false;
                    Log.e(TAG, "doShellCommand command =" + command);
                    if (os != null) {
                        Log.e(TAG, "-----------os.close");
                        os.close();
                    }
                    if (successResult2 != null) {
                        Log.e(TAG, "-----------successResult");
                        successResult2.close();
                    }
                    if (errorResult != null) {
                        Log.e(TAG, "-----------errorResult");
                        errorResult.close();
                    }
                    if (p != null) {
                        p.destroy();
                    }
                    Log.e(TAG, "----------------------end");
                    return retValue;
                } catch (Throwable th6) {
                    th = th6;
                    os = os2;
                    stringBuilder2 = successMsg;
                    Log.e(TAG, "doShellCommand command =" + command);
                    if (os != null) {
                        Log.e(TAG, "-----------os.close");
                        os.close();
                    }
                    if (successResult2 != null) {
                        Log.e(TAG, "-----------successResult");
                        successResult2.close();
                    }
                    if (errorResult != null) {
                        Log.e(TAG, "-----------errorResult");
                        errorResult.close();
                    }
                    if (p != null) {
                        p.destroy();
                    }
                    throw th;
                }
            } catch (Exception e7) {
                e = e7;
                os = os2;
                e.printStackTrace();
                retValue = false;
                Log.e(TAG, "doShellCommand command =" + command);
                if (os != null) {
                    Log.e(TAG, "-----------os.close");
                    os.close();
                }
                if (successResult2 != null) {
                    Log.e(TAG, "-----------successResult");
                    successResult2.close();
                }
                if (errorResult != null) {
                    Log.e(TAG, "-----------errorResult");
                    errorResult.close();
                }
                if (p != null) {
                    p.destroy();
                }
                Log.e(TAG, "----------------------end");
                return retValue;
            } catch (Throwable th7) {
                th = th7;
                os = os2;
                Log.e(TAG, "doShellCommand command =" + command);
                if (os != null) {
                    Log.e(TAG, "-----------os.close");
                    os.close();
                }
                if (successResult2 != null) {
                    Log.e(TAG, "-----------successResult");
                    successResult2.close();
                }
                if (errorResult != null) {
                    Log.e(TAG, "-----------errorResult");
                    errorResult.close();
                }
                if (p != null) {
                    p.destroy();
                }
                //throw th;
            }
        } catch (Exception e8) {/*
            e = e8;
            e.printStackTrace();
            retValue = false;
            Log.e(TAG, "doShellCommand command =" + command);
            if (os != null) {
                Log.e(TAG, "-----------os.close");
                os.close();
            }
            if (successResult2 != null) {
                Log.e(TAG, "-----------successResult");
                successResult2.close();
            }
            if (errorResult != null) {
                Log.e(TAG, "-----------errorResult");
                errorResult.close();
            }
            if (p != null) {
                p.destroy();
            }
            Log.e(TAG, "----------------------end");
            return retValue;*/
        }
        return false;
    }


    String sendAtBySmd(String at) {
        String command = "modem_at " + at;
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
                    String response = sb.toString();
                    return response;
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "sendAtBySmd error: " + at);
            return HttpVersions.HTTP_0_9;
        }
    }

    public int getCfun() {
        int cfun = 7;
        String response = sendAtBySmd("AT+CFUN?");
        if (response.indexOf("+CFUN: 0") != -1) {
            cfun = 0;
        } else if (response.indexOf("+CFUN: 1") != -1) {
            cfun = 1;
        }
        Log.d(TAG, "getCfun: " + cfun);
        return cfun;
    }

    public void setcfun(int cfun) {
        String command = "AT+CFUN=" + cfun;
        sendAtBySmd(command);
    }

    public void setPreferedNetworkTypeAtcmd(int mode) {
        int attype = 2;
        if (mode == 0) {
            attype = 12;
            if (isChinaTelecom()) {
                attype = 11;
            }
        } else if (mode == 1) {
            attype = 14;
        } else if (mode == 2) {
            attype = 11;
        }
        String imsi = getIMSI();
        if (imsi == null || imsi.length() < 5) {
            Log.d(TAG, "setPreferedNetworkTypeAtcmd imsi is null");
            return;
        }
        Log.d(TAG, "setPreferedNetworkTypeAtcmd " + attype);
        String string = "AT^SYSCONFIG=" + attype + ",0,1,2";
        sendAtBySmd(string);
    }

    public void setPreferedNetworkType(int mode) {
        int type = 12;
        int attype = 2;
        if (mode == 0) {
            type = 12;
            attype = 12;
            if (isChinaTelecom()) {
                type = 11;
                attype = 11;
            }
        } else if (mode == 1) {
            type = 2;
            attype = 14;
        } else if (mode == 2) {
            type = 11;
            attype = 11;
        }
        Log.d(TAG, "setPreferredNetworkType " + type);
        String command = "settings put global preferred_network_mode " + type;
        try {
            Runtime.getRuntime().exec(command);
        } catch (Exception e) {
            Log.d(TAG, "settings put fail");
        }
        String string = "AT^SYSCONFIG=" + attype + ",0,1,2";
        sendAtBySmd(string);
    }

    /* JADX WARN: Type inference failed for: r0v1, types: [com.mifiservice.device.WanDataController$1] */
    public void HandleCheckNetwork() {
        this.stopCheckNetwork = true;
        new Thread() { // from class: com.mifiservice.device.WanDataController.1
            @Override // java.lang.Thread, java.lang.Runnable
            public void run() {
                Log.d(WanDataController.TAG, "HandleCheckNetwork");
                try {
                    Thread.sleep(10000L);
                } catch (InterruptedException e) {
                }
                if (!WanDataController.this.getNetStatus().equals("Connected") && MifiConfiguration.getInstance().WANnetworkType == 0 && !WanDataController.this.isChinaTelecom()) {
                    WanDataController.this.setPreferedNetworkTypeAtcmd(0);
                }
                WanDataController.this.stopCheckNetwork = false;
            }
        }.start();
    }

    public int getPreferedNetworkType() {
        Log.d(TAG, "getPreferredNetworkType 22");
        if (22 != 22) {
            if (22 != 21) {
                if (22 != 11) {
                    return 0;
                }
                return 2;
            }
            return 1;
        }
        return 0;
    }

    public boolean setUsbTethering(boolean enabled) {
        return this.connectivityManager.setUsbTethering(enabled) == 0;
    }

    private boolean checkImeiByAt(String imei) {
        int result = 0;
        String response = sendAtBySmd("AT+WRIMEI?");
        String hresult = "+WRIMEI: " + imei;
        if (response.indexOf(hresult) != -1) {
            result = 1;
        }
        Log.d(TAG, "checkImeiByAt: " + result);
        if (result == 1) {
            this.imeiString = imei;
        }
        return result == 1;
    }

    public boolean setImei(String imei) {
        String string = "AT+WRIMEI=" + imei;
        sendAtBySmd(string);
        return checkImeiByAt(imei);
    }
}