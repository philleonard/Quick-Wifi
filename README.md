Quick-Wifi
==========

#TODO

* Implement asset extraction (Extract Tesseract english training data to users external storage dir)
* Improve UI
* Camera flash and picture mode options
* Bleeding edge and versioned apk download (I won't be putting this app on the Play Store)

#Idea
Use of OCR to automatically connect to a WiFi AP after taking a photo of the underside of the router (that contains SSID and Key information).

The app runs the Tesseract OCR system locally from compiled arm libraries for android: [Android Tesseract](https://github.com/rmtheis/tess-two)

![Alt text](/readme_img/wireless-details.jpg?raw=true "Underside of a typical router")

#How it works
* Quick WiFi uses the android camera library to create it's own camera instance
* The user takes a photo
* The user crops the photo to try and include just the SSID and Key text, using the [android-crop](https://github.com/jdamcd/android-crop) library, in order to improve the accuracy of OCR reading (removal of extraneuos text)
* Quick WiFi then calls [Tesseract](https://github.com/rmtheis/tess-two) to extract the text from the cropped image
* Then the SSID and the Key is extracted from the text
* Quick WiFi calculates the hamming distance of the SSID against the SSID of each currently available WiFi connection (This allows space for error in the SSID but not in the key)
* It then connects the network with the shortest hamming distance using the aquired key.

#Progress
Application works well under controlled conditions (good lighting and a printout of the SSID and Key). 
Back of the router test produces varied results mainly due to the following accuracy depreciation problem: [Tesseract FAQ](https://code.google.com/p/tesseract-ocr/wiki/FAQ#Is_there_a_Minimum_Text_Size?_(It_won't_read_screen_text!))
