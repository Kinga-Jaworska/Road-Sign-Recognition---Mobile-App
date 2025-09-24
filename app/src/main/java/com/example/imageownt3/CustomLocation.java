package com.example.imageownt3;
import android.location.Location;

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
        return super.getSpeed() * 3.6f;
    }
}

