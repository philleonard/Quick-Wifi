package com.westcoastlabs.quickwifi;

import android.os.AsyncTask;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

/**
 * Created by ppl on 12/06/14.
 */
public class ExtractAndConnect extends AsyncTask<Void, Void, Void> {

    boolean success = false;
    public String extractedText;
    MainActivity main;
    String SSID = "";
    String key = "";

    boolean foundSSID = false;
    boolean foundKey = false;
    public ExtractAndConnect (MainActivity main, String extractedText) {
        this.main = main;
        this.extractedText = extractedText;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... params) {
        ExtractSSIDandKey extract = new ExtractSSIDandKey(extractedText);

        try {
            SSID = extract.getSSID();
        } catch (NoWifiInfoFoundException e) {
            success = false;
            foundSSID = false;
            return null;
        }

        foundSSID = true;

        try {
            key = extract.getKey();
        } catch (NoWifiInfoFoundException e) {
            success = false;
            foundKey = false;
            return null;
        }

        foundKey = true;
        success = true;


        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (!success) {
            if (!foundSSID)
                Toast.makeText(main.getApplicationContext(), "Failed to find SSID in image", Toast.LENGTH_SHORT).show();
            else if (!foundKey)
                Toast.makeText(main.getApplicationContext(), "Found SSID, but failed to find Key in image", Toast.LENGTH_SHORT).show();
            Animation fadeOutAnimation = AnimationUtils.loadAnimation(main.getApplicationContext(), R.anim.fade_out);
            fadeOutAnimation.setFillAfter(false);
            main.grey.startAnimation(fadeOutAnimation);

            main.capture.setVisibility(View.VISIBLE);
            main.load.setVisibility(View.INVISIBLE);
            main.prog.setVisibility(View.INVISIBLE);
            main.load.setText("");
        }
        else {
            //Success, move on to connect
            main.load.setText("Connecting to best SSID match");
            new Connect(main, SSID, key).execute();
        }
    }
}
