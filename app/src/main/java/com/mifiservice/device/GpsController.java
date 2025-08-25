package com.mifiservice.device;

import android.content.Context;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import com.mifiservice.service.MifiService;
import java.util.Locale;

/* loaded from: classes.dex */
public class GpsController {
    private static final String TAG = "GpsController";
    private static GpsController instance = null;
    private String lastLatitude;
    private String lastLongitude;
    private Context mContext;
    private Geocoder mGeocoder;
    private LocationListener mListener = new LocationListener() { // from class: com.mifiservice.device.GpsController.1
        @Override // android.location.LocationListener
        public void onLocationChanged(Location location) {
            GpsController.this.lastLatitude = Double.toString(location.getLatitude());
            GpsController.this.lastLongitude = Double.toString(location.getLongitude());
        }

        @Override // android.location.LocationListener
        public void onProviderDisabled(String provider) {
        }

        @Override // android.location.LocationListener
        public void onProviderEnabled(String provider) {
        }

        @Override // android.location.LocationListener
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };
    private LocationManager mLocationManager;

    public GpsController(Context context) {
        this.mContext = null;
        this.mContext = context;
        this.mGeocoder = new Geocoder(this.mContext, Locale.getDefault());
        this.mLocationManager = (LocationManager) this.mContext.getSystemService(Context.LOCATION_SERVICE);
    }

    public static GpsController getInstance() {
        if (instance == null) {
            instance = new GpsController(MifiService.getContext());
        }
        return instance;
    }

    public void startGps() {
        this.mLocationManager.requestLocationUpdates("gps", 5000L, 0.0f, this.mListener);
    }

    public void stopGps() {
        this.mLocationManager.removeUpdates(this.mListener);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.mifiservice.device.GpsController$1 */
    /* loaded from: classes.dex */
    public class AnonymousClass1 implements LocationListener {
        AnonymousClass1() {
        }

        @Override // android.location.LocationListener
        public void onLocationChanged(Location location) {
            GpsController.this.lastLatitude = Double.toString(location.getLatitude());
            GpsController.this.lastLongitude = Double.toString(location.getLongitude());
        }

        @Override // android.location.LocationListener
        public void onProviderDisabled(String provider) {
        }

        @Override // android.location.LocationListener
        public void onProviderEnabled(String provider) {
        }

        @Override // android.location.LocationListener
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }

    public void getLocation() {
        Location location = this.mLocationManager.getLastKnownLocation("gps");
        if (location != null) {
            this.lastLatitude = Double.toString(location.getLatitude());
            this.lastLongitude = Double.toString(location.getLongitude());
        }
    }

    public String getLatitude() {
        getLocation();
        return this.lastLatitude;
    }

    public String getLongitude() {
        getLocation();
        return this.lastLongitude;
    }
}