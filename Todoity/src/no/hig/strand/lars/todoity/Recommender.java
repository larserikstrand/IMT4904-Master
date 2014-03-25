package no.hig.strand.lars.todoity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map.Entry;

import no.hig.strand.lars.todoity.TasksContract.ContextEntry;
import no.hig.strand.lars.todoity.utils.DatabaseUtilities;
import no.hig.strand.lars.todoity.utils.Utilities;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;

public class Recommender {
	
	public static class RecommendTask extends AsyncTask<Void, Integer, Void> 
			implements GooglePlayServicesClient.ConnectionCallbacks, 
					   GooglePlayServicesClient.OnConnectionFailedListener {

		private TodayFragment mFragment;
		private Context mContext;
		private int mProgressIncrements;
		private TasksDb mTasksDb;
		private LocationClient mLocationClient;
		private Location mLastKnownLocation;
		private long mTimeOfCalculation;
		private HashMap<String, Float> mRecommendationMap;
		private Task mRecommendedTask;
		private ArrayList<Task> mTaskHisory;
		private ArrayList<Task> mPlannedTasks;
		private ArrayList<Task> mRecommendedList;
		
		// The maximum distance between current location and the context location
		//  of a task for the locations to be counted as 'equal'.
		private static final int MAXIMUM_DISTANCE_LOCATION_RECOMMENDATION = 100;
		
		// Default time if task to use if recommender is not able to calculate
		//  the average time spent on a task and task has no 'end time'.
		private static final long DEFAULT_AVERAGE_TIME = 1000 * 60 * 60;
		
		
		
		public RecommendTask(TodayFragment fragment) {
			mFragment = fragment;
			mContext = fragment.getActivity();
		}
		
		

		@Override
		protected void onProgressUpdate(Integer... values) {
			mFragment.mProgress.setProgress(values[0]);
		}

		
		
		@Override
		protected void onPreExecute() {
			publishProgress(0);
		}



		@Override
		protected Void doInBackground(Void... params) {
			mTasksDb = TasksDb.getInstance(mContext);
			mLocationClient = new LocationClient(mContext, this, this);
			mLocationClient.connect();
			mLastKnownLocation = null;
			// Get calculation time (since midnight).
			mTimeOfCalculation = Utilities.getTimeOfDay(
					Calendar.getInstance().getTimeInMillis());
			mRecommendationMap = new HashMap<String, Float>();
			mRecommendedTask = null;
			
			publishProgress(10);
			
			mTaskHisory = mTasksDb.getTaskHistory();
			mPlannedTasks = mTasksDb.getTasksByDate(Utilities.getTodayDate());
			mProgressIncrements = mPlannedTasks.size() > 0 ?
					70 / mPlannedTasks.size() : 35;
			mRecommendedList = new ArrayList<Task>();
			
			// Put the currently active tasks first (should be on top of list).
			for (int i = mPlannedTasks.size() - 1; i >= 0; i--) {
				if (mPlannedTasks.get(i).isActive()) {
					mRecommendedList.add(mPlannedTasks.get(i));
					mPlannedTasks.remove(i);
				}
			}
			
			publishProgress(20);
			
			while (! mLocationClient.isConnected()) {}
			
			publishProgress(30);
			
			mLastKnownLocation = mLocationClient.getLastLocation();
			recommend(mTimeOfCalculation);
			updatePriorities();
			
			return null;
		}
		
		
		
		@Override
		protected void onPostExecute(Void result) {
			mFragment.new LoadTasksFromDatabase().execute();
			publishProgress(100);
		}

		
		
		// Recommend a task for the time provided as parameter
		private void recommend(long recommendationTime) {
			
			publishProgress((mRecommendedList.size() + 1) * mProgressIncrements);
			
			mRecommendedTask = null;
			mRecommendationMap.clear();
			timeOfDayRecommendation(recommendationTime);
			timeOfDayAndDayOfWeekRecommendation(recommendationTime);
			locationRecommendation();
			timeOfDayAndLocationRecommendation(recommendationTime);
			timeOfDayAndDayOfWeekAndLocationRecommendation(recommendationTime);
			
			float probability = 0;
			for (Entry<String, Float> entry : mRecommendationMap.entrySet()) {
				if (entry.getValue() > probability) {
					for (Task task : mPlannedTasks) {
						if (! task.isFinished() && 
								task.getCategory().equals(entry.getKey())) {
							mRecommendedTask = task;
						}
					}
				}
			}
			
			
			// A recommendation has been found.
			if (mRecommendedTask != null) {
				
				// Check if there is a task with a fixed time that may interfere
				//  with the task to be recommended.
				Task fixedTask = null;
				long fixedTaskStartTime = 0;
				for (Task task : mPlannedTasks) {
					if (! task.getFixedStart().isEmpty()) {
						long taskStartTime = Utilities.getTimeOfDay(
								task.getFixedStart());
						
						// If the start time of the task is later than 'now' and if
						//  the task is sooner than a previously found task or if
						//  a task have not been found.
						if (taskStartTime - recommendationTime > 0 && 
								( taskStartTime < fixedTaskStartTime
								|| fixedTaskStartTime == 0 )) {
							fixedTaskStartTime = taskStartTime;
							fixedTask = task;
						}
					}
				}
				
				// If a task with a fixed start time has been found, we must
				//  account for this.
				long avgTimeSpent  = getAverageTimeSpentOnTask(mRecommendedTask);
				if (fixedTask != null) {
					long timeToFixedStart = fixedTaskStartTime - recommendationTime;
						
					// If able to find an average time and this time is less 
					//  than the time until the fixed task is to be started. 
					//  Recommend task and perform new recommendation with
					//  new time.
					if (avgTimeSpent > 0 && avgTimeSpent < timeToFixedStart) {
						mRecommendedList.add(mRecommendedTask);
						mPlannedTasks.remove(mRecommendedTask);
						mTimeOfCalculation += avgTimeSpent;
						recommend(mTimeOfCalculation);
					
					// If not able to find an average time, or there is not enough
					//  time before the fixed task.
					} else {
						mRecommendedList.add(fixedTask);
						mPlannedTasks.remove(fixedTask);
						if (! fixedTask.getFixedEnd().isEmpty()) {
							long timeToNextTask = Utilities.getTimeOfDay(
									fixedTask.getFixedEnd());
							mTimeOfCalculation = timeToNextTask;
						} else {
							avgTimeSpent = getAverageTimeSpentOnTask(fixedTask);
							mTimeOfCalculation += avgTimeSpent > 0 ? 
									avgTimeSpent : DEFAULT_AVERAGE_TIME;
						}
						recommend(mTimeOfCalculation);
					}
						
				} else {
					mRecommendedList.add(mRecommendedTask);
					mPlannedTasks.remove(mRecommendedTask);
					mTimeOfCalculation += avgTimeSpent > 0 ? 
							avgTimeSpent : DEFAULT_AVERAGE_TIME;
					recommend(mTimeOfCalculation);
				}
			}
		}
		
		
		
		/*
		 * Calculate the type of task (category) that is completed most often
		 *  at the time of day of the calculation.
		 */
		private void timeOfDayRecommendation(long recommendationTime) {
			HashMap<String, Integer> categoryOccurrences = 
					new HashMap<String, Integer>();
			
			long startTimeTask;
			long endTimeTask;
			for (Task task : mTaskHisory) {
				if (task.getTimeStarted() > 0) {
					// Get start and end time since midnight of the task.
					startTimeTask = Utilities.getTimeOfDay(task.getTimeStarted());
					endTimeTask = Utilities.getTimeOfDay(task.getTimeEnded());
					
					if (startTimeTask < recommendationTime && 
							endTimeTask > recommendationTime) {
						int occurrences = 1;
						if (categoryOccurrences.containsKey(task.getCategory())) {
							occurrences = categoryOccurrences
									.get(task.getCategory()) + 1;
						}
						categoryOccurrences.put(task.getCategory(), occurrences);
					}
				}
			}

			addRecommendationsFromMap(categoryOccurrences);
		}
		
		
		
		private void timeOfDayAndDayOfWeekRecommendation(long recommendationTime) {
			HashMap<String, Integer> categoryOccurrences = 
					new HashMap<String, Integer>();
			
			int dayNow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
			long startTimeTask;
			long endTimeTask;
			for (Task task : mTaskHisory) {
				if (task.getTimeStarted() > 0) {
					// Get start and end time since midnight of the task.
					int dayTask = Utilities.getDayOfWeek(task.getTimeStarted());
					startTimeTask = Utilities.getTimeOfDay(task.getTimeStarted());
					endTimeTask = Utilities.getTimeOfDay(task.getTimeEnded());
					
					if (startTimeTask < recommendationTime && 
							endTimeTask > recommendationTime && 
							dayNow == dayTask) {
						int occurrences = 1;
						if (categoryOccurrences.containsKey(task.getCategory())) {
							occurrences = categoryOccurrences
									.get(task.getCategory()) + 1;
						}
						categoryOccurrences.put(task.getCategory(), occurrences);
					}
				}
			}
			
			addRecommendationsFromMap(categoryOccurrences);
		}
		
		
		
		private void locationRecommendation() {
			if (mLocationClient.isConnected()) {
				mLastKnownLocation = mLocationClient.getLastLocation();
			} else if (mLastKnownLocation == null) {
				return;
			}
			
			HashMap<String, Integer> categoryOccurrences = 
					new HashMap<String, Integer>();
			
			Cursor c;
			for (Task task : mTaskHisory) {
				
				c = mTasksDb.fetchContextsByTaskId(task.getId());
				if (c.moveToFirst()) {
					String location = c.getString(c.getColumnIndexOrThrow(
							ContextEntry.COLUMN_NAME_DETAILS));
					String[] latLng = location.split("\\s+");
					double latitude = Double.valueOf(latLng[0]);
					double longitude = Double.valueOf(latLng[1]);
					
					// Check if location where task was performed is the 
					//  same as the current location.
					float[] result = new float[3];
					Location.distanceBetween(mLastKnownLocation.getLatitude(), 
							mLastKnownLocation.getLongitude(), 
							latitude, longitude, result);
					float distance = result[0];
					
					if (distance <= MAXIMUM_DISTANCE_LOCATION_RECOMMENDATION) {
						int occurrences = 1;
						if (categoryOccurrences.containsKey(task.getCategory())) {
							occurrences = categoryOccurrences
									.get(task.getCategory()) + 1;
						}
						categoryOccurrences.put(task.getCategory(), occurrences);
					}
				}
				
			}
			
			addRecommendationsFromMap(categoryOccurrences);
		}
		
		
		
		/*
		 * Calculates and recommends the type of task (category) that is completed 
		 * most often at the time of the calculation and the current location.
		 */
		private void timeOfDayAndLocationRecommendation(long recommendationTime) {
			if (mLocationClient.isConnected()) {
				mLastKnownLocation = mLocationClient.getLastLocation();
			} else if (mLastKnownLocation == null) {
				return;
			}
			
			HashMap<String, Integer> categoryOccurrences = 
					new HashMap<String, Integer>();
			
			long startTimeTask;
			long endTimeTask;
			for (Task task : mTaskHisory) {
				
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
					
					if (startTimeTask < recommendationTime && 
							endTimeTask > recommendationTime) {
						int occurrences = 1;
						if (categoryOccurrences.containsKey(task.getCategory())) {
							occurrences = categoryOccurrences
									.get(task.getCategory()) + 1;
						}
						categoryOccurrences.put(task.getCategory(), occurrences);
					}
				}
			}
			
			addRecommendationsFromMap(categoryOccurrences);
		}
		
		
		
		private void timeOfDayAndDayOfWeekAndLocationRecommendation(
				long recommendationTime) {
			if (mLocationClient.isConnected()) {
				mLastKnownLocation = mLocationClient.getLastLocation();
			} else if (mLastKnownLocation == null) {
				return;
			}
			
			HashMap<String, Integer> categoryOccurrences = 
					new HashMap<String, Integer>();
			
			int dayNow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
			long startTimeTask;
			long endTimeTask;
			for (Task task : mTaskHisory) {
				
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
					int dayTask = Utilities.getDayOfWeek(task.getTimeStarted());
					startTimeTask = Utilities.getTimeOfDay(task.getTimeStarted());
					endTimeTask = Utilities.getTimeOfDay(task.getTimeEnded());
					
					if (startTimeTask < recommendationTime && 
							endTimeTask > recommendationTime &&
							dayTask == dayNow) {
						int occurrences = 1;
						if (categoryOccurrences.containsKey(task.getCategory())) {
							occurrences = categoryOccurrences
									.get(task.getCategory()) + 1;
						}
						categoryOccurrences.put(task.getCategory(), occurrences);
					}
				}
			}
			
			addRecommendationsFromMap(categoryOccurrences);
		}
		
		
		
		private void addRecommendationsFromMap(HashMap<String, Integer> map) {
			float total = 0;
			for (Entry<String, Integer> entry : map.entrySet()) {
				total += entry.getValue();
			}
			
			float probability;
			for (Entry<String, Integer> entry : map.entrySet()) {
				probability = (float) entry.getValue() / total;
				if (! mRecommendationMap.containsKey(entry.getKey()) ||
						mRecommendationMap.get(entry.getKey()) < probability) {
					mRecommendationMap.put(entry.getKey(), probability);
				}
			}
			
		}
		
		
		
		private long getAverageTimeSpentOnTask(Task task) {
			long totalTimeSpent = 0;
			int numberOfTasks = 0;
			for (Task t : mTaskHisory) {
				if (t.getCategory().equals(task.getCategory()) && 
						t.getTimeStarted() > 0) {
					numberOfTasks += 1;
					totalTimeSpent += task.getTimeSpent();
				}
			}
			return numberOfTasks > 0 ? totalTimeSpent / numberOfTasks : -1;
		}
		
		
		
		private void updatePriorities() {
			for (int i = 0; i < mRecommendedList.size(); i++) {
				mRecommendedList.get(i).setPriority(i+1);
				new DatabaseUtilities.UpdateTask(
						mContext, mRecommendedList.get(i)).execute();
			}
			for (int i = 0; i < mPlannedTasks.size(); i++) {
				mPlannedTasks.get(i).setPriority(mRecommendedList.size() + i + 1);
				new DatabaseUtilities.UpdateTask(
						mContext, mPlannedTasks.get(i)).execute();
			}
		}
		
		
		
		@Override
		public void onConnectionFailed(ConnectionResult connectionResult) {
			cancel(true);
		}
		
		
		
		@Override
		public void onConnected(Bundle bundle) {}
		
		
		
		@Override
		public void onDisconnected() {
			mLocationClient = null;
			cancel(true);
		}
		
	}
}
