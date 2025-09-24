package com.example.imageownt3;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;

import com.google.firebase.database.core.Constants;

import java.util.ArrayList;
import java.util.Locale;

@RunWith(MockitoJUnitRunner.class)
public class UnitTest
{
    @Mock
    LocationManager mockLocationManager;
    @Mock
    Context mockContext;
    @Mock
    TextToSpeech tts;
    @Mock
    Location location;

    @Before
    public void setup()
    {
        when(mockContext.getSystemService(Context.LOCATION_SERVICE)).thenReturn(mockLocationManager);
    }
    @Test
    public void LocalizeEnabledTest()
    {
        assertFalse(mockLocationManager.isLocationEnabled());
        when(mockLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)).thenReturn(true);
        LocationManager locationManager = (LocationManager) mockContext.getSystemService(Context.LOCATION_SERVICE);
        boolean locationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        assertTrue(locationEnabled);
    }
    @Test
    public void LanguageAvailableTest()
    {
        Locale locale = new Locale("pl_PL");
        int langEnabled = tts.isLanguageAvailable(locale);
        tts.setLanguage(locale);
        assertEquals(0,langEnabled);
    }

    @Test
    public void LocalizeTest()
    {
        assertFalse(mockLocationManager.isLocationEnabled());
        when(mockLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)).thenReturn(true);
        LocationManager locationManager = (LocationManager) mockContext.getSystemService(Context.LOCATION_SERVICE);
        boolean locationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        location = new Location(LocationManager.GPS_PROVIDER);
        location.setLatitude(10.0);
        location.setLongitude(20.0);
        assertTrue(locationEnabled);
        assertNotNull(location.getSpeed());
    }

}