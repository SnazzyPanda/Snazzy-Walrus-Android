package com.snazzy.android.snazzywalrus.ui;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

import com.snazzy.android.snazzywalrus.Constants;
import com.snazzy.android.snazzywalrus.R;
import com.snazzy.android.snazzywalrus.util.SettingsManager;
import com.snazzy.android.snazzywalrus.util.WallpaperList;

import java.util.ArrayList;

/**
 * A simple {@link PreferenceFragment} subclass.
 */
public class SetSettingsFragment extends PreferenceFragment {

	private SettingsManager sm;
	// overview list
	private ArrayList<WallpaperList> setList;
	// holds the full WallpaperList object that we are editing
	private ArrayList<WallpaperList> actual;
	// position in full list (overview) that we have selected
	private ArrayList<Integer> listToEdit;
	private int savedId;

	//TODO: consider listPreference with a default settings option to allow users to set a specific settings to default...(uhg)...

	public SetSettingsFragment() {
		// Required empty public constructor
	}

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 * @return A new instance of fragment SetSettingsFragment.
	 */
	public static SetSettingsFragment newInstance() {
		SetSettingsFragment fragment = new SetSettingsFragment();
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sm = SettingsManager.getInstance();


		setList = sm.getOverviewListOfSets();
		if(null != getArguments()){
			this.listToEdit = getArguments().getIntegerArrayList(Constants.SETTINGS_EDIT_SETS_KEY);
		} else{
			Log.e("SetSettingsFragment", "No arguments supplied");
			return;
		}

		actual = new ArrayList<>();
		savedId = sm.getLastSeenSetId();
		for(int i = 0; i < listToEdit.size(); i++){
			int pos = listToEdit.get(i);
			int id = setList.get(pos).getUniqueId();
			// load the list (better way to do this?
			sm.loadWallpaperList(getActivity(), id);
			// add the list to list of WallpaperLists we are editing
			actual.add(sm.getWallpaperList());
		}

		// load the settings fields
		addPreferencesFromResource(R.xml.set_preferences);
		// image alteration settings
		addPreferencesFromResource(R.xml.image_preferences);
		// setup the fields
		initSettings();
	}


	private void initSettings(){
		WallpaperList wall = sm.copyWallpaperListWithoutNull(actual.get(0));

		final ListPreference interval = (ListPreference) findPreference(getString(R.string.list_interval_key));
		CheckBoxPreference randNext = (CheckBoxPreference) findPreference(getString(R.string.cb_random_key));
		CheckBoxPreference showPreview = (CheckBoxPreference) findPreference(getString(R.string.cb_preview_key));

		// handle interval changes
		interval.setValue(wall.getIntervalInMinutes().toString());
		interval.setSummary(R.string.interval_notice_text);
		interval.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				// update description text base on selected value
				interval.setValue(newValue.toString());
				interval.setSummary(interval.getEntry());

				for(int i = 0; i < actual.size(); i++){
					setList.get(listToEdit.get(i)).setIntervalInMinutes(Integer.parseInt(newValue.toString()));
					actual.get(i).setIntervalInMinutes(Integer.parseInt(newValue.toString()));
				}
				return true;
			}
		});

		// handle random next image preference
		randNext.setChecked(wall.getRandomNext());
		randNext.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				for(int i = 0; i < actual.size(); i++){
					setList.get(listToEdit.get(i)).setRandomNext((Boolean) newValue);
					actual.get(i).setRandomNext((Boolean) newValue);
				}
				return true;
			}
		});

		showPreview.setChecked(wall.getShowPreview());
		showPreview.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				for(int i = 0; i < actual.size(); i++){
					setList.get(listToEdit.get(i)).setShowPreview((Boolean) newValue);
					actual.get(i).setShowPreview((Boolean) newValue);
				}
				return true;
			}
		});

		/* Per wallpaper settings */

		CheckBoxPreference resizeCB = (CheckBoxPreference) findPreference(getString(R.string.cb_resize_key));
		CheckBoxPreference resizeHeightCB = (CheckBoxPreference) findPreference(getString(R.string.cb_resize_height_key));
		CheckBoxPreference resizeWidthCB = (CheckBoxPreference) findPreference(getString(R.string.cb_resize_width_key));
		CheckBoxPreference cropCB = (CheckBoxPreference) findPreference(getString(R.string.cb_crop_key));

		final ListPreference cropList = (ListPreference) findPreference(getString(R.string.list_crop_location_key));

		// use resize
		resizeCB.setChecked(wall.getUseResize());
		resizeCB.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {

				for(int i = 0; i < actual.size(); i++){
					setList.get(listToEdit.get(i)).setUseResize((Boolean) newValue);
					actual.get(i).setUseResize((Boolean) newValue);
				}
				return true;
			}
		});

		// resize by height
		resizeHeightCB.setChecked(wall.getResizeHeight());
		resizeHeightCB.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {

				for(int i = 0; i < actual.size(); i++){
					setList.get(listToEdit.get(i)).setResizeHeight((Boolean) newValue);
					actual.get(i).setResizeHeight((Boolean) newValue);
				}

				return true;
			}
		});

		// resize by width
		//TODO: fix set setting changes overwriting other sets and individual wallpaper settings
		resizeWidthCB.setChecked(wall.getResizeWidth());
		resizeWidthCB.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {

				for(int i = 0; i < actual.size(); i++){
					setList.get(listToEdit.get(i)).setResizeWidth((Boolean) newValue);
					actual.get(i).setResizeWidth((Boolean) newValue);
				}

				return true;
			}
		});

		// use crop
		cropCB.setChecked(wall.getUseCrop());
		cropCB.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {

				for(int i = 0; i < actual.size(); i++){
					setList.get(listToEdit.get(i)).setUseCrop((Boolean) newValue);
					actual.get(i).setUseCrop((Boolean) newValue);
				}
				return true;
			}
		});

		// crop location
		cropList.setValue(wall.getCropLocation().toString());
		cropList.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {

				Integer val = Integer.parseInt(newValue.toString());
				for(int i = 0; i < actual.size(); i++){
					sm.getOverviewListOfSets().get(listToEdit.get(i)).setCropLocation(val);
					actual.get(i).setCropLocation(val);
				}
				return true;
			}
		});

	}

	/**
	 * Save the changes to disk and re-load the list that was selected before changes.
	 * Lists are currently re-saved regardless of if changes were made.
	 * Saving is done synchronously.
	 * Loading of previous list is to ensure that pressing back (from set management view) will
	 * have expected results.
	 */
	private void applyChanges(){
		for(int i = 0; i < actual.size(); i++){
			// save each full list
			sm.saveWallpaperList(getActivity(), actual.get(i));
			WallpaperList tmp = sm.copyWallpaperListWithoutNull(actual.get(i));
			if(!tmp.getShowPreview()){
				// if we do not want to show them, delete the images
				sm.deletePreviewImage(getActivity(), actual.get(i).getUniqueId());
			}
		}
		// save brief list
		sm.saveIdList(getActivity());
		if(savedId != sm.getLastSeenSetId()){
			// re-load our original list if we need to
			sm.loadWallpaperList(getActivity(), savedId);
		}
	}


	@Override
	public void onDestroyView(){
		applyChanges();
		super.onDestroyView();
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}
}
