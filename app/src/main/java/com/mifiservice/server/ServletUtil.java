package com.mifiservice.server;

import android.util.Log;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;

/* loaded from: classes.dex */
public class ServletUtil {
    public static final String LS = System.getProperty("line.separator");

    public static void responseHtml(HttpServletResponse resp, String title, String message) throws IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html");
        StringBuilder sb = new StringBuilder();
        sb.append("<html>").append(LS);
        sb.append("<head><title>").append(title).append("</title></head>").append(LS);
        sb.append("<body><h1>").append(title).append("</h1>").append(LS);
        sb.append("<p>").append(message).append("</p>").append(LS);
        sb.append("</body></html>").append(LS);
        Log.d("ServletUtil", "responseHtml=" + sb.toString());
        resp.getWriter().println(sb.toString());
    }

    public static void responseJson(HttpServletResponse resp, String title, String message) throws IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/json");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("flag", "1");
            jsonObject.put("age", 22);
        } catch (Exception e) {
        }
        Log.d("ServletUtil", "jsonObject=" + jsonObject.toString());
        resp.getWriter().println(jsonObject.toString());
    }

    private ServletUtil() {
    }
}