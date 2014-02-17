package no.hig.strand.lars.mtp.services;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import android.app.IntentService;
import android.content.Intent;

public class ActivityRecognitionIntentService extends IntentService {

	
	public ActivityRecognitionIntentService(String name) {
		super(name);
	}

	
	
	@Override
	protected void onHandleIntent(Intent intent) {
		if (ActivityRecognitionResult.hasResult(intent)) {
			ActivityRecognitionResult result = 
					ActivityRecognitionResult.extractResult(intent);
			
			DetectedActivity mostProbableActivity = 
					result.getMostProbableActivity();
			int confidence = mostProbableActivity.getConfidence();
			int activityType = mostProbableActivity.getType();
			String activityName = getNameFromType(activityType);
		}
	}
	
	
	
	private String getNameFromType(int activityType) {
		switch (activityType) {
		case DetectedActivity.IN_VEHICLE:
			return "in_vehicled";
		case DetectedActivity.ON_BICYCLE:
			return "on_bicycle";
		case DetectedActivity.ON_FOOT:
			return "on_foot";
		case DetectedActivity.STILL:
			return "still";
		case DetectedActivity.TILTING:
			return "tilting";
		case DetectedActivity.UNKNOWN:
			return "unknown";
		}
		return "unknown";
	}
}
