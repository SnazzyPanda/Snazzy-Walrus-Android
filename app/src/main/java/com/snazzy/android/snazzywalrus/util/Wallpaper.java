package com.snazzy.android.snazzywalrus.util;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by SnazzyPanda on 6/30/16.
 * Storage of settings for each individual wallpaper
 */
public class Wallpaper {

	// point is to store individual settings for each wallpaper

	private static final String PREF_KEY_SRC = "key_src";
	private static final String PREF_KEY_RESIZE = "key_resize";
	private static final String PREF_KEY_RESIZE_HEIGHT = "key_resize_height";
	private static final String PREF_KEY_RESIZE_WIDTH = "key_resize_width";
	private static final String PREF_KEY_CROP = "key_crop";
	private static final String PREF_KEY_CROP_LOC = "key_crop_location";

	// TODO allow enter and set resize values?

	public static final Boolean RESIZE_DEFAULT = null;
	public static final Boolean RESIZE_HEIGHT_DEFAULT = null;
	public static final Boolean RESIZE_WIDTH_DEFAULT = null;
	public static final Boolean CROP_DEFAULT = null;
	public static final Integer CROP_LOCATION_DEFAULT = null;

	private String src = null;
	private Boolean useResize = RESIZE_DEFAULT;
	private Boolean resizeHeight = RESIZE_HEIGHT_DEFAULT;
	private Boolean resizeWidth = RESIZE_WIDTH_DEFAULT;
	private Boolean useCrop = CROP_DEFAULT;
	private Integer cropLocation = CROP_LOCATION_DEFAULT;

	public Wallpaper() {
	}

	public Wallpaper(String location){
		this.src = location;
	}

	public String getSrc(){
		return this.src;
	}

	public void setSrc(String loc){
		this.src = loc;
	}

	public Boolean getUseResize(){
		return this.useResize;
	}

	public void setUseResize(boolean use){
		this.useResize = use;
	}

	public Boolean getResizeHeight(){
		return this.resizeHeight;
	}

	public void setResizeHeight(boolean resize){
		this.resizeHeight = resize;
	}

	public Boolean getResizeWidth(){
		return this.resizeWidth;
	}

	public void setResizeWidth(boolean resize){
		this.resizeWidth = resize;
	}

	public Boolean getUseCrop(){
		return this.useCrop;
	}

	public void setUseCrop(boolean use){
		this.useCrop = use;
	}

	public Integer getCropLocation(){
		return this.cropLocation;
	}

	public void setCropLocation(int loc){
		this.cropLocation = loc;
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
	 * Turns this WallpaperSetting into a JSONObject
	 * @return JSONObject representing this Wallpaper
	 */
	public JSONObject toJsonObject(){
		JSONObject json = new JSONObject();

		try {
			json.put(PREF_KEY_SRC, this.getSrc());
		} catch (JSONException e) {
			e.printStackTrace();
			// we can't save without the location
			return null;
		}
		try{
			json.put(PREF_KEY_RESIZE, this.getUseResize());
		} catch(JSONException e){
			// if failed, we assume because no value was provided.
			// in which case, we will use settings manager as default values
		}
		try{
			json.put(PREF_KEY_RESIZE_HEIGHT, this.getResizeHeight());
		} catch(JSONException e){
			// if failed, we assume because no value was provided.
			// in which case, we will use settings manager as default values
		}
		try{
			json.put(PREF_KEY_RESIZE_WIDTH, this.getResizeWidth());
		} catch(JSONException e){
			// if failed, we assume because no value was provided.
			// in which case, we will use settings manager as default values
		}
		try{
			json.put(PREF_KEY_CROP, this.getUseCrop());
		} catch(JSONException e){
			// if failed, we assume because no value was provided.
			// in which case, we will use settings manager as default values
		}
		try{
			json.put(PREF_KEY_CROP_LOC, this.getCropLocation());
		} catch(JSONException e){
			// if failed, we assume because no value was provided.
			// in which case, we will use settings manager as default values
		}

		return json;
	}

	/**
	 * Reads the values of the given JSONObject and sets them on the current Wallpaper object
	 * @param json The JSONObject whose values to use
	 */
	public void decodeJsonObjectIntoThis(JSONObject json){
		try {
			this.setSrc(json.getString(PREF_KEY_SRC));
		} catch (JSONException e) {
			// I don't know if not having a src will break anything
			e.printStackTrace();
		}
		try{
			this.setUseResize(json.getBoolean(PREF_KEY_RESIZE));
		} catch(JSONException e){
			// if failed, we assume because no value was provided.
			// in which case, we will use settings manager as default values
		}
		try{
			this.setResizeHeight(json.getBoolean(PREF_KEY_RESIZE_HEIGHT));
		} catch(JSONException e){
			// if failed, we assume because no value was provided.
			// in which case, we will use settings manager as default values
		}
		try{
			this.setResizeWidth(json.getBoolean(PREF_KEY_RESIZE_WIDTH));
		} catch(JSONException e){
			// if failed, we assume because no value was provided.
			// in which case, we will use settings manager as default values
		}
		try{
			this.setUseCrop(json.getBoolean(PREF_KEY_CROP));
		} catch(JSONException e){
			// if failed, we assume because no value was provided.
			// in which case, we will use settings manager as default values
		}
		try{
			this.setCropLocation(json.getInt(PREF_KEY_CROP_LOC));
		} catch(JSONException e){
			// if failed, we assume because no value was provided.
			// in which case, we will use settings manager as default values
		}

	}

	/**
	 * Reads a JSONObject as a Wallpaper
	 * @param json JSONObject to read
	 * @return Wallpaper object
	 */
	public static Wallpaper decodeJsonObject(JSONObject json){
		Wallpaper ws = new Wallpaper();

		try {
			ws.setSrc(json.getString(PREF_KEY_SRC));
		} catch (JSONException e) {
			e.printStackTrace();
			// no source return null
			return null;
		}

		try{
			ws.setUseResize(json.getBoolean(PREF_KEY_RESIZE));
		} catch(JSONException e){
			// if failed, we assume because no value was provided.
			// in which case, we will use settings manager as default values
		}
		try{
			ws.setResizeHeight(json.getBoolean(PREF_KEY_RESIZE_HEIGHT));
		} catch(JSONException e){
			// if failed, we assume because no value was provided.
			// in which case, we will use settings manager as default values
		}
		try{
			ws.setResizeWidth(json.getBoolean(PREF_KEY_RESIZE_WIDTH));
		} catch(JSONException e){
			// if failed, we assume because no value was provided.
			// in which case, we will use settings manager as default values
		}
		try{
			ws.setUseCrop(json.getBoolean(PREF_KEY_CROP));
		} catch(JSONException e){
			// if failed, we assume because no value was provided.
			// in which case, we will use settings manager as default values
		}
		try{
			ws.setCropLocation(json.getInt(PREF_KEY_CROP_LOC));
		} catch(JSONException e){
			// if failed, we assume because no value was provided.
			// in which case, we will use settings manager as default values
		}

		return ws;
	}

	/**
	 * Checks if any settings are different from their default value
	 * @return true if any values are different
	 */
	public boolean isEdited(){
		// if any of these are not default, it has been edited
		return !this.isResizeDefault() || !this.isResizeHeightDefault() ||
				!this.isResizeWidthDefault() || !this.isCropDefault() ||
				!this.isCropLocationDefault();
	}

	/**
	 * Sets all of current Wallpaper's settings to default (null)
	 */
	public void restoreDefault(){
		useResize = RESIZE_DEFAULT;
		resizeHeight = RESIZE_HEIGHT_DEFAULT;
		resizeWidth = RESIZE_WIDTH_DEFAULT;
		useCrop = CROP_DEFAULT;
		cropLocation = CROP_LOCATION_DEFAULT;
	}

	/**
	 * Checks the current Wallpaper against the given Wallpaper and returns true iff all settings match.
	 * (src is not considered a setting)
	 * @param wall Wallpaper to check this against
	 * @return true if all settings match, else false
	 */
	public boolean hasSameSettingsAs(Wallpaper wall){
		// TODO: check if this actually works as intended (I don't think it does)
		// return true if all settings are equal
		return this.useResize == wall.useResize && this.resizeHeight == wall.resizeHeight &&
				this.resizeWidth == wall.resizeWidth && this.useCrop == wall.useCrop &&
				this.cropLocation.equals(wall.cropLocation);
	}

}
