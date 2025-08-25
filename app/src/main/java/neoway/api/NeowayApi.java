package neoway.api;

/* loaded from: classes.dex */
public class NeowayApi {
    private static NeowayApi mNeowayApi;

    public native int SetGPIOState(char c);

    public native int SetLed(int i, int i2, int i3);

    public native int neoGpioGetDir(int i);

    public native int neoGpioGetValue(int i);

    public native int neoGpioSetDir(int i, int i2, int i3);

    public native int neoGpioSetValue(int i, int i2);

    public native int sendChannelCommand(int i, int[] iArr, int[] iArr2, int i2, int i3);

    public native int setDefaultChannelCommand(int i, int[] iArr);

    static {
        System.loadLibrary("neo_gpio");
        mNeowayApi = null;
    }

    private NeowayApi() {
    }

    public static synchronized NeowayApi getInstance() {
        NeowayApi neowayApi;
        synchronized (NeowayApi.class) {
            if (mNeowayApi == null) {
                mNeowayApi = new NeowayApi();
            }
            neowayApi = mNeowayApi;
        }
        return neowayApi;
    }
}