package com.westcoastlabs.quickwifi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.soundcloud.android.crop.*;

import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;
import android.provider.MediaStore;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class MainActivity extends ActionBarActivity {

    public MainActivity main;
    protected int FOCUS = 0;
    protected int CAPTURE = 1;
    protected int AUTO = 3;

    protected TextView load;
    protected ProgressBar prog;
    protected ImageView capture, grey, flash;
    public String rawText = "";
    protected String ROOT = Environment.getExternalStorageDirectory().getAbsolutePath() + "/QuickWifi/";
    protected String TEMP_IMAGE = ROOT + "tmp.png";
    protected String TEMP_IMAGE_CROPPED = ROOT + "tmp_crop.png";
    protected String TRAINED_DATA = ROOT + "tesseract-ocr";
    protected String ENG_TRAINED = TRAINED_DATA + "/tessdata/eng.traineddata";

    protected boolean taken;
    boolean flashon = false;
    protected int CROP = 2;
    protected static final String PHOTO_TAKEN = "photo_taken";

    Camera mCamera;
    private CameraPreview mPreview;

    public void init() {
        // Create an instance of Camera
        mCamera = getCameraInstance();

        addCameraParams();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);

    }

    public void initDir() {
        //NEEDS TO BE THREADED
        File home = new File(ROOT);
        if (!home.isDirectory()) {
            home.mkdir();
        }

        File f = new File(TRAINED_DATA);
        if (!f.isDirectory()) {
            f.mkdir();
        }

        File g = new File(TRAINED_DATA + "/tessdata");
        if (!g.isDirectory()) {
            g.mkdir();
        }
        AssetManager assetManager = getApplicationContext().getAssets();

        File trained = new File(ENG_TRAINED);
        if (!(trained.isFile() || trained.exists())) {
            try {
                InputStream in = assetManager.open("eng.traineddata");
                OutputStream out = new FileOutputStream(ENG_TRAINED);
                byte[] buffer = new byte[1024];
                int read;
                while((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }

                Log.i("Init", "Trained Data written");
            } catch (Exception e){
                Toast.makeText(getApplicationContext(), "Error writing tesseract data. Can't work without it. Please check storage.", Toast.LENGTH_LONG).show();
                finish();
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        File f = new File(ENG_TRAINED);
        if (!f.isFile()) {
            initDir();
        }
        //ExtractAssets extract = new ExtractAssets();
        //extract.copyFolder(ROOT, getApplicationContext());
        try {
            init();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Failed to initialise camera and preview.", Toast.LENGTH_LONG).show();
            finish();
        }

        grey = (ImageView) findViewById(R.id.imageView2);
        flash = (ImageView) findViewById(R.id.imageView);
        flash.setVisibility(View.VISIBLE);
        flash.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    flashon = !flashon;
                    Camera.Parameters params = mCamera.getParameters();
                    if (flashon) {
                        flash.setImageResource(R.drawable.flash);
                        params.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                    } else {
                        flash.setImageResource(R.drawable.noflash);
                        params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    }
                    mCamera.setParameters(params);
                } catch(Exception e) {}
            }
        });
        prog = (ProgressBar) findViewById(R.id.progressBar1);
        capture = (ImageView) findViewById(R.id.imageView1);
        load = (TextView) findViewById(R.id.textView1);
        load.setTextColor(Color.parseColor("#FF8800"));
        capture.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View arg0) {

                AutoFocusCallback AutoFocusCallBack = new AutoFocusCallback() {

                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        cameraSound(CAPTURE);

                        new TakePicture(mCamera, TEMP_IMAGE, main).execute();
                    }
                };

                cameraSound(AUTO);
                mCamera.autoFocus(AutoFocusCallBack);
                return true;

            }
        });

        capture.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                    capture.setImageResource(R.drawable.capture_press);
                    Log.d("TouchTest", "Touch down");
                }

                else if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                    capture.setImageResource(R.drawable.capture);
                    Log.d("TouchTest", "Touch up");
                    new TakePicture(mCamera, TEMP_IMAGE, main).execute();
                    //mCamera.takePicture(null, null, null);

                    cameraSound(CAPTURE);
                }

                return true;
            }
        });

        capture.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //mCamera.setPreviewCallback(null);
                //new TakePicture(mCamera, mPicture).execute();
                new TakePicture(mCamera, TEMP_IMAGE, main).execute();
                //mCamera.takePicture(null, null, null);

                cameraSound(CAPTURE);
            }
        });
        main = this;
    }

    public void processingView() {
        //grey.setVisibility(View.VISIBLE);
        capture.setVisibility(View.INVISIBLE);
        prog.setVisibility(View.VISIBLE);
        load.setVisibility(View.VISIBLE);
        flash.setVisibility(View.INVISIBLE);
        Animation fadeInAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        fadeInAnimation.setFillAfter(true);

        grey.startAnimation(fadeInAnimation);
    }

    public void addCameraParams() {
        Camera.Parameters params = mCamera.getParameters();

        List<String> focusModes = params.getSupportedFocusModes();

        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_MACRO)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
        } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }

        List<Size> sizes = params.getSupportedPictureSizes();

        params.setPictureSize(sizes.get(0).width, sizes.get(0).height);

        params.setColorEffect(Camera.Parameters.EFFECT_MONO);

        mCamera.setParameters(params);

    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            float x = event.getX();
            float y = event.getY();
            float touchMajor = event.getTouchMajor();
            float touchMinor = event.getTouchMinor();

            Rect touchRect = new Rect((int) (x - touchMajor / 2), (int) (y - touchMinor / 2), (int) (x + touchMajor / 2), (int) (y + touchMinor / 2));

            cameraSound(FOCUS);
            this.submitFocusAreaRect(touchRect);
        }
        return true;
    }

    private void cameraSound(int sound) {
        MediaPlayer mediaPlayer = null;
        if (sound == FOCUS)
            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.auto);
        else if (sound == CAPTURE)
            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.camerashutter);
        else if (sound == AUTO)
            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.auto);
        mediaPlayer.start(); // no need to call prepare(); create() does that for you
    }

    private void submitFocusAreaRect(final Rect touchRect) {
        Camera.Parameters cameraParameters = mCamera.getParameters();

        if (cameraParameters.getMaxNumFocusAreas() == 0) {
            return;
        }

        // Convert from View's width and height to +/- 1000

        Rect focusArea = new Rect();

        focusArea.set(touchRect.left * 2000 / mPreview.getWidth() - 1000,
                touchRect.top * 2000 / mPreview.getHeight() - 1000,
                touchRect.right * 2000 / mPreview.getWidth() - 1000,
                touchRect.bottom * 2000 / mPreview.getHeight() - 1000);

        // Submit focus area to camera

        ArrayList<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
        focusAreas.add(new Camera.Area(focusArea, 1000));

        cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        cameraParameters.setFocusAreas(focusAreas);
        mCamera.setParameters(cameraParameters);

        // Start the autofocus operation

        mCamera.autoFocus(null);
    }

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    private void stopPreviewAndFreeCamera() {

        try {
            if (mCamera != null) {
                // Call stopPreview() to stop updating the preview surface.
                mCamera.stopPreview();

                // Important: Call release() to release the camera for use by other
                // applications. Applications should release the camera immediately
                // during onPause() and re-open() it during onResume()).
                mCamera.release();

                mCamera = null;
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error when closing camera", Toast.LENGTH_LONG).show();
        }
    }

    public void extractAndConnect() {
        new ExtractAndConnect(main, rawText).execute();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        stopPreviewAndFreeCamera();
    }



    @Override
    protected void onPause() {
        super.onPause();
        stopPreviewAndFreeCamera();
    }


    protected void onPhotoTaken() {
        prog.setVisibility(View.VISIBLE);

        load.setText("Saving Image");
        load.setVisibility(View.VISIBLE);

        cropImage();

    }

    @Override
    protected void onResume() {
        try {
            stopPreviewAndFreeCamera();
            init();
        } catch (Exception e) {}
        super.onResume();
    }

    protected void cropImage() {
        Uri inputUri = Uri.parse("file:///" + TEMP_IMAGE);
        Uri outputUri = Uri.parse("file:///" + TEMP_IMAGE_CROPPED);
        new Crop(inputUri).output(outputUri).start(this);
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == Crop.REQUEST_CROP) {
            init();
            mCamera.startPreview();
            load.setText("Extracting Text");
            if (res == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "Retake Image.", Toast.LENGTH_SHORT).show();
                fadeBack();
            } else {
                new GetText(TEMP_IMAGE_CROPPED, "eng", TRAINED_DATA, this).execute();
            }

        }

    }

    public void fadeBack() {
        Animation fadeOutAnimation = AnimationUtils.loadAnimation(main.getApplicationContext(), R.anim.fade_out);
        fadeOutAnimation.setFillAfter(false);
        main.grey.startAnimation(fadeOutAnimation);

        capture.setVisibility(View.VISIBLE);
        flash.setVisibility(View.VISIBLE);
        load.setVisibility(View.INVISIBLE);
        prog.setVisibility(View.INVISIBLE);
        load.setText("");
    }
}
