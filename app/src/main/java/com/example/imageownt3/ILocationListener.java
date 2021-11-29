package com.example.imageownt3;

import android.location.GnssStatus;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

public interface ILocationListener extends LocationListener//, GnssStatus.Callback
{

    void onLocationChanged(Location location);

    /**void onProviderDisabled(String provider);

    void onProviderEnabled(String provider);

    void onStatusChanged(String provider, int status, Bundle extras);/*/

    //public void onGpsStatusChanged(int event);

}