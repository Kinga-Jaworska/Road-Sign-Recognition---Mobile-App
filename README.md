# Mobile application for road signs recognition

<img align="right" width="100" height="100" src="">

## About App
The application serves as a system supporting safe car driving, dedicated to the Android platform. 
Model was trined by efficient Yolov4 algorithm. App uses model in real-time by downloading labels and model file to device thanks to Firebase tools.

---

### Used technologies/languages/envirnoments/tools:
* Model:
  - Yolov4_tiny
  - Darknet
  - LabelImg
  
* Base App:
  - Java (Android)
  - OpenCv
  - Tensorflow Lite
  - Firebase

---

### Functionalities:
- Text and Image information about recognized sign
- Text-to-Speech information about recognized sign (required appropriate language packet)
- Speed Measure (Required permission for location)
- Information about exceeding the speed limit in built-up areas (after turn on speed measure option)
- Silent mode (extra option to mute notification, incoming calls and messages)
- 
---

### View 

![app_sign](https://user-images.githubusercontent.com/67658221/164888428-c0fa8eaa-5e49-4aa5-adae-1b99bfe28b95.png)
