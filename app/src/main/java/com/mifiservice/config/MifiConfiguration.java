package com.mifiservice.config;

import android.content.Context;
import android.util.Log;
import com.mifiservice.service.MifiService;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import org.apache.http.util.EncodingUtils;
import org.eclipse.jetty.http.HttpVersions;
import org.json.JSONObject;

/* loaded from: classes.dex */
public class MifiConfiguration {
    private static final String TAG = "MifiConfiguration";
    private static final String WIFI_FAC_BACKUP_FILE = "/persist/wifi.json";
    private static final String WIFI_MAC_FILE = "/persist/wcnss_mac_addr";
    private static final String hostapd_config_file = "/data/misc/wifi/hostapd.conf";
    private static MifiConfiguration instance = null;
    private static final String mifi_config_file = "/data/mificonfig.json";
    public Context mContext;
    public String loginPassword = "admin";
    public boolean WANautoConnect = true;
    public int WANnetworkType = 0;
    public String WIFIssid = "4G MIFI-DA7";
    public boolean WIFIssidhidden = false;
    public String WIFIhwmode = "n";
    public int WIFIchannel = 6;
    public int WIFImaxSta = 10;
    public int WIFIencrypt = 1;
    public String WIFIpassword = "1234567890";
    public String DHCPrangeLow = "192.168.100.100";
    public String DHCPrangeHigh = "192.168.100.200";
    public int WANdefaultAPN = 0;
    public int WANdefaultAPNSave = 0;
    public String WANCurrentImei = HttpVersions.HTTP_0_9;
    public String WANCurrentImsi = HttpVersions.HTTP_0_9;
    public long WANtrafficStartTime = 0;
    public long WANtrafficTotalCount = 0;
    private String backWifiSsid = HttpVersions.HTTP_0_9;
    private String backWifiKey = HttpVersions.HTTP_0_9;
    private String wifiMac = HttpVersions.HTTP_0_9;
    public int MACFilterType = 3;
    public String[] MACFilterList = new String[10];

    public static MifiConfiguration getInstance() {
        if (instance == null) {
            instance = new MifiConfiguration(MifiService.getContext());
        }
        return instance;
    }

    public MifiConfiguration(Context context) {
        this.mContext = null;
        this.mContext = context;
        loadWifiFacFile();
        loadWifiMacFile();
        loadFromFile();
        new File(hostapd_config_file).delete();
    }

    public void loadFromFile() {
        try {
            String acceptjson = readFileSd(mifi_config_file);
            JSONObject jo = new JSONObject(acceptjson);
            this.loginPassword = jo.getString("loginPassword");
            this.WANautoConnect = jo.getBoolean("WANautoConnect");
            this.WANnetworkType = jo.getInt("WANnetworkType");
            this.WIFIssid = jo.getString("WIFIssid");
            this.WIFIssidhidden = jo.getBoolean("WIFIssidhidden");
            this.WIFIhwmode = jo.getString("WIFIhwmode");
            this.WIFIchannel = jo.getInt("WIFIchannel");
            this.WIFImaxSta = jo.getInt("WIFImaxSta");
            this.WIFIencrypt = jo.getInt("WIFIencrypt");
            this.WIFIpassword = jo.getString("WIFIpassword");
            this.DHCPrangeLow = jo.getString("DHCPrangeLow");
            this.DHCPrangeHigh = jo.getString("DHCPrangeHigh");
            this.WANdefaultAPN = jo.getInt("WANdefaultAPN");
            this.WANCurrentImei = jo.getString("WANCurrentImei");
            this.WANCurrentImsi = jo.getString("WANCurrentImsi");
            this.WANtrafficStartTime = jo.getLong("WANtrafficStartTime");
            this.WANtrafficTotalCount = jo.getLong("WANtrafficTotalCount");
            this.WANdefaultAPNSave = jo.getInt("WANdefaultAPNSave");
            this.MACFilterType = jo.getInt("MACFilterType");
            this.MACFilterList[0] = jo.getString("MACFilter_0");
            this.MACFilterList[1] = jo.getString("MACFilter_1");
            this.MACFilterList[2] = jo.getString("MACFilter_2");
            this.MACFilterList[3] = jo.getString("MACFilter_3");
            this.MACFilterList[4] = jo.getString("MACFilter_4");
            this.MACFilterList[5] = jo.getString("MACFilter_5");
            this.MACFilterList[6] = jo.getString("MACFilter_6");
            this.MACFilterList[7] = jo.getString("MACFilter_7");
            this.MACFilterList[8] = jo.getString("MACFilter_8");
            this.MACFilterList[9] = jo.getString("MACFilter_9");
        } catch (Exception e) {
            Log.d(TAG, "read error");
            this.loginPassword = "admin";
            this.WANautoConnect = true;
            this.WANnetworkType = 0;
            this.WIFIssid = getWifiDefaultSsid();
            this.WIFIssidhidden = false;
            this.WIFIhwmode = "n";
            this.WIFIchannel = 6;
            this.WIFImaxSta = 10;
            this.WIFIencrypt = 4;
            this.WIFIpassword = getWifiDefaultKey();
            this.DHCPrangeLow = "192.168.100.2";
            this.DHCPrangeHigh = "192.168.100.254";
            this.WANdefaultAPN = 0;
            this.WANCurrentImei = HttpVersions.HTTP_0_9;
            this.WANCurrentImsi = HttpVersions.HTTP_0_9;
            this.WANtrafficStartTime = 0L;
            this.WANtrafficTotalCount = 0L;
            this.MACFilterType = 3;
            this.MACFilterList[0] = HttpVersions.HTTP_0_9;
            this.MACFilterList[1] = HttpVersions.HTTP_0_9;
            this.MACFilterList[2] = HttpVersions.HTTP_0_9;
            this.MACFilterList[3] = HttpVersions.HTTP_0_9;
            this.MACFilterList[4] = HttpVersions.HTTP_0_9;
            this.MACFilterList[5] = HttpVersions.HTTP_0_9;
            this.MACFilterList[6] = HttpVersions.HTTP_0_9;
            this.MACFilterList[7] = HttpVersions.HTTP_0_9;
            this.MACFilterList[8] = HttpVersions.HTTP_0_9;
            this.MACFilterList[9] = HttpVersions.HTTP_0_9;
            saveToFile();
        }
    }

    public void loadWifiFacFile() {
        try {
            String acceptjson = readFileSd(WIFI_FAC_BACKUP_FILE);
            JSONObject jo = new JSONObject(acceptjson);
            this.backWifiSsid = jo.getString("WIFIssid");
            this.backWifiKey = jo.getString("WIFIpassword");
            Log.d(TAG, "backWifiSsid:" + this.backWifiSsid);
            Log.d(TAG, "backWifiKey:" + this.backWifiKey);
        } catch (Exception e) {
            Log.d(TAG, "read fac file error");
        }
    }

    public void loadWifiMacFile() {
        try {
            this.wifiMac = readFileSd(WIFI_MAC_FILE);
            Log.d(TAG, "wifiMac:" + this.wifiMac);
        } catch (Exception e) {
            Log.d(TAG, "read mac file error");
        }
    }

    public String getWifiDefaultSsid() {
        String macString = this.wifiMac.replace(":", HttpVersions.HTTP_0_9).toUpperCase();
        loadWifiFacFile();
        if (macString.length() > 3) {
            if (this.backWifiSsid.length() > 0) {
                String ssid = this.backWifiSsid + macString.substring(macString.length() - 3);
                return ssid;
            }
            String ssid2 = "4G-UFI-" + macString.substring(macString.length() - 3);
            return ssid2;
        }
        return "4G-UFI-DA7";
    }

    public String getWifiDefaultKey() {
        if (this.backWifiKey.length() <= 0) {
            return "1234567890";
        }
        String key = this.backWifiKey;
        return key;
    }

    public void saveToFile() {
        Log.d(TAG, "saveToFile");
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("loginPassword", this.loginPassword);
            jsonObject.put("WANautoConnect", this.WANautoConnect);
            jsonObject.put("WANnetworkType", this.WANnetworkType);
            jsonObject.put("WIFIssid", this.WIFIssid);
            jsonObject.put("WIFIssidhidden", this.WIFIssidhidden);
            jsonObject.put("WIFIhwmode", this.WIFIhwmode);
            jsonObject.put("WIFIchannel", this.WIFIchannel);
            jsonObject.put("WIFImaxSta", this.WIFImaxSta);
            jsonObject.put("WIFIencrypt", this.WIFIencrypt);
            jsonObject.put("WIFIpassword", this.WIFIpassword);
            jsonObject.put("DHCPrangeLow", this.DHCPrangeLow);
            jsonObject.put("DHCPrangeHigh", this.DHCPrangeHigh);
            jsonObject.put("WANdefaultAPN", this.WANdefaultAPN);
            jsonObject.put("WANdefaultAPNSave", this.WANdefaultAPNSave);
            jsonObject.put("WANCurrentImei", this.WANCurrentImei);
            jsonObject.put("WANCurrentImsi", this.WANCurrentImsi);
            jsonObject.put("WANtrafficStartTime", this.WANtrafficStartTime);
            jsonObject.put("WANtrafficTotalCount", this.WANtrafficTotalCount);
            jsonObject.put("MACFilterType", this.MACFilterType);
            jsonObject.put("MACFilter_0", this.MACFilterList[0]);
            jsonObject.put("MACFilter_1", this.MACFilterList[1]);
            jsonObject.put("MACFilter_2", this.MACFilterList[2]);
            jsonObject.put("MACFilter_3", this.MACFilterList[3]);
            jsonObject.put("MACFilter_4", this.MACFilterList[4]);
            jsonObject.put("MACFilter_5", this.MACFilterList[5]);
            jsonObject.put("MACFilter_6", this.MACFilterList[6]);
            jsonObject.put("MACFilter_7", this.MACFilterList[7]);
            jsonObject.put("MACFilter_8", this.MACFilterList[8]);
            jsonObject.put("MACFilter_9", this.MACFilterList[9]);
            saveFileSd(mifi_config_file, jsonObject.toString());
        } catch (Exception e) {
            Log.d(TAG, "save error");
        }
    }

    public void reset() {
        deleteFileSd();
    }

    public String readFile(String fileName) throws IOException {
        String res = HttpVersions.HTTP_0_9;
        try {
            FileInputStream fin = this.mContext.openFileInput(fileName);
            int length = fin.available();
            byte[] buffer = new byte[length];
            fin.read(buffer);
            res = EncodingUtils.getString(buffer, "UTF-8");
            fin.close();
            return res;
        } catch (Exception e) {
            return res;
        }
    }

    public boolean saveFile(String file, String content) {
        BufferedWriter bw;
        BufferedWriter bw2 = null;

        boolean isSaveSuccess = false;
        try {
            Context context = this.mContext;
            Context context2 = this.mContext;
            FileOutputStream fileOutputStream = context.openFileOutput(file, 0);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            bw = new BufferedWriter(outputStreamWriter);
        } catch (FileNotFoundException e) {
        } catch (IOException e2) {
        } catch (Throwable th) {
            th = th;
        }/* TODO smali2java buggy nested try-catches. This method is not used.
        try {
            bw.write(content);
            isSaveSuccess = true;
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e3) {
                }
            }
        } catch (FileNotFoundException e4) {
            bw2 = bw;
            if (bw2 != null) {
                try {
                    bw2.close();
                } catch (IOException e5) {
                }
            }
            return isSaveSuccess;
        } catch (IOException e6) {
            bw2 = bw;
            if (bw2 != null) {
                try {
                    bw2.close();
                } catch (IOException e7) {
                }
            }
            return isSaveSuccess;
        } catch (Throwable th2) {
            th = th2;
            bw2 = bw;
            if (bw2 != null) {
                try {
                    bw2.close();
                } catch (IOException e8) {
                }
            }
            throw th;
        }*/
        return isSaveSuccess;
    }

    public void deleteFile() {
        boolean result = this.mContext.deleteFile(mifi_config_file);
        Log.e(TAG, "delete file , " + result);
    }

    public String readFileSd(String fileName) throws IOException {
        String res = HttpVersions.HTTP_0_9;
        try {
            File file = new File(fileName);
            InputStream fin = new FileInputStream(file);
            int length = fin.available();
            byte[] buffer = new byte[length];
            fin.read(buffer);
            res = EncodingUtils.getString(buffer, "UTF-8");
            fin.close();
            return res;
        } catch (Exception e) {
            return res;
        }
    }

    public boolean saveFileSd(String fileName, String content) {
        Throwable th;
        BufferedWriter bw = null;
        boolean isSaveSuccess = false;
        // TODO: refactor smali2java buggy try-catch branches
        try {
            BufferedWriter bw2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(fileName))));
            try {
                bw2.write(content);
                isSaveSuccess = true;
                if (bw2 != null) {
                    try {
                        bw2.close();
                        bw = bw2;
                    } catch (IOException e) {
                        bw = bw2;
                    }
                }
            } catch (FileNotFoundException e2) {
                bw = bw2;
                if (bw != null) {
                    try {
                        bw.close();
                    } catch (IOException e3) {
                    }
                }
                return isSaveSuccess;
            } catch (IOException e4) {
                bw = bw2;
                if (bw != null) {
                    try {
                        bw.close();
                    } catch (IOException e5) {
                    }
                }
                return isSaveSuccess;
            } catch (Throwable th2) {
                th = th2;
                bw = bw2;
                if (bw != null) {
                    try {
                        bw.close();
                    } catch (IOException e6) {
                    }
                }
                throw th;
            }
        } catch (Throwable e) {
            if (bw != null) {
                try {
                    bw.close();
                } catch (Throwable e1) {

                }
            }
            e.printStackTrace();
            return isSaveSuccess;
        }
        return isSaveSuccess;
    }

    public void deleteFileSd() {
        new File(mifi_config_file).delete();
    }
}