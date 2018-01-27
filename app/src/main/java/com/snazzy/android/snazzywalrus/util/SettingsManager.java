package com.snazzy.android.snazzywalrus.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.util.Log;

import com.snazzy.android.snazzywalrus.R;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by SnazzyPanda on 6/15/16.
 *
 * Central location for application settings for accessibility
 */
public class SettingsManager {

	private static SettingsManager sm;

	/**
	 * An array of valid file extension that can be used as backgrounds
	 */
	public static final String[] ACCEPTED_EXTS = {"jpg", "jpeg", "png", "bmp", "webp"};

	private static final String PREF_KEY_INTERVAL = "key_interval";
	private static final String PREF_KEY_RANDOM_NEXT = "key_random_next";
	private static final String PREF_KEY_SHOW_PREVIEW = "key_show_preview";
	private static final String PREF_KEY_RESIZE = "key_resize";
	private static final String PREF_KEY_RESIZE_HEIGHT = "key_resize_height";
	private static final String PREF_KEY_RESIZE_WIDTH = "key_resize_width";
	private static final String PREF_KEY_CROP = "key_crop";
	private static final String PREF_KEY_CROP_LOC = "key_crop_location";
	private static final String PREF_KEY_DUPLICATE = "key_duplicate";
	private static final String PREF_KEY_SUBDIR = "key_subdirs";
	private static final String PREF_KEY_THEME_ID = "key_theme_id";
	private static final String PREF_KEY_GLOBAL = "key_global";

	private static final String PREF_LAST_SET_ID = "key_last_set_id";
	/**
	 * Number of times we try the next genereated id before grabbing last id in list and using next value
	 */
	private static final int ITERATIVE_ID_GEN_LIMIT = 10;

	private int intervalInMinutes = 30;
	private boolean randomNext = true;
	private boolean showPreview = true;

	private boolean useResize = true;
	private boolean resizeHeight = true;
	private boolean resizeWidth = false;

	private boolean useCrop = true;
	/**
	 * Specifies the location to crop image from.
	 * Supported values come from {@link WallpaperManipulator} and include:
	 * <p/>
	 * WallpaperManipulator.CROP_FROM_RANDOM = 0
	 * WallpaperManipulator.CROP_FROM_LEFT = 1
	 * WallpaperManipulator.CROP_FROM_RIGHT = 2
	 * WallpaperManipulator.CROP_FROM_CENTER = 3
	 *
	 */
	private int cropLocation = WallpaperManipulator.CROP_FROM_RANDOM;

	private WallpaperList wallpaperList = new WallpaperList();
	private ArrayList<WallpaperList> overviewListOfSets = new ArrayList<>();
	// identifier of last known list for restoring list on load
	private int lastSeenSetId = -1;
	// I guess? to try and have unique, but probably may as well use int and manage myself...
	private int lastGeneratedInt = -1;
//	private AtomicInteger ai = new AtomicInteger();

	/**
	 * If true when adding directories, we will go through subdirectories and add all valid content from them as well
	 */
	private boolean includeSubDirectories = false;

	/**
	 * If true, we do not do redundancy checking and will allow user to add a single image multiple times.
	 */
	private boolean allowRepeatedEntries = false;

	/**
	 * Stores the last index from the list of files that was used as a wallpaper.
	 * This value is not saved to disk, and so it should not be expected to persist.
	 */
	private int lastIndexPosition = -1;

	private int themeId = -1;
	// TODO: see if better way than this to prevent rapid changes
	private long lastChange = 0;
	private boolean globalOnly = false;

	// end vars start functions


	private SettingsManager(){
		super();
	}

	/**
	 * Get the instance of the SettingsManager
	 * @return SettingeManager instance
	 */
	public static SettingsManager getInstance(){
		if (sm == null) {
			sm = new SettingsManager();
		}
		return sm;
	}

	public int getIntervalInMinutes(){
		return this.intervalInMinutes;
	}

	public void setIntervalInMinutes(int interval){
		this.intervalInMinutes = interval;
	}

	public boolean getRandomNext(){
		return this.randomNext;
	}

	public void setRandomNext(boolean random){
		this.randomNext = random;
	}

	public boolean getShowPreview(){
		return this.showPreview;
	}

	public void setShowPreview(boolean preview){
		this.showPreview = preview;
	}

	public boolean getUseCrop(){
		return this.useCrop;
	}

	public void setUseCrop(boolean use){
		this.useCrop = use;
	}

	public boolean getUseResize(){
		return this.useResize;
	}

	public void setUseResize(boolean use){
		this.useResize = use;
	}

	public boolean getResizeHeight(){
		return this.resizeHeight;
	}

	public void setResizeHeight(boolean resize){
		this.resizeHeight = resize;
	}

	public boolean getResizeWidth(){
		return this.resizeWidth;
	}

	public void setResizeWidth(boolean resize){
		this.resizeWidth = resize;
	}

	public boolean getIncludeSubDirectories(){
		return this.includeSubDirectories;
	}

	public void setIncludeSubDirectories(boolean include){
		this.includeSubDirectories = include;
	}

	public WallpaperList getWallpaperList(){
		return this.wallpaperList;
	}

	public void setWallpaperList(WallpaperList list){
		this.wallpaperList = list;
	}

	public ArrayList<WallpaperList> getOverviewListOfSets(){
		return this.overviewListOfSets;
	}

	public void setOverviewListOfSets(ArrayList<WallpaperList> list){
		this.overviewListOfSets = list;
	}

	public boolean getAllowRepeatedEntries(){
		return this.allowRepeatedEntries;
	}

	public void setAllowRepeatedEntries(boolean allow){
		this.allowRepeatedEntries = allow;
	}

	public int getCropLocation(){
		return this.cropLocation;
	}

	public void setCropLocation(int loc){
		this.cropLocation = loc;
	}

	public int getLastIndexPosition(){
		return this.lastIndexPosition;
	}

	public void setLastIndexPosition(int newpos){
		this.lastIndexPosition = newpos;
	}

	public int getLastSeenSetId(){
		return this.lastSeenSetId;
	}

	public int getThemeId(){
		return this.themeId;
	}

	public void setThemeId(int id){
		this.themeId = id;
	}

	public void addToListOfDirs(String file){
		if(this.allowRepeatedEntries){
			this.addToListOfFileNoRepeat(file);
		} else{
			this.wallpaperList.add(new Wallpaper(file));
		}
	}

	public long getLastChange(){
		return this.lastChange;
	}

	public void setLastChange(long time){
		this.lastChange = time;
	}

	public boolean getGlobalOnly(){
		return this.globalOnly;
	}

	public void setGlobalOnly(boolean global){
		this.globalOnly = global;
	}

	public void addToListOfDirs(File file){
		this.addToListOfDirs(file.getPath());
	}

	/**
	 * Gets the first index of Wallpaper in the listOfFiles that has a src equal to given dir
	 * @param dir String directory for comparison
	 * @return first index if it is in the list, or -1 if it was not found
	 */
	public int getFirstIndexForDir(String dir){
		for(int i = 0; i < this.wallpaperList.size(); i++){
			if(this.wallpaperList.get(i).getSrc().equals(dir)){
				return i;
			}
		}
		return -1;
	}

	/**
	 * Removes first Wallpaper whose src matches given dir
	 * @param dir String dir of Wallpaper to remove
	 * @return true if removed, otherwise false
	 */
	public boolean removeFromListOfFiles(String dir){
		int index = getFirstIndexForDir(dir);
		if(-1 != index){
			this.wallpaperList.remove(index);
			return true;
		}
		return false;
	}

	public boolean removeFromListOfFiles(File dir){
		return this.removeFromListOfFiles(dir.getAbsolutePath());
	}

	public boolean removeFromListOfFiles(Wallpaper wall){
		return this.removeFromListOfFiles(wall.getSrc());
	}

	public ArrayList<Wallpaper> getFilteredFilesFromDirectory(String dir){
		ArrayList<Wallpaper> tmp = new ArrayList<>();
		File folder = new File(dir);
		for (final File fileEntry : folder.listFiles()) {
			// if it is a directory AND we are going through subdirectories
			if (fileEntry.isDirectory() && this.includeSubDirectories) {
				// go through the subdirectory as well
				tmp.addAll(getFilteredFilesFromDirectory(fileEntry.getAbsolutePath()));
			} else {
				// if it has valid extension
				if(fileHasValidExt(fileEntry.getAbsolutePath())){
					// and repeats are allowed OR it is not a repeat
					if(this.allowRepeatedEntries || !this.isDuplicateFile(fileEntry.getAbsolutePath())){
						// add to list
						tmp.add(new Wallpaper(fileEntry.getAbsolutePath()));
					}
				}
			}
		}

		return tmp;
	}

	public ArrayList<Wallpaper> getFilteredFilesFromDirectory(File dir){
		return getFilteredFilesFromDirectory(dir.getAbsolutePath());
	}

	public void addFullDirectoryToList(String dir){
		File folder = new File(dir);
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory() && this.includeSubDirectories) {
				addFullDirectoryToList(fileEntry.getAbsolutePath());
			} else {
				if(fileHasValidExt(fileEntry.getAbsolutePath())){
					addToListOfDirs(fileEntry);
				}
			}
		}
	}

	public void addFullDirectoryToList(File dir){
		this.addToListOfDirs(dir.getAbsolutePath());
	}

	/**
	 * Checks if the file extension matches any of the values in the ACCEPTED_EXTS array.
	 * Checks are done with everything past the final '.' in the final name, case insensitive
	 * @param file String file path and/or name to check
	 * @return True if given file's extension matches any valid ext
	 */
	public boolean fileHasValidExt(String file){
		String ext = file.substring(file.lastIndexOf('.') + 1);
		for(String acceptedExt : ACCEPTED_EXTS){
			if(acceptedExt.toLowerCase().equals(ext.toLowerCase())){
				return true;
			}
		}
		return false;
	}

	public boolean fileHasValidExt(File file){
		return this.fileHasValidExt(file.getAbsolutePath());
	}

	/**
	 * Returns an ArrayList containing any non-duplicated file in given list
	 * @param list ArrayList of files to go through
	 * @return ArrayList without duplicated files from list
	 */
	public ArrayList<String> getFilesWithoutDuplicates(ArrayList<String> list){
		ArrayList<String> tmp = new ArrayList<>();

		for(String file: list){
			if(!this.isDuplicateFile(file)){
				tmp.add(file);
			}
		}

		return tmp;
	}

	private boolean addToListOfFileNoRepeat(String file){
		boolean duplicate = isDuplicateFile(file);
		if(duplicate){
			return false;
		} else{
			this.wallpaperList.add(new Wallpaper(file));
			return true;
		}
	}

	private boolean addToListOfFileNoRepeat(File file){
		return this.addToListOfFileNoRepeat(file.getAbsolutePath());
	}

	private boolean addToListOfFileNoRepeat(Wallpaper wall){
		return this.addToListOfFileNoRepeat(wall.getSrc());
	}

	public boolean isDuplicateFile(String file){
		for(int i = 0; i < this.wallpaperList.size(); i++){
			if(this.wallpaperList.get(i).getSrc().equals(file)){
				return true;
			}
		}
		return false;
	}

	public boolean isDuplicateFile(File file){
		return this.isDuplicateFile(file.getAbsolutePath());
	}

	public boolean isDuplicateFile(Wallpaper wall){
		return this.isDuplicateFile(wall.getSrc());
	}

	public void printCurrentSettings(){
		String s = "interval: " + intervalInMinutes
				+ "\nRandomize: " + randomNext
				+ "\nTheme: " + themeId
				+ "\nResize: " + useResize
				+ "\nResizeHeight: " + resizeHeight
				+ "\nResizeWidth: " + resizeWidth
				+ "\nCrop: " + useCrop
				+ "\nCrop Location: " + cropLocation
				+ "\nInclude Subdir: " + includeSubDirectories
				+ "\nAllow Repeats: " + allowRepeatedEntries;

		Log.v("settings", s);
	}

	/**
	 * Removes Wallpapers that no longer exist on disk from the current WallpaperList
	 */
	private void cleanList(){
		int last = sm.getWallpaperList().size() - 1;
		for(int i = last; 0 < i; i--){
			if(!(new File(sm.wallpaperList.get(i).getSrc())).exists()){
				sm.wallpaperList.remove(i);
			}
		}
	}

	/**
	 * Saves the wallpaper set if it is either a non-empty placeholder, or a placeholder containing items
	 * @param context Context to use
	 * @param list WallpaperList to save
	 */
	public void saveWallpaperList(Context context, WallpaperList list){
		if(1 > list.size() && WallpaperList.ID_PLACEHOLDER == list.getUniqueId()){
			// nothing in the list and placeholder id, do not save
			return;
		}
		if(WallpaperList.ID_PLACEHOLDER == list.getUniqueId()){
			// non-empty placeholder, create new id then save
			int id = getUnusedId();
			list.setUniqueId(id);
			this.overviewListOfSets.add(WallpaperList.getBriefListFrom(list));
			this.saveIdList(context);
			// save list of ids
		}

		// get the JSONObject as a string
		String json = list.getAsJsonObject().toString();
//		Log.i("save", json);
		FileOutputStream fos = null;
		try {
			fos = context.openFileOutput(
					getListFileName(context, list.getUniqueId()),
					Context.MODE_PRIVATE);
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
			// write out our JSONArray
			writer.write(json);
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch(IOException io){
			io.printStackTrace();
		}
	}

	/**
	 * Loads wallpaper list using given id
	 * @param context
	 * @param id
	 */
	public void loadWallpaperList(Context context, int id){
		String jstring;
		// open and read contents of file
		try {
			InputStream is = context.openFileInput(
					getListFileName(context, id));
			int size = is.available();
			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();
			jstring = new String(buffer, "UTF-8");
		} catch (IOException e) {
			Log.w("load", "error loading file: " + getListFileName(context, id) + ", file may not exist");
			jstring = "";
			//return;
		}

//		Log.d("loadList", jstring);

		try {
			if(0 == jstring.length()){
				// if list is empty
				this.wallpaperList.clear();
			} else{
				// list not empty, so populate with what was loaded
				JSONObject jsonObject = new JSONObject(jstring);
				this.wallpaperList.clear();
				this.wallpaperList.populateFromJsonObject(jsonObject);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Saves the brief list of existing sets to disk
	 * @param context Context to use
	 */
	public void saveIdList(Context context){
		FileOutputStream fos = null;
		try {
			fos = context.openFileOutput(context.getString(
					R.string.local_id_list_file),
					Context.MODE_PRIVATE);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if(null == fos){
			return;
		}

		JSONArray jsonArray = new JSONArray();
		if(!this.overviewListOfSets.contains(this.wallpaperList)){
			if (0 < this.wallpaperList.size() || WallpaperList.ID_PLACEHOLDER != this.wallpaperList.getUniqueId()) {
				// if current wallpaper not in sets, and not empty placeholder
				this.overviewListOfSets.add(WallpaperList.getBriefListFrom(this.wallpaperList));
			} else {
				// do nothing since list is empty and has the placeholder id
			}
		}
		ArrayList<WallpaperList> tmpset = new ArrayList<>();
		// for each list in overviewListOfSets, create a json object and add it to json array
		for(WallpaperList list : this.overviewListOfSets){
			// as long as the id is not a placeholder id
			if(WallpaperList.ID_PLACEHOLDER != list.getUniqueId()){
				// add item to a temp list (this will filter out placeholders)
				tmpset.add(list);
				// do not save the placeholder
				jsonArray.put(list.getAsBriefJsonObject());
			}
		}
		// reset set list to have no placeholder
		this.setOverviewListOfSets(tmpset);

//		Log.v("saveID", jsonArray.toString());
		// Create buffered writer
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
		try{
			writer.write(jsonArray.toString());
			writer.close();
		} catch(IOException e){
			e.printStackTrace();
		}
	}

	/**
	 * Loads the brief list of existing sets from disk.
	 * @param context Context to use
	 */
	public void loadIdList(Context context){
		String jstring;
		// open and read contents of file
		try {
			InputStream is = context.openFileInput(
					context.getString(R.string.local_id_list_file));
			int size = is.available();
			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();
			jstring = new String(buffer, "UTF-8");
		} catch (IOException ex) {
			ex.printStackTrace();
			jstring = "";
		}
//		Log.v("loadID", jstring);
		try {
			JSONArray jsonArray = new JSONArray(jstring);
			this.overviewListOfSets.clear();
			this.overviewListOfSets.addAll(WallpaperList.getBriefListFromJsonArray(jsonArray));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Uses the context and list id to get the filename used for the given list
	 * @param context Context to use
	 * @param id integer id of the list whose filename to get
	 * @return String filename for the list to use
	 */
	private String getListFileName(Context context, int id){
		return String.format(context.getString(R.string.local_image_list_file_with_id), id);
	}

	/**
	 * Get path to the preview image (does not check if it exists)
	 * @param context Context to use
	 * @param id Id to get preview of
	 * @return String path to preview image
	 */
	public String getPreviewFileName(Context context, int id){
		return String.format(context.getString(R.string.wallpaper_list_preview_image_file), id);
	}

	/**
	 * Saves the preview image to disk using id given
	 * @param context Context to use
	 * @param bm Bitmap to save
	 * @param id Integer id to save under
	 */
	public void savePreview(Context context, Bitmap bm, int id){
		String filename = getPreviewFileName(context, id);
		String direct = context.getFilesDir() + context.getString(R.string.wallpaper_list_preview_image_dir);
		File dir = new File(direct);

		WallpaperManipulator wallman = new WallpaperManipulator();
		wallman.saveBitmap(bm, dir, filename, Bitmap.CompressFormat.PNG);
	}

	/**
	 * Does not necessarily belong here, but this saves the settings from this settingmanager object
	 * into the stored sharedpreferences
	 * @param context the application context to access shared preferences with
	 */
	public void saveToDisk(Context context){
		SharedPreferences sf = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor edit = sf.edit();

		edit.putInt(PREF_KEY_INTERVAL, this.getIntervalInMinutes());
		edit.putBoolean(PREF_KEY_RANDOM_NEXT, this.getRandomNext());
		edit.putBoolean(PREF_KEY_SHOW_PREVIEW, this.getShowPreview());

		edit.putBoolean(PREF_KEY_RESIZE, this.getUseResize());
		edit.putBoolean(PREF_KEY_RESIZE_HEIGHT, this.getResizeHeight());
		edit.putBoolean(PREF_KEY_RESIZE_WIDTH, this.getResizeWidth());

		edit.putBoolean(PREF_KEY_CROP, this.getUseCrop());
		edit.putInt(PREF_KEY_CROP_LOC, this.getCropLocation());

		edit.putBoolean(PREF_KEY_DUPLICATE, this.getAllowRepeatedEntries());
		edit.putBoolean(PREF_KEY_SUBDIR, this.getIncludeSubDirectories());

		edit.putInt(PREF_KEY_THEME_ID, this.getThemeId());
		edit.putBoolean(PREF_KEY_GLOBAL, this.getGlobalOnly());

		// save brief list of sets
		saveIdList(context);
		// update last seen?
		updateWallpaperId();
		// save id of last set we worked with for loading
		edit.putInt(PREF_LAST_SET_ID, this.lastSeenSetId);

		// save the active wallpaper list to disk (in its own file)
		this.saveWallpaperList(context, this.wallpaperList);

		edit.apply();
	}

	/**
	 * Updates the last seen wallpaper set with given id, or does nothing if given placeholder id
	 * @param id int id to update last seen to
	 */
	public void setNewWorkingSet(int id){
		if(WallpaperList.ID_PLACEHOLDER == id){
//			Log.e("lastSeen", "Error with the id, was " + WallpaperList.ID_PLACEHOLDER + "...");
			return;
		}
		this.lastSeenSetId = id;
	}

	/**
	 * Creates a new unique id if current wallpaperlist is non-empty with placeholder id
	 */
	private void updateWallpaperId(){
		if(WallpaperList.ID_PLACEHOLDER == this.wallpaperList.getUniqueId() && 0 < this.wallpaperList.size()){
			this.wallpaperList.setUniqueId(getUnusedId());
		}
	}

	/**
	 * Does not necessarily belong here, but this Loads the settings from stored sharedpreferences
	 * into this settingsmanager object
	 * @param context the application context to access shared preferences with
	 */
	public void loadFromDisk(Context context){
		SharedPreferences sf = PreferenceManager.getDefaultSharedPreferences(context);

		// load last seen set id
		int lastCheck = sf.getInt(PREF_LAST_SET_ID, WallpaperList.ID_PLACEHOLDER);

		this.setIntervalInMinutes(sf.getInt(PREF_KEY_INTERVAL, 30));
		this.setRandomNext(sf.getBoolean(PREF_KEY_RANDOM_NEXT, true));
		this.setShowPreview(sf.getBoolean(PREF_KEY_SHOW_PREVIEW, true));

		this.setUseResize(sf.getBoolean(PREF_KEY_RESIZE, true));
		this.setResizeHeight(sf.getBoolean(PREF_KEY_RESIZE_HEIGHT, true));
		this.setResizeWidth(sf.getBoolean(PREF_KEY_RESIZE_WIDTH, false));

		this.setUseCrop(sf.getBoolean(PREF_KEY_CROP, true));
		this.setCropLocation(sf.getInt(PREF_KEY_CROP_LOC, WallpaperManipulator.CROP_FROM_RANDOM));

		this.setAllowRepeatedEntries(sf.getBoolean(PREF_KEY_DUPLICATE, false));
		this.setIncludeSubDirectories(sf.getBoolean(PREF_KEY_SUBDIR, false));

		this.setThemeId(sf.getInt(PREF_KEY_THEME_ID, WallpaperList.ID_PLACEHOLDER));
		this.setGlobalOnly(sf.getBoolean(PREF_KEY_GLOBAL, false));

		// TODO: handle list no longer exists
		// load last seen wallpaperlist
		this.loadWallpaperList(context, lastCheck);
		// remove anything that no longer exists
		cleanList();
		// update last seen to this new list
		this.lastSeenSetId = lastCheck;
	}

	/**
	 * For each empty/null field in the Wallpaper given, replaces it with current value from settings manager.
	 * This SHOULD NOT override any saved Wallpaper and only be used to fill null as needed
	 * @param wallIn The Wallpaper to use as reference
	 * @return A new Wallpaper with no null or empty values, or null if given WallpaperSetting has no src
	 */
	public Wallpaper copyWallpaperWithoutNull(Wallpaper wallIn){
		Wallpaper wall = new Wallpaper(wallIn.getSrc());
		// get the wallpaper list without nulls for comparison (will have replaced null from next level up)
		WallpaperList wallList = sm.copyWallpaperListWithoutNull(sm.getWallpaperList());

		// use resize
		if(wallIn.isResizeDefault()){
			wall.setUseResize(wallList.getUseResize());
		} else{
			wall.setUseResize(wallIn.getUseResize());
		}

		// resize height
		if(wallIn.isResizeHeightDefault()){
			wall.setResizeHeight(wallList.getResizeHeight());
		} else{
			wall.setResizeHeight(wallIn.getResizeHeight());
		}

		// resize width
		if(wallIn.isResizeWidthDefault()){
			wall.setResizeWidth(wallList.getResizeWidth());
		} else{
			wall.setResizeWidth(wallIn.getResizeWidth());
		}

		// use crop
		if(wallIn.isCropDefault()){
			wall.setUseCrop(wallList.getUseCrop());
		} else{
			wall.setUseCrop(wallIn.getUseCrop());
		}

		// crop location
		if(wallIn.isCropLocationDefault()){
			wall.setCropLocation(wallList.getCropLocation());
		} else{
			wall.setCropLocation(wallIn.getCropLocation());
		}

		return wall;
	}

	//TODO: wallpaperlistsettings and wallpapersettings classes?
	// or a "WallpaperSettings" class that handles settings stuff, with manager managing it?

	public WallpaperList copyWallpaperListWithoutNull(WallpaperList wallList){
		// if globals only
		if(this.getGlobalOnly()){
			return getCopyWallpaperListWithGlobals(wallList);
		}

		WallpaperList tmp = new WallpaperList();
		tmp.addAll(wallList);
		tmp.setUniqueId(wallList.getUniqueId());
		tmp.setName(wallList.getName());

		// interval
		if(wallList.isIntervalDefault()){
			tmp.setIntervalInMinutes(this.getIntervalInMinutes());
		} else{
			tmp.setIntervalInMinutes(wallList.getIntervalInMinutes());
		}

		// random
		if(wallList.isRandomDefault()){
			tmp.setRandomNext(this.getRandomNext());
		} else{
			tmp.setRandomNext(wallList.getRandomNext());
		}

		// show preview
		if(wallList.isShowPreviewDefault()){
			tmp.setShowPreview(this.getShowPreview());
		} else{
			tmp.setShowPreview(wallList.getShowPreview());
		}

		// resize
		if(wallList.isResizeDefault()){
			tmp.setUseResize(this.getUseResize());
		} else{
			tmp.setUseResize(wallList.getUseResize());
		}

		// resize height
		if(wallList.isResizeHeightDefault()){
			tmp.setResizeHeight(this.getResizeHeight());
		} else{
			tmp.setResizeHeight(wallList.getResizeHeight());
		}

		// resize width
		if(wallList.isResizeWidthDefault()){
			tmp.setResizeWidth(this.getResizeWidth());
		} else{
			tmp.setResizeWidth(wallList.getResizeWidth());
		}

		// crop
		if(wallList.isCropDefault()){
			tmp.setUseCrop(this.getUseCrop());
		} else {
			tmp.setUseCrop(wallList.getUseCrop());
		}

		// crop location
		if(wallList.isCropLocationDefault()){
			tmp.setCropLocation(this.getCropLocation());
		} else{
			tmp.setCropLocation(wallList.getCropLocation());
		}

		return tmp;
	}

	/**
	 * Creates copy of wallpaperList whose settings match the globals, and returns the list
	 * @param wallList List to copy with global settings
	 * @return WallpaperList with global settings set
	 */
	private WallpaperList getCopyWallpaperListWithGlobals(WallpaperList wallList){
		WallpaperList tmp = new WallpaperList();
		tmp.addAll(wallList);
		tmp.setUniqueId(wallList.getUniqueId());
		tmp.setName(wallList.getName());

		tmp.setIntervalInMinutes(this.getIntervalInMinutes());
		tmp.setRandomNext(this.getRandomNext());
		tmp.setShowPreview(this.getShowPreview());
		tmp.setUseResize(this.getUseResize());
		tmp.setResizeHeight(this.getResizeHeight());
		tmp.setResizeWidth(this.getResizeWidth());
		tmp.setUseCrop(this.getUseCrop());
		tmp.setCropLocation(this.getCropLocation());
		return tmp;
	}

	/**
	 * Deletes the stored file for each item in the overviewListOfSets, then clears the list
	 * @param context Context to use
	 */
	public void deleteAllSets(Context context){
		for(WallpaperList list : this.overviewListOfSets){
			deleteWallpaperSet(context, list.getUniqueId());
		}
		this.overviewListOfSets.clear();
		this.wallpaperList = new WallpaperList();
		this.lastSeenSetId = WallpaperList.ID_PLACEHOLDER;
		lastGeneratedInt = WallpaperList.ID_PLACEHOLDER;
		this.saveToDisk(context);
	}

	/**
	 * Deletes the stored file for the given set id, but does not remove from the overviewListOfSets
	 * @param context Context to use
	 * @param id int Id of set to delete
	 */
	public void deleteWallpaperSet(Context context, int id){
		String fileName = getListFileName(context, id);
		String path = context.getFilesDir() + "/" + fileName;
		File file = new File(path);
		boolean success = false;

		// if file exists, delete it
		if(file.exists()){
			success = file.delete();
		}
		deletePreviewImage(context, id);

		if(!success){
			//there was an issue
			Log.w("fileDelete", "Issue deleting: " + path);
		} else{
//			Log.d("fileDelete", "Deleted: " + path);
		}
	}

	/**
	 * Deletes the preview image associated with the given id
	 * @param context Context to use
	 * @param id Integer WallpaperList id associated with preview image to delete
	 * @return true if a file was successfully deleted
	 */
	public boolean deletePreviewImage(Context context, int id){
		String filename = getPreviewFileName(context, id);
		String direct = context.getFilesDir() + context.getString(R.string.wallpaper_list_preview_image_dir);
		File preview = new File(direct, filename);
//		Log.d("fileDelete", "Deleted: " + preview.getAbsolutePath());
		invalidatePreviews(context, preview);
		return preview.delete();
	}

	private void invalidatePreviews(Context context, File file){
		Picasso.with(context).invalidate(file);
	}

	/**
	 * Attempts to generate a WallpaperList ID that is not currently in use.
	 * First tries iteratively incrementing by 1 until it succeeds or decides
	 * it has tried too many times unsuccessfully, in which case it goes to largest known id and adds 1.
	 * If (for whatever reason) that fails, returns a placeholder id
	 * @return Integer of a unique WallpaperList ID, or a placeholder WallpaperList ID
	 */
	public int getUnusedId(){
		// this is supposed to be unique
		lastGeneratedInt++;
		int id = lastGeneratedInt;
		int count = 0;
		boolean iterativeFailed = false;
		Log.i("idGen", "Generated id: " + id);

		// while generated id is already used in our list
		while(this.overviewListOfSets.contains(new WallpaperList(id))){

			// check secondary methods
			if(iterativeFailed && count > 1){
				Log.w("idGen", "Iteration failed and next number did not work!");
				// something went wrong with secondary method, return placeholder id
				return WallpaperList.ID_PLACEHOLDER;
			} else if(count == ITERATIVE_ID_GEN_LIMIT){
				Log.w("idGen", "more work than I should have experienced, attempting alternate generation method.");
				// get copy of list
				ArrayList<WallpaperList> tmp = new ArrayList<>();
				tmp.addAll(this.getOverviewListOfSets());
				// sort copy
				Collections.sort(tmp);
				// get an id that is 1 larger than the last item in the list
				lastGeneratedInt = tmp.get(tmp.size() - 1).getUniqueId();
				// reset count
				count = 0;
				// set flag that iterative method failed
				iterativeFailed = true;
			}

			// increment and try again
			lastGeneratedInt++;
			id = lastGeneratedInt;
			count++;
			Log.i("idGen", "matched, generated another id: " + id);
		}
		Log.i("idGen", "Returning new unique id: " + id);
		return id;
	}

}
