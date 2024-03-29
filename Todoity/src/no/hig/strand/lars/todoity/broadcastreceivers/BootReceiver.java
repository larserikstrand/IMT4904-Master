package no.hig.strand.lars.todoity.broadcastreceivers;

import java.util.ArrayList;

import no.hig.strand.lars.todoity.Task;
import no.hig.strand.lars.todoity.TasksDb;
import no.hig.strand.lars.todoity.services.ContextService;
import no.hig.strand.lars.todoity.services.GeofenceService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

	private TasksDb mTasksDb;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// Check if there are active tasks.. if so, start context service.
		mTasksDb = TasksDb.getInstance(context);
		ArrayList<Task> tasks = mTasksDb.getActiveTasks();
		if (! tasks.isEmpty()) {
			Intent contextServiceintent = 
					new Intent(context, ContextService.class);
			context.startService(contextServiceintent);
		}
		
		// Start recommender service.
		Intent recommenderServiceIntent =
				new Intent(context, GeofenceService.class);
		context.startService(recommenderServiceIntent);
	}

}
