package com.snazzy.android.snazzywalrus;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.GregorianCalendar;

/**
 * Created by SnazzyPanda on 6/15/16.
 *
 * Handles scheduling for the changer.
 * Schedules on real time clock, and does not wake device.
 *
 */
public class Scheduler {

	private static Scheduler scheduler;
	private PendingIntent pi;
	private AlarmManager alarmManager;

	// allowed delays: 5, 10, 15, 30, 60, 120, 360, 720, 1440
	private int delayInMinutes = 30;
	private boolean scheduled = false;
	private static final long ONE_MINUTE_AS_MILLISECONDS = AlarmManager.INTERVAL_HOUR / 60;

	private static final int FLAG = 0;
	private static final int REQUEST_CODE = 0;

	private Scheduler(){
		super();
	}

	private Scheduler(Context context){
		Intent i = new Intent(context, WallpaperInteractor.class);
		pi = PendingIntent.getService(context, REQUEST_CODE, i, FLAG);
	}

	public static synchronized Scheduler getInstance(Context context){
		if(null == scheduler){
			scheduler = new Scheduler(context);
		}
		return scheduler;
	}

	public void stopAlarms(Context context){
		alarmManager.cancel(pi);
		this.scheduled = false;
	}

	public PendingIntent getPendingIntent(){
		return this.pi;
	}

	public void setDelayInMinutes(int delay){
		this.delayInMinutes = delay;
	}

	public int getDelayInMinutes(){
		return this.delayInMinutes;
	}

	public boolean getScheduled(){
		return this.scheduled;
	}

	/**
	 * This method may result in times closer to the beginning of the minute, but is slightly more resource intensive
	 * @param interval The integer interval, in minutes, to calculate a near time with
	 * @return A near time in milliseconds as a long
	 */
	private long getCloseTimeFromNow(int interval){
		// setup base time to start on (attempting to get hour + interval, such as 1:00, 1:30, and avoiding things like 1:21, 4:53, etc)
		GregorianCalendar d = new GregorianCalendar();

		d.set(GregorianCalendar.SECOND, 0);
		if(this.delayInMinutes > 60){
			// greater than an hour, so work with that
			d.set(GregorianCalendar.MINUTE, 0);
			// set 24 hour time to...
			d.set(GregorianCalendar.HOUR_OF_DAY, (interval / 60));
		} else{

			// get the ((time/interval) + 1) * interval
			int min = d.get(GregorianCalendar.MINUTE) / interval * interval + interval;

			if(min < 60){
				d.set(GregorianCalendar.MINUTE, min);
			} else{
				// exceeded an hour, so set to next hour
				d.set(GregorianCalendar.MINUTE, 0);
				d.set(GregorianCalendar.HOUR_OF_DAY, d.get(GregorianCalendar.HOUR_OF_DAY) + 1);
			}
		}

		d.set(GregorianCalendar.MILLISECOND, 0);

		return d.getTimeInMillis();

	}

	/**
	 * Gets a zeroed out GregorianCalendar and returns the Millisecond time as a long
	 * This results in the scheduler being run once right away then schedule (I think) for every interval after that first run?
	 * (Though it may just be innaccuracies and it is actually trying to run at the start of the minute/whatever)
	 * @return A time as a long
	 */
	private long scheduleTimeFromNow(){
		// set it to go right away, and then after intervals
		GregorianCalendar d = new GregorianCalendar(0, 0, 0, 0, 0, 0);
		d.set(GregorianCalendar.MILLISECOND, 0);
		return d.getTimeInMillis();
	}

	/**
	 * Setup the alarm, which will occur every delayInMinutes (on hopefully a nice memorable time)
	 * @param context The context to use
	 */
	public void scheduleAlarms(Context context) {
		scheduleAlarms(context, false);
	}

	/**
	 * Schedule an alarm to run based on set interval
	 * @param context The context to use
	 * @param explicitFuture boolean whether to try to get explicit future date, or set to past and let alarm manager handle it.
	 */
	public void scheduleAlarms(Context context, boolean explicitFuture){
		long futureDate;
		if(explicitFuture){
			futureDate = getCloseTimeFromNow(delayInMinutes);
		} else{
			futureDate = scheduleTimeFromNow();
		}

		// get the alarm manager
		alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		// set the repeating task (will not wake phone up for this, we are NOT that important)
		alarmManager.setRepeating(AlarmManager.RTC, futureDate, delayInMinutes * ONE_MINUTE_AS_MILLISECONDS, pi);

		this.scheduled = true;
	}

	/**
	 * Runs the intent immediately, switching to next wallpaper (works even if nothing is scheduled yet)
	 * @param context The context to use
	 */
	public void forceIntentNow(Context context){
		Intent i = new Intent(context, WallpaperInteractor.class);
		pi = PendingIntent.getService(context, REQUEST_CODE, i, FLAG);
		try {
			pi.send();
		} catch (PendingIntent.CanceledException e) {
			e.printStackTrace();
		}
	}

}
