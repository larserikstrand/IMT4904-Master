package no.hig.strand.lars.todoity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import no.hig.strand.lars.todoity.utils.AppEngineUtilities;
import no.hig.strand.lars.todoity.utils.DatabaseUtilities;
import no.hig.strand.lars.todoity.utils.DatabaseUtilities.DeleteList;
import no.hig.strand.lars.todoity.utils.DatabaseUtilities.DeleteTask;
import no.hig.strand.lars.todoity.utils.DatabaseUtilities.MoveTaskToDate;
import no.hig.strand.lars.todoity.utils.DatabaseUtilities.OnDeletionCallback;
import no.hig.strand.lars.todoity.utils.DatabaseUtilities.OnTaskMovedCallback;
import no.hig.strand.lars.todoity.utils.Utilities;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;


public class TodayFragment extends Fragment {
	
	private View mRootView;
	private ArrayList<Task> mTasks;
	private TodayListAdapter mAdapter;
	private int mSelectedTask;
	
	// The minimum number of seconds before a task is considered as
	//  actually started.
	public static final int MINIMUM_TASK_START = 1000 * 10;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_today, 
				container, false);
		
		mRootView = rootView;
		mTasks = new ArrayList<Task>();
		mAdapter = new TodayListAdapter(getActivity(), mTasks);
		
		setupUI();
		new LoadTasksFromDatabase().execute();
		
		return rootView;
	}



	@Override
	public void onResume() {
		super.onResume();
		setUserVisibleHint(true);
	}
	
	
	
	// Workaround for getting tasks from database when fragment enters view.
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser) {
			new LoadTasksFromDatabase().execute();
		}
	}

	
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		mSelectedTask = (Integer) v.getTag();
		MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.context_menu_task, menu);
	}
	
	
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (getUserVisibleHint() == false) {
			return false;
		}
		switch (item.getItemId()) {
		case R.id.move_task:
			DialogFragment dpf = new MainActivity.DatePickerFragment();
			dpf.setTargetFragment(this, 0);
			dpf.show(getActivity().getSupportFragmentManager(), "datePicker");
			return true;
		case R.id.delete_task:
			new DeleteTask(getActivity(), mTasks.get(mSelectedTask),
					new OnDeletionCallback() {
				@Override
				public void onDeletionDone() {
					mTasks.remove(mSelectedTask);
					mAdapter = new TodayListAdapter(getActivity(), mTasks);
					ListView listView = (ListView) mRootView
							.findViewById(R.id.tasks_list);
					listView.setAdapter(mAdapter);
				}
			}).execute();
			return true;
		}
		return super.onContextItemSelected(item);
	}



	private void setupUI() {
		ListView listView = (ListView) mRootView.findViewById(R.id.tasks_list);
		listView.setAdapter(mAdapter);
		
		Button button = (Button) mRootView.findViewById(R.id.new_list_button);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getActivity(), ListActivity.class));
			}
		});
		
		button = (Button) mRootView.findViewById(R.id.edit_button);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Check if there are tasks to edit
				if (! mTasks.isEmpty()) {
					Intent intent = new Intent(getActivity(),
							ListActivity.class);
					String date = Utilities.getTodayDate();
					intent.putExtra(MainActivity.TASKS_EXTRA, mTasks);
					intent.putExtra(MainActivity.DATE_EXTRA, date);
					startActivity(intent);
				}
			}
		});
		button.setEnabled(false);
		
		button = (Button) mRootView.findViewById(R.id.delete_button);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Show dialog to the user asking for confirmation of deletion.
				Utilities.showConfirmDialog(getActivity(), 
						getString(R.string.confirm), 
						getString(R.string.delete_list_message), 
						new Utilities.ConfirmDialogListener() {
					// The user confirms deletion.
					@Override
					public void PositiveClick(DialogInterface dialog, int id) {
						String date = Utilities.getTodayDate();
						new DeleteList(getActivity(), new OnDeletionCallback() {
							// Task has been deleted. Update UI.
							@Override
							public void onDeletionDone() {
								mTasks.clear();
								ListView listView = (ListView) mRootView
										.findViewById(R.id.tasks_list);
								listView.setAdapter(new TodayListAdapter(
										getActivity(), mTasks));
								Button edit = (Button) mRootView
										.findViewById(R.id.edit_button);
								Button delete = (Button) mRootView
										.findViewById(R.id.delete_button);
								edit.setEnabled(false);
								delete.setEnabled(false);
							}
						}).execute(date);
					}
				});
			}
		});
		button.setEnabled(false);
	}
	
	
	
	private void startTask(Task task) {
		task.setActive(true);
		
		Date now = Calendar.getInstance().getTime();
		long timeStart = now.getTime();
		task.setTempStart(timeStart);
		new DatabaseUtilities.UpdateTask(getActivity(), task).execute();
		new AppEngineUtilities.UpdateTask(getActivity(), task).execute();
		
		if (getActivity() instanceof MainActivity) {
			MainActivity activity = (MainActivity) getActivity();
			activity.startTask();
		}
	}
	
	
	
	private void pauseTask(Task task) {
		task.setActive(false);
		
		Date now = Calendar.getInstance().getTime();
		long timeEnd = now.getTime();
		// If the task isn't considered as started
		if (task.getTimeStarted() == 0) {
			// If enough time has passed, consider the task as started.
			if ((timeEnd - task.getTempStart()) > MINIMUM_TASK_START) {
				task.setTimeStarted(task.getTempStart());
				task.setTimeEnded(timeEnd);
				task.updateTimeSpent(timeEnd - task.getTempStart());
			}
		// Task is considered started, update times.
		} else {
			task.setTimeEnded(timeEnd);
			task.updateTimeSpent(timeEnd - task.getTempStart());
		}
		
		new DatabaseUtilities.UpdateTask(getActivity(), task).execute();
		new AppEngineUtilities.UpdateTask(getActivity(), task).execute();
		
		if (getActivity() instanceof MainActivity) {
			MainActivity activity = (MainActivity) getActivity();
			activity.pauseTask();
		}
	}
	
	
	
	public void onDateSet(String date) {
		String today = Utilities.getTodayDate();
		if (! date.equals(today)) {
			new MoveTaskToDate(getActivity(), mTasks.get(mSelectedTask), date, 
					new OnTaskMovedCallback() {
				@Override
				public void onTaskMoved() {
					mTasks.remove(mSelectedTask);
					mAdapter = new TodayListAdapter(getActivity(), mTasks);
					ListView listView = (ListView) mRootView
							.findViewById(R.id.tasks_list);
					listView.setAdapter(mAdapter);
				}
			}).execute();
		}
	}
	
	
	
	private class LoadTasksFromDatabase extends AsyncTask<Void, Void, Void> {
		
		@Override
		protected Void doInBackground(Void... params) {
			String date = Utilities.getTodayDate();
			TasksDb tasksDb = TasksDb.getInstance(MainActivity.mContext);
			mTasks = tasksDb.getTasksByDate(date);
			Collections.sort(mTasks, new Task.TaskCategoryComparator());
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			ListView listView = (ListView) mRootView
					.findViewById(R.id.tasks_list);
			mAdapter = new TodayListAdapter(MainActivity.mContext, mTasks);
			listView.setAdapter(mAdapter);
			Button edit = (Button) mRootView.findViewById(R.id.edit_button);
			Button delete = (Button) mRootView.findViewById(R.id.delete_button);
			if (! mTasks.isEmpty()) {
				edit.setEnabled(true);
				delete.setEnabled(true);
			}
		}
		
	}
	
	
	
	private class TodayListAdapter extends ArrayAdapter<Task> {
		private final Context context;
		private final ArrayList<Task> tasks;
		
		public TodayListAdapter(Context context, ArrayList<Task> tasks) {
			super(context, R.layout.item_today_list, tasks);
			this.context = context;
			this.tasks = tasks;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater.inflate(R.layout.item_today_list,
					parent, false);
			rowView.setTag(position);
			registerForContextMenu(rowView);
			
			TextView taskText = (TextView) rowView.findViewById(R.id.task_text);
			taskText.setText(tasks.get(position).getCategory() + ": "
							+ tasks.get(position).getDescription());
			TextView locationText = (TextView) rowView
					.findViewById(R.id.location_text);
			locationText.setText(tasks.get(position).getAddress());
			
			
			Button startPauseButton = (Button) rowView.
					findViewById(R.id.start_pause_button);
			if (tasks.get(position).isActive()) {
				rowView.setBackgroundColor(getResources()
						.getColor(R.color.lightgreen));
				startPauseButton.setText(getString(R.string.pause));
			}
			startPauseButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					LinearLayout layout = (LinearLayout) v.getParent();
					int position = (Integer) layout.getTag();
					Task task = mTasks.get(position);
					String text = ((Button) v).getText().toString();
					if (text.equals(getString(R.string.start))) {
						((Button) v).setText(getString(R.string.pause));
						layout.setBackgroundColor(getResources()
								.getColor(R.color.lightgreen));
						startTask(task);
					} else {
						((Button) v).setText(getString(R.string.start));
						layout.setBackgroundResource(0);
						pauseTask(task);
					}
				}
			});
			
			CheckBox checkBox = (CheckBox) rowView
					.findViewById(R.id.finish_check);
			checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(
						CompoundButton buttonView, boolean isChecked) {
					LinearLayout layout = (LinearLayout) buttonView.getParent();
					int position = (Integer) layout.getTag();
					
					TextView taskText = (TextView) layout
							.findViewById(R.id.task_text);
					TextView locationText = (TextView) layout
							.findViewById(R.id.location_text);
					Button button = (Button) layout.findViewById(
							R.id.start_pause_button);
					
					Task task = mTasks.get(position);
					// When the task is checked and finished.
					if (isChecked) {
						task.setFinished(true);
						button.setText(getString(R.string.start));
						layout.setBackgroundResource(0);
						taskText.setPaintFlags(taskText.getPaintFlags() 
								| Paint.STRIKE_THRU_TEXT_FLAG);
						locationText.setPaintFlags(locationText.getPaintFlags() 
								| Paint.STRIKE_THRU_TEXT_FLAG);
						button.setEnabled(false);
						pauseTask(task);
					} else {
						task.setFinished(false);
						new DatabaseUtilities.UpdateTask(
								getActivity(), task).execute();
						new AppEngineUtilities.UpdateTask(
								getActivity(), task).execute();
						taskText.setPaintFlags(taskText.getPaintFlags() 
								& (~Paint.STRIKE_THRU_TEXT_FLAG));
						locationText.setPaintFlags(locationText.getPaintFlags() 
								& (~Paint.STRIKE_THRU_TEXT_FLAG));
						button.setEnabled(true);
					}
				}
			});
			
			if (tasks.get(position).isFinished()) {
				checkBox.setChecked(true);
				taskText.setPaintFlags(taskText.getPaintFlags() 
						| Paint.STRIKE_THRU_TEXT_FLAG);
				locationText.setPaintFlags(locationText.getPaintFlags() 
						| Paint.STRIKE_THRU_TEXT_FLAG);
				startPauseButton.setEnabled(false);
			}
			
			return rowView;
		}
		
	}


}
