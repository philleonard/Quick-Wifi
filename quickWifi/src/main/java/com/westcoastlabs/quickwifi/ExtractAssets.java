package com.westcoastlabs.quickwifi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

public class ExtractAssets {

    private void copyFolder(String name, Context context) {
        // "Name" is the name of your folder!
        AssetManager assetManager = context.getAssets();
        String[] files = null;

        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            // Checking file on assets subfolder
            try {
                files = assetManager.list(name);
            } catch (IOException e) {
                Log.e("ERROR", "Failed to get asset file list.", e);
            }
            // Analyzing all file on assets subfolder
            for(String filename : files) {
                InputStream in = null;
                OutputStream out = null;
                // First: checking if there is already a target folder
                File folder = new File(Environment.getExternalStorageDirectory() + "/QuickWifi/" + name);
                boolean success = true;
                if (!folder.exists()) {
                    success = folder.mkdir();
                }
                if (success) {
                    // Moving all the files on external SD
                    try {
                        in = assetManager.open(name + "/" +filename);
                        copyFile(in, out);
                        in.close();
                        in = null;
                        out.flush();
                        out.close();
                        out = null;
                    } catch(IOException e) {
                        Log.e("ERROR", "Failed to copy asset file: " + filename, e);
                    }
                }
                else {
                    // Do something else on failure
                }
            }
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
        } else {
            // Something else is wrong. It may be one of many other states, but all we need
            // is to know is we can neither read nor write
        }
    }

    // Method used by copyAssets() on purpose to copy a file.
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }
}
