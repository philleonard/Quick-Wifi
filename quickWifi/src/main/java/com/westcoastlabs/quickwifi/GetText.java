package com.westcoastlabs.quickwifi;

import java.io.IOException;

import com.googlecode.tesseract.android.TessBaseAPI;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.InputFilter.LengthFilter;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView.FindListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class GetText extends AsyncTask<Void, Void, Void>{
	
	static String bitmapLoc = ""; 
	static String lang = "";
	static String dataLoc = ""; 
	String text = "";
	MainActivity mainActivity = null;
	
	GetText(String bitmapLoc, String lang, String dataLoc, MainActivity mainActivity) {
		this.bitmapLoc = bitmapLoc;
		this.lang = lang;
		this.dataLoc = dataLoc;
		this.mainActivity = mainActivity;
	}
	
	static String LOG_TAG = "ocr_system";

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mainActivity.load.setText("Extracting Text");
    }

    public String ocr() {
		 
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        Bitmap bitmap = BitmapFactory.decodeFile(bitmapLoc, options);
 
        try {
            ExifInterface exif = new ExifInterface(bitmapLoc);
            int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
 
            Log.v(LOG_TAG, "Orient: " + exifOrientation);
 
            int rotate = 0;
            switch (exifOrientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
            }
            
            Log.v(LOG_TAG, "Rotation: " + rotate);
 
            if (rotate != 0) {
 
                // Getting width & height of the given image.
                int w = bitmap.getWidth();
                int h = bitmap.getHeight();
 
                // Setting pre rotate
                Matrix mtx = new Matrix();
                mtx.preRotate(rotate);
 
                // Rotating Bitmap
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
                // tesseract req. ARGB_8888
                bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            }
 
        } catch (IOException e) {
            Log.e(LOG_TAG, "Rotate or coversion failed: " + e.toString());
        }
 
        Log.v(LOG_TAG, "Before baseApi");
 
        TessBaseAPI baseApi = new TessBaseAPI();
        baseApi.setDebug(true);
        baseApi.init(dataLoc, lang);
        baseApi.setImage(bitmap);
        String recognizedText = baseApi.getUTF8Text();
        baseApi.end();
 
        Log.v(LOG_TAG, "OCR Result: " + recognizedText);
 
        // clean up and show
        if (lang.equalsIgnoreCase("eng")) { 
            recognizedText = recognizedText.replaceAll("[^a-zA-Z0-9]+", " ");
        }
        return recognizedText;
    }

	@Override
	protected Void doInBackground(Void... arg0) {
		text = ocr();
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
        super.onPostExecute(result);
		if (text.equals("")) {
            Toast.makeText(mainActivity.getApplicationContext(), "Error: No text found in image", Toast.LENGTH_SHORT).show();
        }
        else {
            mainActivity.load.setText("Finding SSID and Key");
            mainActivity.rawText = text;
            mainActivity.extractAndConnect();
            //mainActivity.mCamera.startPreview();
        }
	}
}
