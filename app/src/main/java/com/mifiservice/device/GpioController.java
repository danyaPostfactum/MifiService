package com.mifiservice.device;

import neoway.api.NeowayApi;

/* loaded from: classes.dex */
public class GpioController {
    private static GpioController instance = null;
    NeowayApi mNeowayApi;

    public GpioController() {
        this.mNeowayApi = null;
        this.mNeowayApi = NeowayApi.getInstance();
    }

    public static GpioController getInstance() {
        if (instance == null) {
            instance = new GpioController();
        }
        return instance;
    }

    public void configGPIO(int gpio, boolean is_out) {
        this.mNeowayApi.neoGpioSetDir(gpio, is_out ? 1 : 0, 0);
    }

    public int readGPIO(int gpio) {
        return this.mNeowayApi.neoGpioGetValue(gpio);
    }

    public void writeGPIO(int gpio, boolean onoff) {
        this.mNeowayApi.neoGpioSetValue(gpio, onoff ? 1 : 0);
    }
}