package com.example.imageownt3;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ml.modeldownloader.CustomModel;
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions;
import com.google.firebase.ml.modeldownloader.DownloadType;
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.tensorflow.lite.Delegate;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.GpuDelegate;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class objectDetector
{
    private Interpreter interpreter; //load and predict model
    private ArrayList<String> labelList = new ArrayList<>();
    private int INPUT_SIZE;
//    private int PIXEL_SIZE = 3 ; //RGB
//    private int IMAGE_MEAN=0;
    private float IMAGE_STD=127.5f;
    private ImageRecognitionInterface ImageRecognitionInterface;
    private static final int[] OUTPUT_TINY = new int[]{2535, 2535};
    //    private GpuDelegate gpuDelegate;
    //    private Delegate delegate;
    int height =0 ;
    int width = 0;
    Context context;
    GpuDelegate gpuDelegate;

    objectDetector(ArrayList<String> arrayList, Context context, int inputSize, ImageRecognitionInterface ImageRecognitionInterface) throws IOException
    {
        this.ImageRecognitionInterface = ImageRecognitionInterface;
        this.context = context;
        this.labelList = arrayList;
        Log.d("sizeListobjectDetector",String.valueOf(labelList.size()));

        INPUT_SIZE = inputSize;

        //define gpu/cpu
        Interpreter.Options options = new Interpreter.Options();

        //gpuDelegate = new GpuDelegate();

        // Initialize interpreter with GPU delegate

        //CompatibilityList compatList = new CompatibilityList();

        //GPU - check if exists and set options


        //gpuDelegate = new GpuDelegate();
       // options.addDelegate(gpuDelegate);

        options.setNumThreads(4);
        //ImageRecognitionInterface.gpuDelegate("Gpu");


//        if(compatList.isDelegateSupportedOnThisDevice())
//        {
//            // if the device has a supported GPU, add the GPU delegate
//            //GpuDelegate.Options delegateOptions = compatList.getBestOptionsForThisDevice();
//            gpuDelegate = new GpuDelegate();
//            options.addDelegate(gpuDelegate);
//            options.setNumThreads(4);
//            ImageRecognitionInterface.gpuDelegate("Gpu");
//
//        } else {
//            // if the GPU is not supported, run on 4 threads
//            options.setNumThreads(4);
//            ImageRecognitionInterface.gpuDelegate("NO Gpu");
//            //ImageRecognitionInterface.onModelError("not supported Gpu");
//        }
        //options.addDelegate(gpuDelegate);
        //options.setNumThreads(4);  //depending on phone
        LoadModel(options);
    }

    public void LoadModel(Interpreter.Options options)
    {
        //LOAD MODEL from FireBase
        CustomModelDownloadConditions conditions = new CustomModelDownloadConditions.Builder()
                .requireWifi()  // Also possible: .requireCharging() and .requireDeviceIdle()
                .build();
        FirebaseModelDownloader.getInstance()
                .getModel("signModel", DownloadType.LOCAL_MODEL_UPDATE_IN_BACKGROUND, conditions)
                .addOnSuccessListener(new OnSuccessListener<CustomModel>() {
                    @Override
                    public void onSuccess(CustomModel model)
                    {
                        File modelFile = model.getFile();
                        if (modelFile != null)
                        {
                            interpreter = new Interpreter(modelFile,options);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                ImageRecognitionInterface.onModelError(e.toString());
            }
        });
    }

    public Mat recognizeImage(Mat matImage)
    {
        //90°
        Mat rotatedMatImage = new Mat();
        Core.flip(matImage.t(), rotatedMatImage, 1);


        //convert to Bitmap
        //bitmap = null;
        Bitmap bitmap = Bitmap.createBitmap(rotatedMatImage.cols(), rotatedMatImage.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(rotatedMatImage, bitmap);

        //define
        height = bitmap.getHeight();
        width = bitmap.getWidth();

        //scale
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);
        ByteBuffer byteBuffer = convertBitmapToByteBuffer(scaledBitmap);

        Object[] input = new Object[1];

        input[0] = byteBuffer;

        Map<Integer, Object> outputMap = new TreeMap<>();

        Log.d("sizeList2",String.valueOf(labelList.size()));

        outputMap.put(0, new float[1][OUTPUT_TINY[0]][4]);
        outputMap.put(1, new float[1][OUTPUT_TINY[1]][labelList.size()]); //labels.size()

        interpreter.runForMultipleInputsOutputs(input, outputMap);

        int gridWidth = OUTPUT_TINY[0];
        //float[][][] bboxes = (float[][][]) outputMap.get(0);
        float[][][] out_score = (float[][][]) outputMap.get(1);

        for (int i = 0; i < gridWidth; i++) {
            float maxClass = 0;
            int detectedClass = -1;
            final float[] classes = new float[labelList.size()];

            for (int c = 0; c < labelList.size(); c++)
            {
                classes[c] = out_score[0][i][c];
            }
            for (int c = 0; c < labelList.size(); ++c)
            {
                if (classes[c] > maxClass) {
                    detectedClass = c;
                    maxClass = classes[c];
                }
            }

            final float score = maxClass;
            if (score > 0.85)
            {
                ImageRecognitionInterface.onRecognition(String.valueOf(labelList.get(detectedClass)));
                //ImageRecognitionInterface.onRecognitionTimer(String.valueOf(labelList.get(detectedClass)));
            }
        }

        //-90°
        Core.flip(rotatedMatImage.t(),matImage,0);

        return matImage;
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap)
    {
        ByteBuffer byteBuffer;
        boolean quant = false;
        int sizeImages = INPUT_SIZE;

        if(quant)
        {
            byteBuffer = ByteBuffer.allocateDirect(sizeImages * sizeImages * 3);
        }
        else //false
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
                if(quant)
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

    public interface ImageRecognitionInterface
    {
        void onRecognition(String detectedClass);
        void onModelError(String modelError);
        //void gpuDelegate(String gpuInfo);
        void onRecognitionTimer(String valueOf);
    }

}
