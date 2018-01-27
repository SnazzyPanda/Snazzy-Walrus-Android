package com.snazzy.android.snazzywalrus.ui;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.snazzy.android.snazzywalrus.Constants;
import com.snazzy.android.snazzywalrus.R;
import com.snazzy.android.snazzywalrus.helper.AndroidValueHelper;
import com.snazzy.android.snazzywalrus.util.SettingsManager;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatActivity {

	public static final int INVALID_SETTINGS_TYPE = -1;
	private int settingsType;
	private AndroidValueHelper avh;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// get a helper
		avh = new AndroidValueHelper(this);

		// get settings manager reference
		SettingsManager sm = SettingsManager.getInstance();
		sm.loadFromDisk(this);

		// set saved theme if it exists
		avh.checkSetTheme(sm.getThemeId());
		setContentView(R.layout.activity_settings);

		try{
			settingsType = getIntent().getIntExtra(Constants.SETTINGS_IDENTIFIER_KEY, INVALID_SETTINGS_TYPE);
		} catch(NullPointerException e){
			Log.e("SettingsActivity", "Preference identifier not found in bundle!");
			e.printStackTrace();
			finish();
		}

		// Begin the transaction
		FragmentTransaction ft = getFragmentManager().beginTransaction();

		// if main settings
		if(Constants.MAIN_SETTINGS_REQUEST == settingsType){
			// Replace the contents of the container with the new fragment
			ft.replace(R.id.placeholder_settings_frame, new SettingsFragment());
		} else if(Constants.WALLPAPER_LIST_ITEMS_REQUEST == settingsType){
			// if selected list item settings
			Bundle bundle = new Bundle();
			bundle.putAll(getIntent().getExtras());
			// create an ImageSettingsFragment with passed info
			ImageSettingsFragment isf = new ImageSettingsFragment();
			isf.setArguments(bundle);
			// insert the fragment
			ft.replace(R.id.placeholder_settings_frame, isf);
		} else if(Constants.SETS_LIST_ITEMS_SETTINGS_REQUEST == settingsType){
			// if selected set item settings
			Bundle bundle = new Bundle();
			bundle.putAll(getIntent().getExtras());
			// create a SetSettingsFragment with passed info
			SetSettingsFragment ssf = new SetSettingsFragment();
			ssf.setArguments(bundle);
			// inset the fragment
			ft.replace(R.id.placeholder_settings_frame, ssf);
		}
		// apply changes
		ft.commit();
	}

	@Override
	protected void onPause(){
		// we might need to save synchronously here?
		SettingsManager sm = SettingsManager.getInstance();
		sm.saveToDisk(this);
		super.onPause();
	}

	@Override
	public void onBackPressed(){
		// finish activity with result ok
		setResult(Activity.RESULT_OK);
		finish();
		super.onBackPressed();
	}
}
