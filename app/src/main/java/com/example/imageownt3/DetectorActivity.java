package com.example.imageownt3;


import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.IOException;
import java.util.Locale;

public class DetectorActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2, objectDetector.onImageRecognition
{
    private Mat mRgba;
    private Mat mGray;
    private CameraBridgeViewBase openCvCamera;
    private String TAG = "DetectorActivity";
    private objectDetector objectDetector;
    TextView textView;
    ImageView imageView;
    private DatabaseReference imageReference;
    String classDetectionText="";
    FirebaseDatabase database;
    boolean imgOption, speechOption, textOption;
    private TextToSpeech textToSpeech;

    private BaseLoaderCallback openCvLoaderCallback =new BaseLoaderCallback(this)
    {
        @Override
        public void onManagerConnected(int status)
        {
            switch (status)
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
        //Log.i(TAG, "constructor: " + this.getClass());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        int MY_PERMISSIONS_REQUEST_CAMERA = 0;
        //check camera permission
        if (ContextCompat.checkSelfPermission(DetectorActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(DetectorActivity.this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        }

        //LAYOUT
        setContentView(R.layout.activity_detektor);
        textView = findViewById(R.id.textView);
        imageView = findViewById(R.id.imageView);

        openCvCamera = (CameraBridgeViewBase) findViewById(R.id.cameraFrames);
        openCvCamera.setVisibility(SurfaceView.VISIBLE);
        openCvCamera.setCvCameraViewListener(this);
        //openCvCamera.enableFpsMeter();  //fps display

        database = FirebaseDatabase.getInstance();
        imageReference = database.getReference("data").child("images");


        Bundle bundle = getIntent().getExtras();
        imgOption = bundle.getBoolean("imgOption");
        textOption = bundle.getBoolean("textOption");
        speechOption = bundle.getBoolean("speechOption");

        //TEXT OPTION
        if(!textOption)
            textView.setVisibility(View.INVISIBLE);
        else
            textView.setVisibility(View.VISIBLE);

        //IMG OPTION
        if(!imgOption)
            imageView.setVisibility(View.INVISIBLE);
            //Toast.makeText(this, "option "+String.valueOf(bundle.getBoolean("imgOption")), Toast.LENGTH_SHORT).show();
        else
            imageView.setVisibility(View.VISIBLE);

        if(speechOption)
        {

            textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener()
            {
                @Override
                public void onInit(int status)
                {
                    if(status==TextToSpeech.SUCCESS)
                    {
                        Locale locale = new Locale("pl_PL");
                        //System.out(Locale.getDefault().getDisplayCountry());
                        textToSpeech.setLanguage(locale);

                    }
                    else
                        Toast.makeText(getApplicationContext(), "Error speech", Toast.LENGTH_SHORT).show();
                }

            });
        }

        //input size - for model : 320
        try
        {
            objectDetector = new objectDetector(getAssets(), "label3.txt", 320, getApplicationContext(),this);
            Log.d("TensorflowMessage", "Successfully loaded model");
        }
        catch (IOException e)
        {
            Log.d("TensorflowMessage", "Error with loading model");
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        openCvLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
//        if (!OpenCVLoader.initDebug()) {
//            Log.d(TAG, "OpenCV loaded");
//            openCvLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
//        } else {
//            Log.d(TAG, "OpenCV not loaded");
//            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, openCvLoaderCallback);
//        }
    }

    @Override
    protected void onPause()
    {
        if(textToSpeech !=null){
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        if (openCvCamera != null)
            openCvCamera.disableView();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (openCvCamera != null)
            openCvCamera.disableView();
    }
    @Override
    public void onCameraViewStarted(int width, int height)
    {
        mRgba = new Mat(height, width, CvType.CV_8UC4); //RGB
        mGray = new Mat(height, width, CvType.CV_8UC1);
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

        Mat out =  new Mat();
        out = objectDetector.recognizeImage(mRgba);

        return out;
        //return mRgba;
    }

    @Override
    public void onRecognition(String detectedClass)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run()
            {

                //if(!classDetectionText.isEmpty())
                //{
                   if(!classDetectionText.equals(detectedClass))
                    {
                        if(speechOption)
                            textToSpeech.speak(detectedClass, TextToSpeech.QUEUE_ADD, null);
                    }
                //}

                classDetectionText = detectedClass;

                if(textOption)
                textView.setText(detectedClass);

                if(imgOption)
                    getImageFromBase(detectedClass);
            }
        });
    }

    public void getImageFromBase(String detectedClass)
    {
        //get Product from base
        FirebaseDatabase database = FirebaseDatabase.getInstance();
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
                        //Toast.makeText(CameraActivity.this, "img "+img.getName(), Toast.LENGTH_SHORT).show();
                        //String mark = String.valueOf(textView.getText());

                        if((img.getName().toLowerCase().contains(classDetectionText.toLowerCase())))
                        {
                            Glide.with(getBaseContext()).load(img.getLink()).into(imageView);
                            //imageView.setImageURI(Uri.parse(img.getLink()));
                            //Toast.makeText(CameraActivity.this, "TEXT "+classDetectionText, Toast.LENGTH_SHORT).show();
                        }
//                        else
//                        {
//                            // imageView.setImageResource(R.drawable.ic_car);
//                            //Toast.makeText(CameraActivity.this, "Nie widzi "+img.getName(), Toast.LENGTH_SHORT).show();
//                        }
//                        //imageArrayList.add(img);
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