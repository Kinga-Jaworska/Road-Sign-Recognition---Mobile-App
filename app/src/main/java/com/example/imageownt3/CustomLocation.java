package com.example.imageownt3;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.util.List;

public class CustomLocation extends Location
{
    public CustomLocation(Location location)
    {
        super(location);
    }
    @Override
    public float distanceTo(Location dest)
    {
        return super.distanceTo(dest);
    }
    @Override
    public float getAccuracy()
    {
        return super.getAccuracy();
    }
    @Override
    public double getAltitude()
    {
        return super.getAltitude();
    }
    @Override
    public float getSpeed()
    {
        return super.getSpeed() * 3.6f; //converted to km/h
    }
}