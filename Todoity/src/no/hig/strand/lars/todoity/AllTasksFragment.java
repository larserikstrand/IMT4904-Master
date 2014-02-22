package no.hig.strand.lars.todoity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import no.hig.strand.lars.todoity.R;
import no.hig.strand.lars.todoity.MainActivity.DeleteListFromDatabase;
import no.hig.strand.lars.todoity.WeekFragment.LoadWeekListFromDatabase;
import no.hig.strand.lars.todoity.WeekFragment.WeekListAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AllTasksFragment extends Fragment {

	private View mRootView;
	private WeekListAdapter mAdapter;
	private String mSelectedDate;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_all_tasks, container, false);
		
		mRootView = rootView;
		
		setupUI();
		
		return rootView;
	}
	
	@Override
	public void onResume() {
		new LoadWeekListFromDatabase().execute();
		super.onResume();
	}
	
	
	
	private void setupUI() {
		Button button = (Button) mRootView.findViewById(R.id.new_list_button);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getActivity(), ListActivity.class));
			}
		});
	}
	
	
	
	private class LoadAllTasksFromDatabase extends AsyncTask<Void, Void, Void> {
		TasksDb tasksDb;
		List<String> dates;
		HashMap<String, List<Task>> tasks;

		@Override
		protected Void doInBackground(Void... params) {
			tasksDb = new TasksDb(getActivity());
			tasksDb.open();
			
			dates = new ArrayList<String>();
			tasks = new HashMap<String, List<Task>>();
			
1			List<Task> dateTasks;
				dateTasks = tasksDb.getTasksByDate(date);
				if (! dateTasks.isEmpty()) {
					dates.add(date);
					tasks.put(date, dateTasks);
				}
			tasksDb.close();
			
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			ExpandableListView list = (ExpandableListView) mRootView
					.findViewById(R.id.all_tasks_list);
			mAdapter = new WeekListAdapter(getActivity(), dates, tasks);
			list.setAdapter(mAdapter);
		}
	}
	
	
	
	private class AllTasksListAdapter extends BaseExpandableListAdapter {
		private Context context;
		private List<String> groupDates;
		private HashMap<String, List<Task>> tasks;
		
		
		public AllTasksListAdapter(Context context, List<String> groupDates, 
				HashMap<String, List<Task>> tasks) {
			this.context = context;
			this.groupDates = groupDates;
			this.tasks = tasks;
		}
		
		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return tasks.get(groupDates.get(groupPosition)).get(childPosition);
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			Task task = (Task) getChild(groupPosition, childPosition);
			
			LayoutInflater inflater = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.item_week_list, null);
			
			TextView taskText = (TextView) convertView
					.findViewById(R.id.task_text);
			taskText.setText(task.getCategory() + ": " + task.getDescription());
			TextView locationText = (TextView) convertView
					.findViewById(R.id.location_text);
			locationText.setText(task.getAddress());
			
			return convertView;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return tasks.get(groupDates.get(groupPosition)).size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			return groupDates.get(groupPosition);
		}

		@Override
		public int getGroupCount() {
			return groupDates.size();
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			String date = (String) getGroup(groupPosition);
			
			LayoutInflater inflater = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.group_week_list, null);
			
			TextView dateText = (TextView) convertView
					.findViewById(R.id.group_date_text);
			dateText.setText(date);
			
			ImageButton imageButton = (ImageButton) convertView
					.findViewById(R.id.group_edit_button);
			imageButton.setFocusable(false);
			imageButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					LinearLayout layout = (LinearLayout) v.getParent();
					TextView textView = (TextView) layout
							.findViewById(R.id.group_date_text);
					String date = textView.getText().toString();
					int index = groupDates.indexOf(date);
					ArrayList<Task> dateTasks = 
							(ArrayList<Task>) tasks.get(groupDates.get(index));
					
					Intent intent = new Intent(context, ListActivity.class);
					intent.putExtra(MainActivity.TASKS_EXTRA, dateTasks);
					intent.putExtra(MainActivity.DATE_EXTRA, date);
					startActivity(intent);
				}
			});
			
			imageButton = (ImageButton) convertView
					.findViewById(R.id.group_delete_button);
			imageButton.setFocusable(false);
			imageButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					LinearLayout layout = (LinearLayout) v.getParent();
					TextView textView = (TextView) layout
							.findViewById(R.id.group_date_text);
					mSelectedDate = textView.getText().toString();
					
					Utilities.showConfirmDialog(getActivity(), 
							getString(R.string.confirm), 
							getString(R.string.delete_list_message), 
							new Utilities.ConfirmDialogListener() {
						@Override
						public void PositiveClick(DialogInterface dialog, 
								int id) {
							if (getActivity() instanceof MainActivity) {
								MainActivity activity = 
										(MainActivity) getActivity();
								activity.new DeleteListFromDatabase()
										.execute(mSelectedDate);
							}
						}
					});
				}
			});
			
			return convertView;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

	}
}