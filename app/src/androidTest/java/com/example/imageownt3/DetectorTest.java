package com.example.imageownt3;

import static androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread;

import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.SystemClock;
import android.os.Vibrator;

import androidx.test.annotation.UiThreadTest;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;


@RunWith(AndroidJUnit4.class)
public class DetectorTest
{
    DetectorActivity detectorActivity;
    LocationManager locationManager;
    Activity[] scenarioActivity = new Activity[1];

    @Rule
    public ActivityScenarioRule<DetectorActivity> detector  = new  ActivityScenarioRule<>(DetectorActivity.class);

    @Before
    public void setUp()
    {
        detector.getScenario().onActivity(activity -> {scenarioActivity[0] = activity;});
        detectorActivity = (DetectorActivity) scenarioActivity[0];
        locationManager = (LocationManager) detectorActivity.getSystemService(Context.LOCATION_SERVICE);
    }

    @Test
    public void silentModeOn()
    {
        detectorActivity.isDetectorReady = false;
        AudioManager audioManager = (AudioManager) detectorActivity.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        detectorActivity.adjustAudio(true);
        assertTrue(audioManager.isStreamMute(AudioManager.STREAM_NOTIFICATION));
    }

    @Test
    public void silentModeOff()
    {
        detectorActivity.isDetectorReady = false;
        AudioManager audioManager = (AudioManager) detectorActivity.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        detectorActivity.adjustAudio(false);
        assertFalse(audioManager.isStreamMute(AudioManager.STREAM_NOTIFICATION));
    }

    @Test(expected = SecurityException.class)
    public void silentModeException()
    {
        detectorActivity.isDetectorReady = false;
        AudioManager audioManager = (AudioManager) detectorActivity.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        detectorActivity.adjustAudio(true);
        assertTrue(audioManager.isStreamMute(AudioManager.STREAM_NOTIFICATION));
    }

    @Test
    public void CreateObjectDetectorTest()
    {
        detectorActivity.getLabels();
        assertNotNull(detectorActivity.getLabels());
        detectorActivity.createObjectDetector(detectorActivity.getLabels());
        assertNotNull(detectorActivity);
    }

    @Test
    public void getTextTest()
    {
        detectorActivity.isDetectorReady=false;
        detectorActivity.detectedClass="Jednokierunkowa";
        detectorActivity.setTextAndImage();
        assertEquals("Jednokierunkowa",detectorActivity.textSign1.getText());
        detectorActivity.previousDetection="Zakaz wjazdu";
        detectorActivity.setTextAndImage();
        assertEquals("Zakaz wjazdu",detectorActivity.textSign2.getText());
        assertEquals("Jednokierunkowa",detectorActivity.textSign1.getText());
    }

    @Test
    public void vibrationCheck()
    {
        detectorActivity.vibrationOption = true;
        detectorActivity.vib = detectorActivity.getSystemService(Vibrator.class);
        assertTrue(detectorActivity.vib.hasVibrator());
        detectorActivity.detectedClass="Zakaz wjazdu";
        detectorActivity.setVibration();
    }
}