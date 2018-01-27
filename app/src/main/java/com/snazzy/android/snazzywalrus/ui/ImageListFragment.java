package com.snazzy.android.snazzywalrus.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.snazzy.android.snazzywalrus.Constants;
import com.snazzy.android.snazzywalrus.R;
import com.snazzy.android.snazzywalrus.helper.AndroidValueHelper;
import com.snazzy.android.snazzywalrus.service.SavingService;
import com.snazzy.android.snazzywalrus.WallpaperInteractor;
import com.snazzy.android.snazzywalrus.util.SettingsManager;
import com.snazzy.android.snazzywalrus.util.WallpaperList;
import com.snazzy.android.snazzywalrus.util.WallpaperManipulator;
import com.snazzy.android.snazzywalrus.util.Wallpaper;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import paul.arian.fileselector.FileSelectionActivity;
import paul.arian.fileselector.FolderSelectionActivity;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

/**
 */
@RuntimePermissions
public class ImageListFragment extends Fragment implements View.OnClickListener, AbsListView.MultiChoiceModeListener,
		BottomMenuFragment.OnBottomMenuFragmentClickListener{

	private ListView fileListView;
	private WallpaperArrayAdapter<String> arrayAdapter;
	private SettingsManager sm = SettingsManager.getInstance();
	private WallpaperList imageList = new WallpaperList();
	private ArrayList<Integer> selectedList = new ArrayList<>();

	private AndroidValueHelper avh;

	private RelativeLayout chooserLayout;
	private BottomMenuFragment bottomToolBar;

	// don't really know why this needs to be upload, but seems to fail when I change it
	private static final String FILES_TO_UPLOAD = "upload";

	private static final String REQUEST_IDENTIFIER = "key_chooser_type";
	private static final int REQUEST_SINGLE_IMAGE_CHOOSER = 200;
	private static final int REQUEST_MULTI_IMAGE_CHOOSER = 201;
	private static final int REQUEST_FOLDER_CHOOSER = 202;

	/**
	 * Size to display a single image relative to device size (width?)
	 */
	private static final double SINGLE_IMAGE_PCT_SIZE = .8;
	private static final int LIST_IMAGE_SIZE = 75;

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @return A new instance of fragment ImageListFragment.
	 */
	public static ImageListFragment newInstance() {
		return new ImageListFragment();
	}

	public ImageListFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_image_list, container, false);
		avh = new AndroidValueHelper(getContext());

		// get the chooser ui and make sure it is initially hidden
		chooserLayout = (RelativeLayout) view.findViewById(R.id.methodChooserOverlayLayout);
		chooserLayout.setVisibility(View.GONE);

		// get current list of files
		imageList.addAll(sm.getWallpaperList());
		imageList.setUniqueId(sm.getWallpaperList().getUniqueId());
		imageList.setName(sm.getWallpaperList().getName());
		// set the array adapter to watch the list
		arrayAdapter = new WallpaperArrayAdapter<>(this.getContext(), android.R.layout.simple_list_item_1, imageList);

		// get our list view
		fileListView = (ListView) view.findViewById(R.id.imageListView);
		// set adapter for list view
		fileListView.setAdapter(arrayAdapter);
		// handle list item clicks
		//fileListView.setOnItemClickListener(arrayAdapter);
		//fileListView.setOnItemLongClickListener(arrayAdapter);

		fileListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		fileListView.setMultiChoiceModeListener(this);
		//registerForContextMenu(fileListView);

		if(null == bottomToolBar){
			bottomToolBar = (BottomMenuFragment) getChildFragmentManager().findFragmentById(R.id.imageListBottomMenuFragment);
		}

		return view;
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	@Override
	public void onDestroyView(){
		WallpaperList tmp = sm.copyWallpaperListWithoutNull(sm.getWallpaperList());
		if(0 < sm.getWallpaperList().size() && tmp.getShowPreview()){
			// create preview image if list has pictures and is set to show a preview
			sm.savePreview(getContext(), sm.getWallpaperList().genPreview(), sm.getWallpaperList().getUniqueId());
		}
		super.onDestroyView();
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()){
			case R.id.chooseMultiRadioButton:
				// handle multiple item chooser
				// reset and hide prompt
				promptChooseMethodCleanup();
				// start picker with proper request
				ImageListFragmentPermissionsDispatcher.invokePickerWithCheck(this, REQUEST_MULTI_IMAGE_CHOOSER);
				break;
			case R.id.chooseFolderRadioButton:
				// handle folder chooser
				// reset and hide prompt
				promptChooseMethodCleanup();
				// start picker with proper request
				ImageListFragmentPermissionsDispatcher.invokePickerWithCheck(this, REQUEST_FOLDER_CHOOSER);
				break;
			case R.id.methodChooserOverlayLayout:
				// clicked outside options (on overlay), so reset and hide prompt
				promptChooseMethodCleanup();
				break;
			default:
				break;
		}
	}

	/**
	 * Displays prompt for what method users want to choose files with.
	 * @param v The view that the prompt items can be found in
	 */
	public void promptChooseMethod(View v){
		// show our overlay
		chooserLayout.setVisibility(View.VISIBLE);
		// make sure buttons have listeners on them
		chooserLayout.findViewById(R.id.chooseMultiRadioButton).setOnClickListener(this);
		chooserLayout.findViewById(R.id.chooseFolderRadioButton).setOnClickListener(this);
		// listen for clicks other than on buttons to remove overlay
		chooserLayout.setOnClickListener(this);
	}

	/**
	 * Resets and re-hides the method prompt
	 */
	private void promptChooseMethodCleanup(){
		// reset checked status for future uses
		((RadioButton) chooserLayout.findViewById(R.id.chooseMultiRadioButton)).setChecked(false);
		((RadioButton) chooserLayout.findViewById(R.id.chooseFolderRadioButton)).setChecked(false);
		// hide overlay
		chooserLayout.setVisibility(View.GONE);
	}

	/**
	 * Starts the file chooser activity based on give choice type
	 * Requires: READ_EXTERNAL_STORAGE permissions
	 * @param chooserType The integer representing what chooser to use.
	 * Supported values are: REQUEST_SINGLE_IMAGE_CHOOSER = 200,
	 *                       REQUEST_MULTI_IMAGE_CHOOSER = 201,
	 *                       REQUEST_FOLDER_CHOOSER = 202
	 */
	@NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
	public void invokePicker(int chooserType) {
		Intent intent;
		int request = Constants.WALLPAPER_LIST_ITEMS_REQUEST;

		switch(chooserType){
			case REQUEST_SINGLE_IMAGE_CHOOSER:
				// get location of pictures (MediaStore?)
				File file = new File(Environment.DIRECTORY_PICTURES);
				Uri uri = Uri.fromFile(file);
				// single image chooser (completely built in, but severely limited)
				intent = new Intent(Intent.ACTION_PICK, uri);
				intent.setType("image/*");
				intent.putExtra(REQUEST_IDENTIFIER, request);
				getActivity().startActivityForResult(Intent.createChooser(intent, "Complete action using"), REQUEST_SINGLE_IMAGE_CHOOSER);
				break;
			case REQUEST_MULTI_IMAGE_CHOOSER:
				// multi-image chooser
				intent = new Intent(this.getContext(), FileSelectionActivity.class);
				intent.putExtra(REQUEST_IDENTIFIER, REQUEST_MULTI_IMAGE_CHOOSER);
				getActivity().startActivityForResult(intent, request);
				break;
			case REQUEST_FOLDER_CHOOSER:
				// folder chooser
				intent = new Intent(this.getContext(), FolderSelectionActivity.class);
				intent.putExtra(REQUEST_IDENTIFIER, REQUEST_FOLDER_CHOOSER);
				getActivity().startActivityForResult(intent, request);
				break;
			default:
				break;
		}
	}

	/**
	 * Handles file chooser activity. Gets the return from the activity and add valid images to list
	 * CURRENTLY DOES NOT HANDLE SINGLE FILE CHOOSER
	 * @param requestCode The request code (altered from what was originally set due to going through MainActivity)
	 * @param resultCode The integer result code
	 * @param data The Intent data returned from activity
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data){
//		Log.d("activityResult", "here " + resultCode + " " + requestCode);
		if (resultCode != Activity.RESULT_OK) {
//			Log.d("activityResult", "Bad result " + Activity.RESULT_OK);
			return;
		}

		int reqType = -1;
		try{
			reqType = data.getIntExtra(REQUEST_IDENTIFIER, -1);
		} catch(RuntimeException e){
			// probably not important (aka coming from settings instead of chooser)
		}

		ArrayList<File> files;

		if(reqType == REQUEST_MULTI_IMAGE_CHOOSER){
			// get returned list of files
			files = (ArrayList<File>) data.getSerializableExtra(FILES_TO_UPLOAD);
			getListFromMultiSelect(files);
		} else if (reqType == REQUEST_FOLDER_CHOOSER) {
			// get returned folder path
			String folderPath = data.getSerializableExtra(FILES_TO_UPLOAD).toString();
			getListFromFolderSelect(folderPath);
		} else if(requestCode == Constants.WALLPAPER_LIST_ITEMS_REQUEST){
			// request is from selected item settings
			// update held list
			updateDisplayedList();
		}

		arrayAdapter.notifyDataSetChanged();
//		sm.saveWallpaperList(getContext(), imageList);
		// save
		SavingService ss = new SavingService(this.getContext());
		ss.execute(true);
		bottomToolBar.updateRegisteredImagesText();
	}

	/**
	 * Takes the given files and adds each file, which has valid extension, to the lst of files
	 * @param files ArrayList of files to go through
	 */
	private void getListFromMultiSelect(ArrayList<File> files){
		for(File file : files){
			if(!sm.fileHasValidExt(file)){
				// file does not have valid extension
				continue;
			}
			// if repeats are allowed, or it is not a repeat
			if(sm.getAllowRepeatedEntries() || !sm.isDuplicateFile(file.getAbsolutePath()) ){
				// add to list
				imageList.add(new Wallpaper(file.getAbsolutePath()));
			}
		}
		sm.setWallpaperList(imageList);
	}

	/**
	 * Takes the string representing the folder path and adds all valid images from it to the list.
	 * @param folderPath String representing the folder's path
	 */
	private void getListFromFolderSelect(String folderPath){
		// grab the files and add to list
		imageList.addAll(sm.getFilteredFilesFromDirectory(folderPath));
		sm.setWallpaperList(imageList);
	}

	/**
	 * Resets the list and notifies the adapter of changes
	 */
	public void updateDisplayedList(){
		// apparently, imageList is suddenly pointing at sm.wallpaperList, so editing one edits the other or something...
		WallpaperList tmp = new WallpaperList(sm.getWallpaperList().getUniqueId(), sm.getWallpaperList().getName());
		tmp.addAll(sm.getWallpaperList());
		imageList.clear();
		imageList.addAll(tmp);
		if(sm.getWallpaperList().size() != tmp.size()){
			sm.getWallpaperList().clear();
			sm.getWallpaperList().addAll(tmp);
		}

		arrayAdapter.notifyDataSetChanged();
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
		inflater.inflate(R.menu.multi_image_action_menu, menu);
		return true;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
		return false;
	}

	@Override
	public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
		switch (menuItem.getItemId()) {
			case R.id.multi_image_context_settings:
				startSelectedImageSettings();
				// TODO: allow user to return with items still selected...
				actionMode.finish();
				arrayAdapter.notifyDataSetChanged();
				return true;
			case R.id.multi_image_context_restore_defaults:
				checkRevertSelectedToDefault();
				arrayAdapter.notifyDataSetChanged();
				return true;
			case R.id.multi_image_context_delete:
				// ensure we have selected items
				if(selectedList.size() > 0){
					// TODO: look for some way to retain the state of selection/whatnot after starting activity and returning
					getRemoveSelectedImageDialog(getView(), actionMode).show();
				}
				arrayAdapter.notifyDataSetChanged();
				return true;
			default:
				return false;
		}
	}

	@Override
	public void onDestroyActionMode(ActionMode actionMode) {
		resetMultiItemActions();
	}

	private void resetMultiItemActions(){
		// empty list
		selectedList.clear();
		arrayAdapter.notifyDataSetChanged();
	}

	private void startSelectedImageSettings(){
		// start the settings activity
		Intent intent = new Intent(this.getContext(), SettingsActivity.class);
		// tell settings activity what type of settings this is (image list)
		intent.putExtra(Constants.SETTINGS_IDENTIFIER_KEY, Constants.WALLPAPER_LIST_ITEMS_REQUEST);
		// give it the positions of the items we want to edit
		intent.putExtra(Constants.SETTINGS_EDIT_LIST_KEY, selectedList);
		getActivity().startActivityForResult(intent, Constants.WALLPAPER_LIST_ITEMS_REQUEST);
	}

	private void checkRevertSelectedToDefault(){
		// create and show alert dialog
		getRestoreDefaultDialog().show();
	}

	private void restoreSelectedDefaults(){
		for(int i = 0; i < selectedList.size(); i++){
			imageList.get(selectedList.get(i)).restoreDefault();
		}
	}

	/**
	 *
	 * @param view View whose context to use
	 * @return AlertDialog for this action
	 */
	public AlertDialog getClearListDialog(final View view){
		AlertDialog.Builder adb = new AlertDialog.Builder(view.getContext());
		adb.setMessage(R.string.remove_all_image_prompt);
		// user confirms deletion
		adb.setPositiveButton(R.string.remove_all_image_confirm, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				imageList.clear();
				// update setting's list
				sm.setWallpaperList(imageList);
				// save list to file
				SavingService ss = new SavingService(view.getContext());
				ss.execute(true);
				// notify array adapter
				arrayAdapter.notifyDataSetChanged();

				// get main activity to get text of registered images to update
				bottomToolBar.updateRegisteredImagesText();
			}
		});
		adb.setNegativeButton(R.string.remove_all_image_deny, null);
		adb.setTitle(R.string.remove_all_image_title);
		return adb.create();
	}

	private AlertDialog getRemoveSelectedImageDialog(final View view, final ActionMode actionMode){
		// create alert dialog
		AlertDialog.Builder adb = new AlertDialog.Builder(view.getContext());

		adb.setMessage(R.string.remove_multi_image_prompt);
		adb.setTitle(R.string.remove_multi_image_title);

		// user confirms deletion
		adb.setPositiveButton(R.string.remove_multi_image_confirm, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// remove selected items
				// sort ascending
				Collections.sort(selectedList);

				// remove from last to occur to first to occur
				for(int i = (selectedList.size() - 1); i >= 0; i--){
					imageList.remove((int) (selectedList.get(i)));
				}

				// update setting's list
				sm.setWallpaperList(imageList);
				// save list to file
				SavingService ss = new SavingService(view.getContext());
				ss.execute(true);

				// notify array adapter
				arrayAdapter.notifyDataSetChanged();
				// tell toolbar that value may have changed
				bottomToolBar.updateRegisteredImagesText();
				actionMode.finish();
			}
		});
		adb.setNegativeButton(R.string.remove_multi_image_deny, null);
		return adb.create();
	}

	private AlertDialog getRestoreDefaultDialog(){
		// create alert dialog
		AlertDialog.Builder adb = new AlertDialog.Builder(this.getActivity());

		adb.setTitle(R.string.restore_defaults_on_selected_title);
		adb.setMessage(R.string.restore_defaults_on_selected_prompt);

		// user confirms deletion
		adb.setPositiveButton(R.string.restore_defaults_on_selected_confirm, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				restoreSelectedDefaults();
				// save the changes
				SavingService ss = new SavingService(getActivity());
				ss.execute(true);
			}
		});
		adb.setNegativeButton(R.string.restore_defaults_on_selected_deny, null);
		return adb.create();
	}

	private void restoreAllDefaults(){
		for(Wallpaper wall : imageList){
			wall.restoreDefault();
		}
	}

	@Override
	public void onBottomMenuFragmentClickListener(View view) {
		switch (view.getId()){
			case R.id.bottomAddButton:
				promptChooseMethod(view.getRootView());
				break;
			case R.id.bottomDeleteButton:
				getClearListDialog(view.getRootView()).show();
				break;
			default:
				break;
		}
	}

	/**
	 * Provides special display logic for our list of wallpapers, displaying a preview image along with original text
	 * @param <S>
	 */
	private class WallpaperArrayAdapter<S> extends ArrayAdapter<Wallpaper>
			implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener{

		private LayoutInflater mInflater;
		Context context;

		public WallpaperArrayAdapter(Context context, int resource, List<Wallpaper> objects) {
			super(context, resource, objects);
			this.context = context;
		}

		@Override
		public View getView(int i, View convertView, ViewGroup vg){
			String loc = imageList.get(i).getSrc();

			if(mInflater == null){
				mInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			}

			if(convertView == null){
				// if null, create the layout, otherwise we will recycle an old one
				convertView = mInflater.inflate(R.layout.image_list_item, null);
			}

			if(selectedList.contains(i)){
				// if selected
				convertView.setBackgroundColor(avh.getItemSelectedBGColor());
			} else if(imageList.get(i).isEdited()){
				// if it is edited, change background color
				convertView.setBackgroundColor(avh.getItemEditedBGColor());
			} else{
				// if not edited
				convertView.setBackgroundColor(avh.getItemDefaultBGColor());
			}

			ImageView iv = (ImageView) convertView.findViewById(R.id.imageListItemImageView);
			TextView tv = (TextView) convertView.findViewById(R.id.imageListItemTextView);

			iv.setImageBitmap(null);
			// load image
			// TODO: figure out good size scheme?
			Picasso.with(this.context).load(new File(loc)).noPlaceholder().resize(LIST_IMAGE_SIZE,LIST_IMAGE_SIZE).into(iv);
			// set text to the file path
			tv.setText(loc);

			return convertView;
		} // end getView

		/**
		 * Creates an AlertDialog prompting removal of a single image
		 * @param view View reference
		 * @param position Integer position of item to remove
		 * @return AlertDialog that prompts for removal of an item
		 */
		private AlertDialog getRemoveSingleImageDialog(final View view, final int position){

			// create alert dialog
			AlertDialog.Builder adb = new AlertDialog.Builder(view.getContext());

			try{
				// get the image location
				String extra = imageList.get(position).getSrc();

				// inflate the dialog view
				LayoutInflater inflater = getActivity().getLayoutInflater();
				View v = inflater.inflate(R.layout.dialog_remove_image, null);
				// set the text view to the image location
				((TextView) v.findViewById(R.id.dialogTextView)).setText(extra);
				// get the image view
				ImageView iv = (ImageView) v.findViewById(R.id.dialogImageView);

				WallpaperManipulator wm = new WallpaperManipulator();
				WallpaperInteractor wi = new WallpaperInteractor(this.getContext());
				// get the image
				Bitmap bm = BitmapFactory.decodeFile(extra);

				// scale image to (roughly) 80% screen width
				//TODO: scale based on closest dimension?
				double tmpWidth = wi.getRoughDeviceWidth() * SINGLE_IMAGE_PCT_SIZE;
				int newWidth = (int) tmpWidth;
				//int newHeight = wm.getScaledHeight(bm, newWidth);

				// get filename to set as description
				String fileName = extra.substring(extra.lastIndexOf('/') + 1);
				// set description (I guess, for accessibility)
				iv.setContentDescription(fileName);

				// set to new scaled bitmap
				iv.setImageBitmap(wm.relativeResizeByWidth(bm, newWidth));
//				Picasso.with(this.getContext()).load(new File(extra)).noPlaceholder().resize(newWidth, newHeight).into(iv);

				// set the view in the builder
				adb.setView(v);
			} catch(NullPointerException e){
				e.printStackTrace();
			}

			// user confirms deletion
			adb.setPositiveButton(R.string.remove_image_confirm, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// get the item
					// Remove the item
					imageList.remove(position);
					// update setting's list
					sm.setWallpaperList(imageList);
					// save list to file
					SavingService ss = new SavingService(view.getContext());
					ss.execute(true);

					// notify array adapter
					arrayAdapter.notifyDataSetChanged();
					//
					bottomToolBar.updateRegisteredImagesText();
				}
			});
			adb.setNegativeButton(R.string.remove_image_deny, null);
			adb.setTitle(R.string.remove_image_title);
			return adb.create();
		}

		/**
		 * Handles click of items in list. NOT IN USE
		 * @param adapterView The adapterView to use
		 * @param view The view whose context to use
		 * @param position The integer position of the item clicked in the list of items
		 * @param l Long whose purpose and identity is currently unknown!!
		 */
		@Override
		public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
		}

		@Override
		public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
			return true;
		}

	} // end WallpaperArrayAdapter class

}
