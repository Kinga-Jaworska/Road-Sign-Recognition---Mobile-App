package com.example.imageownt3;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class DetectorActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2, objectDetector.ImageRecognitionInterface, ILocationListener
{
    //OpenCV, Image Recognition
    private Mat mRgba;
    private CameraBridgeViewBase openCvCamera;
    int INPUT_SIZE = 416;
    boolean modelError = false;
    Timer timer = new Timer();

    //Layout
    TextView textSign1, textSign2, speedText;
    ImageView imageView1, imageView2;
    ConstraintLayout backgroundLayout;
    Button cameraOption;
    boolean flag = false;

    //Options - selected in prev Activity
    boolean speechOption=false, textImgOption=false, vibrationOption=false, speedOption=false, silenceOption=false;

    //Detector, Base, TTS, vib
    private String TAG = "DetectorActivity";
    private objectDetector objectDetector;

    DatabaseReference signReference;
    String previousDetection = "", detectedClass = "";
    public TextToSpeech textToSpeech;
    ArrayList<String> labelList = new ArrayList<>();
    Boolean isDetectorReady = false;
    Vibrator vib;

    //Speed counting
    LocationManager locationManager;
    GnssStatus.Callback gnssSatatus;
    float currentSpeedFloat;
    Boolean isGPSEnabled = false;

    final private BaseLoaderCallback openCvLoaderCallback = new BaseLoaderCallback(this)
    {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface
                        .SUCCESS: {
                    Log.i(TAG, "OpenCv successfully loaded");
                    openCvCamera.enableView();
                }
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        Bundle bundle = getIntent().getExtras();
        if(bundle!=null)
        {
            textImgOption = bundle.getBoolean("textImgOption");
            speechOption = bundle.getBoolean("speechOption");
            vibrationOption = bundle.getBoolean("vibrationOption");
            speedOption = bundle.getBoolean("speedOption");
            silenceOption = bundle.getBoolean("silenceOption");
        }


        adjustAudio(silenceOption);


        setContentView(R.layout.activity_detector);

        textSign1 = findViewById(R.id.textView);
        imageView1 = findViewById(R.id.imgView);
        textSign2 = findViewById(R.id.textView2);
        imageView2 = findViewById(R.id.imgView2);
        backgroundLayout = findViewById(R.id.constraintLayout);
        cameraOption = findViewById(R.id.cameraOption);
        speedText = findViewById(R.id.speedText);


        openCvCamera = findViewById(R.id.cameraFrames);




        speedEnabled();
        textImgVisibility(textImgOption);
        speechEnable(speechOption);


        if (vibrationOption)
            vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        labelList = getLabels();
        Log.d("labels", String.valueOf(labelList.size()));


        if(createObjectDetector(labelList)!=null)
            this.onSuccessInterpreter(isDetectorReady);

        timerBuffer();


        cameraOption.setOnClickListener(v -> {
            if (!flag) {
                backgroundLayout.setBackgroundColor(ContextCompat.getColor(DetectorActivity.this, R.color.transparent));
                cameraOption.setText(R.string.cameraMode2);
                flag = true;
            }
            else
            {
                backgroundLayout.setBackgroundResource(R.drawable.background_gradient);
                cameraOption.setText(R.string.cameraMode1);
                flag = false;
            }
        });
    }

    public objectDetector createObjectDetector(ArrayList<String> labelList)
    {
        objectDetector = new objectDetector(labelList, getApplicationContext(), INPUT_SIZE, this);
        return objectDetector;
    }

    private void timerBuffer()
    {
        timer = new Timer();
        timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                previousDetection = detectedClass;
                detectedClass = "";
                onRecognition(detectedClass);
            }
        }, 0, 10000);
    }




    private void speechEnable(boolean speechOption)
    {
        if (speechOption)
        {
            try
            {
                textToSpeech = new TextToSpeech(DetectorActivity.this, status ->
                {
                    if (status == TextToSpeech.SUCCESS)
                    {
                        Locale locale = new Locale("pl_PL");
                        int result = textToSpeech.setLanguage(locale);
                        Log.d("OnInitListener", "Text to speech engine started successfully.");
                        checkTTSLanguage(result, locale);
                    }
                    else
                    {
                        displayAlertDialog(R.string.TTSError,
                                R.string.errorTitle,
                                R.string.errorRestart, true);
                    }
                });
            } catch (Exception exception)
            {
                Toast.makeText(this, "error " + exception.toString(), Toast.LENGTH_SHORT).show();
                Log.d("speechError", exception.toString());
            }
        }
    }

    public void checkTTSLanguage(int result, Locale locale)
    {
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
        {
            Log.e("speechError", "This Language is not supported");

            AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.CustomAlertDialog);
            builder.setCancelable(false);
            builder.setMessage(R.string.speachError)
                    .setTitle(R.string.speachErrorTitle);
            builder.setIcon(R.drawable.ic_language);
            builder.setPositiveButton(R.string.speachErrorNO, (dialogInterface, i) ->
                    android.os.Process.killProcess(android.os.Process.myPid()));
            builder.setNegativeButton(R.string.speachErrorYES, (dialogInterface, i) -> {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.android.settings",
                        "com.android.settings.Settings$LanguageAndInputSettingsActivity"));
                startActivity(intent);
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else
        {
            textToSpeech.setLanguage(locale);
        }
    }








    public ArrayList<String> getLabels()
    {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("labels.txt");
        try
        {
            File finalLocalFile = File.createTempFile("labels", "txt");
            storageReference.getFile(finalLocalFile).addOnSuccessListener(taskSnapshot -> {
                try
                {
                    FileReader fileReader = new FileReader(finalLocalFile.getPath());
                    BufferedReader bufferedReader = new BufferedReader(fileReader);
                    String buffer;

                    while ((buffer = bufferedReader.readLine()) != null)
                        labelList.add(buffer);

                    openCvCamera.setVisibility(SurfaceView.VISIBLE);
                    openCvCamera.setCvCameraViewListener(DetectorActivity.this);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).addOnFailureListener(exception ->
            {
                Log.d("storage", "Error " + exception.toString());
                this.onLoadModelError("Błąd pliku z etykietami");
            });
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return labelList;
    }




    private void textImgVisibility(boolean textOption)
    {
        //TEXT OPTION
        if (!textOption) {
            textSign1.setVisibility(View.INVISIBLE);
            textSign2.setVisibility(View.INVISIBLE);
            imageView1.setVisibility(View.INVISIBLE);
            imageView2.setVisibility(View.INVISIBLE);
        } else {
            textSign1.setVisibility(View.VISIBLE);
            textSign2.setVisibility(View.VISIBLE);
            textSign1.setText("");
            textSign2.setText("");
            imageView1.setVisibility(View.VISIBLE);
            imageView2.setVisibility(View.VISIBLE);
        }
    }


    @Override
    protected void onResume()
    {
        super.onResume();
        if (OpenCVLoader.initDebug())
        {
            Log.d(TAG, "Opencv initialization is done");
            openCvLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        else
        {
            Log.d(TAG, "Opencv is not loaded. try again");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, openCvLoaderCallback);
        }
        adjustAudio(silenceOption);
    }

    @Override
    protected void onPause()
    {
        adjustAudio(false);
        timer.cancel();
        if (textToSpeech != null)
        {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        if (openCvCamera != null)
            openCvCamera.disableView();

        super.onPause();
    }

    @Override
    protected void onDestroy()
    {
        adjustAudio(false);
        timer.cancel();
        super.onDestroy();
        if (openCvCamera != null)
            openCvCamera.disableView();
    }

    @Override
    protected void onStop()
    {
        if(speedOption)
        {
            locationManager.removeUpdates(this);
            locationManager.unregisterGnssStatusCallback(gnssSatatus);
        }

        adjustAudio(false);
        super.onStop();
    }

    @Override
    protected void onStart()
    {
        adjustAudio(silenceOption);
        super.onStart();
    }
    @Override
    public void onCameraViewStarted(int width, int height)
    {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
    }
    @Override
    public void onCameraViewStopped()
    {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame)
    {
        mRgba = inputFrame.rgba();

        if(isDetectorReady)
            objectDetector.recognizeSign(mRgba);

        return mRgba;
    }

    @Override
    public void onRecognition(String currentDetectedClass)
    {
        runOnUiThread(() -> {
            detectedClass = currentDetectedClass;

            if (textImgOption)
                setTextAndImage();

            if (speechOption)
                speakOnRecognition();

            if(vibrationOption)
                setVibration();

            if(speedOption)
                checkSpeedLimit(currentSpeedFloat);

            previousDetection = detectedClass;

            Log.d("DetectionList", detectedClass);
        });
    }

    public void setTextAndImage()
    {
        textSign1.setText(detectedClass);
        getImageFromBase(detectedClass, imageView1);

        if(!previousDetection.isEmpty() && !previousDetection.equals(detectedClass))
        {
            textSign2.setText(previousDetection);
            getImageFromBase(previousDetection, imageView2);
        }
    }

    public void setVibration()
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        signReference = database.getReference("data").child("sign");
        signReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                for (DataSnapshot ds : snapshot.getChildren())
                {
                    Sign sign = ds.getValue(Sign.class);
                    if(sign!=null)
                    {
                        if((sign.getName().equalsIgnoreCase(detectedClass)) && sign.getType()!=null)
                        {
                            if(sign.getType().equals("zakaz"))
                                vib.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                Toast.makeText(DetectorActivity.this,
                        "Bład pobierania pobierania typu znaku" +error.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void speakOnRecognition()
    {
        if(!previousDetection.isEmpty())
        {
            if(!previousDetection.equals(detectedClass))
                textToSpeech.speak(detectedClass, TextToSpeech.QUEUE_ADD, null, null);
        }
        else
            textToSpeech.speak(detectedClass, TextToSpeech.QUEUE_ADD, null, null);
    }

    @Override
    public void onLoadModelError(String errorM)
    {
        Log.d("modelError",errorM);
        modelError = true;
        displayAlertDialog(R.string.modelError,R.string.errorTitle,R.string.errorRestart,false);
    }

    @Override
    public void onModelError(String errorM)
    {
        Log.d("modelError",errorM);
        modelError = true;
        runOnUiThread(() -> displayAlertDialog(R.string.countingModelError,R.string.errorTitle,R.string.errorRestart,false));

       openCvCamera.disableView();
       openCvCamera.setVisibility(SurfaceView.INVISIBLE);
    }

    @Override
    public void onSuccessInterpreter(Boolean isReady)
    {
        isDetectorReady = isReady;
        if(isReady)
            Toast.makeText(getApplicationContext(), "Detektor jest gotowy", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getApplicationContext(), "Przygotowywanie detektora", Toast.LENGTH_SHORT).show();
    }






    private void getImageFromBase(String detectionLabel, ImageView imgView)
    {
        if(detectionLabel.isEmpty())
            imgView.setImageResource(android.R.color.transparent);
        else
        {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            signReference = database.getReference("data").child("sign");
            signReference.addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot)
                {
                    for (DataSnapshot ds : snapshot.getChildren())
                    {
                        Sign sign = ds.getValue(Sign.class);
                        if(sign!=null)
                        {
                            if((sign.getName().equalsIgnoreCase(detectionLabel)))
                            {
                                Glide.with(getBaseContext()).load(sign.getLink()).into(imgView);
                            }
                        }
                        else
                            imgView.setImageResource(android.R.color.transparent);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error)
                {
                    Toast.makeText(DetectorActivity.this, "Bład pobierania obrazu: "
                            +error.toString(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    public void displayAlertDialog(int message, int title, int positiveBtn, boolean isNegativeBtn)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.CustomAlertDialog);
        builder.setCancelable(isNegativeBtn);
        builder.setMessage(message).setTitle(title);
        builder.setPositiveButton(positiveBtn, (dialogInterface, i) -> android.os.Process.killProcess(android.os.Process.myPid()));
        if(isNegativeBtn)
        {
            builder.setNegativeButton(R.string.errorOk, (dialogInterface, i) -> {});
        }
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    public void speedEnabled()
    {
        if(speedOption)
        {
            speedText.setVisibility(View.VISIBLE);
            speedService();
        }
        else
            speedText.setVisibility(View.INVISIBLE);
    }
    private void speedService()
    {
        try
        {
            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this, new String[]
                        {android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if(isGPSEnabled)
            {
                gnssSatatus = new GnssStatus.Callback()
                {
                    @Override
                    public void onSatelliteStatusChanged(@NonNull GnssStatus status)
                    {
                        super.onSatelliteStatusChanged(status);
                    }
                };
                locationManager.registerGnssStatusCallback(gnssSatatus);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                this.updateSpeed(null);
            }
            else
                displayAlertDialog(R.string.localizationError,R.string.errorLocalization,R.string.errorRestart,true);
        }
        catch (Exception e)
        {
            displayAlertDialog(R.string.localizationNotGranted,R.string.errorLocalization,R.string.errorRestart,true);
            Log.d("LocErr",e.toString());
        }
    }
    public void updateSpeed(CustomLocation location)
    {
        currentSpeedFloat = 0;
        if(location != null)
            currentSpeedFloat = location.getSpeed();

        Formatter format = new Formatter(new StringBuilder());
        format.format(Locale.US, "%.00f", currentSpeedFloat);
        String currentSpeedStr = format.toString();
        currentSpeedStr = currentSpeedStr.replace(' ', '0');

        String strUnits = "km/h";
        speedText.setText(String.format("%s %s", currentSpeedStr, strUnits));
    }

    private void checkSpeedLimit(float currentSpeedFloat)
    {
        if(detectedClass.toLowerCase().contains("teren zabudowany") && currentSpeedFloat>50)
        {
            speedText.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.alertSpeed));
            speedText.setTextSize(25);
        }
        else
        {
            speedText.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
            speedText.setTextSize(20);
        }
    }

    @Override
    public void onLocationChanged(Location location)
    {
        if(location != null)
        {
            CustomLocation myLocation = new CustomLocation(location);
            this.updateSpeed(myLocation);
        }
    }

    public void adjustAudio(boolean setMute)
    {
        AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        if (setMute && audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL)
        {
            try
            {
                audioManager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE, 0);
                audioManager.adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_MUTE, 0);
                audioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_MUTE, 0);
                audioManager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_MUTE, 0);
            }
            catch (Exception ex)
            {
                Log.d("silentMode","Error programmatically enable silent mode "+ex);
            }
        }
        else if(setMute && audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT)
        {
            displayAlertDialog(R.string.silentModeInf,R.string.silentModeTitle,R.string.errorRestart,true);
        }
        else
        {
            try
            {
                audioManager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_UNMUTE, 0);
                audioManager.adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_UNMUTE, 0);
                audioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_UNMUTE, 0);
                audioManager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_UNMUTE, 0);
            }
            catch (Exception ex)
            {
                Log.d("silentMode","Error programmatically disable silent mode "+ex);
            }

        }
    }
}