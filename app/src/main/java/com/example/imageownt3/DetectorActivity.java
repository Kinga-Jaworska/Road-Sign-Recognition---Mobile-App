package com.example.imageownt3;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
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
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class DetectorActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2, com.example.imageownt3.objectDetector.ImageRecognitionInterface
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
    ArrayList<String> labelList = new ArrayList<>();
    int INPUT_SIZE = 416;
    boolean modelError=false;

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

//        openCvCamera.setVisibility(SurfaceView.VISIBLE);
//        openCvCamera.setCvCameraViewListener(this);
        //openCvCamera.enableFpsMeter();  //fps display

        database = FirebaseDatabase.getInstance();
        imageReference = database.getReference("data").child("images");

        //getLabelfromBase(); //get labels from storage

        //Log.d("sizeListActivity",String.valueOf(labelList.size()));

        Bundle bundle = getIntent().getExtras();
        imgOption = bundle.getBoolean("imgOption");
        textOption = bundle.getBoolean("textOption");
        speechOption = bundle.getBoolean("speechOption");

        //SET OPTIONS
        textVisibility(textOption);
        imgVisibility(imgOption);
        speechEnable(speechOption);


        labelList = getLabels(); //get labels from storage

        CreateObjectDetector();
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
    private void internetState(ImageView imageView)
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
                    imageView.setImageResource(R.drawable.ic_wifi_off);
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
                    if(status==TextToSpeech.SUCCESS)
                    {
                        Locale locale = new Locale("pl_PL");
                        textToSpeech.setLanguage(locale);
                    }
                    else
                        Toast.makeText(getApplicationContext(), "Error speech", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void imgVisibility(boolean imgOption)
    {
        //IMG OPTION
        if(!imgOption)
            imageView.setVisibility(View.INVISIBLE);
        else
        {
            imageView.setVisibility(View.VISIBLE);
            internetState(imageView);
        }

    }

    private void textVisibility(boolean textOption)
    {
        //TEXT OPTION
        if(!textOption)
            textView.setVisibility(View.INVISIBLE);
        else
            textView.setVisibility(View.VISIBLE);
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
                    try {
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

                    } catch (IOException e) {
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
        openCvLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
    }

    @Override
    protected void onPause()
    {
        //labelList = getLabelfromBase(); //get labels from storage

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

        if(!modelError) //objectDetector!=null &&
            objectDetector.recognizeImage(mRgba);

        Log.d("sizeList","Camera on ");

        //return out;
        return mRgba;
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

    public void getImageFromBase(String detectedClass)
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
                        //Toast.makeText(CameraActivity.this, "img "+img.getName(), Toast.LENGTH_SHORT).show();
                        //String mark = String.valueOf(textView.getText());

                        if((img.getName().toLowerCase().contains(classDetectionText.toLowerCase())))
                        {
                            Glide.with(getBaseContext()).load(img.getLink()).into(imageView);
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