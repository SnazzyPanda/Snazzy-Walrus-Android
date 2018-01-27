package com.snazzy.android.snazzywalrus.service;

import android.content.Context;
import android.os.AsyncTask;

import com.snazzy.android.snazzywalrus.util.SettingsManager;

/**
 * Created by SnazzyPanda on 6/24/16.
 * A class extending {@link AsyncTask}, whose sole purpose is to save the settings.
 */
public class SavingService extends AsyncTask<Boolean, Void, Void> {

	private Context context;

	/**
	 * Constructor for SavingService, takes a context and boolean
	 * @param context Context to use
	 */
	public SavingService(Context context){
		super();
		this.context = context;
	}

	@Override
	protected Void doInBackground(Boolean... booleans) {
		SettingsManager sm = SettingsManager.getInstance();
		boolean justList = booleans[0];

		if(justList){
			sm.saveWallpaperList(context, sm.getWallpaperList());
		} else{
			sm.saveToDisk(context);
		}
		return null;
	}
}
