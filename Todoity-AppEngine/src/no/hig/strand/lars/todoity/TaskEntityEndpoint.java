package no.hig.strand.lars.todoity;

import no.hig.strand.lars.todoity.EMF;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.datanucleus.query.JPACursorHelper;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.persistence.EntityManager;
import javax.persistence.Query;

@Api(name = "taskentityendpoint", namespace = @ApiNamespace(ownerDomain = "hig.no", ownerName = "hig.no", packagePath = "strand.lars.todoity"))
public class TaskEntityEndpoint {

	/**
	 * This method lists all the entities inserted in datastore.
	 * It uses HTTP GET method and paging support.
	 *
	 * @return A CollectionResponse class containing the list of all entities
	 * persisted and a cursor to the next page.
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@ApiMethod(name = "listTaskEntity")
	public CollectionResponse<TaskEntity> listTaskEntity(
			@Nullable @Named("cursor") String cursorString,
			@Nullable @Named("limit") Integer limit) {

		EntityManager mgr = null;
		Cursor cursor = null;
		List<TaskEntity> execute = null;

		try {
			mgr = getEntityManager();
			Query query = mgr
					.createQuery("select from TaskEntity as TaskEntity");
			if (cursorString != null && cursorString != "") {
				cursor = Cursor.fromWebSafeString(cursorString);
				query.setHint(JPACursorHelper.CURSOR_HINT, cursor);
			}

			if (limit != null) {
				query.setFirstResult(0);
				query.setMaxResults(limit);
			}

			execute = (List<TaskEntity>) query.getResultList();
			cursor = JPACursorHelper.getCursor(execute);
			if (cursor != null)
				cursorString = cursor.toWebSafeString();

			// Tight loop for fetching all entities from datastore and accomodate
			// for lazy fetch.
			for (TaskEntity obj : execute)
				;
		} finally {
			mgr.close();
		}

		return CollectionResponse.<TaskEntity> builder().setItems(execute)
				.setNextPageToken(cursorString).build();
	}

	/**
	 * This method gets the entity having primary key id. It uses HTTP GET method.
	 *
	 * @param id the primary key of the java bean.
	 * @return The entity with primary key id.
	 */
	@ApiMethod(name = "getTaskEntity")
	public TaskEntity getTaskEntity(@Named("id") String id) {
		EntityManager mgr = getEntityManager();
		TaskEntity taskentity = null;
		try {
			taskentity = mgr.find(TaskEntity.class, id);
		} finally {
			mgr.close();
		}
		return taskentity;
	}

	/**
	 * This inserts a new entity into App Engine datastore. If the entity already
	 * exists in the datastore, an exception is thrown.
	 * It uses HTTP POST method.
	 *
	 * @param taskentity the entity to be inserted.
	 * @return The inserted entity.
	 */
	@ApiMethod(name = "insertTaskEntity")
	public TaskEntity insertTaskEntity(TaskEntity taskentity) {
		EntityManager mgr = getEntityManager();
		try {
			if (containsTaskEntity(taskentity)) {
				throw new EntityExistsException("Object already exists");
			}
			mgr.persist(taskentity);
		} finally {
			mgr.close();
		}
		return taskentity;
	}

	/**
	 * This method is used for updating an existing entity. If the entity does not
	 * exist in the datastore, an exception is thrown.
	 * It uses HTTP PUT method.
	 *
	 * @param taskentity the entity to be updated.
	 * @return The updated entity.
	 */
	@ApiMethod(name = "updateTaskEntity")
	public TaskEntity updateTaskEntity(TaskEntity taskentity) {
		EntityManager mgr = getEntityManager();
		try {
			if (!containsTaskEntity(taskentity)) {
				throw new EntityNotFoundException("Object does not exist");
			}
			mgr.persist(taskentity);
		} finally {
			mgr.close();
		}
		return taskentity;
	}

	/**
	 * This method removes the entity with primary key id.
	 * It uses HTTP DELETE method.
	 *
	 * @param id the primary key of the entity to be deleted.
	 */
	@ApiMethod(name = "removeTaskEntity")
	public void removeTaskEntity(@Named("id") String id) {
		EntityManager mgr = getEntityManager();
		try {
			TaskEntity taskentity = mgr.find(TaskEntity.class, id);
			mgr.remove(taskentity);
		} finally {
			mgr.close();
		}
	}

	private boolean containsTaskEntity(TaskEntity taskentity) {
		EntityManager mgr = getEntityManager();
		boolean contains = true;
		try {
			TaskEntity item = mgr.find(TaskEntity.class, taskentity.getId());
			if (item == null) {
				contains = false;
			}
		} finally {
			mgr.close();
		}
		return contains;
	}

	private static EntityManager getEntityManager() {
		return EMF.get().createEntityManager();
	}

}
