package com.mifiservice.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/* loaded from: classes.dex */
public class JsonUtil {
    public static JSONObject buildCommonResp(int code, String msg) {
        JSONObject json = new JSONObject();
        json.put("code", (Object) Integer.valueOf(code));
        json.put("msg", (Object) msg);
        return json;
    }

    public static JSONObject bytes2Json(byte[] bs) {
        if (TextUtil.isEmpty(bs)) {
            return null;
        }
        return JSON.parseObject(new String(bs));
    }

    public static JSONArray bytes2JsonArray(byte[] bs) {
        if (TextUtil.isEmpty(bs)) {
            return null;
        }
        return JSON.parseArray(new String(bs));
    }

    public static byte[] json2Bytes(JSONObject jsonObject) {
        return jsonObject == null ? new byte[0] : jsonObject.toJSONString().getBytes();
    }

    public static byte[] json2Bytes(JSONArray jsonArray) {
        return jsonArray == null ? new byte[0] : jsonArray.toJSONString().getBytes();
    }
}