package com.mifiservice.server;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.provider.Settings;
import android.util.Log;
import com.mifiservice.config.MifiConfiguration;
import com.mifiservice.device.DeviceController;
import com.mifiservice.device.GpsController;
import com.mifiservice.device.WanApnController;
import com.mifiservice.device.WanDataController;
import com.mifiservice.device.WifiApController;
import com.mifiservice.ota.Updater;
import com.mifiservice.service.MifiService;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.http.HttpVersions;
import org.json.JSONArray;
import org.json.JSONObject;

/* loaded from: classes.dex */
public class AjaxSevlet extends HttpServlet {
    private static final int MAX_FILE_SIZE = 524288000;
    private static final int MAX_REQUEST_SIZE = 629145600;
    private static final int MEMORY_THRESHOLD = 3145728;
    private static final long serialVersionUID = 1;
    private WifiApController mWifiApController = null;
    private WanDataController mWanDataController = null;
    private WanApnController mWanApnController = null;
    private DeviceController mDeviceController = null;
    private GpsController mGpsController = null;
    private MifiConfiguration mifiConfiguration = null;
    private Context context = null;
    private Updater mUpdater = null;

    @Override // javax.servlet.http.HttpServlet
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/json");
        if (this.context == null && MifiService.getContext() != null) {
            this.mWifiApController = WifiApController.getInstance();
            this.mWanDataController = WanDataController.getInstance();
            this.mWanApnController = WanApnController.getInstance();
            this.mDeviceController = DeviceController.getInstance();
            this.mGpsController = GpsController.getInstance();
            this.mifiConfiguration = MifiConfiguration.getInstance();
            this.context = MifiService.getContext();
            this.mUpdater = Updater.getInstance();
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(req.getInputStream(), "utf-8"));
        StringBuffer sb = new StringBuffer(HttpVersions.HTTP_0_9);
        while (true) {
            String temp = br.readLine();
            if (temp != null) {
                sb.append(temp);
            } else {
                br.close();
                String acceptjson = sb.toString();
                Log.d("Ajax", "request: " + acceptjson);
                return;
            }
        }
    }

    @Override // javax.servlet.http.HttpServlet
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int webpronum;
        boolean isupload = true;
        if (!ServletFileUpload.isMultipartContent(req)) {
            isupload = false;
            Log.d("Ajax", "isuploadΪfalse");
        }
        if (isupload) {
            resp.setCharacterEncoding("UTF-8");
            Log.d("Ajax", "isuploadΪtrue");
            DiskFileItemFactory factory = new DiskFileItemFactory();
            factory.setSizeThreshold(MEMORY_THRESHOLD);
            factory.setRepository(new File(System.getProperty("java.io.tmpdir")));
            ServletFileUpload upload = new ServletFileUpload(factory);
            upload.setFileSizeMax(MAX_FILE_SIZE);
            upload.setSizeMax(MAX_REQUEST_SIZE);
            upload.setHeaderEncoding("UTF-8");
            File uploadDir = new File("/data");
            if (!uploadDir.exists()) {
                uploadDir.mkdir();
            }
            JSONObject jsonObject = new JSONObject();
            try {
                List<FileItem> formItems = upload.parseRequest(req);
                if (formItems != null && formItems.size() > 0) {
                    for (FileItem item : formItems) {
                        if (!item.isFormField()) {
                            String filePath = "/data" + File.separator + "update.zip";
                            File storeFile = new File(filePath);
                            Log.d("Ajax", "filePath: " + filePath);
                            if (storeFile.exists()) {
                                storeFile.delete();
                                Log.d("Ajax", "�ļ��Ѵ��ڣ�ɾ���ļ�");
                            } else {
                                Log.d("Ajax", "�ļ������ڣ�����write");
                            }
                            item.write(storeFile);
                            Log.d("Ajax", "�ļ��ϴ��ɹ�");
                            jsonObject.put("flag", "1");
                            resp.getWriter().println(jsonObject.toString());
                            return;
                        }
                    }
                    resp.getWriter().write("flag:0");
                    return;
                }
                return;
            } catch (Exception ex) {
                Log.d("Ajax", "������Ϣ: " + ex.getMessage());
                return;
            }
        }
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/json");
        if (this.context == null && MifiService.getContext() != null) {
            this.mWifiApController = WifiApController.getInstance();
            this.mWanDataController = WanDataController.getInstance();
            this.mWanApnController = WanApnController.getInstance();
            this.mDeviceController = DeviceController.getInstance();
            this.mGpsController = GpsController.getInstance();
            this.mifiConfiguration = MifiConfiguration.getInstance();
            this.context = MifiService.getContext();
            this.mUpdater = Updater.getInstance();
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(req.getInputStream(), "utf-8"));
        StringBuffer sb = new StringBuffer(HttpVersions.HTTP_0_9);
        while (true) {
            String temp = br.readLine();
            if (temp == null) {
                break;
            } else {
                sb.append(temp);
            }
        }
        br.close();
        String acceptjson = sb.toString();
        Log.d("Ajax", "request: " + acceptjson);
        try {
            JSONObject jo = new JSONObject(acceptjson);
            int funcNo = jo.getInt("funcNo");
            if (funcNo == 1000) {
                String username = (String) jo.get("username");
                String password = (String) jo.get("password");
                JSONObject jsonObject2 = new JSONObject();
                if (username.equals("admin") && password.equals(MifiConfiguration.getInstance().loginPassword)) {
                    jsonObject2.put("flag", "1");
                    jsonObject2.put("error_info", "none");
                    JSONObject resultObject = new JSONObject();
                    resultObject.put("imei", this.mWanDataController.getIMEI());
                    resultObject.put("conn_mode", this.mWanDataController.getMobileDataEnable() ? "0" : "1");
                    resultObject.put("net_mode", 0);
                    resultObject.put("fwversion", this.mDeviceController.getDeviceName() + "-" + this.mDeviceController.getSWVersion());
                    JSONArray jsonArray = new JSONArray();
                    jsonArray.put(resultObject);
                    jsonObject2.put("results", jsonArray);
                } else {
                    jsonObject2.put("flag", "0");
                    jsonObject2.put("error_info", "username or password error!");
                }
                resp.getWriter().println(jsonObject2.toString());
                return;
            }
            if (funcNo == 1001) {
                JSONObject jsonObject3 = new JSONObject();
                jsonObject3.put("flag", "1");
                jsonObject3.put("error_info", "none");
                JSONObject resultObject2 = new JSONObject();
                if (this.mWanDataController.getRegNetworkOperatorName().length() > 0 || this.mWanDataController.getNetStatus().equals("Connected")) {
                    resultObject2.put("rssi", this.mWanDataController.getSignalStrength());
                    resultObject2.put("netmode", this.mWanDataController.getNetMode());
                } else {
                    resultObject2.put("rssi", 0);
                    resultObject2.put("netmode", "UNKNOWN");
                }
                resultObject2.put("netstatus", this.mWanDataController.getNetStatus());
                resultObject2.put("oper", this.mWanDataController.getNetworkOperatorName());
                JSONArray jsonArray2 = new JSONArray();
                jsonArray2.put(resultObject2);
                jsonObject3.put("results", jsonArray2);
                resp.getWriter().println(jsonObject3.toString());
                return;
            }
            if (funcNo == 1002) {
                JSONObject jsonObject4 = new JSONObject();
                jsonObject4.put("flag", "1");
                jsonObject4.put("error_info", "none");
                JSONObject resultObject3 = new JSONObject();
                resultObject3.put("IP", this.mWanDataController.getMobileIP());
                resultObject3.put("mask", "255.255.255.0");
                resultObject3.put("dns1", this.mWifiApController.getWlanDNS(1));
                resultObject3.put("dns2", this.mWifiApController.getWlanDNS(2));
                resultObject3.put("ssid", this.mWifiApController.getSsid());
                resultObject3.put("wlan_ip", this.mWifiApController.getWlanIP());
                resultObject3.put("pwd", this.mWifiApController.getWifiApConfiguration().preSharedKey);
                JSONArray jsonArray3 = new JSONArray();
                jsonArray3.put(resultObject3);
                jsonObject4.put("results", jsonArray3);
                resp.getWriter().println(jsonObject4.toString());
                return;
            }
            if (funcNo == 1003) {
                JSONObject jsonObject5 = new JSONObject();
                jsonObject5.put("flag", "1");
                jsonObject5.put("error_info", "none");
                JSONObject resultObject4 = new JSONObject();
                resultObject4.put("up_bytes", this.mWanDataController.getMobileTxBytes() / FileUtils.ONE_KB);
                resultObject4.put("down_bytes", this.mWanDataController.getMobileRxBytes() / FileUtils.ONE_KB);
                resultObject4.put("longtitude", this.mGpsController.getLongitude());
                resultObject4.put("latitude", this.mGpsController.getLatitude());
                resultObject4.put("client_num", this.mWifiApController.getClientNumber());
                resultObject4.put("maxSta", this.mWifiApController.getWifiMaxSta());
                JSONArray jsonArray4 = new JSONArray();
                jsonArray4.put(resultObject4);
                jsonObject5.put("results", jsonArray4);
                resp.getWriter().println(jsonObject5.toString());
                return;
            }
            if (funcNo == 1004) {
                String conn_mode = (String) jo.get("conn_mode");
                boolean auto_conn = false;
                JSONObject jsonObject6 = new JSONObject();
                if (conn_mode.equals("1")) {
                    auto_conn = false;
                } else if (conn_mode.equals("0")) {
                    auto_conn = true;
                }
                this.mWanDataController.setMobileDataEnable(auto_conn);
                MifiConfiguration.getInstance().WANautoConnect = auto_conn;
                MifiConfiguration.getInstance().saveToFile();
                jsonObject6.put("flag", "1");
                jsonObject6.put("error_info", "none");
                resp.getWriter().println(jsonObject6.toString());
                return;
            }
            if (funcNo == 1005) {
                int net_mode = Integer.parseInt((String) jo.get("net_mode"));
                JSONObject jsonObject7 = new JSONObject();
                this.mWanDataController.setPreferedNetworkType(net_mode);
                MifiConfiguration.getInstance().WANnetworkType = net_mode;
                MifiConfiguration.getInstance().saveToFile();
                if (!this.mWanDataController.getNetMode().equals("LTE") && net_mode == 0) {
                    WanDataController wanDataController = this.mWanDataController;
                    WanDataController.getInstance().HandleCheckNetwork();
                    this.mWanDataController.setPreferedNetworkTypeAtcmd(2);
                }
                jsonObject7.put("flag", "1");
                jsonObject7.put("error_info", "none");
                resp.getWriter().println(jsonObject7.toString());
                return;
            }
            if (funcNo == 1006) {
                JSONObject jsonObject8 = new JSONObject();
                jsonObject8.put("flag", "1");
                jsonObject8.put("error_info", "none");
                JSONObject resultObject5 = new JSONObject();
                resultObject5.put("wifi_status", this.mWifiApController.getWlanStatus() ? "1" : "0");
                resultObject5.put("ssid_flag", this.mWifiApController.getWifiSsidHidden() ? "0" : "1");
                resultObject5.put("mode", this.mWifiApController.getWifiHwMode());
                resultObject5.put("ip", this.mWifiApController.getWlanIP());
                resultObject5.put("mac", this.mWifiApController.getWifiMAC());
                resultObject5.put("ssid", this.mWifiApController.getSsid());
                resultObject5.put("client_num", this.mWifiApController.getClientNumber());
                resultObject5.put("maxSta", this.mWifiApController.getWifiMaxSta());
                resultObject5.put("channel", this.mWifiApController.getWifiChannel());
                JSONArray jsonArray5 = new JSONArray();
                jsonArray5.put(resultObject5);
                jsonObject8.put("results", jsonArray5);
                resp.getWriter().println(jsonObject8.toString());
                return;
            }
            if (funcNo == 1007) {
                JSONObject jsonObject9 = new JSONObject();
                String ssid = (String) jo.get("ssid");
                int maxSta = jo.getInt("maxSta");
                String[] strArr = {"b", "g-only", "n-only", "g", "n", "a"};
                if (ssid.length() < 2) {
                    jsonObject9.put("flag", "0");
                    jsonObject9.put("error_info", "ssid too short!");
                } else {
                    jsonObject9.put("flag", "1");
                    jsonObject9.put("error_info", "none");
                    Log.d("Ajax", "request: 1");
                    this.mWifiApController.setSsid(ssid);
                    this.mWifiApController.setWifiMaxSta(maxSta);
                    Log.d("Ajax", "request: 2");
                    MifiConfiguration.getInstance().WIFIssid = ssid;
                    MifiConfiguration.getInstance().WIFImaxSta = maxSta;
                    MifiConfiguration.getInstance().saveToFile();
                }
                resp.getWriter().println(jsonObject9.toString());
                return;
            }
            if (funcNo == 1008) {
                JSONObject jsonObject10 = new JSONObject();
                jsonObject10.put("flag", "1");
                jsonObject10.put("error_info", "none");
                resp.getWriter().println(jsonObject10.toString());
                this.mWifiApController.restartAP();
                return;
            }
            if (funcNo == 1009) {
                JSONObject jsonObject11 = new JSONObject();
                jsonObject11.put("flag", "1");
                jsonObject11.put("error_info", "none");
                JSONObject resultObject6 = new JSONObject();
                resultObject6.put("encryp_type", this.mWifiApController.getWifiEncryptType());
                resultObject6.put("pwd", this.mWifiApController.getWifiApConfiguration().preSharedKey);
                JSONArray jsonArray6 = new JSONArray();
                jsonArray6.put(resultObject6);
                jsonObject11.put("results", jsonArray6);
                resp.getWriter().println(jsonObject11.toString());
                return;
            }
            if (funcNo == 1010) {
                JSONObject jsonObject12 = new JSONObject();
                jsonObject12.put("flag", "1");
                jsonObject12.put("error_info", "none");
                int encryp_type = jo.getInt("encryp_type");
                String pwd = jo.getString("pwd");
                WifiConfiguration conf = this.mWifiApController.getWifiApConfiguration();
                this.mWifiApController.setWifiApConfig(conf.SSID, encryp_type, pwd);
                MifiConfiguration.getInstance().WIFIssid = conf.SSID;
                MifiConfiguration.getInstance().WIFIencrypt = encryp_type;
                MifiConfiguration.getInstance().WIFIpassword = pwd;
                MifiConfiguration.getInstance().saveToFile();
                this.mWifiApController.restartAP();
                resp.getWriter().println(jsonObject12.toString());
                return;
            }
            if (funcNo == 1011) {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("flag", "1");
                jSONObject.put("error_info", "none");
                JSONObject jSONObject2 = new JSONObject();
                jSONObject2.put("ip", this.mWifiApController.getWlanIP());
                jSONObject2.put("dns1", this.mWifiApController.getWlanDNS(1));
                jSONObject2.put("dns2", this.mWifiApController.getWlanDNS(2));
                jSONObject2.put("range_low", this.mWifiApController.getWlanDHCP_Low());
                jSONObject2.put("range_high", this.mWifiApController.getWlanDHCP_High());
                JSONArray clientArray = new JSONArray();
                if (this.mWifiApController.getWifiClients().size() > 0) {
                    for (WifiApController.WifiClient client : this.mWifiApController.getWifiClients()) {
                        Log.d("AJAX", "wifi client: " + client.name + " " + client.ip);
                        JSONObject clientObject = new JSONObject();
                        clientObject.put("name", client.name);
                        clientObject.put("ip", client.ip);
                        clientObject.put("mac", client.mac);
                        clientObject.put("media", client.media);
                        clientArray.put(clientObject);
                    }
                }
                jSONObject2.put("device_arr", clientArray);
                JSONArray jSONArray = new JSONArray();
                jSONArray.put(jSONObject2);
                jSONObject.put("results", jSONArray);
                resp.getWriter().println(jSONObject.toString());
                return;
            }
            if (funcNo == 1012) {
                JSONObject jsonObject13 = new JSONObject();
                jsonObject13.put("flag", "1");
                jsonObject13.put("error_info", "none");
                String dns = (String) jo.get("dns1");
                String dns2 = (String) jo.get("dns2");
                this.mWifiApController.setWlanDns(1, dns);
                this.mWifiApController.setWlanDns(2, dns2);
                resp.getWriter().println(jsonObject13.toString());
                return;
            }
            if (funcNo == 1013) {
                JSONObject jsonObject14 = new JSONObject();
                jsonObject14.put("flag", "1");
                jsonObject14.put("error_info", "none");
                this.mDeviceController.rebootDevice();
                resp.getWriter().println(jsonObject14.toString());
                return;
            }
            if (funcNo == 1014) {
                JSONObject jsonObject15 = new JSONObject();
                jsonObject15.put("flag", "1");
                jsonObject15.put("error_info", "none");
                this.mDeviceController.restoreDevice();
                resp.getWriter().println(jsonObject15.toString());
                return;
            }
            if (funcNo == 1015) {
                JSONObject jsonObject16 = new JSONObject();
                jsonObject16.put("flag", "1");
                jsonObject16.put("error_info", "none");
                JSONObject resultObject7 = new JSONObject();
                resultObject7.put("sim_status", this.mWanDataController.getSIMStatus());
                resultObject7.put("imsi", this.mWanDataController.getIMSI());
                resultObject7.put("iccid", this.mWanDataController.getICCID());
                JSONArray jsonArray7 = new JSONArray();
                jsonArray7.put(resultObject7);
                jsonObject16.put("results", jsonArray7);
                resp.getWriter().println(jsonObject16.toString());
                return;
            }
            if (funcNo == 1016) {
                JSONObject jSONObject3 = new JSONObject();
                jSONObject3.put("flag", "1");
                jSONObject3.put("error_info", "none");
                JSONObject jSONObject4 = new JSONObject();
                int profile_num = this.mWanApnController.getCurrentAPN();
                WanApnController wanApnController = this.mWanApnController;
                if (profile_num <= 1941) {
                    webpronum = 0;
                    profile_num = this.mWanApnController.getDefaultApnId();
                } else {
                    WanApnController wanApnController2 = this.mWanApnController;
                    webpronum = profile_num - 1941;
                }
                jSONObject4.put("profile_num", webpronum);
                WanApnController wanApnController3 = this.mWanApnController;
                if (profile_num <= 1941 && MifiConfiguration.getInstance().WANdefaultAPNSave != profile_num) {
                    MifiConfiguration.getInstance().WANdefaultAPNSave = profile_num;
                    MifiConfiguration.getInstance().saveToFile();
                }
                JSONArray apnsArray = new JSONArray();
                for (WanApnController.APN apn : this.mWanApnController.getAPNList()) {
                    JSONObject apnObject = new JSONObject();
                    apnObject.put("no", apn.id);
                    apnObject.put("name", apn.name);
                    apnObject.put("apn", apn.apn);
                    apnObject.put("user", apn.user);
                    apnObject.put("pwd", apn.password);
                    apnObject.put("auth", apn.authType);
                    apnsArray.put(apnObject);
                }
                jSONObject4.put("info_arr", apnsArray);
                JSONArray jSONArray2 = new JSONArray();
                jSONArray2.put(jSONObject4);
                jSONObject3.put("results", jSONArray2);
                resp.getWriter().println(jSONObject3.toString());
                return;
            }
            if (funcNo == 1017) {
                JSONObject jsonObject17 = new JSONObject();
                jsonObject17.put("flag", "1");
                jsonObject17.put("error_info", "none");
                int no = Integer.parseInt((String) jo.get("no"));
                String name = (String) jo.get("name");
                String apn2 = (String) jo.get("apn");
                String user = (String) jo.get("user");
                String pwd2 = (String) jo.get("pwd");
                int auth = Integer.parseInt((String) jo.get("auth"));
                this.mWanApnController.addAPN2(no, name, apn2, user, pwd2, auth);
                resp.getWriter().println(jsonObject17.toString());
                return;
            }
            if (funcNo == 1018) {
                JSONObject jsonObject18 = new JSONObject();
                jsonObject18.put("flag", "1");
                jsonObject18.put("error_info", "none");
                int profile_num2 = jo.getInt("profile_num");
                this.mWanApnController.setCurrentAPN(profile_num2);
                MifiConfiguration.getInstance().WANdefaultAPN = profile_num2;
                MifiConfiguration.getInstance().saveToFile();
                resp.getWriter().println(jsonObject18.toString());
                return;
            }
            if (funcNo == 1020) {
                JSONObject jsonObject19 = new JSONObject();
                String oldpwd = jo.getString("oldpwd");
                String newpwd = jo.getString("newpwd");
                if (oldpwd.equals(MifiConfiguration.getInstance().loginPassword)) {
                    jsonObject19.put("flag", "1");
                    jsonObject19.put("error_info", "none");
                    MifiConfiguration.getInstance().loginPassword = newpwd;
                    MifiConfiguration.getInstance().saveToFile();
                } else {
                    jsonObject19.put("flag", "0");
                    jsonObject19.put("error_info", "old password error");
                }
                resp.getWriter().println(jsonObject19.toString());
                return;
            }
            if (funcNo == 1021) {
                JSONObject jsonObject20 = new JSONObject();
                jsonObject20.put("flag", "1");
                jsonObject20.put("error_info", "none");
                JSONObject resultObject8 = new JSONObject();
                int enable = Settings.Global.getInt(this.context.getContentResolver(), "adb_enabled");
                resultObject8.put("mode", enable > 0 ? "1" : "0");
                JSONArray jsonArray8 = new JSONArray();
                jsonArray8.put(resultObject8);
                jsonObject20.put("results", jsonArray8);
                resp.getWriter().println(jsonObject20.toString());
                return;
            }
            if (funcNo == 1022) {
                JSONObject jsonObject21 = new JSONObject();
                jsonObject21.put("flag", "1");
                jsonObject21.put("error_info", "none");
                int enable2 = jo.getInt("mode");
                Settings.Global.putInt(this.context.getContentResolver(), "adb_enabled", enable2 > 0 ? 1 : 0);
                resp.getWriter().println(jsonObject21.toString());
                return;
            }
            if (funcNo == 1023) {
                JSONObject jsonObject22 = new JSONObject();
                jsonObject22.put("flag", "1");
                jsonObject22.put("error_info", "none");
                JSONObject resultObject9 = new JSONObject();
                resultObject9.put("up_bytes", this.mWanDataController.getMobileTxBytes() / FileUtils.ONE_KB);
                resultObject9.put("down_bytes", this.mWanDataController.getMobileRxBytes() / FileUtils.ONE_KB);
                JSONArray jsonArray9 = new JSONArray();
                jsonArray9.put(resultObject9);
                jsonObject22.put("results", jsonArray9);
                resp.getWriter().println(jsonObject22.toString());
                return;
            }
            if (funcNo == 1024) {
                JSONObject jsonObject23 = new JSONObject();
                jsonObject23.put("flag", "1");
                jsonObject23.put("error_info", "none");
                this.mDeviceController.rebootDevice();
                resp.getWriter().println(jsonObject23.toString());
                return;
            }
            if (funcNo == 1025) {
                JSONObject jsonObject24 = new JSONObject();
                jsonObject24.put("flag", "1");
                jsonObject24.put("error_info", "none");
                String ssid2 = jo.getString("ssid");
                String pwd3 = jo.getString("pwd");
                this.mWifiApController.getWifiApConfiguration();
                this.mWifiApController.setWifiApConfig(ssid2, 3, pwd3);
                MifiConfiguration.getInstance().WIFIssid = ssid2;
                MifiConfiguration.getInstance().WIFIencrypt = 3;
                MifiConfiguration.getInstance().WIFIpassword = pwd3;
                MifiConfiguration.getInstance().saveToFile();
                this.mWifiApController.restartAP();
                resp.getWriter().println(jsonObject24.toString());
                return;
            }
            if (funcNo == 1026) {
                JSONObject jsonObject25 = new JSONObject();
                jsonObject25.put("flag", "1");
                jsonObject25.put("error_info", "none");
                new JSONObject();
                String dhcpLow = jsonObject25.getString("range_low");
                String dhcpHigh = jsonObject25.getString("range_high");
                this.mWifiApController.setWlanDHCP_Low(dhcpLow);
                this.mWifiApController.setWlanDHCP_High(dhcpHigh);
                resp.getWriter().println(jsonObject25.toString());
                return;
            }
            if (funcNo == 1027) {
                JSONObject jsonObject26 = new JSONObject();
                jsonObject26.put("flag", "1");
                jsonObject26.put("error_info", "none");
                JSONObject resultObject10 = new JSONObject();
                jsonObject26.put("range_low", this.mWifiApController.getWlanDHCP_Low());
                jsonObject26.put("range_high", this.mWifiApController.getWlanDHCP_High());
                JSONArray jsonArray10 = new JSONArray();
                jsonArray10.put(resultObject10);
                jsonObject26.put("results", jsonArray10);
                resp.getWriter().println(jsonObject26.toString());
                return;
            }
            if (funcNo == 1028) {
                JSONObject jSONObject5 = new JSONObject();
                jSONObject5.put("flag", "1");
                jSONObject5.put("error_info", "none");
                JSONObject jSONObject6 = new JSONObject();
                JSONArray clientArray2 = new JSONArray();
                if (this.mWifiApController.getWifiClients().size() > 0) {
                    for (WifiApController.WifiClient client2 : this.mWifiApController.getWifiClients()) {
                        Log.d("AJAX", "wifi client: " + client2.name + " " + client2.ip);
                        JSONObject clientObject2 = new JSONObject();
                        clientObject2.put("ip", client2.ip);
                        clientObject2.put("mac", client2.mac);
                        clientArray2.put(clientObject2);
                    }
                }
                jSONObject6.put("device_arr", clientArray2);
                JSONArray jSONArray3 = new JSONArray();
                jSONArray3.put(jSONObject6);
                jSONObject5.put("results", jSONArray3);
                resp.getWriter().println(jSONObject5.toString());
                return;
            }
            if (funcNo == 1029) {
                JSONObject jsonObject27 = new JSONObject();
                jsonObject27.put("flag", "1");
                jsonObject27.put("error_info", "none");
                JSONObject resultObject11 = new JSONObject();
                resultObject11.put("imei", this.mWanDataController.getIMEI());
                resultObject11.put("manufacture", this.mDeviceController.getMaunufactor());
                resultObject11.put("fwversion", this.mDeviceController.getSWVersion());
                int dbm = this.mWanDataController.getDbm();
                if (dbm != 10000) {
                    resultObject11.put("dbm", " " + dbm + " dBm");
                } else {
                    resultObject11.put("dbm", HttpVersions.HTTP_0_9);
                }
                JSONArray jsonArray11 = new JSONArray();
                jsonArray11.put(resultObject11);
                jsonObject27.put("results", jsonArray11);
                resp.getWriter().println(jsonObject27.toString());
                return;
            }
            if (funcNo == 1052) {
                JSONObject jSONObject7 = new JSONObject();
                jSONObject7.put("flag", "1");
                jSONObject7.put("error_info", "none");
                JSONObject jSONObject8 = new JSONObject();
                WifiApController.MACFILTER macfilter = this.mWifiApController.getMacfilter();
                jSONObject8.put("type", macfilter.type);
                JSONArray macArray = new JSONArray();
                for (int i = 0; i < macfilter.macaddr.length; i++) {
                    JSONObject macObject = new JSONObject();
                    macObject.put("id", i + 1);
                    macObject.put("macaddr", macfilter.macaddr[i]);
                    macArray.put(macObject);
                }
                jSONObject8.put("count", macArray.length());
                jSONObject8.put("info_arr", macArray);
                JSONArray jSONArray4 = new JSONArray();
                jSONArray4.put(jSONObject8);
                jSONObject7.put("results", jSONArray4);
                resp.getWriter().println(jSONObject7.toString());
                return;
            }
            if (funcNo == 1053) {
                JSONObject jsonObject28 = new JSONObject();
                jsonObject28.put("error_info", "none");
                int type = Integer.parseInt((String) jo.get("type"));
                if (this.mWifiApController.setMacfilterType(type)) {
                    jsonObject28.put("flag", "1");
                    this.mifiConfiguration.MACFilterType = type;
                    this.mifiConfiguration.saveToFile();
                } else {
                    jsonObject28.put("flag", "0");
                }
                resp.getWriter().println(jsonObject28.toString());
                return;
            }
            if (funcNo == 1054) {
                JSONObject jsonObject29 = new JSONObject();
                jsonObject29.put("error_info", "none");
                int id = jo.getInt("id") - 1;
                String mac = (String) jo.get("mac");
                if (this.mWifiApController.setMacfilterMac(id, mac)) {
                    jsonObject29.put("flag", "1");
                    this.mifiConfiguration.MACFilterList[id] = mac;
                    this.mifiConfiguration.saveToFile();
                } else {
                    jsonObject29.put("flag", "0");
                }
                resp.getWriter().println(jsonObject29.toString());
                return;
            }
            if (funcNo == 1055) {
                JSONObject jsonObject30 = new JSONObject();
                jsonObject30.put("flag", "1");
                jsonObject30.put("error_info", "none");
                this.mWifiApController.commitMacfilter();
                resp.getWriter().println(jsonObject30.toString());
                return;
            }
            if (funcNo == 2000) {
                JSONObject jsonObject31 = new JSONObject();
                jsonObject31.put("flag", "1");
                jsonObject31.put("error_info", "none");
                this.mDeviceController.rebootBootloader();
                resp.getWriter().println(jsonObject31.toString());
                return;
            }
            if (funcNo == 2001) {
                JSONObject jsonObject32 = new JSONObject();
                jsonObject32.put("flag", "1");
                jsonObject32.put("error_info", "none");
                try {
                    Runtime.getRuntime().exec("setprop persist.sys.usb.config rndis,serial_smd,diag,adb");
                    Runtime.getRuntime().exec("sync");
                    this.mDeviceController.rebootDevice();
                } catch (Exception e) {
                    Log.d("Ajax", "setprop error");
                }
                resp.getWriter().println(jsonObject32.toString());
                return;
            }
            if (funcNo == 2002) {
                JSONObject jsonObject33 = new JSONObject();
                jsonObject33.put("flag", "1");
                jsonObject33.put("error_info", "none");
                String imei = jo.getString("imei");
                if (this.mWanDataController.setImei(imei)) {
                    jsonObject33.put("flag", "1");
                    jsonObject33.put("error_info", "none");
                } else {
                    jsonObject33.put("flag", "0");
                    jsonObject33.put("error_info", "none");
                }
                resp.getWriter().println(jsonObject33.toString());
                return;
            }
            if (funcNo == 3000) {
                MifiConfiguration.getInstance().WANdefaultAPN = 0;
                MifiConfiguration.getInstance().WANdefaultAPNSave = 0;
                MifiConfiguration.getInstance().saveToFile();
                this.mUpdater.doUpdateNoMd5();
                Log.d("Ajax", "������ʼ��");
                return;
            }
            if (funcNo == 3001) {
                File file = new File("/data/update.zip");
                if (file.exists()) {
                    file.delete();
                }
                if (!file.exists()) {
                    JSONObject jsonObject34 = new JSONObject();
                    jsonObject34.put("flag", "1");
                    resp.getWriter().println(jsonObject34.toString());
                    return;
                } else {
                    JSONObject jsonObject35 = new JSONObject();
                    jsonObject35.put("flag", "0");
                    resp.getWriter().println(jsonObject35.toString());
                    return;
                }
            }
            if (funcNo == 3002) {
                JSONObject jsonObject36 = new JSONObject();
                jsonObject36.put("flag", "1");
                jsonObject36.put("error_info", "none");
                JSONObject resultObject12 = new JSONObject();
                resultObject12.put("device_name", this.mDeviceController.getDeviceName());
                resultObject12.put("sw_version", this.mDeviceController.getSWVersion());
                JSONArray jsonArray12 = new JSONArray();
                jsonArray12.put(resultObject12);
                jsonObject36.put("results", jsonArray12);
                resp.getWriter().println(jsonObject36.toString());
            }
        } catch (Exception e2) {
            e2.printStackTrace();
            resp.getWriter().write("flag:0");
        }
    }
}