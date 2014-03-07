package no.hig.strand.lars.todoity.utils;

import no.hig.strand.lars.todoity.MainActivity;
import no.hig.strand.lars.todoity.Task;
import no.hig.strand.lars.todoity.TasksContract.ListEntry;
import android.database.Cursor;
import android.os.AsyncTask;

public class DatabaseUtilities {

	public DatabaseUtilities() {}
	
	
	public interface OnDeletionCallback {
		public void onDeletionDone();
	}
	
	public interface OnTaskMovedCallback {
		public void onTaskMoved();
	}
	
	
	public static class UpdateTask extends AsyncTask<Task, Void, Void> {

		@Override
		protected Void doInBackground(Task... params) {
			Task task = params[0];
			MainActivity.tasksDb.updateTask(task);
			return null;
		}
	}
	
	
	
	public static class DeleteList extends AsyncTask<String, Void, Void> {
		private OnDeletionCallback callback;

		public DeleteList(OnDeletionCallback callback) {
			this.callback = callback;
		}

		@Override
		protected Void doInBackground(String... params) {
			MainActivity.tasksDb.deleteListByDate(params[0]);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			callback.onDeletionDone();
		}
	}
	
	
	
	public static class DeleteTask extends AsyncTask<Integer, Void, Void> {	
		private OnDeletionCallback callback;
		
		public DeleteTask(OnDeletionCallback callback) {
			this.callback = callback;
		}

		@Override
		protected Void doInBackground(Integer... params) {
			int taskId = params[0];
			MainActivity.tasksDb.deleteTaskById(taskId);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			callback.onDeletionDone();
		}
	}
	
	
	
	public static class MoveTaskToDate extends AsyncTask<Void, Void, Void> {
		private OnTaskMovedCallback callback;
		private Task task;
		private String date;

		public MoveTaskToDate(Task task, String date, 
				OnTaskMovedCallback callback) {
			this.callback = callback;
			this.task = task;
			this.date = date;
		}

		@Override
		protected Void doInBackground(Void... params) {			
			// Move the task to the selected date. 
			//  Create list on that date if none exist.
			long listId = -1;
			Cursor c = MainActivity.tasksDb.fetchListByDate(date);
			if (c.moveToFirst()) {
				listId = c.getLong(c.getColumnIndexOrThrow(ListEntry._ID));
			} else {
				listId = MainActivity.tasksDb.insertList(date);
			}
			
			// Remove old task and insert new one.
			MainActivity.tasksDb.deleteTaskById(task.getId());
			MainActivity.tasksDb.insertTask(listId, task);
			
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			callback.onTaskMoved();
		}
		
	}
}
