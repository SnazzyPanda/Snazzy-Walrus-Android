package com.snazzy.android.snazzywalrus.ui;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.snazzy.android.snazzywalrus.Constants;
import com.snazzy.android.snazzywalrus.R;
import com.snazzy.android.snazzywalrus.helper.AndroidValueHelper;
import com.snazzy.android.snazzywalrus.util.SettingsManager;
import com.snazzy.android.snazzywalrus.util.WallpaperList;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Fragment to handle displaying and managing WallpaperLists.
 */
public class SetManagementFragment extends Fragment implements View.OnClickListener,
		BottomMenuFragment.OnBottomMenuFragmentClickListener, AbsListView.MultiChoiceModeListener {

	private OnSetListInteractionListener mListener;
	private SettingsManager sm;
	private AndroidValueHelper avh;

	private ArrayList<WallpaperList> wallList = new ArrayList<>();
	private ArrayList<Integer> selectedList = new ArrayList<>();
	private WallpaperSetArrayAdapter arrayAdapter;
	private BottomMenuFragment bottomToolBar;

	public static SetManagementFragment newInstance() {
		SetManagementFragment fragment = new SetManagementFragment();
		return fragment;
	}

	public SetManagementFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_set_management, container, false);

		sm = SettingsManager.getInstance();
		avh = new AndroidValueHelper(getContext());
		// get a shortened list of wallpaper sets
		wallList = sm.getOverviewListOfSets();
		// setup the custom array adapter
		arrayAdapter = new WallpaperSetArrayAdapter(this.getContext(), android.R.layout.simple_list_item_1, wallList);
		GridView wallSetListView = (GridView) view.findViewById(R.id.wallSetGridView);
		wallSetListView.setAdapter(arrayAdapter);
		wallSetListView.setOnItemClickListener(arrayAdapter);
		// setup modal multi-choice
		wallSetListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		wallSetListView.setMultiChoiceModeListener(this);

//		wallSetGridView.setOnItemLongClickListener(arrayAdapter);

		// if the bottom tool bar is not found, make a new one
		if(null == bottomToolBar){
			bottomToolBar = (BottomMenuFragment) getChildFragmentManager().findFragmentById(R.id.setListBottomMenuFragment);
		}

		return view;
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if (context instanceof OnSetListInteractionListener) {
			mListener = (OnSetListInteractionListener) context;
		} else {
			throw new RuntimeException(context.toString()
					+ " must implement OnSetListInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	@Override
	public void onClick(View view) {
		// not handling anything (yet)
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data){
		if (resultCode != Activity.RESULT_OK) {
//			Log.d("activityResult", "Bad result " + Activity.RESULT_OK);
			return;
		}

		if(requestCode == Constants.SETS_LIST_ITEMS_SETTINGS_REQUEST){
			// request is from selected item settings
			// update held list
			updateDisplayedList();
		}

	}

	/**
	 * Performs actions to force the arrayAdapter to update the display of list items on screen
	 */
	public void updateDisplayedList(){
		ArrayList<WallpaperList> tmp = new ArrayList<>();
		tmp.addAll(sm.getOverviewListOfSets());
		wallList.clear();
		wallList.addAll(tmp);
		if(sm.getOverviewListOfSets().size() != tmp.size()){
			sm.getOverviewListOfSets().clear();
			sm.getOverviewListOfSets().addAll(tmp);
		}
		arrayAdapter.notifyDataSetChanged();
	}

	// TODO: add a merge set option?
	@Override
	public void onBottomMenuFragmentClickListener(View view) {
		switch (view.getId()){
			case R.id.bottomAddButton:
				// add button
				// display a dialog to set the name
				// create brief ver, and add to set (updating the displayed list)
				createSetDialog().show();
				break;
			case R.id.bottomDeleteButton:
				// delete button
				deleteAllSetsDialog().show();
				break;
			default:
				break;
		}
	}

	/**
	 * Creates and returns an AlertDialog for permanently deleting all sets
	 * @return AlertDialog prompting deletion of sets
	 */
	private AlertDialog deleteAllSetsDialog(){
		AlertDialog.Builder adb = new AlertDialog.Builder(getContext());
		adb.setTitle(getString(R.string.remove_all_sets_title));
		adb.setMessage(getString(R.string.remove_all_sets_prompt));
		adb.setPositiveButton(getString(R.string.remove_all_sets_confirm), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				sm.deleteAllSets(getContext());
				wallList.clear();
				wallList = sm.getOverviewListOfSets();
				arrayAdapter.notifyDataSetChanged();
				bottomToolBar.updateRegisteredSetsText();
			}
		});

		adb.setNegativeButton(getString(R.string.remove_all_sets_deny), null);
		return adb.create();
	}

	/**
	 * Creates and returns the AlertDialog for creating a Wallpaper Set
	 * @return AlertDialog for creating a Wallpaper Set
	 */
	private AlertDialog createSetDialog(){
		AlertDialog.Builder adb = new AlertDialog.Builder(getContext());
		adb.setTitle(getString(R.string.add_set_title));
		adb.setMessage(getString(R.string.add_set_prompt));

		LayoutInflater inflater = getActivity().getLayoutInflater();
		View inputView = inflater.inflate(R.layout.dialog_create_set, null);
		final EditText editText = (EditText) inputView.findViewById(R.id.setNameInputField);

		adb.setView(inputView);

		adb.setNegativeButton(getString(R.string.add_set_deny), null);
		adb.setPositiveButton(getString(R.string.add_set_confirm), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				String name = "";
				int id = WallpaperList.ID_PLACEHOLDER;
				try{
					// empty allowed I guess?
					name = editText.getText().toString();
					// if we fail before here, id remains a -1
					id = sm.getUnusedId();
				} catch(Exception e){
					// Forget what exception I was dealing with
					e.printStackTrace();
				}

				WallpaperList tmp = new WallpaperList(id, name);
				// add to list
				wallList.add(tmp);
				// create new file for this new set, which will have the id and name for later identification
				sm.saveWallpaperList(getContext(), tmp);
				sm.setOverviewListOfSets(wallList);
				arrayAdapter.notifyDataSetChanged();
				bottomToolBar.updateRegisteredSetsText();
			}
		});

		return adb.create();
	}

	@Override
	public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean checked) {
		// if item was check
		if(checked){
			// add its location to a list, so we can perform actions on our list of items
			selectedList.add(i);
		} else{
			// remove this position from our list to modify
			selectedList.remove(Integer.valueOf(i));
		}
		// updates checked state
		arrayAdapter.notifyDataSetChanged();
	}

	@Override
	public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
		MenuInflater inflater = actionMode.getMenuInflater();
		inflater.inflate(R.menu.multi_set_action_menu, menu);
		return true;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
		return false;
	}

	@Override
	public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
		switch (menuItem.getItemId()) {
			case R.id.multi_set_context_settings:
				// settings
				startSelectedSetSettings();
				return true;
			case R.id.multi_set_context_rename:
				// rename
				getRenameSelectedSetsDialog(getView(), actionMode).show();
				return true;
			case R.id.multi_set_context_restore_defaults:
				// defaults
				getRestoreDefaultSelectedSetsDialog(getView(), actionMode).show();
				return true;
			case R.id.multi_set_context_delete:
				// delete
				getDeleteSelectedSetsDialog(getView(), actionMode).show();
				return true;
			default:
				return false;
		}
	}

	@Override
	public void onDestroyActionMode(ActionMode actionMode) {
		resetMultiSetActions();
	}

	/**
	 * Clears list of selected items and requests for ui to update to show the change
	 */
	private void resetMultiSetActions(){
		// empty list
		selectedList.clear();
		arrayAdapter.notifyDataSetChanged();
	}

	/**
	 * Starts the settings activity to handle settings for selected WallpaperLists
	 */
	private void startSelectedSetSettings(){
		// start the settings activity
		Intent intent = new Intent(this.getContext(), SettingsActivity.class);
		// tell settings activity what type of settings this is (image list)
		intent.putExtra(Constants.SETTINGS_IDENTIFIER_KEY, Constants.SETS_LIST_ITEMS_SETTINGS_REQUEST);
		// give it the positions of the items we want to edit
		intent.putExtra(Constants.SETTINGS_EDIT_SETS_KEY, selectedList);
		getActivity().startActivityForResult(intent, Constants.SETS_LIST_ITEMS_SETTINGS_REQUEST);
	}

	/**
	 * Removes specific settings from selected items
	 */
	private void restoreSelectedDefaults(){

		int lastId = sm.getLastSeenSetId();
		// remove from last to occur to first to occur
		for(int i = 0; i < selectedList.size(); i++){
			int pos = selectedList.get(i);
			int id = wallList.get(pos).getUniqueId();

			sm.getOverviewListOfSets().get(pos).restoreDefaultSettings();
			// expensive restoring of defaults to all applicable wallpapersets

			// load the wallpaper as active
			sm.loadWallpaperList(getContext(), id);
			// set its name
			sm.getWallpaperList().restoreDefaultSettings();
			wallList.get(i).restoreDefaultSettings();
			// save it back to disk
			sm.saveWallpaperList(getContext(), sm.getWallpaperList());
		}

		// reload original last seen wallpaperset
		sm.loadWallpaperList(getContext(), lastId);
		sm.saveIdList(getContext());
	}

	/**
	 * Renames the currently selected sets with the given string.
	 * Currently uses a probably inefficient and blocking method for updating the saved sets.
	 * @param newName String to assign as the name of selected sets
	 */
	private void renameSelectedSets(String newName){
		int lastId = sm.getLastSeenSetId();

		// remove from last to occur to first to occur
		for(int i = 0; i < selectedList.size(); i++){
			int pos = selectedList.get(i);
			int id = wallList.get(i).getUniqueId();

			sm.getOverviewListOfSets().get(pos).setName(newName);

			// expensive inserting of name to all applicable wallpapersets

			// load the wallpaper as active
			sm.loadWallpaperList(getContext(), id);
			// set its name
			sm.getWallpaperList().setName(newName);
			// save it back to disk
			sm.saveWallpaperList(getContext(), sm.getWallpaperList());
		}

		// reload original last seen wallpaperset
		sm.loadWallpaperList(getContext(), lastId);

		// update setting's list
//				sm.setOverviewListOfSets(wallList);
		// save changes
		sm.saveIdList(getContext());
	}

	/**
	 * Removes selected items from list and deletes related files from disk
	 */
	private void deleteSelectedSets(){
		// remove selected items
		// sort ascending
		Collections.sort(selectedList);

		// remove from last to occur to first to occur
		for(int i = (selectedList.size() - 1); i >= 0; i--){
			// get selected item's index in list of WallpaperLists
			int pos = selectedList.get(i);
			// get item's id
			int id = wallList.get(pos).getUniqueId();
			if(id == sm.getWallpaperList().getUniqueId()){
				// clear the list -> this will refuse to save, as it is an empty list with placeholder id
				sm.setWallpaperList(new WallpaperList());
			}
			sm.deleteWallpaperSet(getContext(), id);
			wallList.remove(pos);
		}

		// update setting's list
		sm.setOverviewListOfSets(wallList);
		// save changes
		sm.saveIdList(getContext());

		// notify array adapter
		arrayAdapter.notifyDataSetChanged();
	}

	/**
	 * Creates an AlertDialog for giving selected WallpaperLists a new name
	 * @param view View whose context to use
	 * @param actionMode ActionMode
	 * @return AlertDialog for renaming selected sets
	 */
	private AlertDialog getRenameSelectedSetsDialog(View view, final ActionMode actionMode){
		AlertDialog.Builder adb = new AlertDialog.Builder(view.getContext());

		adb.setMessage(R.string.rename_selected_sets_prompt);
		adb.setTitle(R.string.rename_selected_sets_title);

		LayoutInflater inflater = getActivity().getLayoutInflater();
		View inputView = inflater.inflate(R.layout.dialog_create_set, null);
		final EditText editText = (EditText) inputView.findViewById(R.id.setNameInputField);

		adb.setView(inputView);

		// user confirms deletion
		adb.setPositiveButton(R.string.rename_selected_sets_confirm, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// sort ascending
				Collections.sort(selectedList);
				String newName = "";

				// get inputted name
				try{
					// (empty allowed)
					newName = editText.getText().toString();
				} catch(Exception e){
					// Forgot what exception I was dealing with
					e.printStackTrace();
				}

				// perform renaming operations
				renameSelectedSets(newName);
				// notify array adapter
				arrayAdapter.notifyDataSetChanged();
				actionMode.finish();
			}
		});
		adb.setNegativeButton(R.string.rename_selected_sets_deny, null);
		return adb.create();
	}

	/**
	 * Builds an AlertDialog for restoring the default settings of the selected WallpaperLists
	 * @param view View whose context to use
	 * @param actionMode ActionMode
	 * @return AlertDialog for restoring default settings to selected sets
	 */
	private AlertDialog getRestoreDefaultSelectedSetsDialog(View view, final ActionMode actionMode) {
		AlertDialog.Builder adb = new AlertDialog.Builder(view.getContext());

		adb.setMessage(R.string.default_selected_sets_prompt);
		adb.setTitle(R.string.default_selected_sets_title);
		adb.setNegativeButton(R.string.default_selected_sets_deny, null);
		adb.setPositiveButton(R.string.default_selected_sets_confirm, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				// perform defaulting operations
				restoreSelectedDefaults();
				arrayAdapter.notifyDataSetChanged();
			}
		});

		return adb.create();
	}

	/**
	 * Builds an AlertDialog for deleting the selected WallpaperLists
	 * @param view View whose context to use
	 * @param actionMode ActionMode
	 * @return AlertDialog for deleting selected sets
	 */
	private AlertDialog getDeleteSelectedSetsDialog(View view, final ActionMode actionMode){
		AlertDialog.Builder adb = new AlertDialog.Builder(view.getContext());

		adb.setMessage(R.string.delete_selected_sets_prompt);
		adb.setTitle(R.string.delete_selected_sets_title);
		adb.setNegativeButton(R.string.delete_selected_sets_deny, null);
		adb.setPositiveButton(R.string.delete_selected_sets_confirm, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				deleteSelectedSets();
				// notify array adapter
				arrayAdapter.notifyDataSetChanged();
				// call main activity (due to nothing being used, just assumes list changed and handles appropriately
				bottomToolBar.updateRegisteredSetsText();
				actionMode.finish();
			}
		});

		return adb.create();
	}

	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated
	 * to the activity and potentially other fragments contained in that
	 * activity.
	 * <p>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
	public interface OnSetListInteractionListener {
		void onSetListInteraction();
	}


	/**
	 * Class to customize the displaying and handling of WallpaperLists in a listview
	 * @param <S>
	 */
	private class WallpaperSetArrayAdapter<S> extends ArrayAdapter<WallpaperList>
			implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener{

		private LayoutInflater mInflater;
		private Context context;

		public WallpaperSetArrayAdapter(Context context, int resource, List<WallpaperList> objects) {
			super(context, resource, objects);
			this.context = context;
		}

		@Override
		public View getView(int i, View convertView, ViewGroup vg){

			if(mInflater == null){
				mInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			}

			String name = wallList.get(i).getName();
			if(null == name){
				// display blank instead of null (if applicable)
				name = "";
			}
			String display = wallList.get(i).getUniqueId() + ": " + name;

			if(convertView == null){
				// if null, create the layout, otherwise we will recycle an old one
				convertView = mInflater.inflate(R.layout.set_list_item, null);
			}

			if(selectedList.contains(i)){
				// if selected
				convertView.setBackgroundColor(avh.getItemSelectedBGColor());
			} else if(wallList.get(i).isEdited()){
				// if edited
				convertView.setBackgroundColor(avh.getItemEditedBGColor());
			} else{
				// if not edited
				convertView.setBackgroundColor(avh.getItemDefaultBGColor());
			}

			((TextView) convertView.findViewById(R.id.setItemTextView)).setText(display);

			// get image view
			ImageView iv = (ImageView) convertView.findViewById(R.id.setPreviewImageView);

			// get a WallpaperList with valid settings
			WallpaperList tmp = sm.copyWallpaperListWithoutNull(wallList.get(i));
			// remove any previous image from imageview
			iv.setImageBitmap(null);

			// if the list or global setting says to show preview
			if(tmp.getShowPreview()){
				// get path to preview image
				String loc = sm.getPreviewFileName(getContext(), wallList.get(i).getUniqueId());
				String dir = getContext().getFilesDir() + getContext().getString(R.string.wallpaper_list_preview_image_dir);
				// load preview image into the image view
				Picasso.with(this.context).load(new File(dir + "/" + loc)).noPlaceholder()
						.resize(Constants.PREVIEW_IMAGE_SIZE, Constants.PREVIEW_IMAGE_SIZE).into(iv);
			} else{
				// image has been hidden no more work needed
			}

			return convertView;
		}

		@Override
		public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
			// load clicked item
			int selected = wallList.get(i).getUniqueId();
			// set as active?
			sm.loadWallpaperList(this.getContext(), selected);
			// update the set we are working with
			sm.setNewWorkingSet(selected);

			mListener.onSetListInteraction();
		}

		@Override
		public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
			return false;
		}
	}

}
