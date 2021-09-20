package com.example.imageownt3;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.modeldownloader.CustomModel;
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions;
import com.google.firebase.ml.modeldownloader.DownloadType;
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class objectDetector
{
    private Interpreter interpreter; //load and predict model
    private List<String> labelList;
    private int INPUT_SIZE;
    private int PIXEL_SIZE = 3 ; //RGB
    private int IMAGE_MEAN=0;
    private float IMAGE_STD=127.5f;
    private onImageRecognition onImageRecognition;
    //    private GpuDelegate gpuDelegate;
    //    private Delegate delegate;

    private int height = 0;
    private int width = 0;
    private Context context;

    objectDetector(AssetManager assetManager, String labelPath, int inputSize, Context context, onImageRecognition onImageRecognition) throws IOException
    {
        this.context = context;
        this.onImageRecognition=onImageRecognition;

        INPUT_SIZE = inputSize;

        //define gpu, cpu
        Interpreter.Options options = new Interpreter.Options();
        //gpuDelegate = new GpuDelegate();

        //options.addDelegate(gpuDelegate);
        options.setNumThreads(4);  //depending on phone

        //loading model:

        //FirebaseApp.initializeApp(getApplicationContext());

        //LOAD MODEL from FireBase
        CustomModelDownloadConditions conditions = new CustomModelDownloadConditions.Builder()
                .requireWifi()  // Also possible: .requireCharging() and .requireDeviceIdle()
                .build();
        FirebaseModelDownloader.getInstance()
                .getModel("signModel2", DownloadType.LOCAL_MODEL_UPDATE_IN_BACKGROUND, conditions)
                .addOnSuccessListener(new OnSuccessListener<CustomModel>() {
                    @Override
                    public void onSuccess(CustomModel model) {
                        // Download complete. Depending on your app, you could enable the ML
                        // feature, or switch from the local model to the remote model, etc.

                        // The CustomModel object contains the local path of the model file,
                        // which you can use to instantiate a TensorFlow Lite interpreter.
                        File modelFile = model.getFile();
                        if (modelFile != null)
                        {
                            //interpreter = new Interpreter(modelFile);
                            interpreter = new Interpreter(modelFile);
                            //interpreter = new Interpreter(loadModelFile(assetManager, modelFile),options);
                            //Toast.makeText(context, "zaladowano model", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        //load labelmap:
        labelList = loadLabelList(assetManager, labelPath);
    }

    private List<String> loadLabelList(AssetManager assetManager, String labelPath) throws IOException
    {
        //save label
        List<String> labelList = new ArrayList<>();

        //new reader
        BufferedReader reader = new BufferedReader(new InputStreamReader(assetManager.open(labelPath)));
        String line;

        //loop through each line and store it to labelList
        while((line = reader.readLine())!=null)
        {
            labelList.add(line);
        }

        reader.close();
        return labelList;
    }

    public Mat recognizeImage(Mat matImage)
    {
        //90°

        Mat rotatedMatImage = new Mat();
        Core.flip(matImage.t(), rotatedMatImage, 1);

        //rotatedMatImage =matImage.t();

        //convert to Bitmap
        Bitmap bitmap = null;

        bitmap = Bitmap.createBitmap(rotatedMatImage.cols(), rotatedMatImage.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(rotatedMatImage,bitmap);

        //define
        height = bitmap.getHeight();
        width = bitmap.getWidth();

        //scale
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);
        //Log.i("After bitmap","Ok");
        //convert bitmap to bytebuffer
        ByteBuffer byteBuffer = convertBitmapToByteBuffer(scaledBitmap);
        //Log.i("After byteBuffer","Ok");

        //output
        //2nd : -
        //3th : coordinates in image
        //float[][][] result = new float[1][4][4];

        Object[] input = new Object[1];
        input[0] = byteBuffer;

        Map<Integer,Object> outputMap = new TreeMap<>();
        float[][][] boxes = new float[1][10][4];
        float[][] scores = new float[1][10];
        float[][] classes = new float[1][10];


//        outputMap.put(0,boxes);
//        outputMap.put(1,classes);
//        outputMap.put(2,scores);


        outputMap.put(1,boxes);
        outputMap.put(3,classes);
        outputMap.put(0,scores);

        //Log.i("znak","score: "+);

        //predict
        interpreter.runForMultipleInputsOutputs(input,outputMap);

//        Object value = outputMap.get(0);
//        Object objectClass = outputMap.get(1);
//        Object score = outputMap.get(2);


        Object value = outputMap.get(1);
        Object objectClass = outputMap.get(3);
        Object score = outputMap.get(0);


        //protect by nulls

        //each object:
        for(int i=0; i<10; i++)
        {
            float classValue = (float) Array.get(Array.get(objectClass,0),i);
            float scoreValue = (float) Array.get(Array.get(score,0),i);

            Log.i("znak","score: "+String.valueOf(scoreValue));

            //THRESHOLD
            if(scoreValue>0.8)
            {
                Object box = Array.get(Array.get(value,0),i);
                float top = (float) Array.get(box,0)*height;
                float left = (float) Array.get(box,1)*width;
                float bottom = (float) Array.get(box,2)*height;
                float right = (float) Array.get(box,3)*width;

                //draw rectangle
                Imgproc.rectangle(rotatedMatImage, new Point(left,top), new Point(right,bottom), new Scalar(255,155,155),4);

                //write text on frame
                //Imgproc.putText(rotatedMatImage,labelList.get((int) classValue), new Point(left,right),2,1,new Scalar(100,100,100),2);

                String text = "";
                text= labelList.get((int) classValue);

                onImageRecognition.onRecognition(text);
                Log.i("znak",labelList.get((int) classValue));
            }

            else
            {
                Log.i("znak","brak");
            }
        }


        //-90°
        Core.flip(rotatedMatImage.t(),matImage,0);

        return matImage;
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap)
    {
        ByteBuffer byteBuffer;
        int quant = 1;
        int sizeImages = INPUT_SIZE;

        // ???
        if(quant==0)
        {
            byteBuffer = ByteBuffer.allocateDirect(sizeImages * sizeImages * 3);
        }
        else
        {
            byteBuffer = ByteBuffer.allocateDirect(sizeImages * sizeImages * 3 * 4);
        }

        byteBuffer.order(ByteOrder.nativeOrder());
        int [] intValues = new int[sizeImages*sizeImages];
        bitmap.getPixels(intValues,0,bitmap.getWidth(),0,0,bitmap.getWidth(), bitmap.getHeight());

        int pixel = 0;

        for(int i=0; i<sizeImages; ++i)
        {
            for (int j=0;j<sizeImages; ++j)
            {
                final int val =  intValues[pixel++];
                if(quant==0)
                {
                    byteBuffer.put((byte) ((val>>16)&0xFF));
                    byteBuffer.put((byte) ((val>>8)&0xFF));
                    byteBuffer.put((byte) (val&0xFF));
                }
                else
                {
                    byteBuffer.putFloat((((val>>16) & 0xFF))/IMAGE_STD);
                    byteBuffer.putFloat((((val>>8) & 0xFF))/IMAGE_STD);
                    byteBuffer.putFloat((((val)&0xFF))/IMAGE_STD);
                }
            }
        }

        return byteBuffer;
    }

    public interface onImageRecognition
    {
        void onRecognition(String detectedClass);
    }

}
