package no.hig.strand.lars.mtp.services;

import java.io.IOException;
import java.util.List;

import no.hig.strand.lars.mtp.MainActivity;
import no.hig.strand.lars.mtp.R;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

public class ContextService extends IntentService implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener,
		LocationListener {

	ResultReceiver resultReceiver;
	LocationClient locationClient;
	LocationRequest locationRequest;
	PendingIntent activityRecognitionIntent;
	ActivityRecognitionClient activityRecognitionClient;

	
	private static final int NOTIFICATION_ID = 1984;
	
	private static final int ACTIVITY_DETECTION_INTERVAL = 1000*10;
	
	
	public ContextService() {
		super("ContextService");
	}

	
	
	@Override
	public void onCreate() {
		super.onCreate();
		setIntentRedelivery(true);
		
		locationClient = new LocationClient(this, this, this);
		locationClient.connect();
		locationRequest = LocationRequest.create();
		locationRequest.setPriority(
				LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
		
		activityRecognitionClient = 
				new ActivityRecognitionClient(this, this, this);
		Intent intent = new Intent(this, ActivityRecognitionIntentService.class);
		activityRecognitionIntent = PendingIntent.getService(
				this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		activityRecognitionClient.connect();
		
		// ActivityRecognition should be handled here.. Shouldn't need its own service...
	}



	@Override
	public void onDestroy() {
		locationClient.disconnect();
		super.onDestroy();
	}



	@Override
	protected void onHandleIntent(Intent intent) {
		resultReceiver = intent.getParcelableExtra(MainActivity.RECEIVER_EXTRA);
		
		startServiceInForeground();
		
		final int SLEEP_TIME = 5000;
		
        while (true) {
            try {
                Thread.sleep(SLEEP_TIME);

                // TODO Do something useful...
                
  
            } catch (InterruptedException e) {
                // this should never happen
                e.printStackTrace();
            }
        }
	}
	
	
	
	private void startServiceInForeground() {
		final Intent intent = new Intent(this, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
				Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pending = PendingIntent.getActivity(this, 0, intent, 0);
		final Notification note = new NotificationCompat.Builder(this)
				.setContentTitle(getString(R.string.service_notification_title))
				.setContentText(getString(R.string.service_notification_message))
				.setSmallIcon(android.R.drawable.ic_popup_sync)
				.setContentIntent(pending)
				.build();
		note.flags |= Notification.FLAG_NO_CLEAR;
		startForeground(NOTIFICATION_ID, note);
	}



	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {}



	@Override
	public void onConnected(Bundle dataBundle) {
		locationClient.requestLocationUpdates(locationRequest, this);
		
		activityRecognitionClient.requestActivityUpdates(
				ACTIVITY_DETECTION_INTERVAL, activityRecognitionIntent);
		activityRecognitionClient.disconnect();
	}



	@Override
	public void onDisconnected() {}



	@Override
	public void onLocationChanged(Location location) {
		// TODO store location...
		Geocoder geocoder = new Geocoder(this);
		List<Address> addresses = null;
		try {
			addresses = geocoder.getFromLocation(
					location.getLatitude(), location.getLongitude(), 1);
		} catch (IOException e) {
			Log.e("MTP ContextService", "IO Exception in onLocationChanged()");
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			Log.e("MTP ContextService", "Illegal arguments " + 
					Double.toString(location.getLatitude()) + ", " +
					Double.toString(location.getLongitude()) +
					" passed to address service");
			e.printStackTrace();
		}
		if (addresses != null && addresses.size() > 0) {
			Address address = addresses.get(0);
			String addressText = "";
			for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
				addressText += address.getAddressLine(i);
				if (i < address.getMaxAddressLineIndex() -1) {
					addressText += ", ";
				}
			}
			
			Bundle bundle = new Bundle();
			bundle.putString("address", addressText);
			resultReceiver.send(100, bundle);
		}
	}

}
