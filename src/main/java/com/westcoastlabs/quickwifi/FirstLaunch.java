package com.westcoastlabs.quickwifi;

import java.io.File;

import android.content.Context;
import android.os.Environment;

public class FirstLaunch {
	
	static String ROOT = Environment.getExternalStorageDirectory() + "/QuickWifi/";
	
	public static boolean isFirstLaunch() {
		return true;
	}
	
	public static void initialisation(Context context) {
		
		File f = new File(ROOT);
		if(!(f.isDirectory() || f.exists()))
			f.mkdir();
		
		ExtractAssets.copyAssets(ROOT, context);
	}
}
