package com.snazzy.android.snazzywalrus;

import android.Manifest;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.PermissionChecker;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.snazzy.android.snazzywalrus.util.SettingsManager;
import com.snazzy.android.snazzywalrus.util.WallpaperList;
import com.snazzy.android.snazzywalrus.util.Wallpaper;
import com.snazzy.android.snazzywalrus.util.WallpaperManipulator;

import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;

/**
 * Created by SnazzyPanda on 6/14/16.
 * Handles interactions between the {@link WallpaperManipulator}
 * and classes that want to use it's functions.
 *
 * This class extends {@link IntentService},
 * and is ready to receive broadcasts from the {@link Scheduler}.
 */
public class WallpaperInteractor extends IntentService{

	private WallpaperManipulator wallman = new WallpaperManipulator();
	/**
	 * A reusable id for any generate notifications
	 */
	private static final int mId = 3259406;
	private Context context;
	private static final int CHANGE_DELAY = 500;


	public WallpaperInteractor(){
		super("WallpaperInteractor");
	}

	public WallpaperInteractor(Context context){
		super("WallpaperInteractor");
		this.context = context;
	}

	public void setContext(Context context){
		this.context = context;
	}

	/**
	 * Gets the position of a random element from the given list
	 * @param list A list of objects to randomly get from
	 * @return An integer specifying the location of an item from the list
	 */
	private int getRandIndexInList(List list){
		Random randomizer = new Random();
		return randomizer.nextInt(list.size());
	}

	/**
	 * Tries to get the current orientation, assumes portrait if issues occur
	 * @return True if portrait orientation, or something went wrong, false otherwise
	 */
	public boolean isPortrait(){
		try{
			return Configuration.ORIENTATION_PORTRAIT == context.getResources().getConfiguration().orientation;
		} catch(NullPointerException e){
			return true;
		}
	}

	/**
	 * Attempts to get (the possibly rough) current device width (currently meant for android devices), this method assumes device has greater height than width.
	 * @return Point with the pixel width (as integer) in x and pixel height (as integer) in y
	 */
	public Point getRoughDeviceDimensions(){
		return getRoughDeviceDimensions(isPortrait());
	}

	/**
	 * Attempts to get (the possibly rough) current device width (currently meant for android devices)
	 * @param portrait boolean, true if height of device should be greater than width, false otherwise
	 * @return Point with the pixel width (as integer) in x and pixel height (as integer) in y
	 */
	public Point getRoughDeviceDimensions(boolean portrait){
		WindowManager wm = null;

		try{
			wm = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
		} catch(NullPointerException e){
			try{
				wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
			} catch(Exception ex){
				ex.printStackTrace();
			}
		}

		// create our point
		Point point = new Point();

		// if our version supports our preferred method (it seems more accurate for our purposes)
		if(Build.VERSION.SDK_INT > 16){
			// set our point the the real size of the screen
			wm.getDefaultDisplay().getRealSize(point);
		} else{
			// otherwise use displaymetrics to get rough size of screen
			DisplayMetrics metrics = new DisplayMetrics();
			wm.getDefaultDisplay().getMetrics(metrics);

			// put values into our point
			point.x = metrics.widthPixels;
			point.y = metrics.heightPixels;
		}

		// check screen orientation (since we are assuming phones, height should be larger than width)
		if(point.x > point.y && portrait){
			// portrait specified, but width was larger than height
			// swap
			int t = point.y;
			point.y = point.x;
			point.x = t;
		} else if(point.y > point.x && !portrait){
			// landscape specified, but height was larger than width
			// swap
			int t = point.y;
			point.y = point.x;
			point.x = t;
		}

		// return our point
		return point;
	}

	/**
	 * Converts a given pixel value to dp
	 * @param context The context to use
	 * @param px integer pixel value
	 * @return Integer dp value
	 */
	public int convertPxToDp(Context context, int px){
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
	}

	/**
	 * Converts given dp to pixel value
	 * @param context Context to use
	 * @param dp integer dp value to convert
	 * @return Integer pixel value
	 */
	public int convertDpToPx(Context context, int dp){
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
	}

	/**
	 *
	 * @return integer value for screen width
	 */
	public int getRoughDeviceWidth(){
		return getRoughDeviceDimensions().x;
	}

	/**
	 *
	 * @return integer value for screen height
	 */
	public int getRoughDeviceHeight(){
		return getRoughDeviceDimensions().y;
	}

	/**
	 * Gets the file located in specified location, manipulates it as necessary, and returns the altered Bimap of it
	 * <p/>
	 * Logic manipulating the image is here (ie, resize if useResize etc)
	 * @param wallpaper The Wallpaper for the desired wallpaper, which has NO NULL OR EMPTY VALUES
	 * @return Altered Bitmap of next desired Wallpaper
	 */
	private Bitmap setupNextWallpaper(Wallpaper wallpaper){
		// get device dimensions
		Point dim = getRoughDeviceDimensions();

		// get the source image
		Bitmap img = wallman.getBitmapFromString(wallpaper.getSrc());

		// initialize to given image to be sure we have something accessible to alter
		Bitmap nimg = img;

		// Manipulate the image as user has specified via settings
		// if user wants to resize
		if(wallpaper.getUseResize()){
			// by height and width
			if(wallpaper.getResizeHeight() && wallpaper.getResizeWidth()){
				// force resize of height AND width (to screen size)
				nimg = wallman.resizeToSize(img, dim.x, dim.y);
			} else if(wallpaper.getResizeWidth()){
				// resize by width, maintain aspect ratio
				nimg = wallman.relativeResizeByWidth(img, dim.x);
			} else {
				// if neither of previous matched, assume we resize by height only
				nimg = wallman.relativeResizeByHeight(img, dim.y);
			}
		} // end image resizing

		// user wants to crop after resize
		if(wallpaper.getUseCrop()){
			nimg = wallman.cropByLocation(nimg, dim, wallpaper.getCropLocation());
		}

		return nimg;
	}

	/**
	 * Sets the device's wallpaper to the given Bitmap
	 * I may end up toasting to alert the user if this fails
	 * @param bm bitmap to set as the device's wallpaper
	 */
	public void setAsNewWallpaper(Bitmap bm){
		long now = new GregorianCalendar().getTimeInMillis();
		WallpaperManager wpm = WallpaperManager.getInstance(this);

		SettingsManager sm = SettingsManager.getInstance();
		long prev = sm.getLastChange();

		Log.d("WallInteractor", "Time since last change: " + (now - prev));

		sm.setLastChange(now);

		if((now - prev) < CHANGE_DELAY){
			String tmp = "Change too close to last! " + now + " " + prev + " " + (now - prev);
			Log.w("time", tmp);
			return;
		}

		try {
			wpm.setBitmap(bm);
		} catch (IOException e) {
			e.printStackTrace();
			// toast that change failed?
		}
	}

	/**
	 * Creates and displays a notification with information regarding failed change due to
	 * missing permissions.
	 * Includes a pending intent that will take users to this app's details page in the app manager
	 */
	private void resetAndDisplayPermissionNotification(){
		NotificationManager mNotificationManager =
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// seems to not add multiples, so should be unnecessary
//		mNotificationManager.cancel(mId);

		// build notification
		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(this)
						.setSmallIcon(R.mipmap.ic_launcher)
						.setContentTitle(getResources().getString(R.string.permission_fail_notification_title))
						.setContentText(getResources().getString(R.string.permission_fail_notification_short_text))
						.setAutoCancel(true);

		// set the intent
		PendingIntent pendingIntent = getOpenPermissionsPendingIntent();
		mBuilder.setContentIntent(pendingIntent);

		// creates and notifies notification manager of new notification
		mNotificationManager.notify(mId, mBuilder.build());
	}

	/**
	 * Creates and returns a PendingIntent to use with failed permission Notification
	 * @return PendingIntent that will open this app's settings page in the device application manager
	 */
	private PendingIntent getOpenPermissionsPendingIntent(){
		// set intent to open this app's details page in the application manager
		// specifies to open details
		Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
		// specified app to use
		intent.setData(Uri.parse("package:" + getPackageName()));

		//unique requestID to differentiate between various notification with same NotifId
		int requestID = (int) System.currentTimeMillis();
		// cancel old intent and create new one
		int flags = PendingIntent.FLAG_CANCEL_CURRENT;

		return PendingIntent.getActivity(this, requestID, intent, flags);
	}

	/**
	 * Verifies that SDK level is high enough to verify permission status,
	 * then verifies permission status.
	 * @return False if SDK level is high enough and permission is NOT granted, otherwise true
	 */
	private boolean checkSdkAndPermissions(){
		// actions past this check require version 16 or above
		if(Build.VERSION.SDK_INT < 16){
			// cant continue with check, so automatically passes
			// (technically, I could probably use 23+ as user cannot revoke permission before that)
			return true;
		}

		// make sure we still have permission to read files
		int check = PermissionChecker.checkCallingOrSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
		// return true if permission status is granted, otherwise will return false
		return check == PermissionChecker.PERMISSION_GRANTED;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		SettingsManager sm = SettingsManager.getInstance();

		// if we do not have permissions
		if(!checkSdkAndPermissions()){
			// user has revoked permissions, display a notification
			// (this notification currently will take user to app's page in app manager when clicked)
			resetAndDisplayPermissionNotification();
			return;
		}

		// make sure we have some wallpapers to choose from
		if(sm.getWallpaperList().size() > 0){
			Bitmap bm;
			// get settings from de-nulled wallpaper list to check if next should be random
			WallpaperList lst = sm.copyWallpaperListWithoutNull(sm.getWallpaperList());
			// get next image to use base on random/sequential as specified by user
			int lastPos = sm.getLastIndexPosition();
			if(lst.getRandomNext()){
				// user wants random wallpaper for next wallpaper
				lastPos = getRandIndexInList(sm.getWallpaperList());
			} else{
				// go in sequential order
				if(lastPos >= sm.getWallpaperList().size() - 1){
					// if lastPos not yet used, or it needs to go to beginning of list, set to 0
					lastPos = 0;
				} else{
					// next value is still in list
					lastPos++;
				}
				// no else needed, since value of pos remains 0 when nothing is matched
			}

			// update position of last used image
			sm.setLastIndexPosition(lastPos);
			// extremely important to replace any nulls before continuing!
			Wallpaper wallpaper = sm.copyWallpaperWithoutNull(sm.getWallpaperList().get(lastPos));

			// perform setup (including selecting new image, cropping/resizing, and saving to local storage)
			bm = setupNextWallpaper(wallpaper);

			// set the image as the new wallpaper
			setAsNewWallpaper(bm);
		} // end if list not empty
	}

}
