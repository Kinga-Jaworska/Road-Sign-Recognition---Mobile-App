package com.example.imageownt3;

import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;


public interface ILocationListener extends LocationListener
{
    void onLocationChanged(Location location);
}