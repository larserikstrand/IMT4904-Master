package no.hig.strand.lars.todoity;

import android.os.AsyncTask;


/**
 * Class for holding common Google App Engine calls.
 * @author LarsErik
 *
 */
public final class AppEngineUtilities {
	
	public static class SaveTask extends AsyncTask<Task, Void, Void> {
		
		@Override
		protected Void doInBackground(Task... params) {
			Task task = params[0];
			
			return null;
		}
		
	}
}
