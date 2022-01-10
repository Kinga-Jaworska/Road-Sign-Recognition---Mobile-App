package com.example.imageownt3;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions;
import com.google.firebase.ml.modeldownloader.DownloadType;
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.GpuDelegate;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class objectDetector
{
    private Interpreter interpreter;
    private final ArrayList<String> labelList;
    final private int INPUT_SIZE;
    public final ImageRecognitionInterface ImageRecognitionInterface;
    private static final int[] OUTPUT_TINY = new int[]{2535, 2535};
    Context context;

    objectDetector(ArrayList<String> arrayList, Context context, int inputSize, ImageRecognitionInterface ImageRecognitionInterface)
    {
        this.ImageRecognitionInterface = ImageRecognitionInterface;
        this.context = context;
        this.labelList = arrayList;
        this.INPUT_SIZE = inputSize;

        try
        {
            Interpreter.Options options = new Interpreter.Options();
            options.setNumThreads(4);
            LoadModel(options);
            Log.d("objectDetector", "Successfully loaded model");
        }
        catch (Exception ex)
        {
            ImageRecognitionInterface.onModelError("Error - interpreter");
            Log.d("objectDetector", ex.toString());
        }
    }

    public void LoadModel(Interpreter.Options options)
    {
        CustomModelDownloadConditions conditions = new CustomModelDownloadConditions.Builder()
                .build();
        FirebaseModelDownloader.getInstance()
                .getModel("signModel", DownloadType.LOCAL_MODEL_UPDATE_IN_BACKGROUND, conditions)
                .addOnSuccessListener(model -> {
                    File modelFile = model.getFile();
                    if (modelFile != null)
                    {
                        interpreter = new Interpreter(modelFile,options);
                        ImageRecognitionInterface.onSuccessInterpreter(true);
                    }
                }).addOnFailureListener(e -> {
                    ImageRecognitionInterface.onLoadModelError(e.toString());
                    ImageRecognitionInterface.onSuccessInterpreter(false);
                });
    }

    public void recognizeSign(Mat matImage)
    {
        try
        {
            Mat rotatedMatImage = new Mat();
            Core.flip(matImage.t(), rotatedMatImage, 1);

            Bitmap bitmap = Bitmap.createBitmap(rotatedMatImage.cols(), rotatedMatImage.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(rotatedMatImage, bitmap);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);
            ByteBuffer byteBuffer = convertBitmapToByteBuffer(scaledBitmap);

            Object[] input = new Object[1];
            input[0] = byteBuffer;

            Map<Integer, Object> outputMap = new HashMap<>();
            outputMap.put(0, new float[1][OUTPUT_TINY[0]][4]);
            outputMap.put(1, new float[1][OUTPUT_TINY[1]][labelList.size()]);
            interpreter.runForMultipleInputsOutputs(input, outputMap);

            int gridWidth = OUTPUT_TINY[0];
            float[][][] output = (float[][][]) outputMap.get(1);

            for (int i = 0; i < gridWidth; i++)
            {
                float maxClass = 0;
                int detectedClass = -1;
                final float[] classes = new float[labelList.size()];
                assert output != null;
                System.arraycopy(output[0][i], 0, classes, 0, labelList.size());

                for (int c = 0; c < labelList.size(); ++c)
                {
                    if (classes[c] > maxClass)
                    {
                        detectedClass = c;
                        maxClass = classes[c];
                    }
                }
                final float score = maxClass;
                if (score >= 0.9)
                    ImageRecognitionInterface.onRecognition(String.valueOf(labelList.get(detectedClass)));
            }
        }
        catch(Exception ex)
        {
            ImageRecognitionInterface.onModelError("Błąd obliczeń");
            ImageRecognitionInterface.onSuccessInterpreter(false);
            Log.d("recognitionError",ex.toString());
        }
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap)
    {
        ByteBuffer byteBuffer;

        byteBuffer = ByteBuffer.allocateDirect(INPUT_SIZE * INPUT_SIZE * 3 * 4);
        byteBuffer.order(ByteOrder.nativeOrder());

        int [] intValues = new int[INPUT_SIZE*INPUT_SIZE];
        bitmap.getPixels(intValues,0,bitmap.getWidth(),0,0,bitmap.getWidth(), bitmap.getHeight());

        int pixel = 0;

        for(int i=0; i<INPUT_SIZE; ++i)
        {
            for (int j=0;j<INPUT_SIZE; ++j)
            {
                final int val =  intValues[pixel++];

                byteBuffer.putFloat((((val>>16) & 0xFF))/255.0f);
                byteBuffer.putFloat((((val>>8) & 0xFF))/255.0f);
                byteBuffer.putFloat((((val)&0xFF))/255.0f);
            }
        }
        return byteBuffer;
    }

    public interface ImageRecognitionInterface
    {
        void onRecognition(String detectedClass);
        void onLoadModelError(String modelError);
        void onModelError(String modelError);
        void onSuccessInterpreter(Boolean isReady);
    }

}
