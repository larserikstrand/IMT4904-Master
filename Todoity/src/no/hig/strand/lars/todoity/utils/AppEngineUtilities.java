package no.hig.strand.lars.todoity.utils;

import java.io.IOException;

import no.hig.strand.lars.todoity.CloudEndpointUtils;
import no.hig.strand.lars.todoity.Task;
import no.hig.strand.lars.todoity.contextentityendpoint.Contextentityendpoint;
import no.hig.strand.lars.todoity.contextentityendpoint.model.ContextEntity;
import no.hig.strand.lars.todoity.taskentityendpoint.Taskentityendpoint;
import no.hig.strand.lars.todoity.taskentityendpoint.model.TaskEntity;
import android.os.AsyncTask;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.jackson.JacksonFactory;


/**
 * Class for holding common Google App Engine calls.
 * @author LarsErik
 *
 */
public final class AppEngineUtilities {
	
	
	public static class SaveTask extends AsyncTask<Task, Void, Long> {
		
		@Override
		protected Long doInBackground(Task... params) {
			Task task = params[0];
			// Save task externally to AppEngine
			Taskentityendpoint.Builder endpointBuilder = 
					new Taskentityendpoint.Builder(
							AndroidHttp.newCompatibleTransport(), 
							new JacksonFactory(), null);
			Taskentityendpoint endpoint = CloudEndpointUtils
					.updateBuilder(endpointBuilder).build();
			
			TaskEntity taskEntity = new TaskEntity();
			taskEntity.setId(task.getAppEngineId())
					.setCategory(task.getCategory())
					.setDescription(task.getDescription())
					.setLatitude(task.getLocation().latitude)
					.setLongitude(task.getLocation().longitude)
					.setAddress(task.getAddress())
					.setActive(task.isActive())
					.setTimeStarted(task.getTimeStarted())
					.setTimeEnded(task.getTimeEnded())
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
	 * as a variable to execute.
	 * @author LarsErik
	 *
	 */
	public static class UpdateTask extends AsyncTask<Task, Void, Void> {
		
		@Override
		protected Void doInBackground(Task... params) {
			Task task = params[0];
			Taskentityendpoint.Builder endpointBuilder = 
					new Taskentityendpoint.Builder(
							AndroidHttp.newCompatibleTransport(), 
							new JacksonFactory(), null);
			Taskentityendpoint endpoint = CloudEndpointUtils
					.updateBuilder(endpointBuilder).build();
			try {
				TaskEntity taskEntity = endpoint.getTaskEntity(
						task.getAppEngineId()).execute();
				taskEntity.setCategory(task.getCategory())
				.setDescription(task.getDescription())
				.setLatitude(task.getLocation().latitude)
				.setLongitude(task.getLocation().longitude)
				.setAddress(task.getAddress())
				.setActive(task.isActive())
				.setTimeStarted(task.getTimeStarted())
				.setTimeEnded(task.getTimeEnded())
				.setTimeSpent(task.getTimeSpent())
				.setFinished(task.isFinished())
				.setFixedStart(task.getFixedStart())
				.setFixedEnd(task.getFixedEnd());
				endpoint.updateTaskEntity(taskEntity).execute();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
	}
	
	
	
	public static class RemoveTask extends AsyncTask<String, Void, Void> {
		
		@Override
		protected Void doInBackground(String... params) {
			String taskId = params[0];
			Taskentityendpoint.Builder endpointBuilder = 
					new Taskentityendpoint.Builder(
							AndroidHttp.newCompatibleTransport(), 
							new JacksonFactory(), null);
			Taskentityendpoint endpoint = CloudEndpointUtils
					.updateBuilder(endpointBuilder).build();
			try {
				endpoint.removeTaskEntity(taskId).execute();
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
