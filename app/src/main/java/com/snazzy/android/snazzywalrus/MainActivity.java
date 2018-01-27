package com.snazzy.android.snazzywalrus;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.snazzy.android.snazzywalrus.helper.AndroidValueHelper;
import com.snazzy.android.snazzywalrus.service.SavingService;
import com.snazzy.android.snazzywalrus.ui.ImageListFragment;
import com.snazzy.android.snazzywalrus.ui.SetManagementFragment;
import com.snazzy.android.snazzywalrus.ui.SettingsActivity;
import com.snazzy.android.snazzywalrus.util.SettingsManager;
import com.snazzy.android.snazzywalrus.util.Wallpaper;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity implements SetManagementFragment.OnSetListInteractionListener {

	// these are in approximate order of importance/desire to implement
	// TODO: fix initial permission prompt not following through with action (kinda low priority?)
	// TODO: optimize saving and loading (also creation of preview images)

	// TODO: consider list item short click actions (popup displaying larger version of picture?)
	// TODO: handle actively running set when sets are modified and different sets selected, etc
	// TODO: allow user to rearrange items in the list (useful for sequential changing) (also could do a shuffle type thing)

	// TODO: consider allowing multiple lists to be active/specific list active while others can be viewed/edited
	// TODO: check performance of large lists (especially saving and loading)

	// TODO: time until next automatic change? (likely approximate)
	// TODO: consider adding option and capability of re-starting schedule on device startup
	// TODO: maybe: allow users to choose gif???? (via option) (but seriously, why would you?)
	// TODO: look into changing lock screen background as well? -> likely no official support

	private Scheduler scheduler;
	private SettingsManager sm = SettingsManager.getInstance();
	private AndroidValueHelper avh;

	private Toolbar topToolBar;
	private ImageListFragment imageListFragment;
	private SetManagementFragment setManagementFragment;

	// track the lastSeenTheme seen theme, used to determine whether a redraw is necessary
	private int lastSeenTheme = -1;
	// used to hide/display the Manage sets option in the action bar menu
	private boolean showSetActionInMenu = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		avh = new AndroidValueHelper(this);
		// load ID
		sm.loadIdList(this);
		// load our settings
		sm.loadFromDisk(this);
		// TODO: determine if we need to load anywhere else
		// check and set our theme (as applicable)
		// if we have a saved theme
		avh.checkSetTheme(sm.getThemeId());
		// amazingly, makes all the difference?
		lastSeenTheme = sm.getThemeId();

		// get the activity's view
		setContentView(R.layout.activity_main);
		// Begin the transaction
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

		// if the app bar has not been created yet
		if(null == topToolBar){
			// create it and set it as the action bar
			topToolBar = (Toolbar) findViewById(R.id.action_bar_top);
			setSupportActionBar(topToolBar);
			// set logo to app icon
			//topToolBar.setLogo(R.mipmap.snazzy_walrus_icon2);
			getSupportActionBar().setHomeAsUpIndicator(R.mipmap.snazzy_walrus_icon2);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);

			// set a subtitle
			topToolBar.setSubtitle(getString(R.string.app_tagline));
		}

		if(null == imageListFragment){
			imageListFragment = new ImageListFragment();
		}
		if(null == findViewById(R.id.imageListFragment)){
			ft.add(R.id.mainContent, imageListFragment);
		}

		// commit fragment transactions
		ft.commit();

		// get scheduler instance
		scheduler = Scheduler.getInstance(this);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_settings:
				// display the settings activity
				openSettings();
				return true;
			case R.id.action_start:
				// Handle starting and stopping of the scheduler
				// if scheduled
				if(scheduler.getScheduled()){
					// stop the changer
					stopWallpaperChanger();
					updateStartStopButton();
				} else{
					// start changer (after check)
					MainActivityPermissionsDispatcher.startWallpaperChangerWithCheck(this);
				}
				return true;
			case R.id.action_next:
				// force change to next wallpaper
				MainActivityPermissionsDispatcher.forceWallpaperChangeWithCheck(this);
				return true;
			case android.R.id.home:
				// create a special dialog and show it
				AlertDialog ad = getDevBonusDialog();
				ad.show();
				return true;
			case R.id.action_default_all:
				// TODO: consider if this or imageListFragment should handle (likely image list fragment?)
				getRestoreDefaultDialog().show();
				// have list view update
				// send to list view in the first place?
				// reset all list items to default (with user confirmation of course)
				return true;
			case R.id.action_sets:
				// start set manager
				startSetManagerFromList();
				return true;
			default:
				// If we got here, the user's action was not recognized.
				// Invoke the superclass to handle it.
				return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Updates the start/stop button based on if events are scheduled
	 */
	private void updateStartStopButton(){
		// get the button
		Menu menu = topToolBar.getMenu();
		MenuItem item = menu.findItem(R.id.action_start);

		if(scheduler.getScheduled()){
			// set display to stop
			item.setIcon(android.R.drawable.ic_media_pause);
			item.setTitle(R.string.action_top_stop);
		} else{
			// set display to start
			item.setIcon(android.R.drawable.ic_media_play);
			item.setTitle(R.string.action_top_start);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.top_action_bar_menu, menu);

		// get the set item in the menu
		MenuItem setItem = menu.findItem(R.id.action_sets);
		// if we want to show the set menu item
		if(showSetActionInMenu){
			// have it be visible
			setItem.setVisible(true);
		} else{
			// otherwise, hide it
			setItem.setVisible(false);
		}

		// make sure start/stop button is accurate
		updateStartStopButton();
		return true;
	}

	/**
	 * Prepares and starts the alarm manager via the scheduler.
	 * Requires: READ_EXTERNAL_STORAGE permissions
	 */
	@NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
	public void startWallpaperChanger(){
		// if we are already scheduled, do nothing (try removing)
		if(!scheduler.getScheduled()){
			// get current lists' (de-nulled) interval to use
			scheduler.setDelayInMinutes(
					sm.copyWallpaperListWithoutNull(sm.getWallpaperList())
							.getIntervalInMinutes());
			scheduler.scheduleAlarms(this);

			// check if there are no items in list
			if(sm.getWallpaperList().size() < 1){
				// no items in list, alert user about it
				Toast.makeText(this, R.string.toast_changer_start_no_files, Toast.LENGTH_LONG).show();
			} else{
				Toast.makeText(this, R.string.toast_changer_start, Toast.LENGTH_SHORT).show();
			}
		}
		updateStartStopButton();
	}

	/**
	 * Immediately sends the scheduler's intent, forcing the wallpaper to be changed immediately
	 * Requires: READ_EXTERNAL_STORAGE permissions
	 */
	@NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
	public void forceWallpaperChange(){
		scheduler.forceIntentNow(this);
	}

	/**
	 * Stops the scheduler's alarms
	 */
	public void stopWallpaperChanger(){
		scheduler.stopAlarms(this);
		Toast.makeText(this, R.string.toast_changer_stop, Toast.LENGTH_SHORT).show();
	}

	/**
	 * Starts and opens the settings activity
	 */
	private void openSettings(){
		// start the settings activity
		Intent intent = new Intent(this, SettingsActivity.class);
		// tell settings activity that this intent is for the main settings
		intent.putExtra(Constants.SETTINGS_IDENTIFIER_KEY, Constants.MAIN_SETTINGS_REQUEST);
		startActivity(intent);
	}

	/**
	 * Removes the imageList fragment and adds the setManagement fragment to the activity
	 */
	private void startSetManagerFromList(){
		sm.saveToDisk(this);
//		SavingService ss = new SavingService(this);
//		ss.execute(false);

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.remove(imageListFragment);
		setManagementFragment = new SetManagementFragment();
		ft.add(R.id.mainContent, setManagementFragment);
		ft.commit();
		// make sure option to manage sets is not available (otherwise multiple fragments may be created by user)
		hideSetMenuItem();
	}

	/**
	 * Removes the setManagement fragment and adds the imageList fragment to the activity.
	 */
	private void startListFromSetManager(){
		sm.saveToDisk(this);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		imageListFragment = new ImageListFragment();
		ft.remove(setManagementFragment);
		ft.add(R.id.mainContent, imageListFragment);
		ft.commit();
		// make sure option to manage sets is available
		showSetMenuItem();
	}

	@Override
	public void onBackPressed(){
		if(null != findViewById(R.id.setListFragment)){
			// should be set list manager
			startListFromSetManager();
		} else{
			super.onBackPressed();
		}
	}

	@Override
	protected void onStop(){
		// save our settings
		sm.saveToDisk(this); // alternatively save this ONLY after changes to file list (assuming that is handled in main activity)
		// then again, might be better here
		// also should settings store whether or not the changer is active?
		super.onStop();
	}

	@Override
	protected void onSaveInstanceState(Bundle bundle){
		super.onSaveInstanceState(bundle);
		// removes the saved fragment (overwriting it also works)
		// this prevents loading multiple fragments into the main activity by
		// going home, then returning to the app without fully closing the app
		// bundle.putBoolean("android:support:fragments", false);
		// TODO: surely better way to handle this?, also no hardcode?
		bundle.remove("android:support:fragments");
	}

	@Override
	protected void onResume(){
		super.onResume();
		// check for changes to the theme
		checkUpdateTheme();
	}

	/**
	 * Refreshes the display of the action bar, hiding the Manage Sets menu item
	 */
	private void hideSetMenuItem(){
		showSetActionInMenu = false;
		getSupportActionBar().invalidateOptionsMenu();
	}

	/**
	 * Refreshes the display of the action bar, displaying the Manage Sets menu item
	 */
	private void showSetMenuItem(){
		showSetActionInMenu = true;
		getSupportActionBar().invalidateOptionsMenu();
	}

	/**
	 * Checks for changes in saved theme since lastSeenTheme seen theme and reloads activity if there are.
	 */
	private void checkUpdateTheme(){
		// check if theme has changed (since we are used in onResume, to prevent infinite loop)
		if(lastSeenTheme != sm.getThemeId()){
			// if we have a saved theme
			avh.checkSetTheme(sm.getThemeId());
			// amazingly, makes all the difference?
			lastSeenTheme = sm.getThemeId();
			// stop activity
			finish();
			// restart activity
			startActivity(getIntent());
		}
	}

	/**
	 * Creates an alert dialog to be show when logo is clicked
	 * @return AlertDialog created with the dialog_logo_bonus layout
	 */
	private AlertDialog getDevBonusDialog(){
		// get dialog builder and inflater
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		LayoutInflater inflater = getLayoutInflater();

		// inflate the layout
		View view = inflater.inflate(R.layout.dialog_logo_bonus, null);
		// for bonus secrets, add here
//		ImageView iv = (ImageView) view.findViewById(R.id.logoImageView);
//		iv.setOnClickListener(null);

		adb.setView(view);
		// use android OK text with not special click handling
		adb.setPositiveButton(android.R.string.ok, null);

		// no title?
		//adb.setTitle("");
		// return the AlertDialog
		return adb.create();
	}

	// TODO: consider where this should be
	private AlertDialog getRestoreDefaultDialog(){
		// create alert dialog
		AlertDialog.Builder adb = new AlertDialog.Builder(this);

		adb.setTitle(R.string.restore_defaults_on_all_title);
		adb.setMessage(R.string.restore_defaults_on_all_prompt);

		// user confirms deletion
		adb.setPositiveButton(R.string.restore_defaults_on_all_confirm, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				restoreAllDefaults();
				// save the changes
				SavingService ss = new SavingService(getApplicationContext());
				ss.execute(true);
			}
		});
		adb.setNegativeButton(R.string.restore_defaults_on_all_deny, null);
		return adb.create();
	}

	private void restoreAllDefaults(){
		for(Wallpaper wall : sm.getWallpaperList()){
			wall.restoreDefault();
		}
		// have imagelist update the display to reflect the changes
		imageListFragment.updateDisplayedList();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		// NOTE: delegate the permission handling to generated method
		MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
	}

	/**
	 * Prepare and display the reason we need these permissions to the user.
	 * @param request The PermissionRequest to launch or cancel by user decision
	 */
	@OnShowRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
	void showRationaleForExternalRead(final PermissionRequest request) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		// this gets displayed before the permissions requests

		dialog.setMessage(R.string.permission_reason_read_external);
		dialog.setPositiveButton(R.string.permission_reason_positive_button, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				request.proceed();
			}
		});
		dialog.setNegativeButton(R.string.permission_reason_negative_button, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				request.cancel();
			}
		});

		dialog.show();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		if(Constants.WALLPAPER_LIST_ITEMS_REQUEST == requestCode){
			// if image list request
			imageListFragment.onActivityResult(requestCode, resultCode, data);
		} else if(Constants.SETS_LIST_ITEMS_SETTINGS_REQUEST == requestCode){
			// if set list request
			setManagementFragment.onActivityResult(requestCode, resultCode, data);
		} else{
			Log.w("mainOnActResult", "No supported activity known: " + requestCode);
		}
	}

	@Override
	public void onSetListInteraction() {
		startListFromSetManager();
	}

}
