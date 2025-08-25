package com.mifiservice.server;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import com.google.protobuf.CodedOutputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.URIUtil;

/* loaded from: classes.dex */
public class JServer {
    private static final String ASSETS_DIR = "jetty2";
    private static final String TAG = "JServer";
    private static Context mContext;
    private int mPort;
    private Server mServer;

    public JServer(int port) {
        this.mPort = port;
    }

    public synchronized void start(Context applicationContext) {
        Log.d(TAG, "JServer start one");
        if (this.mServer == null || !this.mServer.isStarted()) {
            mContext = applicationContext;
            Log.d(TAG, "JServer start two");
            if (this.mServer == null) {
                ServletContextHandler servletHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
                servletHandler.addServlet(new ServletHolder(new AjaxSevlet()), "/ajax");
                extractAssets(applicationContext.getResources().getAssets(), ASSETS_DIR);
                String resourceBase = mContext.getFilesDir().getPath() + URIUtil.SLASH + ASSETS_DIR;
                Log.d(TAG, "JServer start resourceBase=" + resourceBase);
                ResourceHandler resourceHandler = new ResourceHandler();
                resourceHandler.setResourceBase(resourceBase);
                HandlerList handlerList = new HandlerList();
                handlerList.addHandler(servletHandler);
                handlerList.addHandler(resourceHandler);
                this.mServer = new Server(this.mPort);
                this.mServer.setHandler(handlerList);
            }
            Log.d(TAG, "JServer start three");
            try {
                this.mServer.start();
            } catch (Exception e) {
                Log.d(TAG, "JServer start error");
                e.printStackTrace();
            }
        }
    }



    private static void extractAssets(AssetManager am, String assetsDir) {
        InputStream in;
        BufferedOutputStream out;
        Throwable th;
        String[] files = null;
        if (am != null) {
            try {
                files = am.list(assetsDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (files != null && files.length != 0) {
            File extractDir = new File(mContext.getFilesDir().getPath() + "/" + assetsDir);
            if (extractDir.isFile()) {
                extractDir.delete();
                return;
            }
            if (!extractDir.exists()) {
                extractDir.mkdirs();
            }
            for (String file : files) {
                in = null;
                out = null;
                try {
                    in = am.open(assetsDir + "/" + file);
                    BufferedOutputStream out2 = new BufferedOutputStream(new FileOutputStream(new File(extractDir, file)));
                    try {
                        byte[] buffer = new byte[4096];
                        while (true) {
                            int len = in.read(buffer);
                            if (len == -1) {
                                break;
                            }
                            out2.write(buffer, 0, len);
                        }
                        out2.close();
                        in.close();
                        if (out2 != null) {
                            try {
                                out2.close();
                            } catch (IOException e3) {
                            }
                        }
                        if (in != null) {
                            in.close();
                        }
                        Log.d(TAG, "JServer extractAssets six");
                        out = out2;
                    } catch (FileNotFoundException e4) {
                        out = out2;
                        try {
                            Log.d(TAG, "JServer extractAssets not found");
                            extractAssets(am, assetsDir + "/" + file);
                            if (out != null) {
                                try {
                                    out.close();
                                } catch (IOException e5) {
                                }
                            }
                            if (in != null) {
                                in.close();
                            }
                        } catch (Throwable th2) {
                            th = th2;
                        }
                    } catch (IOException e6) {
                        out = out2;
                        e6.printStackTrace();
                        if (out != null) {
                            out.close();
                        }
                        if (in != null) {
                            in.close();
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        out = out2;
                    }
                } catch (FileNotFoundException e7) {
                    Log.d(TAG, "JServer extractAssets not found");
                    extractAssets(am, assetsDir + "/" + file);
                    if (out != null) {
                        // TODO out.close();
                    }
                    if (in != null) {
                        //in.close();
                    }
                } catch (IOException e8) {
                    e8.printStackTrace();
                    if (out != null) {
                        //out.close();
                    }
                    if (in != null) {
                        //in.close();
                    }
                }
            }
            return;
        }
        /*return;
        if (out != null) {
            try {
                out.close();
            } catch (IOException e9) {
                //throw th;
            }
        }
        if (in != null) {
            in.close();
        }
        throw th;
        */
    }

    public synchronized void stop() {
        Log.d(TAG, "JServer stop");
        if (this.mServer != null && !this.mServer.isStopped()) {
            try {
                this.mServer.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized boolean isStarted() {
        Log.d(TAG, "JServer extractAssets isStarted");
        return this.mServer == null ? false : this.mServer.isStarted();
    }
}