Quick-Wifi
==========

##TODO

* <s>Implement asset extraction and make extarction threaded.</s>
* Improve UI
* <s>Camera flash</s> and picture mode options
* Intro screen with app information.
* <s>WiFi connection status feedback</s>
* <s>Bleeding edge and versioned apk download</s> (I won't be putting this app on the Play Store)


##Usage/Download
You have the following options;
* Clone repository and compile using gradle
* [Bleeding edge download](https://dl.dropboxusercontent.com/s/gqxf060x4z9q4su/quickWifi-debug.apk?dl=1)
* [Version 1.0 download](https://dl.dropboxusercontent.com/s/guaxjn7gnbk45ho/QuickWifi.apk?dl=1)

##Aim
Novelty application of OCR (Optical Character Recognition)

##Idea
Use of OCR to automatically connect to a WiFi AP after taking a photo of the underside of the router (that contains SSID and Key information).

The app runs the Tesseract OCR system locally from compiled arm libraries for android: [Android Tesseract](https://github.com/rmtheis/tess-two)

![Alt text](/readmeimg/wireless-details.jpg?raw=true "Underside of a typical router")

##How it works
* Quick WiFi uses the android camera library to create it's own camera instance
* The user takes a photo
* The user crops the photo to try and include just the SSID and Key text, using the [android-crop](https://github.com/jdamcd/android-crop) library, in order to improve the accuracy of OCR reading (removal of extraneuos text)
* Quick WiFi then calls [Tesseract](https://github.com/rmtheis/tess-two) to extract the text from the cropped image
* Then the SSID and the Key is extracted from the text
* Quick WiFi calculates the hamming distance of the SSID against the SSID of each currently available WiFi connection (This allows space for error in the SSID but not in the key)
* It then connects the network with the shortest hamming distance using the aquired key.

##Progress
Application works well under controlled conditions (good lighting and a printout of the SSID and Key). 
Back of the router test produces varied results mainly due to the following accuracy depreciation problem: [Tesseract FAQ](https://code.google.com/p/tesseract-ocr/wiki/FAQ#Is_there_a_Minimum_Text_Size?_(It_won't_read_screen_text!))

##Screenshots of Progress
Capturing an image...
![Alt text](/readmeimg/photo.png?raw=true "Underside of a typical router")
Cropping image..
![Alt text](/readmeimg/crop.png?raw=true "Underside of a typical router")
Creating a configured WiFi connection using SSID and Key and adding it to the set of configured networks. Android will then handle connecting to it on it's own. 
![Alt text](/readmeimg/connect.png?raw=true "Connecting")
