package com.example.imageownt3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity
{
    static
    {
        if(OpenCVLoader.initDebug())
            Log.d("MainActivity", "Open CV successfully loaded");
        else
            Log.d("MainActivity", "Open CV error");

    }

    private Button btnCamera;
    ImageView internetInfo;
    boolean connected;
    private SwitchCompat imgSwitch, speechSwitch,textSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //LAYOUT
        btnCamera = findViewById(R.id.btnCamera);
        imgSwitch = findViewById(R.id.imgSwitch);
        textSwitch = findViewById(R.id.textSwitch);
        speechSwitch = findViewById(R.id.speechSwitch);
        internetInfo = findViewById(R.id.internetInfo);

        imgSwitch.setChecked(true);
        textSwitch.setChecked(true);
        speechSwitch.setChecked(false);

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(connected)
                {
                    boolean isImgChecked = imgSwitch.isChecked();
                    boolean isTextChecked = textSwitch.isChecked();
                    boolean isSpeechChecked = speechSwitch.isChecked();

                    Intent intent = new Intent(MainActivity.this, DetectorActivity.class);
                    intent.putExtra("imgOption",isImgChecked);
                    intent.putExtra("textOption",isTextChecked);
                    intent.putExtra("speechOption",isSpeechChecked);
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
}