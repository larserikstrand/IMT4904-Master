package no.hig.strand.lars.todoity.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

public class ActivityRecognitionIntentService extends IntentService {
	
	
	public ActivityRecognitionIntentService() {
		super("ActivityRecognitionService");
	}

	
	
	@Override
	protected void onHandleIntent(Intent intent) {
		
		if (ActivityRecognitionResult.hasResult(intent)) {
			ActivityRecognitionResult result = 
					ActivityRecognitionResult.extractResult(intent);
			
			DetectedActivity mostProbableActivity = 
					result.getMostProbableActivity();
			int confidence = mostProbableActivity.getConfidence();
			Log.d("ACTIVITYRECOGNITIONINTENTSERVICE", Integer.toString(confidence));
			int activityType = mostProbableActivity.getType();
			String activityName = getNameFromType(activityType);
			
			Log.d("ACTIVITYRECOGNITIONSERVICE", "ACTIVITY " + activityName);
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
