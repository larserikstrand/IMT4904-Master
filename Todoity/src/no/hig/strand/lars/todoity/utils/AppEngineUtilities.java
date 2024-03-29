package no.hig.strand.lars.todoity.utils;

import java.io.IOException;

import no.hig.strand.lars.todoity.CloudEndpointUtils;
import no.hig.strand.lars.todoity.Task;
import no.hig.strand.lars.todoity.contextentityendpoint.Contextentityendpoint;
import no.hig.strand.lars.todoity.contextentityendpoint.model.ContextEntity;
import no.hig.strand.lars.todoity.taskentityendpoint.Taskentityendpoint;
import no.hig.strand.lars.todoity.taskentityendpoint.model.TaskEntity;
import no.hig.strand.lars.todoity.utils.Utilities.Installation;
import android.content.Context;
import android.os.AsyncTask;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.jackson.JacksonFactory;


/**
 * Class for holding common Google App Engine calls.
 * @author LarsErik
 *
 */
public final class AppEngineUtilities {
	
	
	public static class SaveTask extends AsyncTask<Void, Void, Void> {
		Task task;
		Context context;
		
		public SaveTask(Context context, Task task) {
			this.task = task;
			this.context = context;
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			// Save task externally to AppEngine
			Taskentityendpoint.Builder endpointBuilder = 
					new Taskentityendpoint.Builder(
							AndroidHttp.newCompatibleTransport(), 
							new JacksonFactory(), null);
			Taskentityendpoint endpoint = CloudEndpointUtils
					.updateBuilder(endpointBuilder).build();
			
			TaskEntity taskEntity = new TaskEntity();
			String id = Installation.id(context) + " " + task.getId();
			taskEntity.setId(id)
			.setDate(task.getDate())
			.setCategory(task.getCategory())
			.setDescription(task.getDescription())
			.setLatitude(task.getLocation().latitude)
			.setLongitude(task.getLocation().longitude)
			.setAddress(task.getAddress())
			.setActive(task.isActive())
			.setTimeStarted(Utilities.timeToString(task.getTimeStarted()))
			.setTimeEnded(Utilities.timeToString(task.getTimeEnded()))
			.setTimeSpent(task.getTimeSpent())
			.setFinished(task.isFinished())
			.setFixedStart(task.getFixedStart())
			.setFixedEnd(task.getFixedEnd());
			try {
				taskEntity = endpoint.insertTaskEntity(taskEntity).execute();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return null;
		}
	}
	
	
	
	public static class GetTask extends AsyncTask<String, Void, TaskEntity> {

		@Override
		protected TaskEntity doInBackground(String... params) {
			String taskId = params[0];
			Taskentityendpoint.Builder endpointBuilder = 
					new Taskentityendpoint.Builder(
							AndroidHttp.newCompatibleTransport(), 
							new JacksonFactory(), null);
			Taskentityendpoint endpoint = CloudEndpointUtils
					.updateBuilder(endpointBuilder).build();
			try {
				TaskEntity task = endpoint.getTaskEntity(taskId).execute();
				return task;
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
		
	}
	
	
	
	/**
	 * Updates a task in Google AppEngine. The task to be updated is passed
	 * as a variable the constructor.
	 * @author LarsErik
	 *
	 */
	public static class UpdateTask extends AsyncTask<Void, Void, Void> {
		Task task;
		Context context;
		
		public UpdateTask(Context context, Task task) {
			this.task = task;
			this.context = context;
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			Taskentityendpoint.Builder endpointBuilder = 
					new Taskentityendpoint.Builder(
							AndroidHttp.newCompatibleTransport(), 
							new JacksonFactory(), null);
			Taskentityendpoint endpoint = CloudEndpointUtils
					.updateBuilder(endpointBuilder).build();
			
			String id = Installation.id(context) + " " + task.getId();
			TaskEntity taskEntity = new TaskEntity();
			taskEntity.setId(id)
			.setDate(task.getDate())
			.setCategory(task.getCategory())
			.setDescription(task.getDescription())
			.setLatitude(task.getLocation().latitude)
			.setLongitude(task.getLocation().longitude)
			.setAddress(task.getAddress())
			.setActive(task.isActive())
			.setTimeStarted(Utilities.timeToString(task.getTimeStarted()))
			.setTimeEnded(Utilities.timeToString(task.getTimeEnded()))
			.setTimeSpent(task.getTimeSpent())
			.setFinished(task.isFinished())
			.setFixedStart(task.getFixedStart())
			.setFixedEnd(task.getFixedEnd());
			
			try {
				endpoint.updateTaskEntity(taskEntity).execute();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
	}
	
	
	
	public static class RemoveTask extends AsyncTask<Void, Void, Void> {
		Task task;
		Context context;
		
		public RemoveTask(Context context, Task task) {
			this.task = task;
			this.context = context;
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			String id = Installation.id(context) + " " + task.getId();
			Taskentityendpoint.Builder endpointBuilder = 
					new Taskentityendpoint.Builder(
							AndroidHttp.newCompatibleTransport(), 
							new JacksonFactory(), null);
			Taskentityendpoint endpoint = CloudEndpointUtils
					.updateBuilder(endpointBuilder).build();
			try {
				endpoint.removeTaskEntity(id).execute();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
	}
	
	
	
	public static class SaveContext extends AsyncTask<Void, Void, Void> {
		String taskId;
		String type;
		String context;
		
		public SaveContext(String taskId, String type, String context) {
			this.taskId = taskId;
			this.type = type;
			this.context = context;
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			// Save context externally to AppEngine
			Contextentityendpoint.Builder endpointBuilder =
					new Contextentityendpoint.Builder(
							AndroidHttp.newCompatibleTransport(),
							new JacksonFactory(), null);
			Contextentityendpoint endpoint = CloudEndpointUtils
					.updateBuilder(endpointBuilder).build();
			
			ContextEntity contextEntity = new ContextEntity();
			contextEntity.setTaskId(taskId);
			contextEntity.setType(type);
			contextEntity.setContext(context);
			try {
				endpoint.insertContextEntity(contextEntity).execute();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return null;
		}
		
	}
}
