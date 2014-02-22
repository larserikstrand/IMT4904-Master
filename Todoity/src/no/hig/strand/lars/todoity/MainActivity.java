package no.hig.strand.lars.todoity;

import no.hig.strand.lars.todoity.R;
import no.hig.strand.lars.todoity.Utilities.ErrorDialogFragment;
import no.hig.strand.lars.todoity.services.ContextService;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;


public class MainActivity extends FragmentActivity {

	private TabsPagerAdapter mTabsPagerAdapter;
	private ViewPager mViewPager;
	
	public static int mActiveTasks = 0;
	
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	
	public static final String TASKS_EXTRA = "no.hig.strand.lars.mtp.TASKS";
	public static final String DATE_EXTRA  = "no.hig.strand.lars.mtp.DATE";
	public static final String RECEIVER_EXTRA = 
			"no.hig.strand.lars.mtp.RECEIVER";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        
        mTabsPagerAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mTabsPagerAdapter);
        
        setupUI();
        
    }

    
    
    @Override
	protected void onResume() {
		super.onResume();
	}
    
    
    
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    
	
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
		case R.id.action_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		case R.id.action_about:
			startActivity(new Intent(this, AboutActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}



	private void setupUI() {
    	final ActionBar actionBar = getActionBar();
    	actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
    	
    	ActionBar.TabListener tabListener = new ActionBar.TabListener() {
			@Override
			public void onTabUnselected(Tab tab, FragmentTransaction ft) {}
			
			@Override
			public void onTabSelected(Tab tab, FragmentTransaction ft) {
				mViewPager.setCurrentItem(tab.getPosition());
			}
			
			@Override
			public void onTabReselected(Tab tab, FragmentTransaction ft) {}
		};
		
		actionBar.addTab(actionBar.newTab().setText(R.string.today)
				.setTabListener(tabListener));
		actionBar.addTab(actionBar.newTab().setText(R.string.week)
				.setTabListener(tabListener));
		actionBar.addTab(actionBar.newTab().setText(R.string.all_tasks)
				.setTabListener(tabListener));
		
		mViewPager.setOnPageChangeListener(
				new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				getActionBar().setSelectedNavigationItem(position);
			}
		});
		
    }
	
	
	
	public class DeleteListFromDatabase extends AsyncTask<String, Void, Void> {
		TasksDb tasksDb;

		@Override
		protected Void doInBackground(String... params) {
			TasksDb tasksDb;
			tasksDb = new TasksDb(getApplicationContext());
			tasksDb.open();
			
			tasksDb.deleteListByDate(params[0]);
			tasksDb.close();
			
			return null;
		}
		
	}
	
	
	
	public void startTask() {
		if (isServicesAvailable()) {
			mActiveTasks += 1;
			Intent intent = new Intent(this, ContextService.class);
			startService(intent);
		}
		
	}
	
	
	
	public void pauseTask() {
		mActiveTasks -= 1;
		Intent intent = new Intent(this, ContextService.class);
		stopService(intent);
	}
	
	
	
	private boolean isServicesAvailable() {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (resultCode == ConnectionResult.SUCCESS) {
			Log.d("MTP MainActivity", "Google Play Services is available");
			return true;
		} else {
			Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
					resultCode, this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
			if (errorDialog != null) {
				ErrorDialogFragment errorFragment = new ErrorDialogFragment();
				errorFragment.setDialog(errorDialog);
				errorFragment.show(getSupportFragmentManager(),
						"Location Updates");
			}
			return false;
		}
	}
	
	
	
	@Override
	protected void onActivityResult(
			int requestCode, int resultCode, Intent data) {
		if (requestCode == CONNECTION_FAILURE_RESOLUTION_REQUEST) {
			if (resultCode == RESULT_OK) {
				
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
    
    
    public class TabsPagerAdapter extends FragmentStatePagerAdapter {

    	
		public TabsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		
		
		@Override
		public Fragment getItem(int i) {
			switch (i) {
			case 0: return new TodayFragment();
			case 1: return new WeekFragment();
			case 2: return new AllTasksFragment();
			default: return new TodayFragment();
			}
		}

		
		
		@Override
		public int getCount() {
			return 3;
		}
    	
    }
    
    
}