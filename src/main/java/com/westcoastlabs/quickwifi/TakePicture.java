package com.westcoastlabs.quickwifi;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class TakePicture extends AsyncTask<Void, Void, Void>{
	
	Camera mCamera;
    String TEMP_IMAGE;
    MainActivity main;

	TakePicture(Camera mCamera, String TEMP_IMAGE, MainActivity main) {
		this.mCamera = mCamera;
		this.TEMP_IMAGE = TEMP_IMAGE;
        this.main = main;
	}

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        main.processingView();
    }

    @Override
	protected Void doInBackground(Void... params) {
		// TODO Auto-generated method stub
		mCamera.takePicture(null, null, mPicture);
		return null;
	}

    private PictureCallback mPicture = new PictureCallback() {

        String TAG = "Picture Callback";

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            camera.startPreview();

            File pictureFile = new File(TEMP_IMAGE);
            if (pictureFile == null) {
                Log.d(TAG, "Error creating media file, check storage permissions: ");
                return;
            }

            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
        }

    };

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        //main.mCamera.startPreview();
        main.onPhotoTaken();
    }
}
