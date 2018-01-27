package com.snazzy.android.snazzywalrus.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Created by SnazzyPanda on 6/15/16.
 *
 * Provides functions to manipulate android Bitmaps in various ways,
 * Such as cropping, resizing, loading, and saving
 *
 */
public class WallpaperManipulator {

	/**
	 * Specifies to crop from random (horizontal) location in image
	 */
	public static final int CROP_FROM_RANDOM = 0;
	/**
	 * Specifies to crop from left side of image
	 */
	public static final int CROP_FROM_LEFT = 1;
	/**
	 * Specifies to crop from right side of image
	 */
	public static final int CROP_FROM_RIGHT = 2;
	/**
	 * Specifies to crop image to its (horizontal) center
	 */
	public static final int CROP_FROM_CENTER = 3;

	/**
	 * Fallback and default file format to use when saving files.
	 * <p/>
	 * Android should support: PNG, JPG, (BMP, WEBP)?
	 */
	private static final String DEFAULT_FILE_FORMAT = "PNG";

	private static final int DEFAULT_PREVIEW_SIZE = 200;
	private static final int DEFAULT_PREVIEW_NUMBER = 3;


	public WallpaperManipulator(){
		super();
	}

	/**
	 * Attempt to load the file into a Bitmap object
	 * @return A Bitmap object or null
	 */
	public Bitmap getBitmapFromString(String loc){
		Bitmap img = null;

		try{
			img = BitmapFactory.decodeFile(loc);
		} catch (Exception e){
			e.printStackTrace();
		}

		return img;
	}

	public int getScaledWidth(int originalWidth, int originalHeight, int newHeight){
		return (originalWidth * newHeight) / originalHeight;
	}

	public int getScaledHeight(int originalWidth, int originalHeight, int newWidth){
		return (newWidth * originalHeight) / originalWidth;
	}

	public int getScaledWidth(Point original, int newHeight){
		return getScaledWidth(original.x, original.y, newHeight);
	}

	public int getScaledHeight(Point original, int newWidth){
		return getScaledHeight(original.x, original.y, newWidth);
	}

	public int getScaledWidth(Bitmap bm, int newHeight){
		return getScaledWidth(bm.getWidth(), bm.getHeight(), newHeight);
	}

	public int getScaledHeight(Bitmap bm, int newWidth){
		return getScaledHeight(bm.getWidth(), bm.getHeight(), newWidth);
	}

	/**
	 * Attempts to resize the given Bitmap to fit the given size, uses filtering
	 * @param bm Bitmap to resize
	 * @param width Width to size the Bitmap
	 * @param height Height to size the Bitmap
	 * @return resized Bitmap
	 */
	public Bitmap resizeToSize(Bitmap bm, int width, int height){
		return resizeToSize(bm, width, height, true);
	}

	/**
	 * Resizes given Bitmap to the given size
	 * @param bm Bitmap to resize
	 * @param width pixel width as integer to resize Bitmap to
	 * @param height pixel height as integer to resize Bitmap to
	 * @param filter Boolean for using more advanced filters, true to use false to not use (filtering will smooth edges, which typically results in better quality. However, pixel art, sprites, etc would likely benefit from not using the filter)
	 * @return Resized Bitmap
	 */
	public Bitmap resizeToSize(Bitmap bm, int width, int height, boolean filter){
		return Bitmap.createScaledBitmap(bm, width, height, filter);
	}

	/**
	 * Resizes a Bitmap using the x value of a point as the pixel width and y value as pixel height for the new image
	 * @param bm Bitmap to resize
	 * @param dims Point with width to use in x and height to use in y
	 * @return Resized Bitmap
	 */
	public Bitmap resizeToSize(Bitmap bm, Point dims){
		return resizeToSize(bm, dims, true);
	}

	/**
	 * Resizes a Bitmap using the x value of a point as the pixel width and y value as pixel height for the new image
	 * @param bm Bitmap to resize
	 * @param dims Point with width to use in x and height to use in y
	 * @param filter Boolean for using more advanced filters, true to use false to not use (filtering will smooth edges, which typically results in better quality. However, pixel art, sprites, etc would likely benefit from not using the filter)
	 * @return Resized Bitmap
	 */
	public Bitmap resizeToSize(Bitmap bm, Point dims, boolean filter){
		return Bitmap.createScaledBitmap(bm, dims.x, dims.y, filter);
	}

	/**
	 * Resizes a bitmap while retaining the width to height ratio, using only width, uses filtering
	 * @param bm The Bitmap to resize
	 * @param width The integer desired width for the new Bitmap
	 * @return Resized Bitmap
	 */
	public Bitmap relativeResizeByWidth(Bitmap bm, int width){
		return relativeResizeByWidth(bm, width, true);
	}

	/**
	 * Resizes a bitmap while retaining the width to height ratio, using only width
	 * @param bm The Bitmap to resize
	 * @param width The integer desired width for the new Bitmap
	 * @param filter Boolean for using more advanced filters, true to use false to not use (filtering will smooth edges, which typically results in better quality. However, pixel art, sprites, etc would likely benefit from not using the filter)
	 * @return Resized Bitmap
	 */
	public Bitmap relativeResizeByWidth(Bitmap bm, int width, boolean filter){
		int newHeight = getScaledHeight(bm, width);

		// note that filter smooths edges on scaling, (so I would only not filter for pixel art type images...)
		return Bitmap.createScaledBitmap(bm, width, newHeight, filter);
	}

	/**
	 * Resizes a bitmap while retaining the width to height ratio, using only height, uses filtering
	 * @param bm The Bitmap to resize
	 * @param height The integer desired height for the new Bitmap
	 * @return Resized Bitmap
	 */
	public Bitmap relativeResizeByHeight(Bitmap bm, int height){
		return relativeResizeByHeight(bm, height, true);
	}

	/**
	 * Resizes a bitmap while retaining the width to height ratio, using only height
	 * @param bm The Bitmap to resize
	 * @param height The integer desired height for the new Bitmap
	 * @param filter Boolean for using more advanced filters, true to use false to not use (filtering will smooth edges, which typically results in better quality. However, pixel art, sprites, etc would likely benefit from not using the filter)
	 * @return Resized Bitmap
	 */
	public Bitmap relativeResizeByHeight(Bitmap bm, int height, boolean filter){
		int newWidth = getScaledWidth(bm, height);

		// note that filter smooths edges on scaling, (so I would only not filter for pixel art type images...)
		return Bitmap.createScaledBitmap(bm, newWidth, height, filter);
	}

	public Bitmap cropToSize(Bitmap bm, int startX, int startY, int width, int height){
		return Bitmap.createBitmap(bm, startX, startY, width, height);
	}

	public Bitmap cropRandomToSize(Bitmap bm, int minXSize, int minYSize, int maxXSize, int maxYSize){
		return null;
	}

	/**
	 * Crop a section (random/pseudorandomly selected?) from the image to use as background
	 * @param bm Bitmap image to crop from
	 * @return Bitmap image cropped to screen dimensions (roughly?)
	 */
	public Bitmap cropToSize(Bitmap bm, int width, int height){
		return null;
	}

	/**
	 * Crops the image to fit the same screen ratio as the screen, then resizes to fit the screen.
	 * (The best of both worlds!)
	 * @param bm Bitmap image to crop and resize
	 * @return Cropped and resized Bitmap image
	 */
	public Bitmap cropAndResizeToSize(Bitmap bm, int width, int height){
		return null;
	}

	/**
	 * Crops given Bitmap by random horizontal location
	 * @param bm Bitmap to crop
	 * @param dims Point with desired width in x and desired height in y
	 * @return A Bitmap cropped at random horizontal location to given width and height (x and y of point)
	 */
	public Bitmap cropByLocation(Bitmap bm, Point dims){
		return cropByLocation(bm, dims, CROP_FROM_RANDOM);
	}

	/**
	 * Crops given Bitmap by random horizontal location
	 * @param bm Bitmap to crop
	 * @param width Integer width in pixels to crop image to
	 * @param height Integer height in pixels to crop image to
	 * @return A Bitmap cropped at random horizontal location to given width and height
	 */
	public Bitmap cropByLocation(Bitmap bm, int width, int height){
		return cropByLocation(bm, width, height, CROP_FROM_RANDOM);
	}

	/**
	 * Crop the image based on location specified and dimensions given
	 * @param bm Image to manipulate
	 * @param dims Point with width stored in x and height stored in y
	 * @param location integer location (such as CROP_FROM_RANDOM) specifying location to crop from
	 * @return Bitmap cropped based on size and location given
	 */
	public Bitmap cropByLocation(Bitmap bm, Point dims, int location){
		return cropByLocation(bm, dims.x, dims.y, location);
	}

	/**
	 * Crop the image based on location specified and dimensions given
	 * @param bm Image to manipulate
	 * @param width integer for pixel width
	 * @param height integer for pixel height
	 * @param location integer location (such as CROP_FROM_RANDOM) specifying location to crop from
	 * @return Bitmap cropped based on given params
	 */
	public Bitmap cropByLocation(Bitmap bm, int width, int height, int location){

		//TODO separate vertical and horizontal, check which (if either) we can use for given image (ie, is image and screen same width? then dont use by width)
		// other variations?

		// specify left, center, right
		// alternative? - top, middle, bottom

		Random rnd = new Random();

		int startX = 0;
		int startY = 0;

		switch(location){
			case CROP_FROM_RANDOM:
				// set the farthest right we can go and stay within the image
				int maxX = bm.getWidth() - width;
				// make sure the difference is not 0 to prevent crashing on nextInt
				if(maxX > 0){
					startX = rnd.nextInt(maxX);
				}
				break;
			case CROP_FROM_LEFT:
				// startX already 0
				break;
			case CROP_FROM_RIGHT:
				startX = bm.getWidth() - width;
				break;
			case CROP_FROM_CENTER:
				// calculate center offset: (fullwidth - desiredwidth) / 2
				startX = (bm.getWidth() - width) / 2;
				break;
			default:
				Log.w("WallpaperManipulator", "unhandled crop location supplied: " + location + " defaulting to random...");
				// crop by random as a fallback
				maxX = bm.getWidth() - width;
				if(maxX > 0){
					startX = rnd.nextInt(maxX);
				}
				// else startX 0 by default
				break;
		} // end switch case

		// if Bitmap is not larger or equal to screen size, createBitmap will try to access data that does not exist in image, and throw error
		// to prevent this, make sure the final x and y are accessible from given bitmap (final x given by startx + width, similar for y)
		// hopefully this does not mess with anything unintentionally
		if((startY + height) > bm.getHeight()){
			height = bm.getHeight() - startY;
		}
		if((startX + width) > bm.getWidth()){
			width = bm.getWidth() - startX;
		}

		return Bitmap.createBitmap(bm, startX, startY, width, height);
	}

	/**
	 * Saves the given bitmap to the given directory with the given filename, using given format
	 * @param bm Bitmap to save
	 * @param dir String location to save to
	 * @param filename String name to give file
	 * @param compressFormat Bitmap.CompressFormat to save file with
	 */
	public void saveBitmap(Bitmap bm, File dir, String filename, Bitmap.CompressFormat compressFormat){
		FileOutputStream out;
		// make path if it does'nt exist
		dir.mkdirs();
		// create file
		File file = new File(dir, filename);
		try {
			// set the filestream to new file
			out = new FileOutputStream(file);
			// output Bitmap to file
			bm.compress(compressFormat, 100, out);
			// flush and close the stream
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Saves the given bitmap to the given directory with the given filename, using given format
	 * @param bm Bitmap to save
	 * @param dir File location to save to
	 * @param filename String name to give file
	 * @param compressFormat String compression format to save file with
	 */
	public void saveBitmap(Bitmap bm, File dir, String filename, String compressFormat){
		saveBitmap(bm, dir, filename, Bitmap.CompressFormat.valueOf(compressFormat));
	}

	/**
	 * Saves the given bitmap to the given directory with the given filename, defaults to PNG output
	 * @param bm Bitmap to save
	 * @param dir File location to save to
	 * @param filename String name to give file
	 */
	public void saveBitmap(Bitmap bm, File dir, String filename){
		saveBitmap(bm, dir, filename, DEFAULT_FILE_FORMAT);
	}

	/**
	 * Saves the given bitmap to the given directory with the given filename, defaults to PNG output
	 * @param bm Bitmap to save
	 * @param dir String location to save to
	 * @param filename String name to give file
	 */
	public void saveBitmap(Bitmap bm, String dir, String filename){
		saveBitmap(bm, new File(dir), filename);
	}


	/**
	 * Creates a preview image where each image is slightly offset from previous to get a single image
	 * showing layered images from specified, to use as a preview image for Sets of images.
	 * @param list ArrayList of strings that identify the images to use
	 * @param limit Integer number of images to display in preview
	 * @param width Integer width for preview image to be
	 * @param height Integer height for preview image to be
	 * @param pctImageOffset Float percent (so 5% = .05f) of image to be visible when covered
	 * @return Bitmap preview image with given images layered (with offset) on each other
	 */
	public Bitmap generatePreview(ArrayList<String> list, int limit, int width, int height, float pctImageOffset){
		// list is list of paths to use as bitmaps for overlaying
		// limit is number of images to overlay for preview
		// width is output width of final bitmap
		// height is output height of final bitmap
		// OffsetPerImage (Percentage viewable if image is on top of it)
		// 10% seems fine for now

		// if no images to use return
		if(0 == list.size()){
			return null;
		}

		ArrayList<Bitmap> imgList = new ArrayList<>();

		// if desired images to use exceeds images given, set to number we can use
		if(limit > list.size()){
			limit = list.size();
		}

		// total percentage is: ((NumImages - 1) * PercentOffsetPerImage)
		float finalPct = (limit - 1) * pctImageOffset;
		// calculate height and width for individual pictures
		// ((1 - TotalPercentageUsed) * FullPossibleWidth) -- (or height)
		int picWidth = (int) ((1 - finalPct) * width);
		int picHeight = (int) ((1 - finalPct) * height);

		// grab all images we are going to use for preview
		for(int i = 0; i < limit; i++){
			Bitmap bmp = BitmapFactory.decodeFile(list.get(i));
			imgList.add(bmp);
		}

		// create bitmap and tie it to a canvas
		Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		// reverse list so last image is first
		Collections.reverse(imgList);
		for(int i = 0; i < imgList.size(); i++){
			// get a scaled down bitmap to put in preview image
			Bitmap tmp = Bitmap.createScaledBitmap(imgList.get(i), picWidth, picHeight, true);

			// calculate starting x val
			// (FullPossibleWidth - IndividualImageWidth) - (thisIteration * (PercentOffsetPerImage * FullPossibleWidth)
			float sW =  ((width - picWidth) - (i * (pctImageOffset * width)));
			// calculate starting y val
			// (thisIteration * (PercentOffsetPerImage * FullPossibleHeight))
			float sH = (i * (pctImageOffset * height));

			// draw bitmap onto preview image
			canvas.drawBitmap(tmp, sW, sH, null);
		}

		return output;
	}

	/**
	 * Creates a preview image where each image is slightly offset from previous to get a single image
	 * showing layered images from specified, to use as a preview image for Sets of images.
	 * @param list ArrayList of strings that identify the images to use
	 * @param limit Integer number of images to display in preview
	 * @param width Integer width for preview image to be
	 * @param height Integer height for preview image to be
	 * @return Bitmap preview image with given images layered (with offset) on each other
	 */
	public Bitmap generatePreview(ArrayList<String> list, int limit, int width, int height){
		return generatePreview(list, limit, width, height, .1f);
	}

	/**
	 * Creates a preview image where each image is slightly offset from previous to get a single image
	 * showing layered images from specified, to use as a preview image for Sets of images.
	 * @param list ArrayList of strings that identify the images to use
	 * @param limit Integer number of images to display in preview
	 * @param size Integer height and width to use for the preview
	 * @param pctImageOffset Float percent (so 5% = .05f) of image to be visible when covered
	 * @return Bitmap preview image with given images layered (with offset) on each other
	 */
	public Bitmap generatePreview(ArrayList<String> list, int limit, int size, float pctImageOffset){
		return generatePreview(list, limit, size, size, pctImageOffset);
	}

	/**
	 * Creates a preview image where each image is slightly offset from previous to get a single image
	 * showing layered images from specified, to use as a preview image for Sets of images.
	 * @param list ArrayList of strings that identify the images to use
	 * @param limit Integer number of images to display in preview
	 * @param size Integer height and width to use for the preview
	 * @return Bitmap preview image with given images layered (with offset) on each other
	 */
	public Bitmap generatePreview(ArrayList<String> list, int limit, int size){
		return generatePreview(list, limit, size, size);
	}

	/**
	 * Creates a preview image where each image is slightly offset from previous to get a single image
	 * showing layered images from specified, to use as a preview image for Sets of images.
	 * @param list ArrayList of strings that identify the images to use
	 * @param limit Integer number of images to display in preview
	 * @return Bitmap preview image with given images layered (with offset) on each other
	 */
	public Bitmap generatePreview(ArrayList<String> list, int limit){
		return generatePreview(list, limit, DEFAULT_PREVIEW_SIZE);
	}

	/**
	 * Creates a preview image where each image is slightly offset from previous to get a single image
	 * showing layered images from specified, to use as a preview image for Sets of images.
	 * @param list ArrayList of strings that identify the images to use
	 * @return Bitmap preview image with given images layered (with offset) on each other
	 */
	public Bitmap generatePreview(ArrayList<String> list){
		return generatePreview(list, DEFAULT_PREVIEW_NUMBER);
	}

}
