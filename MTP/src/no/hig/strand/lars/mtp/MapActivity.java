package no.hig.strand.lars.mtp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends FragmentActivity {

	private GoogleMap mMap;
	private LatLng mLocation;
	private MarkerOptions mMarkerOptions;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		// Show the Up button in the action bar.
		setupActionBar();
		
		mMap = ((SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map)).getMap();
		mLocation = null;
		mMarkerOptions = new MarkerOptions();
		
		setupUI();
	}

	
	
	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	
		

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	
	private void setupUI() {
		mMap.setMyLocationEnabled(true);
		
		mMap.setOnMapClickListener(new OnMapClickListener() {
			@Override
			public void onMapClick(LatLng latLng) {
				mMap.clear();
				mLocation = latLng;
				mMarkerOptions.position(mLocation);
				mMap.addMarker(mMarkerOptions);
			}
		});
		
		Button button = (Button) findViewById(R.id.done_button);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mLocation != null) {
					Intent data = new Intent();
					data.putExtra(NewTaskActivity.LOCATION_EXTRA, mLocation);
					setResult(RESULT_OK, data);
				}
				finish();
			}
		});
	}

}
