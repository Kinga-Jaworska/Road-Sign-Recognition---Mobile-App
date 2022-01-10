package com.example.imageownt3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.ArrayList;
import java.util.Locale;

@ExtendWith(MockitoExtension.class)
class DetectorActivityTest
{
    @Mock
    private ArrayList<String> labelList;
    @Mock
    DetectorActivity detectorActivity = mock(DetectorActivity.class);

    @Test
    void getLabelsTest()
    {
        labelList = detectorActivity.getLabels();
        assertNotNull(labelList);
    }
    @Test
    void onRecognitionTest()
    {
        String currentClass="";
        assertNotNull(currentClass);
        detectorActivity.onRecognition(currentClass);
    }

    /*@Test
    void speedServiceTest()
    {
        DetectorActivity detectorActivity = mock(DetectorActivity.class);
        detectorActivity.speechEnable(true);
        assertNotNull(detectorActivity.textToSpeech);
    }*/

    @Test
    void checkTTSLanguageTest()
    {

        DetectorActivity detectorActivity = mock(DetectorActivity.class);
        Locale locale = new Locale("pl_PL");
        int result = 0;
        detectorActivity.checkTTSLanguage(result, locale);
    }

    /*@Test
    void speedServiceTest()
    {

    }*/

    @Test
    void createObjectDetectorTest()
    {
        labelList.add("");
        assertNotNull(labelList);
        //detectorActivity.createObjectDetector(labelList);
    }
}