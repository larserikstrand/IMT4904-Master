package no.hig.strand.lars.todoity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import no.hig.strand.lars.todoity.MainActivity.DeleteListFromDatabase;
import no.hig.strand.lars.todoity.MainActivity.DeleteTaskFromDatabase;
import no.hig.strand.lars.todoity.MainActivity.MoveTaskToDate;
import no.hig.strand.lars.todoity.MainActivity.OnDeletionCallback;
import no.hig.strand.lars.todoity.MainActivity.OnTaskMovedCallback;
import no.hig.strand.lars.todoity.TasksContract.ListEntry;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
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
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AllTasksFragment extends Fragment {

	private View mRootView;
	private AllTasksListAdapter mAdapter;
	private List<String> mDates;
	private HashMap<String, List<Task>> mTasks;
	private String mSelectedDate;
	// Variables for getting the selected group and child. (Hacky, but works!).
	private int mSelectedGroup;
	private int mSelectedTask;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_all_tasks,
				container, false);
		
		mRootView = rootView;
		
		setupUI();
		
		return rootView;
	}
	
	
	
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser) {
			new LoadAllTasksFromDatabase().execute();
		}
	}
	
	
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		String[] tag = ((String) v.getTag()).split("\\s+");
		mSelectedGroup = Integer.valueOf(tag[0]);
		mSelectedTask = Integer.valueOf(tag[1]);
		mSelectedDate = mDates.get(mSelectedGroup);
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
			new DeleteTaskFromDatabase(getActivity(), new OnDeletionCallback() {
				@Override
				public void onDeletionDone() {
					new LoadAllTasksFromDatabase().execute();
				}
			}).execute(mTasks.get(mSelectedDate).get(mSelectedTask).getId());
			return true;
		}
		return super.onContextItemSelected(item);
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
	
	
	
	public void onDateSet(String date) {
		if (! date.equals(mSelectedDate)) {
			Task task = mTasks.get(mSelectedDate).get(mSelectedTask);
			new MoveTaskToDate(getActivity(), task, date, 
					new OnTaskMovedCallback() {
				@Override
				public void onTaskMoved() {
					new LoadAllTasksFromDatabase().execute();
				}
			}).execute();
		}
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
			String date = "";
			List<Task> dateTasks;
			
			Cursor c = tasksDb.fetchLists();
			if (c.moveToFirst()) {
				do {
					date = c.getString(c.getColumnIndexOrThrow(
							ListEntry.COLUMN_NAME_DATE));
					dateTasks = tasksDb.getTasksByDate(date);
					if (! dateTasks.isEmpty()) {
						dates.add(date);
						tasks.put(date, dateTasks);
					}
				} while (c.moveToNext());
			}
			mDates = dates;
			mTasks = tasks;
			tasksDb.close();
			
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			ExpandableListView list = (ExpandableListView) mRootView
					.findViewById(R.id.all_tasks_list);
			mAdapter = new AllTasksListAdapter(getActivity(), dates, tasks);
			list.setAdapter(mAdapter);
		}
	}
	
	
	
	@SuppressLint("SimpleDateFormat")
	private class AllTasksListAdapter extends BaseExpandableListAdapter {
		private Context context;
		private List<String> groupDates;
		private HashMap<String, List<Task>> tasks;
		private String todayDate;
		private long todayInMillis;
		
		
		public AllTasksListAdapter(Context context, List<String> groupDates, 
				HashMap<String, List<Task>> tasks) {
			this.context = context;
			this.groupDates = groupDates;
			this.tasks = tasks;
			
			todayDate = Utilities.getDate();
			SimpleDateFormat formatter = 
					new SimpleDateFormat("EEEE, MMM dd, yyyy");
			formatter.setLenient(false);
			try {
				Date today = formatter.parse(todayDate);
				todayInMillis = today.getTime();
			} catch (ParseException e) {
				e.printStackTrace();
			}
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
			convertView.setTag(groupPosition + " " + childPosition);
			
			String date = (String) getGroup(groupPosition);
			long dateInMillis = 0;
			SimpleDateFormat formatter = 
					new SimpleDateFormat("EEEE, MMM dd, yyyy");
			formatter.setLenient(false);
			try {
				Date taskDate = formatter.parse(date);
				dateInMillis = taskDate.getTime();
			} catch (ParseException e) {
				e.printStackTrace();
			}
			if (dateInMillis >= todayInMillis) {
				registerForContextMenu(convertView);
			}
			
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
			
			// Find the date in milliseconds. 
			//  The buttons should be hidden for tasks older than today.
			long dateInMillis = 0;
			SimpleDateFormat formatter = 
					new SimpleDateFormat("EEEE, MMM dd, yyyy");
			formatter.setLenient(false);
			try {
				Date taskDate = formatter.parse(date);
				dateInMillis = taskDate.getTime();
			} catch (ParseException e) {
				e.printStackTrace();
			}		
			
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
			if (todayInMillis > dateInMillis) {
				imageButton.setVisibility(View.GONE);
			}
			
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
							new DeleteListFromDatabase(context, 
									new OnDeletionCallback() {
								@Override
								public void onDeletionDone() {
									new LoadAllTasksFromDatabase().execute();
								}
							}).execute(mSelectedDate);
						}
					});
				}
			});
			if (todayInMillis > dateInMillis) {
				imageButton.setVisibility(View.GONE);
			}
			
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