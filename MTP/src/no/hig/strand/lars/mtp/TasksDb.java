package no.hig.strand.lars.mtp;

import no.hig.strand.lars.mtp.TasksContract.ListEntry;
import no.hig.strand.lars.mtp.TasksContract.TaskEntry;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class TasksDb {

	TasksDbHelper mDbHelper;
	SQLiteDatabase mDb;
	
	public TasksDb(Context context) {
		mDbHelper = new TasksDbHelper(context);
	}
	
	public void open() {
		mDb = mDbHelper.getWritableDatabase();
	}
	
	public void close() {
		mDb.close();
	}
	
	/*
	 * Fetching queries
	 */
	public Cursor fetchLists() {
		return null;
	}
	
	
	
	public Cursor fetchListByDate(String date) {
		return null;
	}
	
	
	
	public Cursor fetchTasks() {
		return null;
	}
	
	
	
	public Cursor fetchTaskByList(int listId) {
		return null;
	}
	
	
	
	/*
	 * Deletion queries
	 */
	public boolean deleteListByDate(String date) {
		return false;
	}
	
	public boolean deleteTaskById(int taskId) {
		return false;
	}
	
	
	
	/*
	 * Insertion queries
	 */
	public long insertList(String date) {
		ContentValues values = new ContentValues();
		values.put(ListEntry.COLUMN_NAME_DATE, date);
		return mDb.insert(ListEntry.TABLE_NAME, null, values);
	}
	
	
	
	public long insertTask(long listId, String category, 
			String description, String lat, String lng) {
		ContentValues values = new ContentValues();
		values.put(TaskEntry.COLUMN_NAME_LIST, listId);
		values.put(TaskEntry.COLUMN_NAME_CATEGORY, category);
		values.put(TaskEntry.COLUMN_NAME_DESCRIPTION, description);
		values.put(TaskEntry.COLUMN_NAME_LOCATION_LAT, lat);
		values.put(TaskEntry.COLUMN_NAME_LOCATION_LNG, lng);
		values.put(TaskEntry.COLUMN_NAME_IS_ACTIVE, 0);
		values.put(TaskEntry.COLUMN_NAME_TIME_START, 0);
		values.put(TaskEntry.COLUMN_NAME_TIME_END, 0);
		values.put(TaskEntry.COLUMN_NAME_TIME_SPENT, 0);
		return mDb.insert(TaskEntry.TABLE_NAME, null, values);
	}
	
}
