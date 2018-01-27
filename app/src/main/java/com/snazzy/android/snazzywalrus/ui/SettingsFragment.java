package com.snazzy.android.snazzywalrus.ui;

import android.content.Context;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.snazzy.android.snazzywalrus.R;
import com.snazzy.android.snazzywalrus.Scheduler;
import com.snazzy.android.snazzywalrus.helper.AndroidValueHelper;
import com.snazzy.android.snazzywalrus.util.SettingsManager;
import com.snazzy.android.snazzywalrus.util.WallpaperList;

public class SettingsFragment extends PreferenceFragment {

	private Context mainContext;
	private SettingsManager sm = SettingsManager.getInstance();
	private AndroidValueHelper avh;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// get reference to application context
		mainContext = getActivity().getApplicationContext();
		avh = new AndroidValueHelper(mainContext);

		addPreferencesFromResource(R.xml.general_preferences);
		addPreferencesFromResource(R.xml.global_image_preferences);
		addPreferencesFromResource(R.xml.advanced_preferences);
		initializeSettings();
	}

	/**
	 * Sets up change listeners to handle preference changes
	 */
	private void initializeSettings(){
		final ListPreference interval = (ListPreference) findPreference(getString(R.string.list_interval_key));
		CheckBoxPreference randNext = (CheckBoxPreference) findPreference(getString(R.string.cb_random_key));
		CheckBoxPreference previewCB = (CheckBoxPreference) findPreference(getString(R.string.cb_preview_key));

		CheckBoxPreference resizeCB = (CheckBoxPreference) findPreference(getString(R.string.cb_resize_key));
		CheckBoxPreference resizeHeightCB = (CheckBoxPreference) findPreference(getString(R.string.cb_resize_height_key));
		CheckBoxPreference resizeWidthCB = (CheckBoxPreference) findPreference(getString(R.string.cb_resize_width_key));

		CheckBoxPreference cropCB = (CheckBoxPreference) findPreference(getString(R.string.cb_crop_key));
		final ListPreference cropLocation = (ListPreference) findPreference(getString(R.string.list_crop_location_key));

		CheckBoxPreference duplicateCB = (CheckBoxPreference) findPreference(getString(R.string.cb_duplicate_files_key));
		CheckBoxPreference subdirCB = (CheckBoxPreference) findPreference(getString(R.string.cb_use_subdir_key));

		final ListPreference themeList = (ListPreference) findPreference(getString(R.string.list_theme_key));
		CheckBoxPreference globalCB = (CheckBoxPreference) findPreference(getString(R.string.cb_use_global_key));

		/*
		 * General settings group
		 */

		// set summary to user friendly description of current selection
		interval.setValue("" + sm.getIntervalInMinutes());
		interval.setSummary(interval.getEntry());
		// handle interval changes
		interval.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				// update description text base on selected value
				interval.setValue(newValue.toString());
				interval.setSummary(interval.getEntry());

				// set in settings manager
				sm.setIntervalInMinutes(Integer.parseInt(newValue.toString()));
				updateSchedulerInterval();

				return true;
			}
		});

		// handle random next image preference
		randNext.setChecked(sm.getRandomNext());
		randNext.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				sm.setRandomNext((Boolean) newValue);
				return true;
			}
		});

		// handle preview
		previewCB.setChecked(sm.getShowPreview());
		previewCB.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				sm.setShowPreview((Boolean) newValue);
				return true;
			}
		});

		// theme options
		themeList.setValue(sm.getThemeId() + "");
		themeList.setSummary(themeList.getEntry());
		themeList.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				// get the value selected
				String val = newValue.toString();
				// get id using value
				int id = avh.getResId(val, R.style.class);

				// set new theme id in settings
				sm.setThemeId(id);

				// set theme to selected
				getActivity().setTheme(id);

				// redraw the screen
				getActivity().recreate();

				return true;
			}
		});

		/*
		 * Image alteration settings group
		 */

		// handle use resize changes
		resizeCB.setChecked(sm.getUseResize());
		resizeCB.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener(){
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue){
				sm.setUseResize((Boolean) newValue);
				return true;
			}
		});

		// resize by height changes
		resizeHeightCB.setChecked(sm.getResizeHeight());
		resizeHeightCB.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener(){
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue){
				sm.setResizeHeight((Boolean) newValue);
				return true;
			}
		});

		// resize by width changes
		resizeWidthCB.setChecked(sm.getResizeWidth());
		resizeWidthCB.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener(){
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue){
				sm.setResizeWidth((Boolean) newValue);
				return true;
			}
		});

		// handle use crop changes
		cropCB.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener(){
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue){
				sm.setUseCrop((Boolean) newValue);
				return true;
			}
		});

		// set summary to user friendly description of current selection
		cropLocation.setSummary(cropLocation.getEntry());
		// handle crop location changes
		cropLocation.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				// update description text base on selected value
				cropLocation.setValue(newValue.toString());
				cropLocation.setSummary(cropLocation.getEntry());

				// set in settings manager
				sm.setCropLocation(Integer.parseInt(newValue.toString()));

				return true;
			}
		});

		/*
		 * Advanced settings group
		 */

		// handle use global only changes
		globalCB.setChecked(sm.getGlobalOnly());
		globalCB.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				sm.setGlobalOnly((Boolean) newValue);
				return true;
			}
		});

		// handle allow duplicate changes
		duplicateCB.setChecked(sm.getAllowRepeatedEntries());
		duplicateCB.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener(){
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue){
				sm.setAllowRepeatedEntries((Boolean) newValue);
				return true;
			}
		});

		// handle include subdirectories changes
		subdirCB.setChecked(sm.getIncludeSubDirectories());
		subdirCB.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener(){
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue){
				sm.setIncludeSubDirectories((Boolean) newValue);
				return true;
			}
		});
	} // end initialize settings

	/**
	 * Checks if scheduler has a scheduled event.
	 * If it does, it restarts the scheduler so that it uses a new interval.
	 */
	public void updateSchedulerInterval(){
		// this should update currently running scheduler
		Scheduler scheduler = Scheduler.getInstance(mainContext);
		if(scheduler.getScheduled()){
			WallpaperList tmp = sm.copyWallpaperListWithoutNull(sm.getWallpaperList());
			// running, so stop and restart scheduler with new interval
			scheduler.setDelayInMinutes(tmp.getIntervalInMinutes());
			scheduler.stopAlarms(mainContext);
			scheduler.scheduleAlarms(mainContext, true);
		}
	}

	private void cleanupPreviews(){
		// if global is set to no preview
		if(!sm.getShowPreview()){
			// for each set
			for(int i = 0; i < sm.getOverviewListOfSets().size(); i++){
				// see if it overrides preview, if it does delete it
				WallpaperList tmp = sm.copyWallpaperListWithoutNull(sm.getOverviewListOfSets().get(i));
				if(!tmp.getShowPreview()){
					sm.deletePreviewImage(getActivity(), tmp.getUniqueId());
				}
			}
		}
	}

	@Override
	public void onDestroyView(){
		cleanupPreviews();
		super.onDestroyView();
	}
}
