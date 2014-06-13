package com.westcoastlabs.quickwifi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

public class ExtractAssets {
	
	public static void copyAssets(String root, Context context) {
	    AssetManager assetManager = context.getAssets();
	    String[] files = null;
	    try {
	        files = assetManager.list("");
	    } catch (IOException e) {
	        Log.e("tag", "Failed to get asset file list.", e);
	    }
	    for(String filename : files) {
	        InputStream in = null;
	        OutputStream out = null;
	        try {
	          in = assetManager.open(filename);
	          File outFile = new File(root, filename);
	          out = new FileOutputStream(outFile);
	          copyFile(in, out);
	          in.close();
	          in = null;
	          out.flush();
	          out.close();
	          out = null;
	        } catch(IOException e) {
	            Log.e("tag", "Failed to copy asset file: " + filename, e);
	        }       
	    }
	}
	
	private static void copyFile(InputStream in, OutputStream out) throws IOException {
		Log.i("tag", "Copying Asset File");
	    byte[] buffer = new byte[1024];
	    int read;
	    while((read = in.read(buffer)) != -1){
	      out.write(buffer, 0, read);
	    }
	}
}
