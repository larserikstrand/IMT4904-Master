package no.hig.strand.lars.todoity;

import java.util.Comparator;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

public class Task implements Parcelable {

	private int mId;
	
	private String mAppEngineId;
	
	private String mCategory;
	private String mDescription;
	private LatLng mLocation;
	private String mAddress;
	// Whether or not the task is currently in progress
	private boolean mIsActive;
	// Temporary time in milliseconds for the task
	private int mTempStart;
	// Actual times for the task
	private String mTimeStarted;
	private String mTimeEnded;
	// Total time spent on task in milliseconds.
	private int mTimeSpent;
	// Whether or not the task is finished (i.e. should sometimes not be displayed).
	private boolean mIsFinished;
	
	// Variables for tasks with fixed start and end times.
	private String mFixedStart;
	private String mFixedEnd;
	
	public Task() {
		mId = 0;
		mAppEngineId = "";
		mCategory = "";
		mDescription = "";
		mLocation = new LatLng(0, 0);
		mAddress = "";
		mIsActive = false;
		mTempStart = 0;
		mTimeStarted = "";
		mTimeEnded = "";
		mTimeSpent = 0;
		mIsFinished = false;
		
		mFixedStart = "";
		mFixedEnd = "";
	}
	
	
	
	public Task(Parcel in) {
		readFromParcel(in);
	}
	
	
	
	@Override
	public int describeContents() {
		return 0;
	}

	
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(mId);
		dest.writeString(mCategory);
		dest.writeString(mDescription);
		dest.writeParcelable(mLocation, flags);
		dest.writeString(mAddress);
		dest.writeInt((int) (mIsActive ? 1 : 0));
		dest.writeInt(mTempStart);
		dest.writeString(mTimeStarted);
		dest.writeString(mTimeEnded);
		dest.writeInt(mTimeSpent);
		dest.writeString(mFixedStart);
		dest.writeString(mFixedEnd);
	}
	
	
	
	private void readFromParcel(Parcel in) {
		mId = in.readInt();
		mCategory = in.readString();
		mDescription = in.readString();
		mLocation = in.readParcelable(LatLng.class.getClassLoader());
		mAddress = in.readString();
		mIsActive = in.readInt() != 0;
		mTempStart = in.readInt();
		mTimeStarted = in.readString();
		mTimeEnded = in.readString();
		mTimeSpent = in.readInt();
		mFixedStart = in.readString();
		mFixedEnd = in.readString();
	}
	
	
	
	public static final Parcelable.Creator<Task> CREATOR = 
			new Parcelable.Creator<Task>() {

				@Override
				public Task createFromParcel(Parcel source) {
					return new Task(source);
				}

				@Override
				public Task[] newArray(int size) {
					return new Task[size];
				}
		
		
	};
	
	
	
	public int getId() {
		return mId;
	}
	
	
	
	public void setId(int id) {
		mId = id;
	}
	
	
	
	/**
	 * To be used solely for AppEngine calls.
	 * @return
	 */
	public String getAppEngineId() {
		return mAppEngineId;
	}
	
	
	
	public void setAppEngineId(String appEngineId) {
		mAppEngineId = appEngineId;
	}
	
	
	
	public String getCategory() {
		return mCategory;
	}


	
	public void setCategory(String category) {
		mCategory = category;
	}
	
	
	
	public String getDescription() {
		return mDescription;
	}

	
	
	public void setDescription(String description) {
		mDescription = description;
	}
	
	
	
	public LatLng getLocation() {
		return mLocation;
	}

	
	
	public void setLocation(LatLng location) {
		mLocation = location;
	}
	
	
	
	public String getAddress() {
		return mAddress;
	}
	
	
	
	public void setAddress(String address) {
		mAddress = address;
	}
	
	
	
	public boolean isActive() {
		return mIsActive;
	}

	
	
	public void setActive(boolean isActive) {
		mIsActive = isActive;
	}
	
	
	
	public int getTempStart() {
		return mTempStart;
	}
	
	
	
	public void setTempStart(int start) {
		mTempStart = start;
	}
	
	
	
	public String getTimeStarted() {
		return mTimeStarted;
	}

	
	
	public void setTimeStarted(String timeStarted) {
		mTimeStarted = timeStarted;
	}
	
	
	
	public String getTimeEnded() {
		return mTimeEnded;
	}

	
	
	public void setTimeEnded(String timeEnded) {
		mTimeEnded = timeEnded;
	}
	
	
	
	public int getTimeSpent() {
		return mTimeSpent;
	}
	
	
	
	public void setTimeSpent(int timeSpent) {
		mTimeSpent = timeSpent;
	}
	
	
	
	public void updateTimeSpent(int time) {
		mTimeSpent += time;
	}
	
	
	
	public boolean isFinished() {
		return mIsFinished;
	}
	
	
	
	public void setFinished(boolean isFinished) {
		mIsFinished = isFinished;
	}
	
	
	
	public String getFixedStart() {
		return mFixedStart;
	}
	
	
	
	public void setFixedStart(String fixedStart) {
		mFixedStart = fixedStart;
	}
	
	
	
	public String getFixedEnd() {
		return mFixedEnd;
	}
	
	
	
	public void setFixedEnd(String fixedEnd) {
		mFixedEnd = fixedEnd;
	}

	
	
	public static class TaskCategoryComparator implements Comparator<Task> {
		@Override
		public int compare(Task lhs, Task rhs) {
			String task1 = lhs.getCategory() + ": " + lhs.getDescription();
			String task2 = rhs.getCategory() + ": " + rhs.getDescription();
			return task1.compareTo(task2);
		}
	}
	
}
