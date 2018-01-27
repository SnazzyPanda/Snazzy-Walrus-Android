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
import com.snazzy.android.snazzywalrus.util.Wallpaper;
import com.snazzy.android.snazzywalrus.util.WallpaperList;

import java.util.ArrayList;

/**
 * Handles settings for individual/groups of images (selected by user)
 */
public class ImageSettingsFragment extends PreferenceFragment {

	private WallpaperList imageList;
	private ArrayList<Integer> listToEdit;
	private SettingsManager sm;

	public ImageSettingsFragment() {
		// Required empty public constructor
	}

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @return A new instance of fragment ImageSettingsFragment.
	 */
	public static ImageSettingsFragment newInstance() {
		return new ImageSettingsFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sm = SettingsManager.getInstance();

		if(null != getArguments()){
			this.imageList = sm.getWallpaperList();
			this.listToEdit = getArguments().getIntegerArrayList(Constants.SETTINGS_EDIT_LIST_KEY);
		} else{
			Log.e("ImageSettingsFragment", "No arguments supplied");
			return;
		}

		// load the settings fields
		addPreferencesFromResource(R.xml.image_preferences);
		// setup the fields
		initSettings();
	}

	/**
	 * Initializes handling of each preference field
	 */
	private void initSettings(){
		// to default handled elsewhere, still could be option though?
		// TODO: find better way to show settings for multiple selected wallpapers
		// TODO: handle each preference in its own method
		// use the first selected wallpaper as the settings to display
		Wallpaper wall = sm.copyWallpaperWithoutNull(sm.getWallpaperList().get(listToEdit.get(0)));

		CheckBoxPreference resizeCB = (CheckBoxPreference) findPreference(getString(R.string.cb_resize_key));
		CheckBoxPreference resizeHeightCB = (CheckBoxPreference) findPreference(getString(R.string.cb_resize_height_key));
		CheckBoxPreference resizeWidthCB = (CheckBoxPreference) findPreference(getString(R.string.cb_resize_width_key));
		CheckBoxPreference cropCB = (CheckBoxPreference) findPreference(getString(R.string.cb_crop_key));

		final ListPreference cropList = (ListPreference) findPreference(getString(R.string.list_crop_location_key));

		resizeCB.setChecked(wall.getUseResize());
		resizeCB.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {

				for(int i = 0; i < listToEdit.size(); i++){
					imageList.get(listToEdit.get(i)).setUseResize((Boolean) newValue);
				}

				return true;
			}
		});

		resizeHeightCB.setChecked(wall.getResizeHeight());
		resizeHeightCB.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {

				for(int i = 0; i < listToEdit.size(); i++){
					imageList.get(listToEdit.get(i)).setResizeHeight((Boolean) newValue);
				}

				return true;
			}
		});

		resizeWidthCB.setChecked(wall.getResizeWidth());
		resizeWidthCB.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {

				for(int i = 0; i < listToEdit.size(); i++){
					imageList.get(listToEdit.get(i)).setResizeWidth((Boolean) newValue);
				}

				return true;
			}
		});

		cropCB.setChecked(wall.getUseCrop());
		cropCB.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {

				for(int i = 0; i < listToEdit.size(); i++){
					imageList.get(listToEdit.get(i)).setUseCrop((Boolean) newValue);
				}

				return true;
			}
		});

		cropList.setValue(wall.getCropLocation().toString());
		cropList.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {

				Integer val = Integer.parseInt(newValue.toString());

				for(int i = 0; i < listToEdit.size(); i++){
					imageList.get(listToEdit.get(i)).setCropLocation(val);
				}

				return true;
			}
		});
	}

	@Override
	public void onDestroyView(){
		sm.saveToDisk(getActivity());
		super.onDestroyView();
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

}
