package com.example.imageownt3;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
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

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.FpsMeter;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class DetectorActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2, com.example.imageownt3.objectDetector.ImageRecognitionInterface
{
    private Mat mRgba;
    private Mat mGray;
    private CameraBridgeViewBase openCvCamera;
    private String TAG = "DetectorActivity";
    private objectDetector objectDetector;
    TextView textView, textView2;
    ImageView imageView1, imageView2;
    private DatabaseReference imageReference;
    String previousDetection="";
    FirebaseDatabase database;
    boolean speechOption, textImgOption, vibrationOption;
    private TextToSpeech textToSpeech;
    ArrayList<String> labelList = new ArrayList<>();
    int INPUT_SIZE = 416;
    boolean modelError=false;
    Timer timer = new Timer();
    FpsMeter fpsMeter = new FpsMeter();
    ConstraintLayout backgroundLayout;
    Button cameraOption;
    boolean flag=false;
    Vibrator vib;

    private BaseLoaderCallback openCvLoaderCallback =new BaseLoaderCallback(this) //final ?
    {
        @Override
        public void onManagerConnected(int status)
        {
            switch(status)
            {
                case LoaderCallbackInterface
                        .SUCCESS:{
                    Log.i(TAG,"OpenCv successfully loaded");
                    openCvCamera.enableView();
                }
                default:
                {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public DetectorActivity()
    {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //LAYOUT
        setContentView(R.layout.activity_detector2);
        textView = findViewById(R.id.textView);
        imageView1 = findViewById(R.id.imgView);
        textView2 = findViewById(R.id.textView2);
        imageView2 = findViewById(R.id.imgView2);
        backgroundLayout = findViewById(R.id.constraintLayout);
        cameraOption = findViewById(R.id.cameraOption);

        //Service
        vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        openCvCamera = (CameraBridgeViewBase) findViewById(R.id.cameraFrames);

        int MY_PERMISSIONS_REQUEST_CAMERA = 0;
        //check camera permission
        if (ContextCompat.checkSelfPermission(DetectorActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(DetectorActivity.this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        }

        //openCvCamera.enableFpsMeter();  //fps display

        database = FirebaseDatabase.getInstance();
        imageReference = database.getReference("data").child("images");

        //GET Bundles
        Bundle bundle = getIntent().getExtras();
        textImgOption = bundle.getBoolean("textImgOption");
        speechOption = bundle.getBoolean("speechOption");
        vibrationOption = bundle.getBoolean("vibrationOption");

        //SET OPTIONS
        textImgVisibility(textImgOption);
        speechEnable(speechOption);

        //openCvCamera.setVisibility(View.INVISIBLE);
        //openCvCamera.setCvCameraViewListener(this);

        labelList = getLabels(); //get labels from storage
        CreateObjectDetector();

        timerBuffor();

        //Camera mode- camera background
        cameraOption.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(!flag)
                {
                    backgroundLayout.setBackgroundColor(ContextCompat.getColor(DetectorActivity.this, R.color.transparent));
                    cameraOption.setText(R.string.cameraMode2);
                    flag = true;
                }
                else
                {
                    backgroundLayout.setBackgroundResource(R.drawable.background_gradient);
                    cameraOption.setText(R.string.cameraMode);
                    flag = false;
                }
            }
        });

        //timer.cancel();//stop the timer
    }

    private void timerBuffor()
    {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run()
                    {
                        textView.setText("");
                        //imageView1.setColorFilter(R.color.transparent);
                        //imageView1.setImageResource(R.drawable.ic_car);
                    }
                });
                //what you want to do

            }
        }, 0, 100000);//wait 0 ms before doing the action and do it evry 1000ms (1second)
    }

    private void CreateObjectDetector()
    {
        try
        {
            objectDetector = new objectDetector(labelList, getApplicationContext(),INPUT_SIZE, this);
            Log.d("TensorflowMessage", "Successfully loaded model");
        }
        catch (IOException e)
        {
            Log.d("TensorflowMessage", "Error with loading model");
            e.printStackTrace();
        }
    }

    private void internetState()
    {
        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                boolean connected = snapshot.getValue(Boolean.class);

                if (!connected)
                {
                    //imageView.setVisibility(View.VISIBLE);
                    imageView1.setImageResource(R.drawable.ic_wifi_off);
                    imageView2.setImageResource(R.drawable.ic_wifi_off);
                    // Toast.makeText(getApplicationContext(),"Brak Internetu",Toast.LENGTH_SHORT);
                    //ImageRecognitionInterface.onInternetConnection("false");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.err.println("Listener was cancelled");
            }
        });
    }

    private void speechEnable(boolean speechOption)
    {
        if(speechOption)
        {

            textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener()
            {
                @Override
                public void onInit(int status)
                {
                    Locale locale = new Locale("pl_PL");
                    int result = textToSpeech.setLanguage(locale);

                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED)
                    {
                        Log.e("TTS", "This Language is not supported");
                        //Info about instalation process
                        AlertDialog.Builder builder = new AlertDialog.Builder(DetectorActivity.this);
                        builder.setMessage(R.string.speachError).setTitle(R.string.speachErrorTitle);
                        builder.setIcon(R.drawable.ic_language);
                        builder.setPositiveButton(R.string.speachErrorNO, (dialogInterface, i) -> android.os.Process.killProcess(android.os.Process.myPid()));
                        builder.setNegativeButton(R.string.speachErrorYES, (dialogInterface, i) -> {
                            Intent intent = new Intent();
                            intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$LanguageAndInputSettingsActivity"));
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
            });
        }
    }

    private void textImgVisibility(boolean textOption)
    {
        //TEXT OPTION
        if(!textOption)
        {
            textView.setVisibility(View.INVISIBLE);
            textView2.setVisibility(View.INVISIBLE);
            imageView1.setVisibility(View.INVISIBLE);
            imageView2.setVisibility(View.INVISIBLE);
        }
        else
        {
            textView.setVisibility(View.VISIBLE);
            textView2.setVisibility(View.VISIBLE);
            textView.setText("");
            textView2.setText("");
            imageView1.setVisibility(View.VISIBLE);
            imageView2.setVisibility(View.VISIBLE);
            internetState();
        }

    }

    public ArrayList<String> getLabels()
    {
        //load labelmap:
        /*ArrayList<String>*/ //labelList2 = new ArrayList<>();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("labels.txt");

        try
        {
            File localFile = File.createTempFile("labels", "txt");
            File finalLocalFile = localFile;
            storageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot)
                {

                    Log.d("sizeListstorage","skopiowano plik "+ finalLocalFile.getPath());

                    FileReader fileReader = null;
                    try
                    {
                        String path = finalLocalFile.getPath();
                        fileReader = new FileReader(path);

                        BufferedReader bufferedReader = new BufferedReader(fileReader);
                        String buffer;
                        StringBuilder stringBuilder = new StringBuilder();

                        while ((buffer = bufferedReader.readLine()) != null)
                        {
                            stringBuilder.append(buffer);
                            labelList.add(buffer);
                        }

                        Log.d("sizeListActivity",String.valueOf(labelList.size()));

                        //after getting data -> turn on camera
                        openCvCamera.setVisibility(SurfaceView.VISIBLE);
                        openCvCamera.setCvCameraViewListener(DetectorActivity.this);
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                    Log.d("storage","Error "+exception.toString());
                    onModelError("bląd pliku");
                }
            });
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return labelList;
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (OpenCVLoader.initDebug()){
            //if load success
            Log.d(TAG,"Opencv initialization is done");
            openCvLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        else{
            //if not loaded
            Log.d(TAG,"Opencv is not loaded. try again");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0,this,openCvLoaderCallback);
        }
        //timer.cancel();
    }

    @Override
    protected void onPause()
    {
        //labelList = getLabelfromBase(); //get labels from storage

        if(textToSpeech !=null)
        {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        if (openCvCamera != null)
            openCvCamera.disableView();

        timer.cancel();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (openCvCamera != null)
            openCvCamera.disableView();
        timer.cancel();
    }
    @Override
    public void onCameraViewStarted(int width, int height)
    {
        mRgba = new Mat(height, width, CvType.CV_8UC4); //RGB
        mGray = new Mat(height, width, CvType.CV_8UC1);

        fpsMeter.init();
        Log.d("fps",fpsMeter.toString());
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
        mGray = inputFrame.gray();

        if(!modelError) //objectDetector!=null &&
            objectDetector.recognizeImage(mRgba);

        //return out;
        return inputFrame.rgba();
    }

    @Override
    public void onRecognition(String detectedClass)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if (speechOption)
                    speakOnRecognition(detectedClass);

                if (textImgOption)
                    setTextAndImage(detectedClass);

                if(vibrationOption)
                    setVibration(detectedClass);// if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)

                previousDetection = detectedClass;

                Log.d("DetectionList", detectedClass);
                //TimerDet(detectedClass);
            }
        });
    }

    private void setTextAndImage(String detectedClass)
    {
        getImageFromBase(detectedClass, imageView1);
        textView.setText(detectedClass);

        if(!previousDetection.isEmpty() && !previousDetection.equals(detectedClass))
        {
            getImageFromBase(previousDetection, imageView2);
            textView2.setText(previousDetection);
        }
    }

    private void setVibration(String detectedClass)
    {
        if(detectedClass.toLowerCase().contains("zakaz"))
        {
            vib.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        }
    }

    private void speakOnRecognition(String detectedClass)
    {
        if(!previousDetection.isEmpty())
        {
            if(!previousDetection.equals(detectedClass))
                textToSpeech.speak(detectedClass, TextToSpeech.QUEUE_ADD, null);
        }
        else
            textToSpeech.speak(detectedClass, TextToSpeech.QUEUE_ADD, null);
    }

    @Override
    public void onModelError(String errorM)
    {
        Log.d("modelError",errorM);
        modelError = true;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.modelError).setTitle(R.string.errorTitle);
        builder.setPositiveButton(R.string.errorRestart, (dialogInterface, i) -> android.os.Process.killProcess(android.os.Process.myPid()));
        builder.setNegativeButton(R.string.errorOk, (dialogInterface, i) -> {});
        AlertDialog dialog = builder.create();
        dialog.show();
        //textView.setText(modelError);
    }

    /*@Override
    public void gpuDelegate(String gpuInfo)
    {
        //gpuInfoText.setText(gpuInfo);
    }*/

    @Override
    public void onRecognitionTimer(String detection)
    {
    }

    public void getImageFromBase(String detectedClass, ImageView imgView)
    {
        //get Product from base
        FirebaseDatabase database = FirebaseDatabase.getInstance(); //as Global ?
        imageReference = database.getReference("data").child("images");
        imageReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                //imageArrayList.clear();
                for (DataSnapshot ds : snapshot.getChildren())
                {
                    Image img = ds.getValue(Image.class);
                    if(img!=null)
                    {
                        if((img.getName().toLowerCase().contains(detectedClass.toLowerCase())))
                        {
                            Glide.with(getBaseContext()).load(img.getLink()).into(imgView);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                //textView.setText(error.toString());
                Toast.makeText(DetectorActivity.this, "fire" +error.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}