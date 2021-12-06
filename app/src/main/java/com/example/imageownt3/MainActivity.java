package com.example.imageownt3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.opencv.android.OpenCVLoader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Locale;


public class MainActivity extends AppCompatActivity
{
    static
    {
        if(OpenCVLoader.initDebug())
            Log.d("MainActivity", "Open CV successfully loaded");
        else
            Log.d("MainActivity", "Open CV error");
    }

    ImageView internetInfo;
    boolean connected;
    private SwitchCompat speechSwitch, textImgSwitch, vibSwitch, speedSwitch, silenceSwitch; //mapSwitch

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //LAYOUT
        Button btnCamera = findViewById(R.id.btnCamera);
        textImgSwitch = findViewById(R.id.textSwitch);
        speechSwitch = findViewById(R.id.speechSwitch);
        internetInfo = findViewById(R.id.internetInfo);
        vibSwitch = findViewById(R.id.vibSwitch);
        speedSwitch = findViewById(R.id.speedSwitch);
        silenceSwitch = findViewById(R.id.silenceSwitch);

        textImgSwitch.setChecked(true);
        speechSwitch.setChecked(false);
        speedSwitch.setChecked(false);
        vibSwitch.setChecked(true);
        silenceSwitch.setChecked(false);

        //TEST
        btnCamera.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(connected)
                {
                    boolean isTextChecked = textImgSwitch.isChecked();
                    boolean isSpeechChecked = speechSwitch.isChecked();
                    boolean isVibChecked = vibSwitch.isChecked();
                    boolean isSpeedChecked = speedSwitch.isChecked();
                    boolean isSilenceChecked = silenceSwitch.isChecked();

                    Intent intent = new Intent(MainActivity.this, DetectorActivity.class);
                    //intent.putExtra("imgOption",isImgChecked);
                    intent.putExtra("textImgOption",isTextChecked);
                    intent.putExtra("speechOption",isSpeechChecked);
                    intent.putExtra("vibrationOption", isVibChecked);
                    intent.putExtra("speedOption", isSpeedChecked);
                    intent.putExtra("silenceOption", isSilenceChecked);
                    //intent.putExtra("mapOption", isMapChecked);
                    startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
                }
                else
                    Toast.makeText(MainActivity.this, "Brak połączenia z Internetem", Toast.LENGTH_SHORT).show();
            }
        });

        internetState(internetInfo);
    }

    private void internetState(ImageView internetInfo)
    {
        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                connected = snapshot.getValue(Boolean.class);

                if (!connected)
                {
                    internetInfo.setVisibility(View.VISIBLE);
                    internetInfo.setImageResource(R.drawable.ic_wifi_off);
                }
                else
                    internetInfo.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.err.println("Listener was cancelled");
            }
        });
    }

    protected void onResume()
    {
        super.onResume();
    }
}