package com.example.imageownt3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import org.opencv.android.OpenCVLoader;


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

        imgSwitch.setChecked(true);
        textSwitch.setChecked(true);
        speechSwitch.setChecked(false);

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
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
        });
    }
}