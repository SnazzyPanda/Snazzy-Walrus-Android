package com.snazzy.android.snazzywalrus.helper;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.TypedValue;

import com.snazzy.android.snazzywalrus.R;

import java.lang.reflect.Field;

/**
 * Created by SnazzyPanda on 7/6/16.
 * A class defining methods to aid in retrieving Android xml values, such as colors and strings.
 */
public class AndroidValueHelper {

	Context context;

	public AndroidValueHelper(Context context){
		this.context = context;
	}

	public void setContext(Context context){
		this.context = context;
	}

	/**
	 * Checks the given theme id to make sure it is not (obviously) invalid, then sets for this' context
	 * @param id int theme id to set
	 */
	public void checkSetTheme(int id){
		// if we have a saved theme
		if(id != -1){
			// assume it is valid and (try to) set it as theme
			context.setTheme(id);
		}
	}

	/**
	 * Loads and returns the int representing the selected item background color.
	 * Uses the current theme's "listItemSelected" attr
	 * @return int representing the color, or -1
	 */
	public int getItemSelectedBGColor(){
		return getColorByAttr(R.attr.listItemSelected);
	}

	/**
	 * Loads and returns the int representing the default item background color.
	 * Uses the current theme's "color" attr
	 * @return int representing the color, or -1
	 */
	public int getItemDefaultBGColor(){
		return getColorByAttr(R.attr.color);
	}

	/**
	 * Loads and returns the int representing the selected item background color.
	 * Uses the current theme's "listItemEdited" attr
	 * @return int representing the color, or -1
	 */
	public int getItemEditedBGColor(){
		return getColorByAttr(R.attr.listItemEdited);
	}

	/**
	 * Loads and returns the int representing the color specified by the given attr.
	 * @param attr The int attr which points to the color to get. (ex: R.attr.color)
	 * @return int representing the color, or -1
	 */
	public int getColorByAttr(int attr){
		try{
			TypedValue tv = new TypedValue();
			context.getTheme().resolveAttribute(attr, tv, true);
			return tv.data;
		} catch(Exception e){
			return -1;
		}
	}

	/**
	 * From stackoverflow
	 * Gets absolute Uri of a file from its MediaStore Uri
	 * @param context Context
	 * @param contentUri The MediaStore Uri to convert
	 * @return String of absolute location of the file identified by the MediaStore Uri
	 */
	public String getRealPathFromURI(Context context, Uri contentUri) {
		Cursor cursor = null;
		try {
			String[] proj = { MediaStore.Images.Media.DATA };
			cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	/**
	 * From Codepath Android Cliffnotes site, gets the string.xml string
	 * @param key The key identifying the string we want to get
	 * @return The resource's contents as string
	 */
	public String getResourceStringValue(String key) {
		// Retrieve the resource id
		String packageName = context.getPackageName();
		Resources resources = context.getResources();
		int stringId = resources.getIdentifier(key, "string", packageName);
		if (stringId == 0) { return null; }
		// Return the string value based on the res id
		return resources.getString(stringId);
	}

	/**
	 * Gets a resource id using its name and class.
	 * Taken from stack overflow
	 * @param resName String name of the resource
	 * @param c Class of the resource desired
	 * @return integer id of resource or -1 if not found
	 */
	public int getResId(String resName, Class<?> c) {
		try {
			Field idField = c.getDeclaredField(resName);
			return idField.getInt(idField);
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
}
