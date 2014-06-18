package com.westcoastlabs.quickwifi;

import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by ppl on 15/06/14.
 */
public class Init extends AsyncTask<Void, Void, Void> {

    MainActivity main;

    Init(MainActivity main) {
        this.main = main;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        main.load.setText("Initialising application");
        main.load.setVisibility(View.VISIBLE);
        main.prog.setVisibility(View.VISIBLE);
        main.capture.setVisibility(View.INVISIBLE);
        main.flash.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        main.load.setText("");
        main.load.setVisibility(View.INVISIBLE);
        main.prog.setVisibility(View.INVISIBLE);
        main.capture.setVisibility(View.VISIBLE);
        main.flash.setVisibility(View.VISIBLE);
    }

    @Override
    protected Void doInBackground(Void... params) {
        initDir();
        return null;
    }

    public void initDir () {
        //NEEDS TO BE THREADED
        File home = new File(main.ROOT);
        if (!home.isDirectory()) {
            home.mkdir();
        }

        File f = new File(main.TRAINED_DATA);
        if (!f.isDirectory()) {
            f.mkdir();
        }

        File g = new File(main.TRAINED_DATA + "/tessdata");
        if (!g.isDirectory()) {
            g.mkdir();
        }
        AssetManager assetManager = main.getApplicationContext().getAssets();

        File trained = new File(main.ENG_TRAINED);
        if (!(trained.isFile() || trained.exists())) {
            try {
                InputStream in = assetManager.open("eng.traineddata");
                OutputStream out = new FileOutputStream(main.ENG_TRAINED);
                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }

                Log.i("Init", "Trained Data written");
            } catch (Exception e) {
                Toast.makeText(main.getApplicationContext(), "Error writing tesseract data. Can't work without it. Please check storage.", Toast.LENGTH_LONG).show();
                main.finish();
                e.printStackTrace();
            }
        }
    }
}
