package com.snazzy.android.snazzywalrus.util;

import android.graphics.Bitmap;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by SnazzyPanda on 7/7/16.
 * Defines a list of Wallpaper's and relevant overridden settings.
 */
public class WallpaperList extends ArrayList<Wallpaper> implements Comparable<WallpaperList>{

	public static final int ID_PLACEHOLDER = -1;

	private static final String PREF_KEY_ID = "key_list_id";
	private static final String PREF_KEY_NAME = "key_list_name";
	private static final String PREF_KEY_ITEM_ARRAY = "key_item_array";

	private static final String PREF_KEY_INTERVAL = "key_interval";
	private static final String PREF_KEY_RANDOM = "key_random";
	private static final String PREF_KEY_PREVIEW = "key_preview";
	private static final String PREF_KEY_RESIZE = "key_resize";
	private static final String PREF_KEY_RESIZE_HEIGHT = "key_resize_height";
	private static final String PREF_KEY_RESIZE_WIDTH = "key_resize_width";
	private static final String PREF_KEY_CROP = "key_crop";
	private static final String PREF_KEY_CROP_LOC = "key_crop_location";

	private static final Integer INTERVAL_DEFAULT = null;
	private static final Boolean RANDOM_NEXT_DEFAULT = null;
	private static final Boolean SHOW_PREVIEW_DEFAULT = null;
	private static final Boolean RESIZE_DEFAULT = null;
	private static final Boolean RESIZE_HEIGHT_DEFAULT = null;
	private static final Boolean RESIZE_WIDTH_DEFAULT = null;
	private static final Boolean CROP_DEFAULT = null;
	private static final Integer CROP_LOCATION_DEFAULT = null;

	private static final int DEFAULT_NUM_IMAGES_IN_PREVIEW = 3;
	private static final int DEFAULT_PREVIEW_IMAGE_SIZE = 200;

	private int uniqueId = ID_PLACEHOLDER;
	private String name;

	// allow overriding global settings, but do not override image settings
	private Integer intervalInMinutes = INTERVAL_DEFAULT;
	private Boolean randomNext = RANDOM_NEXT_DEFAULT;
	private Boolean showPreview = SHOW_PREVIEW_DEFAULT;
	private Boolean useResize = RESIZE_DEFAULT;
	private Boolean resizeHeight = RESIZE_HEIGHT_DEFAULT;
	private Boolean resizeWidth = RESIZE_WIDTH_DEFAULT;
	private Boolean useCrop = CROP_DEFAULT;
	private Integer cropLocation = CROP_LOCATION_DEFAULT;

	public WallpaperList(){
		super();
		this.uniqueId = ID_PLACEHOLDER;
	}

	public WallpaperList(String name){
		super();
		this.name = name;
		this.uniqueId = ID_PLACEHOLDER;
	}

	public WallpaperList(int id){
		super();
		this.uniqueId = id;
	}

	public WallpaperList(int id, String name){
		super();
		this.uniqueId = id;
		this.name = name;
	}

	public void setUniqueId(int id){
		this.uniqueId = id;
	}

	public int getUniqueId(){
		return this.uniqueId;
	}

	public void setName(String name){
		this.name = name;
	}

	public String getName(){
		return this.name;
	}

	public Integer getIntervalInMinutes(){
		return this.intervalInMinutes;
	}

	public void setIntervalInMinutes(Integer interval){
		this.intervalInMinutes = interval;
	}

	public Boolean getRandomNext(){
		return this.randomNext;
	}

	public void setRandomNext(Boolean random){
		this.randomNext = random;
	}

	public Boolean getShowPreview(){
		return this.showPreview;
	}

	public void setShowPreview(Boolean preview){
		this.showPreview = preview;
	}

	public Boolean getUseResize(){
		return this.useResize;
	}

	public void setUseResize(Boolean use){
		this.useResize = use;
	}

	public Boolean getResizeHeight(){
		return this.resizeHeight;
	}

	public void setResizeHeight(Boolean resize){
		this.resizeHeight = resize;
	}

	public Boolean getResizeWidth(){
		return this.resizeWidth;
	}

	public void setResizeWidth(Boolean resize){
		this.resizeWidth = resize;
	}

	public Boolean getUseCrop(){
		return this.useCrop;
	}

	public void setUseCrop(Boolean use){
		this.useCrop = use;
	}

	public Integer getCropLocation(){
		return this.cropLocation;
	}

	public void setCropLocation(Integer loc){
		this.cropLocation = loc;
	}


	public boolean isIntervalDefault(){
		if(null == this.intervalInMinutes){
			if(null == INTERVAL_DEFAULT){
				return true;
			} else{
				return false;
			}
		} else{
			return this.intervalInMinutes.equals(INTERVAL_DEFAULT);
		}
	}

	public boolean isRandomDefault(){
		if(null == this.randomNext){
			if(null == RANDOM_NEXT_DEFAULT){
				return true;
			} else{
				return false;
			}
		} else{
			return this.randomNext.equals(RANDOM_NEXT_DEFAULT);
		}
	}

	public boolean isShowPreviewDefault(){
		if(null == this.showPreview){
			if(null == SHOW_PREVIEW_DEFAULT){
				return true;
			} else{
				return false;
			}
		} else{
			return this.showPreview.equals(SHOW_PREVIEW_DEFAULT);
		}
	}

	public boolean isResizeDefault(){
		if(null == this.useResize){
			if(null == RESIZE_DEFAULT){
				return true;
			} else{
				return false;
			}
		} else{
			return this.useResize.equals(RESIZE_DEFAULT);
		}
	}

	public boolean isResizeHeightDefault(){
		if(null == this.resizeHeight){
			if(null == RESIZE_HEIGHT_DEFAULT){
				return true;
			} else{
				return false;
			}
		} else{
			return this.resizeHeight.equals(RESIZE_HEIGHT_DEFAULT);
		}
	}

	public boolean isResizeWidthDefault(){
		if(null == this.resizeWidth){
			if(null == RESIZE_WIDTH_DEFAULT){
				return true;
			} else{
				return false;
			}
		} else{
			return this.resizeWidth.equals(RESIZE_WIDTH_DEFAULT);
		}
	}

	public boolean isCropDefault(){
		if(null == this.useCrop){
			if(null == CROP_DEFAULT){
				return true;
			} else{
				return false;
			}
		} else{
			return this.useCrop.equals(CROP_DEFAULT);
		}
	}

	public boolean isCropLocationDefault(){
		if(null == this.cropLocation){
			if(null == CROP_LOCATION_DEFAULT){
				return true;
			} else{
				return false;
			}
		} else{
			return this.cropLocation.equals(CROP_LOCATION_DEFAULT);
		}
	}

	/**
	 * Checks if any settings are different from their default value
	 * @return true if any values are different
	 */
	public boolean isEdited(){
		// if any of these are not default, it has been edited
		return !this.isIntervalDefault() || !this.isRandomDefault() ||
				!this.isShowPreviewDefault() ||	!this.isResizeDefault() ||
				!this.isResizeHeightDefault() || !this.isResizeWidthDefault() ||
				!this.isCropDefault() || !this.isCropLocationDefault();
	}

	/**
	 * Returns all settings to their default values
	 */
	public void restoreDefaultSettings(){
		this.setIntervalInMinutes(INTERVAL_DEFAULT);
		this.setRandomNext(RANDOM_NEXT_DEFAULT);
		this.setShowPreview(SHOW_PREVIEW_DEFAULT);
		this.setUseResize(RESIZE_DEFAULT);
		this.setResizeHeight(RESIZE_HEIGHT_DEFAULT);
		this.setResizeWidth(RESIZE_WIDTH_DEFAULT);
		this.setUseCrop(CROP_DEFAULT);
		this.setCropLocation(CROP_LOCATION_DEFAULT);
	}

	/**
	 * Convert's this WallpaperList's list of Wallpapers to a JSON Array
	 * @return JSON Array of Wallpapers
	 */
	private JSONArray getListAsJsonArray(){
		JSONArray jsonArray = new JSONArray();
		// convert our Wallpapers to a JSONArray of JSONObjects
		for(Wallpaper wall : this){
			jsonArray.put(wall.toJsonObject());
		}
		return jsonArray;
	}

	/**
	 * Populates this WallpaperList with the Wallpapers stored in the given JSON Array
	 * @param jsonArray JSON Array of Wallpapers
	 */
	private void loadListFromJsonArray(JSONArray jsonArray){
		for(int i = 0; i < jsonArray.length(); i++){
			try{
				// get the JSON object
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				// turn each object into Wallpaper and add to this list
				this.add(Wallpaper.decodeJsonObject(jsonObject));
			} catch(JSONException e){
				e.printStackTrace();
			}
		} // end for
	}

	/**
	 * Turns the WallpaperList into a JSON Object
	 * @return JSON Object representing the WallpaperList
	 */
	public JSONObject getAsJsonObject(){
		JSONObject jsonObject = new JSONObject();
		// Get list of Wallpapers as a JSON Array
		JSONArray jsonArray = this.getListAsJsonArray();

		try{
			// put the id
			jsonObject.put(PREF_KEY_ID, this.getUniqueId());
		} catch(JSONException e){
			e.printStackTrace();
		}

		try {
			// put the name
			jsonObject.put(PREF_KEY_NAME, this.getName());
		} catch (JSONException e) {
			Log.w("wallToJson", "No name was supplied");
			// non vital
		}

		// settings
		addSettingsToJsonObject(jsonObject);

		try{
			// put the list of Wallpapers
			jsonObject.put(PREF_KEY_ITEM_ARRAY, jsonArray);
		} catch (JSONException e) {
			// vital unless empty list allowed
			e.printStackTrace();
		}

		return jsonObject;
	}

	/**
	 * Uses given JSON object to try to recreate the WallpaperList it holds
	 * @param jsonObject JSON Object representing a WallpaperList
	 */
	public void populateFromJsonObject(JSONObject jsonObject){
		// fill current with json object
		try{
			// get the id
			this.setUniqueId(jsonObject.getInt(PREF_KEY_ID));
		} catch(JSONException e){
			Log.e("wallLstFromJson", "No valid ID was provided");
			e.printStackTrace();
		}

		try{
			// get the name
			this.setName(jsonObject.getString(PREF_KEY_NAME));
		} catch(JSONException e){
			Log.w("wallLstFromJson", "No name was supplied");
		}

		// add settings
		addSettingsToWallpaperList(this, jsonObject);

		try{
			// try to load the list into this object
			this.loadListFromJsonArray(jsonObject.getJSONArray(PREF_KEY_ITEM_ARRAY));
		} catch(JSONException e){
			e.printStackTrace();
		}
	}

	/**
	 * Creates a JSONObject representing the WallpaperList, without the list of wallpapers
	 * @return JSONObject
	 */
	public JSONObject getAsBriefJsonObject(){
		JSONObject jsonObject = new JSONObject();

		try{
			jsonObject.put(PREF_KEY_ID, this.getUniqueId());
		} catch(JSONException e){
			Log.e("WallList", "No id provided - brief");
			return null;
		}

		try{
			jsonObject.put(PREF_KEY_NAME, this.getName());
		} catch(JSONException e){
			Log.w("WallList", "No name provided - brief");
		}

		// add settings to it
		addSettingsToJsonObject(jsonObject);

		return jsonObject;
	}

	/**
	 * Gets a brief WallpaperList from given JSONObject
	 * @param jsonObject JSONObject representing a WallpaperList
	 * @return WallpaperList in a brief format (no Wallpapers)
	 */
	public static WallpaperList getBriefFromJsonObject(JSONObject jsonObject){
		WallpaperList list = new WallpaperList();

		try{
			list.setUniqueId(jsonObject.getInt(PREF_KEY_ID));
		} catch(JSONException e){
			Log.e("WallList", "No id found in loaded object!");
			return null;
		}

		try{
			list.setName(jsonObject.getString(PREF_KEY_NAME));
		} catch(JSONException e){
			Log.w("WallList", "No name found in loaded object");
		}

		// add settings to list
		addSettingsToWallpaperList(list, jsonObject);
		return list;
	}

	/**
	 * Gets a brief version of given WallpaperList
	 * @param list WallpaperList to get brief version of
	 * @return WallpaperList in a brief format
	 */
	public static WallpaperList getBriefListFrom(WallpaperList list){
		WallpaperList wall = new WallpaperList();
		wall.setUniqueId(list.getUniqueId());
		wall.setName(list.getName());

		wall.fillWithSettingsFrom(list);

		return wall;
	}

	/**
	 * Taks settings from given list and applies to current WallpaperList
	 * @param list WallpaperList to get settings from
	 */
	private void fillWithSettingsFrom(WallpaperList list){
		this.setIntervalInMinutes(list.getIntervalInMinutes());
		this.setRandomNext(list.getRandomNext());
		this.setShowPreview(list.getShowPreview());
		this.setUseResize(list.getUseResize());
		this.setResizeHeight(list.getResizeHeight());
		this.setResizeWidth(list.getResizeWidth());
		this.setUseCrop(list.getUseCrop());
		this.setCropLocation(list.getCropLocation());
	}

	/**
	 * Gets a list of brief WallpaperLists from given JSONArray
	 * @param array JSONArray containing WallpaperLists
	 * @return ArrayList containing brief WallpaperLists
	 */
	public static ArrayList<WallpaperList> getBriefListFromJsonArray(JSONArray array){
		ArrayList<WallpaperList> list = new ArrayList<>();

		for(int i = 0; i < array.length(); i++){
			try{
				JSONObject json = array.getJSONObject(i);
				list.add(WallpaperList.getBriefFromJsonObject(json));
			} catch(JSONException e){
				e.printStackTrace();
			}
		}
		return list;
	}


	/**
	 * Hope is that this will be a central place for adding settings when creating a json object
	 * @param jsonObject JSONObject to add settings to
	 */
	private void addSettingsToJsonObject(JSONObject jsonObject){
		/*
		* If any of these fail, we assume no value was provided (which we will handle, using next level up's settings)
		*/

		try{
			jsonObject.put(PREF_KEY_INTERVAL, this.getIntervalInMinutes());
		} catch(JSONException e){
			//non-vital
		}
		try{
			jsonObject.put(PREF_KEY_RANDOM, this.getRandomNext());
		} catch(JSONException e){
			//non-vital
		}
		try{
			jsonObject.put(PREF_KEY_PREVIEW, this.getShowPreview());
		} catch(JSONException e){
			//non-vital
		}

		// Image alteration settings
		try{
			jsonObject.put(PREF_KEY_RESIZE, this.getUseResize());
		} catch(JSONException e){
			//non-vital
		}
		try{
			jsonObject.put(PREF_KEY_RESIZE_HEIGHT, this.getResizeHeight());
		} catch(JSONException e){
			//non-vital
		}
		try{
			jsonObject.put(PREF_KEY_RESIZE_WIDTH, this.getResizeWidth());
		} catch(JSONException e){
			//non-vital
		}
		try{
			jsonObject.put(PREF_KEY_CROP, this.getUseCrop());
		} catch(JSONException e){
			//non-vital
		}
		try{
			jsonObject.put(PREF_KEY_CROP_LOC, this.getCropLocation());
		} catch(JSONException e){
			//non-vital
		}
	}

	/**
	 * Should load settings values from JSONObject into the list
	 * @param list WallpaperList to load settings into
	 * @param json JSONObject to load settings from
	 */
	private static void addSettingsToWallpaperList(WallpaperList list, JSONObject json){
		/*
		* If any of these fail, we assume no value was provided (which we will handle, using next level up's settings)
		*/
		try{
			list.setIntervalInMinutes(json.getInt(PREF_KEY_INTERVAL));
		} catch(JSONException e){
			//non-vital
		}
		try{
			list.setRandomNext(json.getBoolean(PREF_KEY_RANDOM));
		} catch(JSONException e){
			//non-vital
		}
		try{
			list.setShowPreview(json.getBoolean(PREF_KEY_PREVIEW));
		} catch(JSONException e){
			//non-vital
		}

		try{
			list.setUseResize(json.getBoolean(PREF_KEY_RESIZE));
		} catch(JSONException e){
			//non-vital
		}
		try{
			list.setResizeHeight(json.getBoolean(PREF_KEY_RESIZE_HEIGHT));
		} catch(JSONException e){
			//non-vital
		}
		try{
			list.setResizeWidth(json.getBoolean(PREF_KEY_RESIZE_WIDTH));
		} catch(JSONException e){
			//non-vital
		}
		try{
			list.setUseCrop(json.getBoolean(PREF_KEY_CROP));
		} catch(JSONException e){
			//non-vital
		}
		try{
			list.setCropLocation(json.getInt(PREF_KEY_CROP_LOC));
		} catch(JSONException e){
			//non-vital
		}
	}

	/**
	 * Generates a preview image for this WallpaperList.
	 * @param size Size of preview image to generate
	 * @param num Integer number of images to show in preview image
	 * @return Bitmap of generated preview image
	 */
	public Bitmap genPreview(int size, int num){
		if(0 == this.size()){
			// empty, nothing to do
			return null;
		}

		ArrayList<String> list = new ArrayList<>();
		// if number of images to use is larger than number we have, set num to use equal to size
		if(num > this.size()){
			num = this.size();
		}
		// get list of the paths to the images to use
		for(int i = 0; i < num; i++){
			list.add(this.get(i).getSrc());
		}
		// generate Bitmap and return
		return new WallpaperManipulator().generatePreview(list, num, size);
	}

	/**
	 * Generates a preview image for this WallpaperList.
	 * @param size Size of preview image to generate
	 * @return Bitmap of generated preview image
	 */
	public Bitmap genPreview(int size){
		return genPreview(size, DEFAULT_NUM_IMAGES_IN_PREVIEW);
	}

	/**
	 * Generates a preview image for this WallpaperList.
	 * (Currently uses first 3 images, and creates a Bitmap that is 200x200 px)
	 * @return Bitmap preview
	 */
	public Bitmap genPreview(){
		// TODO: Synchronize this value with place generating and displaying them
		return genPreview(DEFAULT_PREVIEW_IMAGE_SIZE);
	}

	/**
	 * WallpaperLists are considered equal if they have the same id
	 * @param o Object to compare
	 * @return true if object is WallpaperList with same id as th other
	 */
	@Override
	public boolean equals(Object o){
		if(!o.getClass().equals(this.getClass())){
			return false;
		}
		WallpaperList tmp  = (WallpaperList) o;
		return tmp.getUniqueId() == this.getUniqueId();
	}

	@Override
	public int compareTo(WallpaperList other){
		if(this.getUniqueId() > other.getUniqueId()){
			return 1;
		} else if(this.getUniqueId() == other.getUniqueId()){
			return 0;
		} else{
			return -1;
		}
//		return Integer.compare(this.getUniqueId(), other.getUniqueId());
	}

}
