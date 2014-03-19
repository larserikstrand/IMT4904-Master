package no.hig.strand.lars.todoity.services;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import no.hig.strand.lars.todoity.R;
import no.hig.strand.lars.todoity.SettingsActivity;
import no.hig.strand.lars.todoity.Task;
import no.hig.strand.lars.todoity.TasksDb;
import no.hig.strand.lars.todoity.utils.Utilities;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;

public class RecommenderService extends Service implements 
		GooglePlayServicesClient.ConnectionCallbacks, 
		GooglePlayServicesClient.OnConnectionFailedListener {

	private TasksDb mTasksDb;
	private LocationClient mLocationClient;
	private Location mLastKnownLocation;
	
	// The maximum distance between current location and a task location for
	//  the locations to be counted as 'equal'.
	private static final int MAXIMUM_DISTANCE_LOCATION_RECOMMENDATION = 100;
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		mTasksDb = TasksDb.getInstance(this);
		mLocationClient = new LocationClient(this, this, this);
		mLocationClient.connect();
		mLastKnownLocation = null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		
		
		return START_STICKY;
	}
	
	
	
	/*
	 * Calculate the type of task (category) that is completed most often
	 *  at the time of day of the calculation.
	 */
	private AbstractMap.SimpleEntry<String, Float> timeOfDayRecommendation() {
		List<String> categories = readCategories();
		HashMap<String, Integer> categoryOccurrences = 
				new HashMap<String, Integer>();
		ArrayList<Task> tasks = mTasksDb.getTaskHistory();
		
		// Put the categories in a HashMap. This is used to count occurrences
		//  of what type of tasks happen at what times.
		for (String category : categories) {
			categoryOccurrences.put(category, 0);
		}
		
		// Get current time since midnight.
		long timeNow = Utilities.getTimeOfDay(
				Calendar.getInstance().getTimeInMillis());
		long startTimeTask;
		long endTimeTask;
		for (Task task : tasks) {
			if (task.getTimeStarted() > 0) {
				// Get start and end time since midnight of the task.
				startTimeTask = Utilities.getTimeOfDay(task.getTimeStarted());
				endTimeTask = Utilities.getTimeOfDay(task.getTimeEnded());
				
				if (startTimeTask < timeNow && endTimeTask > timeNow) {
					int occurrences = categoryOccurrences
							.get(task.getCategory()) + 1;
					categoryOccurrences.put(task.getCategory(), occurrences);
				}
			}
		}
		
		AbstractMap.SimpleEntry<String, Float> recommendation = 
				getRecommendationFromMap(categoryOccurrences);
		return recommendation;
	}
	
	
	
	private void timeOfDayAndDayOfWeekRecommendation() {
		
	}
	
	
	
	private void locationRecommendation() {
		
	}
	
	
	
	/*
	 * Calculates and recommends the type of task (category) that is completed 
	 * most often at the time of the calculation and the current location.
	 */
	private AbstractMap.SimpleEntry<String, Float> 
			timeOfDayAndLocationRecommendation() {
		if (mLocationClient.isConnected()) {
			mLastKnownLocation = mLocationClient.getLastLocation();
		} else if (mLastKnownLocation == null) {
			return null;
		}
		
		List<String> categories = readCategories();
		HashMap<String, Integer> categoryOccurrences = 
				new HashMap<String, Integer>();
		ArrayList<Task> tasks = mTasksDb.getTaskHistory();
		
		// Put the categories in a HashMap. This is used to count occurrences
		//  of what type of tasks happen at what times.
		for (String category : categories) {
			categoryOccurrences.put(category, 0);
		}
		
		// Get current time since midnight.
		long timeNow = Utilities.getTimeOfDay(
				Calendar.getInstance().getTimeInMillis());
		long startTimeTask;
		long endTimeTask;
		for (Task task : tasks) {
			
			// Check if the task location is the same as the current location.
			// TODO Should use collected location context here instead...
			float[] result = new float[3];
			Location.distanceBetween(mLastKnownLocation.getLatitude(), 
					mLastKnownLocation.getLongitude(), 
					task.getLocation().latitude, 
					task.getLocation().longitude, result);
			float distance = result[0];
			
			if (distance <= MAXIMUM_DISTANCE_LOCATION_RECOMMENDATION &&
					task.getTimeStarted() > 0) {
				// Get start and end time since midnight of the task.
				startTimeTask = Utilities.getTimeOfDay(task.getTimeStarted());
				endTimeTask = Utilities.getTimeOfDay(task.getTimeEnded());
				
				if (startTimeTask < timeNow && endTimeTask > timeNow) {
					int occurrences = categoryOccurrences
							.get(task.getCategory()) + 1;
					categoryOccurrences.put(task.getCategory(), occurrences);
				}
			}
		}
		
		AbstractMap.SimpleEntry<String, Float> recommendation = 
				getRecommendationFromMap(categoryOccurrences);
		return recommendation;
	}
	
	
	
	private void timeOfDayAndDayOfWeekAndLocationRecommendation() {
		
	}
	
	
	
	private List<String> readCategories() {
		List<String> categories;
		
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(this);
		String occupationPref = sharedPref.getString(
				SettingsActivity.PREF_OCCUPATION_KEY, "");
		if (occupationPref.equals(getString(R.string.pref_undergraduate))) {
			categories = Arrays.asList(getResources()
					.getStringArray(R.array.undergraduate_tasks_array));
		} else {
			categories = Arrays.asList(getResources()
					.getStringArray(R.array.postgraduate_tasks_array));
		}
		
		return categories;
	}
	
	
	
	private AbstractMap.SimpleEntry<String, Float> getRecommendationFromMap(
			HashMap<String, Integer> map) {
		Entry<String, Integer> highestOccurrence = null;
		int count = 0, total = 0;
		for (Entry<String, Integer> entry : map.entrySet()) {
			total += entry.getValue();
			if (highestOccurrence == null || 
					entry.getValue() > highestOccurrence.getValue()) {
				highestOccurrence = entry;
				count = entry.getValue();
			}
		}
		if (highestOccurrence != null) {
			float probability = count / total;
			AbstractMap.SimpleEntry<String, Float> recommendation = 
					new AbstractMap.SimpleEntry<String, Float>(
							highestOccurrence.getKey(), probability);
			return recommendation;
		}
		return null;
	}
	
	
	
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {}
	
	
	
	@Override
	public void onConnected(Bundle bundle) {
		mLastKnownLocation = mLocationClient.getLastLocation();
	}
	
	
	
	@Override
	public void onDisconnected() {
		mLocationClient = null;
	}
	
	
	
	
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}
