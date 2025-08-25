This is a decompiled (using smali2java decompiler) __MifiService.apk__ from chinese __4G modem UZ801__ running Android 4.4.4.

The smali2java compiler is not perfect, there are some bugs. I have fixed critical bugs manually.

This is the __Android Studio__ project. To build the project, had to modify the SDK android.jar to make hidden methods available. Also, the official SDK19 lacks methods (`TelephonyManager:enableDataConnectivity()`, `android.net.wifi.WifiDevice` class etc). I have manually added all these to android.jar classes. Replace your default android.jar in `Android\Sdk\platforms\android-19` folder with the modified one.