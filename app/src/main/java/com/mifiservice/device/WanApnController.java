package com.mifiservice.device;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.mifiservice.service.MifiService;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.http.HttpVersions;

/* loaded from: classes.dex */
public class WanApnController {
    private static final int APN_INDEX = 2;
    public static final int APN_PRE_MAX_ID = 1941;
    private static final int ID_INDEX = 0;
    private static final int LOCALIZED_NAME_INDEX = 5;
    private static final int NAME_INDEX = 1;
    private static final int RO_INDEX = 4;
    static final String TAG = "WanApnController";
    private static final int TYPES_INDEX = 3;
    private int apncount = 0;
    private ConnectivityManager connectivityManager;
    private Context mContext;
    private TelephonyManager telephonyManager;
    public static final Uri APN_LIST_URI = Uri.parse("content://telephony/carriers");
    private static final Uri PREFERAPN_URI = Uri.parse("content://telephony/carriers/preferapn/0");
    private static final Uri RESTORE_APN_URI = Uri.parse("content://telephony/carriers/restore");
    private static WanApnController instance = null;

    /* loaded from: classes.dex */
    public class APN {
        public String apn;
        public int authType;
        public String id;
        public String mcc;
        public String mnc;
        public String name;
        public String password;
        public String user;

        public APN() {
        }
    }

    public WanApnController(Context context) {
        this.mContext = null;
        this.telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.mContext = context;
    }

    public static WanApnController getInstance() {
        if (instance == null) {
            instance = new WanApnController(MifiService.getContext());
        }
        return instance;
    }

    private String getOperatorNumeric() {
        String mccmnc = this.telephonyManager.getSimOperator();
        if (mccmnc == null) {
            mccmnc = HttpVersions.HTTP_0_9;
        }
        String imsi = this.telephonyManager.getSubscriberId();
        if (imsi != null && imsi.length() >= 6 && imsi.startsWith("50215")) {
            return imsi.substring(0, 6);
        }
        return mccmnc;
    }

    public int getCurrentAPNStr(String id) {
        String where = "_id = " + id;
        String[] projection = {"_id,apn,name,current,user,password,authtype,mcc,mnc"};
        Cursor cr = this.mContext.getContentResolver().query(APN_LIST_URI, projection, where, null, null);
        if (cr == null || !cr.moveToNext()) {
            return -1;
        }
        Log.d(TAG, "getCurrentAPNStr ");
        String apn = cr.getString(cr.getColumnIndexOrThrow("apn"));
        String name = cr.getString(cr.getColumnIndexOrThrow("name"));
        Log.d(TAG, "apn : " + apn);
        Log.d(TAG, "name : " + name);
        cr.close();
        return 0;
    }

    public int getCurrentAPN() {
        String key = null;
        Cursor cursor = this.mContext.getContentResolver().query(PREFERAPN_URI, new String[]{"_id"}, null, null, "name ASC");
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            key = cursor.getString(0);
        }
        cursor.close();
        Log.d(TAG, "getCurrentAPN : apn id " + key);
        getCurrentAPNStr(key);
        if (key == null) {
            return 0;
        }
        return Integer.parseInt(key);
    }

    public int getDefaultApnId() {
        int id = 0;
        String mccmncs = getOperatorNumeric();
        String where = "numeric = \"" + mccmncs + "\" and type <> \"fota\" and (bearer=\"14\" or bearer=\"0\") and carrier_enabled = 1";
        String[] projection = {"_id,apn,name,type"};
        Cursor cr = this.mContext.getContentResolver().query(APN_LIST_URI, projection, where, null, null);
        Log.d(TAG, "where = " + where);
        if (cr != null && cr.moveToNext()) {
            String type = cr.getString(cr.getColumnIndexOrThrow("type"));
            Log.d(TAG, cr.getString(cr.getColumnIndexOrThrow("_id")));
            Log.d(TAG, cr.getString(cr.getColumnIndexOrThrow("apn")));
            Log.d(TAG, type);
            id = Integer.parseInt(cr.getString(cr.getColumnIndexOrThrow("_id")));
        }
        if (cr != null) {
            cr.close();
        }
        return id;
    }

    public APN getDefaultApn() {
        String mccmncs = getOperatorNumeric();
        String where = "numeric = \"" + mccmncs + "\" and type <> \"fota\" and (bearer=\"14\" or bearer=\"0\") and carrier_enabled = 1";
        String[] projection = {"_id,apn,name,type"};
        Cursor cr = this.mContext.getContentResolver().query(APN_LIST_URI, projection, where, null, null);
        APN a = new APN();
        Log.d(TAG, "where = " + where);
        if (cr != null && cr.moveToNext()) {
            a.id = cr.getString(cr.getColumnIndexOrThrow("_id"));
            a.apn = cr.getString(cr.getColumnIndexOrThrow("apn"));
            String type = cr.getString(cr.getColumnIndexOrThrow("type"));
            Log.d(TAG, cr.getString(cr.getColumnIndexOrThrow("_id")));
            Log.d(TAG, cr.getString(cr.getColumnIndexOrThrow("apn")));
            Log.d(TAG, type);
            Integer.parseInt(cr.getString(cr.getColumnIndexOrThrow("_id")));
        }
        if (cr != null) {
            cr.close();
        }
        return a;
    }

    private String getApn(int id) {
        String where = "_id = " + (id + APN_PRE_MAX_ID);
        String[] projection = {"_id,apn,name,current,user,password,authtype,mcc,mnc,numeric"};
        String mccmncs = getOperatorNumeric();
        if (mccmncs == null) {
            return HttpVersions.HTTP_0_9;
        }
        Cursor cr = this.mContext.getContentResolver().query(APN_LIST_URI, projection, where, null, null);
        if (cr != null && cr.moveToNext()) {
            Log.d(TAG, "getApn ");
            String apn = cr.getString(cr.getColumnIndexOrThrow("apn"));
            String name = cr.getString(cr.getColumnIndexOrThrow("name"));
            cr.getString(cr.getColumnIndexOrThrow("user"));
            cr.getString(cr.getColumnIndexOrThrow("password"));
            cr.getInt(cr.getColumnIndexOrThrow("authtype"));
            cr.getString(cr.getColumnIndexOrThrow("numeric"));
            Log.d(TAG, "apn : " + apn);
            Log.d(TAG, "name : " + name);
            cr.close();
            return apn;
        }
        return HttpVersions.HTTP_0_9;
    }

    private void setApnByAt(String apn) {
        String command = "modem_at set APN \"" + apn + "\"";
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
                    sb.toString();
                    return;
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "setApnByAt error: " + apn);
        }
    }

    public void setCurrentAPN(int id) {
        Log.d(TAG, "setCurrentAPN: apn id " + id);
        ContentValues values = new ContentValues();
        if (id > 0) {
            values.put("apn_id", Integer.valueOf(id + APN_PRE_MAX_ID));
            setApnByAt(getApn(id));
        } else {
            APN apn = getDefaultApn();
            values.put("apn_id", apn.id);
            setApnByAt(apn.apn);
        }
        this.mContext.getContentResolver().update(PREFERAPN_URI, values, null, null);
    }

    public List<APN> getAPNList() {
        int i = 0;
        String mccmncs = getOperatorNumeric();
        String[] projection = new String[]{"_id,apn,name,current,user,password,authtype,mcc,mnc"};
        Cursor cr = this.mContext.getContentResolver().query(APN_LIST_URI, projection, "_id > 1941", null, null);
        List<APN> list = new ArrayList();
        while (cr != null && cr.moveToNext() && i < 5) {
            APN a = new APN();
            a.id = cr.getString(cr.getColumnIndex("_id"));
            a.apn = cr.getString(cr.getColumnIndex("apn"));
            a.name = cr.getString(cr.getColumnIndex("name"));
            a.user = cr.getString(cr.getColumnIndex("user"));
            a.password = cr.getString(cr.getColumnIndex("password"));
            a.authType = cr.getInt(cr.getColumnIndex("authtype"));
            a.mcc = cr.getString(cr.getColumnIndex("mcc"));
            a.mnc = cr.getString(cr.getColumnIndex("mnc"));
            Log.d(TAG, a.id + "  " + a.apn + "  " + a.name + "  " + a.user + "  " + a.password + "  " + a.authType + "  " + a.mcc + "  " + a.mnc);
            if (Integer.parseInt(a.id) > APN_PRE_MAX_ID) {
                a.id = String.valueOf(Integer.parseInt(a.id) - 1941);
                list.add(a);
                i++;
            }
        }
        if (cr != null) {
            cr.close();
        }
        Log.d(TAG, "list:  " + i);
        this.apncount = i;
        return list;
    }


    public void addAPN(int id, String name, String apn, String user, String password, int authtype) {
        Log.d(TAG, "addAPN: apn id " + id);
        ContentResolver resolver = this.mContext.getContentResolver();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("apn", apn);
        values.put("user", user);
        values.put("password", password);
        values.put("authtype", Integer.valueOf(authtype));
        values.put("type", "default,supl");
        values.put("carrier_enabled", (Integer) 1);
        Log.d(TAG, "addAPN: apn id 1 " + id);
        String mccmncs = getOperatorNumeric();
        Log.d(TAG, "addAPN: apn id mccmncs: " + mccmncs);
        if (mccmncs != null && mccmncs.length() >= 5) {
            values.put("numeric", mccmncs);
            values.put("mcc", mccmncs.substring(0, 3));
            values.put("mnc", mccmncs.substring(3));
            values.put("current", (Integer) 1);
        }
        Log.d(TAG, "addAPN: update failed, try insert " + values);
        Uri newUri = resolver.insert(APN_LIST_URI, values);
        if (newUri != null) {
            ContentUris.parseId(newUri);
            Log.d(TAG, "addAPN : new uri: " + newUri);
            Log.d(TAG, "addAPN : id: " + ContentUris.parseId(newUri));
        }
    }

    public void addAPN2(int id, String name, String apn, String user, String password, int authtype) {
        Log.d(TAG, "addAPN2: apn id " + id);
        ContentResolver resolver = this.mContext.getContentResolver();
        ContentValues values = new ContentValues();
        Uri mUri = ContentUris.withAppendedId(APN_LIST_URI, id + APN_PRE_MAX_ID);
        values.put("name", name);
        values.put("apn", apn);
        values.put("user", user);
        values.put("password", password);
        values.put("authtype", Integer.valueOf(authtype));
        values.put("type", "default,supl");
        values.put("carrier_enabled", (Integer) 1);
        String mccmncs = getOperatorNumeric();
        if (mccmncs != null) {
            values.put("numeric", mccmncs);
            values.put("mcc", mccmncs.substring(0, 3));
            values.put("mnc", mccmncs.substring(3));
            values.put("current", (Integer) 1);
        }
        if (resolver.update(mUri, values, null, null) <= 0) {
            Log.d(TAG, "addAPN: update failed, try insert " + values);
            Uri newUri = resolver.insert(APN_LIST_URI, values);
            if (newUri != null) {
                ContentUris.parseId(newUri);
                Log.d(TAG, "addAPN : new uri: " + newUri);
                Log.d(TAG, "addAPN : id: " + ContentUris.parseId(newUri));
            }
        }
    }

    public void restoreAPN() {
        ContentResolver resolver = this.mContext.getContentResolver();
        resolver.delete(RESTORE_APN_URI, null, null);
    }
}